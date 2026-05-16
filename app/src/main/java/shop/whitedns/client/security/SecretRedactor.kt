package shop.whitedns.client.security

data class RedactionSecrets(
    val serverDomains: List<String> = emptyList(),
    val encryptionKeys: List<String> = emptyList(),
    val socksUsernames: List<String> = emptyList(),
    val socksPasswords: List<String> = emptyList(),
    val profileLinks: List<String> = emptyList(),
    val runtimePaths: List<String> = emptyList(),
)

object SecretRedactor {
    fun redact(source: String, secrets: RedactionSecrets = RedactionSecrets()): String {
        if (source.isEmpty()) {
            return source
        }
        return source
            .replace(AnsiEscapeRegex, "")
            .replace(ProfileLinkRegex, "[profile link]")
            .redactNamedSecretFields()
            .replaceValues(secrets.profileLinks, "[profile link]")
            .replaceValues(secrets.encryptionKeys, "[encryption key]")
            .replaceValues(secrets.socksPasswords, "[socks password]")
            .replaceValues(secrets.socksUsernames, "[socks username]")
            .replaceValues(secrets.serverDomains, "[server domain]")
            .replaceValues(secrets.runtimePaths, "[runtime path]")
    }

    private fun String.redactNamedSecretFields(): String {
        return NamedSecretRegex.replace(this) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}[secret]"
        }
    }

    private fun String.replaceValues(values: List<String>, replacement: String): String {
        return values
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sortedByDescending(String::length)
            .fold(this) { text, value ->
                if (value.length < MinimumWholeTokenLength) {
                    Regex("""(?<![A-Za-z0-9])${Regex.escape(value)}(?![A-Za-z0-9])""")
                        .replace(text, replacement)
                } else {
                    text.replace(value, replacement)
                }
            }
    }

    private const val MinimumWholeTokenLength = 3
    private val AnsiEscapeRegex = Regex("\\u001B\\[[;\\d]*m")
    private val ProfileLinkRegex = Regex("""stormdns://[^\s]+""")
    private val NamedSecretRegex = Regex(
        pattern = """(?im)\b([A-Z0-9_]*(?:ENCRYPTION_KEY|SOCKS5_PASS|SOCKS5_USER|PASSWORD|PASS|SECRET|TOKEN|KEY)[A-Z0-9_]*\s*[:=]\s*)([^\s#]+)""",
    )
}
