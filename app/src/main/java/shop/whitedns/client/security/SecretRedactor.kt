package shop.whitedns.client.security

data class RedactionSecrets(
    val serverRoutes: List<String> = emptyList(),
    val encryptionKeys: List<String> = emptyList(),
    val socksUsers: List<String> = emptyList(),
    val socksPasswords: List<String> = emptyList(),
    val profileLinks: List<String> = emptyList(),
    val runtimePaths: List<String> = emptyList(),
)

object SecretRedactor {
    private val ansiEscapeRegex = Regex("\\u001B\\[[;\\d]*m")
    private val stormDnsProfileRegex = Regex("""stormdns://[A-Za-z0-9_\-+/=%.?&#:]+""")
    private val tomlSecretRegex = Regex(
        """(?im)^(\s*(?:ENCRYPTION_KEY|SOCKS5_PASS|SOCKS5_USER)\s*=\s*)("[^"]*"|'[^']*'|[^\r\n#]+)""",
    )
    private val namedSecretRegex = Regex(
        """(?i)\b(password|pass|secret|token|key)\b(\s*[:=]\s*)("[^"]*"|'[^']*'|\S+)""",
    )
    private val androidRuntimePathRegex = Regex(
        """(?i)(?:/data/(?:user|data)/\d*/[A-Za-z0-9_.]+|/data/data/[A-Za-z0-9_.]+|[A-Z]:\\[^:\r\n"]*(?:cache|files|no_backup|noBackupFiles)[^:\r\n"]*)[^\s"'<>]*""",
    )

    fun redact(
        text: String,
        secrets: RedactionSecrets = RedactionSecrets(),
    ): String {
        if (text.isEmpty()) {
            return text
        }

        var redacted = text.replace(ansiEscapeRegex, "")
        redacted = replaceValues(redacted, secrets.profileLinks, "[profile link]")
        redacted = stormDnsProfileRegex.replace(redacted, "[profile link]")
        redacted = replaceValues(redacted, secrets.encryptionKeys, "[encryption key]")
        redacted = replaceValues(redacted, secrets.socksPasswords, "[socks password]")
        redacted = replaceValues(redacted, secrets.socksUsers, "[socks user]")
        redacted = replaceValues(redacted, secrets.serverRoutes, "[server route]")
        redacted = replaceValues(redacted, secrets.runtimePaths, "[runtime path]")
        redacted = tomlSecretRegex.replace(redacted) { match ->
            "${match.groupValues[1]}\"[redacted]\""
        }
        redacted = namedSecretRegex.replace(redacted) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}[redacted]"
        }
        redacted = androidRuntimePathRegex.replace(redacted, "[runtime path]")
        return redacted
    }

    private fun replaceValues(
        source: String,
        values: List<String>,
        replacement: String,
    ): String {
        return values
            .asSequence()
            .map(String::trim)
            .filter { it.length >= MinimumDirectSecretLength }
            .distinct()
            .sortedByDescending(String::length)
            .fold(source) { text, value ->
                text.replace(value, replacement)
            }
    }

    private const val MinimumDirectSecretLength = 3
}
