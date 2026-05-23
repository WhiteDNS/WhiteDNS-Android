package shop.whitedns.client.ui

internal const val RedactedSecretLabel = "<redacted>"

internal fun maskSecretForDisplay(value: String): String {
    return if (value.isBlank()) "" else "********"
}

internal fun redactSecretForDiagnostics(value: String): String {
    return if (value.isBlank()) "not configured" else RedactedSecretLabel
}
