package shop.whitedns.client.model

import java.util.Base64
import org.json.JSONObject

private const val StormDnsProfileScheme = "stormdns"
private const val StormDnsProfileSchema = "whitedns.profile"
private const val StormDnsProfileVersion = 1
private val UnsafeProfileControlRegex = Regex("""[\u0000-\u001F\u007F]""")
private val UnsafeDomainWhitespaceRegex = Regex("""\s""")

data class StormDnsProfileLinkPreview(
    val name: String,
    val domain: String,
    val encryptionMethod: Int,
)

fun WhiteDnsSettings.exportStormDnsProfileLink(profile: ConnectionProfile = selectedConnectionProfile()): String {
    val normalizedProfile = profile.copy(
        name = profile.name.ifBlank { profile.customServerDomain.ifBlank { "WhiteDNS Profile" } },
        serverMode = "custom",
        customServerDomain = profile.customServerDomain.trim().trimEnd('.'),
        customServerEncryptionKey = profile.customServerEncryptionKey.trim(),
        customServerEncryptionMethod = profile.customServerEncryptionMethod.coerceIn(0, 5),
    )
    if (normalizedProfile.customServerDomain.isBlank() || normalizedProfile.customServerEncryptionKey.isBlank()) {
        throw IllegalArgumentException("Custom server domain and encryption key are required to export")
    }

    val profileJson = JSONObject()
        .put("name", normalizedProfile.name)
        .put(
            "server",
            JSONObject()
                .put("domain", normalizedProfile.customServerDomain)
                .put("encryption_key", normalizedProfile.customServerEncryptionKey)
                .put("encryption_method", normalizedProfile.customServerEncryptionMethod),
        )

    val root = JSONObject()
        .put("schema", StormDnsProfileSchema)
        .put("version", StormDnsProfileVersion)
        .put("profile", profileJson)

    return "$StormDnsProfileScheme://${encodeProfilePayload(root)}"
}

fun WhiteDnsSettings.exportAllStormDnsProfileLinks(): String {
    val links = normalizedConnectionProfiles()
        .filter { profile ->
            profile.serverMode == "custom" &&
                profile.customServerDomain.isNotBlank() &&
                profile.customServerEncryptionKey.isNotBlank()
        }
        .map { profile -> exportStormDnsProfileLink(profile) }
    if (links.isEmpty()) {
        throw IllegalArgumentException("No custom profiles are available to export")
    }
    return links.joinToString(separator = "\n")
}

fun WhiteDnsSettings.importStormDnsProfileLinks(
    rawLinks: String,
    nowMillis: Long = System.currentTimeMillis(),
): WhiteDnsSettings {
    val links = rawLinks
        .lineSequence()
        .mapIndexedNotNull { index, line ->
            line.trim().takeIf(String::isNotEmpty)?.let { trimmedLine ->
                (index + 1) to trimmedLine
            }
        }
        .toList()
    if (links.isEmpty()) {
        throw IllegalArgumentException("Enter at least one stormdns:// profile link")
    }

    var nextSettings = this
    links.forEachIndexed { index, (lineNumber, link) ->
        nextSettings = runCatching {
            nextSettings.importStormDnsProfileLink(
                rawLink = link,
                nowMillis = nowMillis + index,
            )
        }.getOrElse { error ->
            throw IllegalArgumentException("Line $lineNumber: ${error.message ?: "Unable to import profile"}", error)
        }
    }
    return nextSettings
}

fun WhiteDnsSettings.importStormDnsProfileLink(
    rawLink: String,
    nowMillis: Long = System.currentTimeMillis(),
): WhiteDnsSettings {
    val imported = parseStormDnsProfileLink(rawLink)
    val profileId = uniqueImportedProfileId(normalizedConnectionProfiles(), nowMillis)

    val importedProfile = ConnectionProfile(
        id = profileId,
        name = imported.name,
        serverMode = "custom",
        customServerDomain = imported.domain,
        customServerEncryptionKey = imported.encryptionKey,
        customServerEncryptionMethod = imported.encryptionMethod,
        resolverProfileId = "",
        connectionMode = connectionMode,
    )

    return copy(
        selectedConnectionProfileId = profileId,
        connectionProfiles = normalizedConnectionProfiles() + importedProfile,
        serverMode = "custom",
        customServerDomain = imported.domain,
        customServerEncryptionKey = imported.encryptionKey,
        customServerEncryptionMethod = importedProfile.customServerEncryptionMethod,
    ).syncSelectedConnectionProfileFields()
}

fun previewStormDnsProfileLink(rawLink: String): StormDnsProfileLinkPreview {
    val imported = parseStormDnsProfileLink(rawLink)
    return StormDnsProfileLinkPreview(
        name = imported.name,
        domain = imported.domain,
        encryptionMethod = imported.encryptionMethod,
    )
}

