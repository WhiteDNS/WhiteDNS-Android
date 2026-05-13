package shop.whitedns.client.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretRedactorTest {
    @Test
    fun redactsConfiguredSecrets() {
        val raw = """
            Server vpn.private.example uses key super-secret-key
            SOCKS user user-name password proxy-password
            Short password xy appears in a free-form line
            stormdns://eyJzZWNyZXQiOiJzdXBlci1zZWNyZXQta2V5In0
            Runtime path /data/data/shop.whitedns.client/no_backup/stormdns/runtime/.wd-a.toml
        """.trimIndent()

        val redacted = SecretRedactor.redact(
            raw,
            RedactionSecrets(
                serverRoutes = listOf("vpn.private.example"),
                encryptionKeys = listOf("super-secret-key"),
                socksUsers = listOf("user-name"),
                socksPasswords = listOf("proxy-password", "xy"),
            ),
        )

        assertFalse(redacted.contains("vpn.private.example"))
        assertFalse(redacted.contains("super-secret-key"))
        assertFalse(redacted.contains("user-name"))
        assertFalse(redacted.contains("proxy-password"))
        assertFalse(redacted.contains("xy"))
        assertFalse(redacted.contains("stormdns://"))
        assertFalse(redacted.contains(".wd-a.toml"))
        assertTrue(redacted.contains("[server route]"))
        assertTrue(redacted.contains("[encryption key]"))
        assertTrue(redacted.contains("[socks user]"))
        assertTrue(redacted.contains("[socks password]"))
        assertTrue(redacted.contains("[profile link]"))
        assertTrue(redacted.contains("[runtime path]"))
    }

    @Test
    fun redactsTomlSecretsAndNamedFields() {
        val raw = """
            ENCRYPTION_KEY = "abc123secret"
            SOCKS5_USER = "alice"
            SOCKS5_PASS = "proxy-password"
            token: bearer-token
            password = plain-password
        """.trimIndent()

        val redacted = SecretRedactor.redact(raw)

        assertFalse(redacted.contains("abc123secret"))
        assertFalse(redacted.contains("alice"))
        assertFalse(redacted.contains("proxy-password"))
        assertFalse(redacted.contains("bearer-token"))
        assertFalse(redacted.contains("plain-password"))
        assertTrue(redacted.contains("ENCRYPTION_KEY = \"[redacted]\""))
        assertTrue(redacted.contains("SOCKS5_USER = \"[redacted]\""))
        assertTrue(redacted.contains("SOCKS5_PASS = \"[redacted]\""))
    }

    @Test
    fun redactionIsIdempotent() {
        val secrets = RedactionSecrets(
            serverRoutes = listOf("vpn.private.example"),
            encryptionKeys = listOf("super-secret-key"),
        )
        val once = SecretRedactor.redact("vpn.private.example super-secret-key", secrets)
        val twice = SecretRedactor.redact(once, secrets)

        assertTrue(once == twice)
    }
}
