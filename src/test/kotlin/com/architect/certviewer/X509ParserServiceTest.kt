package com.architect.certviewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class X509ParserServiceTest {

    private val parser = X509ParserService()

    @Test
    fun parsesPemCertificate() {
        val cert = parseFixtureCertificate()

        assertNotNull(cert)
        assertEquals("CN=Test Certificate,O=Certificate Viewer,C=US", cert.subjectX500Principal.name)
    }

    @Test
    fun parsesDerCertificate() {
        val cert = parseFixtureCertificate()

        val parsed = parser.parseDer(cert.encoded)

        assertNotNull(parsed)
        assertEquals(cert.subjectX500Principal, parsed!!.subjectX500Principal)
    }

    @Test
    fun parsesPkcs12CertificateEntry() {
        val cert = parseFixtureCertificate()
        val keystore = createKeystore("PKCS12", cert, TEST_PASSWORD)

        val certs = parser.parseKeystore(keystore, TEST_PASSWORD, "PKCS12")

        assertEquals(1, certs.size)
        assertEquals(cert.subjectX500Principal, certs.single().subjectX500Principal)
    }

    @Test
    fun parsesJksCertificateEntry() {
        val cert = parseFixtureCertificate()
        val keystore = createKeystore("JKS", cert, TEST_PASSWORD)

        val certs = parser.parseKeystore(keystore, TEST_PASSWORD, "JKS")

        assertEquals(1, certs.size)
        assertEquals(cert.subjectX500Principal, certs.single().subjectX500Principal)
    }

    @Test
    fun rejectsPkcs12WithWrongPassword() {
        val cert = parseFixtureCertificate()
        val keystore = createKeystore("PKCS12", cert, TEST_PASSWORD)

        assertThrows(Exception::class.java) {
            parser.parseKeystore(keystore, "wrong-password".toCharArray(), "PKCS12")
        }
    }

    @Test
    fun returnsNullForInvalidPem() {
        assertNull(parser.parseCertificate("not a certificate"))
    }

    @Test
    fun returnsNullForInvalidDer() {
        assertNull(parser.parseDer(byteArrayOf(1, 2, 3, 4)))
    }

    @Test
    fun calculatesSha256Fingerprint() {
        val cert = parseFixtureCertificate()

        val fingerprint = parser.getFingerprint(cert, "SHA-256")

        assertTrue(fingerprint.matches(Regex("([0-9A-F]{2}:){31}[0-9A-F]{2}")))
        assertNotEquals("Error", fingerprint.substringBefore(":"))
    }

    private fun parseFixtureCertificate(): X509Certificate {
        return parser.parseCertificate(TEST_CERTIFICATE_PEM)
            ?: error("Test certificate fixture must parse")
    }

    private fun createKeystore(type: String, cert: X509Certificate, password: CharArray): ByteArray {
        val keyStore = KeyStore.getInstance(type)
        keyStore.load(null, password)
        keyStore.setCertificateEntry("test-certificate", cert)

        return ByteArrayOutputStream().use { output ->
            keyStore.store(output, password)
            output.toByteArray()
        }
    }

    private companion object {
        private val TEST_PASSWORD = "changeit".toCharArray()

        private val TEST_CERTIFICATE_PEM = """
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