private fun parseStormDnsProfileLink(rawLink: String): ImportedStormDnsProfile {
    val root = decodeProfilePayload(rawLink)
    val schema = root.requiredString("schema")
    if (schema != StormDnsProfileSchema) {
        throw IllegalArgumentException("Unsupported profile schema")
    }
    val version = root.optionalInt("version") ?: StormDnsProfileVersion
    if (version != StormDnsProfileVersion) {
        throw IllegalArgumentException("Unsupported profile version")
    }

    val profileJson = root.optJSONObject("profile")
        ?: throw IllegalArgumentException("Missing profile")
    val serverJson = profileJson.optJSONObject("server")
        ?: throw IllegalArgumentException("Missing server")
    val domain = serverJson.requiredString("domain").trim().trimEnd('.')
    val encryptionKey = serverJson.requiredString("encryption_key").trim()
    rejectControlCharacters(domain, "Server domain")
    rejectControlCharacters(encryptionKey, "Server encryption key")
    if (UnsafeDomainWhitespaceRegex.containsMatchIn(domain)) {
        throw IllegalArgumentException("Server domain cannot contain whitespace")
    }
    if (domain.isBlank()) {
        throw IllegalArgumentException("Server domain is required")
    }
    if (encryptionKey.isBlank()) {
        throw IllegalArgumentException("Server encryption key is required")
    }

    val profileName = profileJson.requiredString("name").trim()
    rejectControlCharacters(profileName, "Profile name")
    if (profileName.isBlank()) {
        throw IllegalArgumentException("Profile name is required")
    }
    val encryptionMethod = serverJson.requiredInt("encryption_method")
    if (encryptionMethod !in 0..5) {
        throw IllegalArgumentException("Server encryption method must be between 0 and 5")
    }
    return ImportedStormDnsProfile(
        name = profileName,
        domain = domain,
        encryptionKey = encryptionKey,
        encryptionMethod = encryptionMethod,
    )
}

private fun encodeProfilePayload(root: JSONObject): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(root.toString().toByteArray(Charsets.UTF_8))
}

private fun decodeProfilePayload(rawLink: String): JSONObject {
    val link = rawLink.trim()
    val prefix = "$StormDnsProfileScheme://"
    if (!link.startsWith(prefix)) {
        throw IllegalArgumentException("Profile link must start with stormdns://")
    }
    val payload = link.removePrefix(prefix).trim()
    if (payload.isBlank()) {
        throw IllegalArgumentException("Profile link is empty")
    }
    val decoded = decodeBase64Payload(payload.substringBefore('#').substringBefore('?'))
    return JSONObject(decoded)
}

private fun decodeBase64Payload(payload: String): String {
    val paddedPayload = payload.padEnd(payload.length + ((4 - payload.length % 4) % 4), '=')
    val bytes = runCatching {
        Base64.getUrlDecoder().decode(paddedPayload)
    }.recoverCatching {
        Base64.getDecoder().decode(paddedPayload)
    }.getOrElse {
        throw IllegalArgumentException("Profile link payload is not valid base64")
    }
    return bytes.toString(Charsets.UTF_8)
}

private fun uniqueImportedProfileId(
    profiles: List<ConnectionProfile>,
    nowMillis: Long,
): String {
    val existingIds = profiles.map { it.id }.toSet()
    val baseId = "profile-imported-$nowMillis"
    if (baseId !in existingIds) {
        return baseId
    }
    var suffix = 2
    while ("$baseId-$suffix" in existingIds) {
        suffix += 1
    }
    return "$baseId-$suffix"
}

private fun JSONObject.requiredString(name: String): String {
    return optionalString(name)?.takeIf(String::isNotBlank)
        ?: throw IllegalArgumentException("Missing $name")
}

private fun JSONObject.requiredInt(name: String): Int {
    return optionalInt(name) ?: throw IllegalArgumentException("Missing $name")
}

private fun JSONObject.optionalString(name: String): String? {
    if (!has(name) || isNull(name)) {
        return null
    }
    return opt(name)?.toString()
}

private fun JSONObject.optionalInt(name: String): Int? {
    if (!has(name) || isNull(name)) {
        return null
    }
    return when (val value = opt(name)) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }
}

private fun rejectControlCharacters(value: String, label: String) {
    if (UnsafeProfileControlRegex.containsMatchIn(value)) {
        throw IllegalArgumentException("$label cannot contain control characters")
    }
}

private data class ImportedStormDnsProfile(
    val name: String,
    val domain: String,
    val encryptionKey: String,
    val encryptionMethod: Int,
)
