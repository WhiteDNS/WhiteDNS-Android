package shop.whitedns.client.scan

/** Reflows pasted resolver text to one entry per line, in pasted order, without duplicates. */
internal fun tidyManualResolverText(rawText: String): String {
    val seen = mutableSetOf<String>()
    val lines = mutableListOf<String>()
    rawText.replace("\r\n", "\n").replace('\r', '\n').lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty()) {
            return@forEach
        }
        if (line.startsWith("#")) {
            if (seen.add(line)) {
                lines += line
            }
            return@forEach
        }
        line.split(',', ';', '\t').forEach { token ->
            val entry = token.trim().trim('"', '\'').trim()
            if (entry.isNotEmpty() && seen.add(entry)) {
                lines += entry
            }
        }
    }
    return lines.joinToString(separator = "\n")
}
