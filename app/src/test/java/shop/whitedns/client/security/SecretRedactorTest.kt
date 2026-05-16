package shop.whitedns.client.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretRedactorTest {

    @Test
    fun redactsConfiguredSecretsAndProfileLinks() {
        val text = """
            Server: example.whitedns.test
            Encryption key: tiny
            SOCKS user: alice
            SOCKS pass: p
            Link: stormdns://abcdef
            Path: /data/user/0/shop.whitedns.client/no_backup/runtime-launch/request.json
        """.trimIndent()

        val redacted = SecretRedactor.redact(
            text,
            RedactionSecrets(
                serverDomains = listOf("example.whitedns.test"),
                encryptionKeys = listOf("tiny"),
                socksUsernames = listOf("alice"),
                socksPasswords = listOf("p"),
                runtimePaths = listOf("/data/user/0/shop.whitedns.client/no_backup"),
            ),
        )

        assertFalse(redacted.contains("example.whitedns.test"))
        assertFalse(redacted.contains("tiny"))
        assertFalse(redacted.contains("alice"))
        assertFalse(redacted.contains("stormdns://abcdef"))
        assertFalse(redacted.contains("/data/user/0/shop.whitedns.client/no_backup"))
        assertTrue(redacted.contains("[server domain]"))
        assertTrue(redacted.contains("[encryption key]"))
        assertTrue(redacted.contains("[socks username]"))
        assertTrue(redacted.contains("[socks password]"))
        assertTrue(redacted.contains("[profile link]"))
        assertTrue(redacted.contains("[runtime path]"))
    }

    @Test
    fun redactsNamedTomlAndLogSecretFields() {
        val text = """
            ENCRYPTION_KEY = "abc123"
            SOCKS5_PASS=password123
            apiToken: secret-token
            harmless = value
        """.trimIndent()

        val redacted = SecretRedactor.redact(text)

        assertFalse(redacted.contains("abc123"))
        assertFalse(redacted.contains("password123"))
        assertFalse(redacted.contains("secret-token"))
        assertTrue(redacted.contains("harmless = value"))
    }

    @Test
    fun stripsAnsiAndIsIdempotent() {
        val text = "\u001B[31mENCRYPTION_KEY=abc123\u001B[0m"

        val once = SecretRedactor.redact(text)
        val twice = SecretRedactor.redact(once)

        assertFalse(once.contains("\u001B"))
        assertFalse(once.contains("abc123"))
        assertTrue(once == twice)
    }
}
