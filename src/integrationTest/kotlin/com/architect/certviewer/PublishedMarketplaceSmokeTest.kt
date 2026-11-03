package com.architect.certviewer

import com.intellij.driver.client.Remote
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.FileEditorManager
import com.intellij.driver.sdk.findFile
import com.intellij.driver.sdk.getOpenProjects
import com.intellij.driver.sdk.getPlugin
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.elements.textField
import com.intellij.driver.sdk.ui.ui
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import kotlin.io.path.copyTo
import kotlin.time.Duration.Companion.seconds

/** Exercises the public artifact, never path.to.build.plugin or project parser classes. */
@EnabledIfEnvironmentVariable(named = "CERTVIEW_PUBLISHED_SMOKE", matches = "1")
class PublishedMarketplaceSmokeTest {
    @Test
    fun publishedPluginRendersGeneratedCertificatesAndPasswordProtectedStores(@TempDir projectDir: Path) {
        val version = "1.0.36"
        val fixtureRoot = Path.of("build/marketplace-verification")
        val names = listOf("certificate.pem", "certificate.der", "chain.pem", "keystore.p12", "keystore.jks", "keystore.jceks")
        names.forEach { fixtureRoot.resolve(it).copyTo(projectDir.resolve(it)) }
        val leaf = Files.newInputStream(fixtureRoot.resolve("certificate.pem")).use {
            CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate
        }
        val fingerprint = MessageDigest.getInstance("SHA-256").digest(leaf.encoded)
            .joinToString(":") { "%02X".format(it) }
        val context = Starter.newContext(
            testName = "publishedMarketplace2026_2",
            testCase = TestCase(IdeProductProvider.IU, projectInfo = LocalProjectInfo(projectDir))
                .withVersion("2026.2.2"),
        ).apply {
            PluginConfigurator(this).installPluginFromPluginManager("com.architect.certviewer", version)
                .assertPluginIsInstalled("com.architect.certviewer")
        }
        assertTrue(context.ide.build.startsWith("262."), "Must execute actual IntelliJ2026.2.x")
        println("MARKETPLACE_PROOF ide=${context.ide.build} version=$version source=JetBrainsPluginManager")
        context.runIdeWithDriver().useDriverAndCloseIde {
            val deadline = System.currentTimeMillis() + 90_000
            while (getOpenProjects().isEmpty() && System.currentTimeMillis() < deadline) Thread.sleep(1_000)
            assertTrue(getOpenProjects().isNotEmpty(), "Sandbox project must open")
            assertTrue(isPluginLoaded("com.architect.certviewer"))
            assertEquals(version, getPlugin("com.architect.certviewer")?.getVersion())
            for (name in names) {
                withContext(OnDispatcher.EDT, LockSemantics.NO_LOCK) {
                    val project = singleProject()
                    val manager = service(FileEditorManager::class, project)
                    val file = findFile(name, project) ?: error("Missing generated fixture: $name")
                    val editors = manager.openFile(file, true, true)
                    assertEquals(1, editors.size, "One custom editor for $name")
                    assertEquals(file.getPath(), manager.getCurrentFile().getPath())
                    assertNull(manager.getSelectedTextEditor(), "No fallback text editor for $name")
                }
                if (name.startsWith("keystore.")) {
                    ui.x { byVisibleText("Certificate Password Required") }.waitFound(30.seconds)
                    ui.textField { byType("javax.swing.JPasswordField") }.text = "changeit"
                    ui.x { and(byType("javax.swing.JButton"), byVisibleText("OK")) }.waitFound().click()
                }
                // waitFound resolves the lazy UI query and fails if content never renders.
                ui.x { byVisibleText("Certificate Analysis") }.waitFound(30.seconds)
                ui.x { byVisibleText("Main Certificate") }.waitFound()
                ui.x { byVisibleText(leaf.subjectX500Principal.name) }.waitFound()
                ui.x { byVisibleText("Subject Alt Names") }.waitFound()
                ui.x { byVisibleText("DNS: marketplace.example") }.waitFound()
                ui.x { byVisibleText("SHA-256") }.waitFound()
                val fingerprintLabel = ui.x { byVisibleText(fingerprint.take(77) + "...") }.waitFound()
                assertEquals(fingerprint, cast(fingerprintLabel.component, TooltipText::class).getToolTipText())
                ui.x { byVisibleText("VALID") }.waitFound()
                val dateFormat = SimpleDateFormat("yyyy-M-d")
                val validity = "${dateFormat.format(leaf.notBefore)} — ${dateFormat.format(leaf.notAfter)}"
                ui.x { byVisibleText(validity) }.waitFound()
                if (name == "chain.pem" || name.startsWith("keystore.")) {
                    ui.x { byVisibleText("Issuer Certificate") }.waitFound()
                }
                assertTrue(ui.xx { byVisibleText("Wrong password or invalid Keystore") }.list().isEmpty())
                println("MARKETPLACE_FLOW_PASS file=$name subject=true san=true fingerprint=true validity=true")
                withContext(OnDispatcher.EDT, LockSemantics.NO_LOCK) {
                    val project = singleProject()
                    service(CloseEditors::class, project).closeAllFiles()
                }
            }
        }
    }
}

@Remote("com.intellij.openapi.fileEditor.FileEditorManager")
interface CloseEditors {
    fun closeAllFiles()
}

@Remote("javax.swing.JComponent")
interface TooltipText {
    fun getToolTipText(): String?
}
