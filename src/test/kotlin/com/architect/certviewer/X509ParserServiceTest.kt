package com.architect.certviewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Base64

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
    fun parsesJceksKeyEntryCertificateChain() {
        val certs = parser.parseKeystore(decodeBase64(JCEKS_KEY_ENTRY_STORE), TEST_PASSWORD, "JCEKS")

        assertEquals(1, certs.size)
        assertEquals("CN=Key Entry Certificate,O=Certificate Viewer,C=US", certs.single().subjectX500Principal.name)
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

        assertEquals(
            "6D:E0:22:B1:A9:06:22:0A:46:E2:10:09:D6:8D:05:A2:3E:30:C7:6C:04:1B:0B:34:BE:44:A7:13:EF:57:22:C1",
            fingerprint,
        )
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

    private fun decodeBase64(data: String): ByteArray {
        return Base64.getMimeDecoder().decode(data)
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

        private val JCEKS_KEY_ENTRY_STORE = """
            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
        """.trimIndent()
    }
}
