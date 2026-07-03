package com.architect.certviewer

import com.intellij.driver.sdk.FileEditorManager
import com.intellij.driver.sdk.findFile
import com.intellij.driver.sdk.getOpenProjects
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.driver.sdk.ui.ui
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class MarketplaceSandboxSmokeTest {

    @Test
    fun opensPemFixtureWithCertificateViewerInsteadOfTextEditor(@TempDir projectDir: Path) {
        val pluginPath = System.getProperty("path.to.build.plugin")
            ?: error("Missing path.to.build.plugin system property")
        projectDir.resolve("demo-certificate.pem").writeText(DEMO_CERTIFICATE_PEM)

        Starter.newContext(
            testName = "certificateFixtureViewer",
            testCase = TestCase(
                IdeProductProvider.IU,
                projectInfo = LocalProjectInfo(projectDir),
            ).withVersion("2026.1.3"),
        ).apply {
            PluginConfigurator(this).installPluginFromPath(Path.of(pluginPath))
        }.runIdeWithDriver().useDriverAndCloseIde {
            val projectOpenDeadline = System.currentTimeMillis() + 60_000
            while (getOpenProjects().isEmpty() && System.currentTimeMillis() < projectOpenDeadline) {
                Thread.sleep(1_000)
            }
            if (getOpenProjects().isEmpty()) {
                error("Timed out waiting for IntelliJ to open the sandbox test project")
            }

            withContext(OnDispatcher.EDT, LockSemantics.NO_LOCK) {
                val project = singleProject()
                val certificateFile = findFile("demo-certificate.pem", project)
                    ?: error("Fixture file was not found in the opened test project")
                val editorManager = service(FileEditorManager::class, project)

                val openedEditors = editorManager.openFile(certificateFile, true, true)

                assertEquals(certificateFile.getPath(), editorManager.getCurrentFile().getPath())
                assertEquals(1, openedEditors.size, "Certificate files should open in a single custom editor")
                assertEquals(certificateFile.getPath(), openedEditors.single().getFile().getPath())
                assertNull(editorManager.getSelectedTextEditor(), "Certificate files must not open in the default text editor")
            }

            val uiRobot = ui
            uiRobot.x { byVisibleText("Certificate Analysis") }
            uiRobot.x { byVisibleText("Main Certificate") }
            uiRobot.x { byVisibleText("Subject") }
            uiRobot.x { byVisibleText("Issuer") }
            uiRobot.x { byVisibleText("Serial") }
            uiRobot.x { byVisibleText("SHA-256") }
            uiRobot.x { byVisibleText("CN=Test Certificate,O=Certificate Viewer,C=US") }
            assertTrue(
                uiRobot.xx { byVisibleText("Error loading certificate") }.list().isEmpty(),
                "The certificate viewer must render the fixture without an error state",
            )
        }
    }

    private companion object {
        private val DEMO_CERTIFICATE_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIDLTCCAhWgAwIBAgIIWyOIWD0B5G4wDQYJKoZIhvcNAQELBQAwRTELMAkGA1UE
            BhMCVVMxGzAZBgNVBAoTEkNlcnRpZmljYXRlIFZpZXdlcjEZMBcGA1UEAxMQVGVz
            dCBDZXJ0aWZpY2F0ZTAeFw0yNjA3MDIxNjQzNDhaFw0yNzA3MDIxNjQzNDhaMEUx
            CzAJBgNVBAYTAlVTMRswGQYDVQQKExJDZXJ0aWZpY2F0ZSBWaWV3ZXIxGTAXBgNV
            BAMTEFRlc3QgQ2VydGlmaWNhdGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
            AoIBAQCE7Fy9ORmi9KLjCcmFORfWk8mIbHEBFfIwt+L5HymS0y1xYaDY/gifK1Cw
            mCaDiLjPqScuskwE8j+SpaVZn9lQ2/MI/estGfTQSsf3np37ZlcrLsBvROsMpw1T
            gOm/jwj4V31bjvnb0a04L9JnwylWOTbwqRHSy8wQum/gtX2kmSVJS3VgSPUEIsSZ
            PbTUzsB7sbvpWSefZ23trzhlbBqo9ONUYCAdZrxLgFURe/H5EK/HNbHaskf0DGKo
            EZKflfuvKtvlrqgA4DzgRvVILgn2lEqS6eklnuNKVTr89PDGK8U+irUD/fxq+ctD
            0jP3iLvjCBh+28emR6JBCqsN/9yhAgMBAAGjITAfMB0GA1UdDgQWBBSUvLRFy7Ik
            xfn71QMq4KFnyrrxOzANBgkqhkiG9w0BAQsFAAOCAQEACGo5HDd7n9xkkVUJI+UL
            1Yb8NDNIbV/uwkL12tL9qB655Azrq6ocXvIQEr7hRvCvG2aVMGaQanWZFEgEfG3L
            So3QHMlVa21ZQ70Rju1B2GKJoLPx6ijX5jLy9cajGE/zCT+UZTUsISLBBETdYeYW
            +ANbzkpgiQSbjMwT3SDwaFn+Q1bFnbd+3iDemisedi0PZo0XMzstkCZNU09VtgRa
            A6X6SiCh7fJXtoJ1svyBWf1OI7o/AY/jBeUp7AvI2T4h/FZQrps0qv3kYDroyUS4
            NzGLqB3rMkeu5/ih5w5w4npkclqhm4o2kf1gC9R4DpjvBhSwDFB5capNFfzy8M7o
            ZA==
            -----END CERTIFICATE-----
        """.trimIndent()
    }
}
