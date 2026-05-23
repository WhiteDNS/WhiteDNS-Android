package shop.whitedns.client.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SensitiveTextTest {
    @Test
    fun maskSecretForDisplayHidesConfiguredSecrets() {
        assertEquals("********", maskSecretForDisplay("secret-password"))
    }

    @Test
    fun maskSecretForDisplayKeepsBlankSecretsBlank() {
        assertEquals("", maskSecretForDisplay(""))
        assertEquals("", maskSecretForDisplay("   "))
    }

    @Test
    fun redactSecretForDiagnosticsDoesNotExposeConfiguredSecrets() {
        assertEquals(RedactedSecretLabel, redactSecretForDiagnostics("server-key"))
    }

    @Test
    fun redactSecretForDiagnosticsKeepsMissingStateUseful() {
        assertEquals("not configured", redactSecretForDiagnostics(""))
        assertEquals("not configured", redactSecretForDiagnostics("   "))
    }
}
