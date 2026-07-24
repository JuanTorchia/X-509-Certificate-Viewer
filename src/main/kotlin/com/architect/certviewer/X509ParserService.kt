package com.architect.certviewer

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class CertificateInputTooLargeException(message: String) : IllegalArgumentException(message)

@Service(Service.Level.APP)
class X509ParserService {

    private val cf = CertificateFactory.getInstance("X.509")

    fun parseCertificate(data: String): X509Certificate? {
        return try {
            if (data.toByteArray(Charsets.UTF_8).size > MAX_CERTIFICATE_BYTES) return null
            val cleanData = cleanPem(data)
            val decoded = Base64.getDecoder().decode(cleanData)
            if (decoded.size > MAX_CERTIFICATE_BYTES) return null
            cf.generateCertificate(ByteArrayInputStream(decoded)) as? X509Certificate
        } catch (e: Exception) {
            // Log error in production
            null
        }
    }

    fun parseDer(data: ByteArray): X509Certificate? {
        return try {
            if (data.size > MAX_CERTIFICATE_BYTES) return null
            cf.generateCertificate(ByteArrayInputStream(data)) as? X509Certificate
        } catch (e: Exception) {
            null
        }
    }

    fun parseKeystore(data: ByteArray, password: CharArray?, type: String = "PKCS12"): List<X509Certificate> {
        if (data.size > MAX_KEYSTORE_BYTES) {
            throw CertificateInputTooLargeException(
                "Keystore exceeds the maximum supported size of ${formatBytes(MAX_KEYSTORE_BYTES.toLong())}",
            )
        }

        val certs = mutableListOf<X509Certificate>()
        try {
            val ks = java.security.KeyStore.getInstance(type)
            ks.load(ByteArrayInputStream(data), password)
            val aliases = ks.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                if (ks.isCertificateEntry(alias)) {
                    (ks.getCertificate(alias) as? X509Certificate)?.let { certs.add(it) }
                } else if (ks.isKeyEntry(alias)) {
                    (ks.getCertificateChain(alias))?.forEach { 
                        (it as? X509Certificate)?.let { cert -> certs.add(cert) }
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        }
        return certs
    }

    // Deprecated alias for compatibility if needed, but we should use parseKeystore
    fun parsePkcs12(data: ByteArray, password: CharArray?): List<X509Certificate> = parseKeystore(data, password, "PKCS12")


    fun getFingerprint(cert: X509Certificate, algorithm: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance(algorithm)
            val der = cert.encoded
            val digest = md.digest(der)
            digest.joinToString(":") { "%02X".format(it) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun cleanPem(pem: String): String {

        return pem.replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("\\s".toRegex(), "")
    }

    companion object {
        const val MAX_CERTIFICATE_BYTES = 1 * 1024 * 1024
        const val MAX_KEYSTORE_BYTES = 10 * 1024 * 1024

        fun formatBytes(bytes: Long): String {
            val mib = 1024L * 1024L
            val kib = 1024L
            return when {
                bytes >= mib -> "${bytes / mib} MiB"
                bytes >= kib -> "${bytes / kib} KiB"
                else -> "$bytes bytes"
            }
        }
    }
}
