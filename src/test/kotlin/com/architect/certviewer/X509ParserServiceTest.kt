package com.architect.certviewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class X509ParserServiceTest {

    private val parser = X509ParserService()

    @Test
    fun testParsePem() {
        val pem = """
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

        val cert = parser.parseCertificate(pem)

        assertNotNull(cert)
        assertEquals("CN=Test Certificate,O=Certificate Viewer,C=US", cert!!.subjectX500Principal.name)
    }
}
