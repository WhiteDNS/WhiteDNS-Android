package shop.whitedns.client.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ManualScanResolverFileTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun writeManualScan(
        lines: List<String>,
        excludedResolvers: Set<String> = emptySet(),
    ): Pair<List<String>, ScanResolverFileSummary> {
        val outputFile = temporaryFolder.newFile()
        val summary = WhiteDnsScannerResultStore.writePendingScanResolverFile(
            lines = lines.asSequence(),
            outputFile = outputFile,
            excludedResolvers = excludedResolvers,
        )
        return outputFile.readLines(Charsets.UTF_8) to summary
    }

    private fun summary(
        total: Int,
        pending: Int,
        alreadyValid: Int = 0,
        invalid: Int = 0,
    ) = ScanResolverFileSummary(
        totalResolverCount = total,
        pendingResolverCount = pending,
        alreadyValidResolverCount = alreadyValid,
        invalidEntryCount = invalid,
    )

    @Test
    fun defaultPortDuplicatesCollapseToOnePendingResolver() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("1.1.1.1", "1.1.1.1:53", "8.8.8.8:53", "  8.8.8.8  "),
        )

        assertEquals(listOf("1.1.1.1", "8.8.8.8"), resolvers)
        assertEquals(summary(total = 2, pending = 2), fileSummary)
    }

    @Test
    fun commaSeparatedLineBecomesOneResolverPerEntry() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("1.1.1.1, 8.8.8.8:5353, 9.9.9.9:53"),
        )

        assertEquals(listOf("1.1.1.1", "8.8.8.8:5353", "9.9.9.9"), resolvers)
        assertEquals(summary(total = 3, pending = 3), fileSummary)
    }

    @Test
    fun blankAndCommentLinesAreIgnoredWithoutCounting() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("", "   ", "# 1.1.1.1 is public", "   # pasted comment", "9.9.9.9"),
        )

        assertEquals(listOf("9.9.9.9"), resolvers)
        assertEquals(summary(total = 1, pending = 1), fileSummary)
    }

    @Test
    fun invalidEntryIsCountedButNeverWritten() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("1.1.1.1", "not-a-server", "8.8.8.8"),
        )

        assertEquals(listOf("1.1.1.1", "8.8.8.8"), resolvers)
        assertEquals(summary(total = 2, pending = 2, invalid = 1), fileSummary)
    }

    @Test
    fun bracketedIpv6DefaultPortIsWrittenAsBareExpandedAddress() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("[2001:4860:4860::8888]:53"),
        )

        // The default port is dropped, so no brackets are needed and the groups come out expanded.
        assertEquals(listOf("2001:4860:4860:0:0:0:0:8888"), resolvers)
        assertEquals(summary(total = 1, pending = 1), fileSummary)
    }

    @Test
    fun nonDefaultPortIsPreservedWhileDefaultPortIsStripped() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf("1.1.1.1:5300", "8.8.8.8:53"),
        )

        assertEquals(listOf("1.1.1.1:5300", "8.8.8.8"), resolvers)
        assertEquals(summary(total = 2, pending = 2), fileSummary)
    }

    @Test
    fun alreadyValidResolversAreCountedButNotRewritten() {
        val (resolvers, fileSummary) = writeManualScan(
            lines = listOf("8.8.8.8:53", "1.1.1.1", "9.9.9.9:5353"),
            excludedResolvers = setOf("8.8.8.8", "9.9.9.9:5353"),
        )

        assertEquals(listOf("1.1.1.1"), resolvers)
        assertEquals(summary(total = 3, pending = 1, alreadyValid = 2), fileSummary)
    }

    @Test
    fun mixedManualPasteWritesOnlyPendingResolversInTypedOrder() {
        val (resolvers, fileSummary) = writeManualScan(
            listOf(
                "# my list",
                "1.1.1.1, 8.8.8.8:53",
                "1.1.1.1:53",
                "",
                "9.9.9.9:5353",
                "not-a-server",
                "[2001:4860:4860::8888]:53",
                "# done",
            ),
        )

        assertEquals(
            listOf("1.1.1.1", "8.8.8.8", "9.9.9.9:5353", "2001:4860:4860:0:0:0:0:8888"),
            resolvers,
        )
        assertEquals(summary(total = 4, pending = 4, invalid = 1), fileSummary)
    }

    @Test
    fun dialogCounterAgreesWithWhatTheWriterWillScan() {
        val typed = listOf(
            "# list",
            "1.1.1.1, 8.8.8.8:53",
            "1.1.1.1:53",
            "9.9.9.9:5353",
            "not-a-server",
            "[2001:4860:4860::8888]:53",
        )

        val validation = WhiteDnsScannerResultStore.normalizeScanResolverLines(typed.asSequence())
        val (_, fileSummary) = writeManualScan(typed)

        assertEquals(validation.normalizedResolvers.size, fileSummary.totalResolverCount)
        assertEquals(validation.invalidEntries.size, fileSummary.invalidEntryCount)
    }

    @Test
    fun invalidOnlyInputScansNothing() {
        val typed = listOf("not-a-server", "still-not-a-server")

        val validation = WhiteDnsScannerResultStore.normalizeScanResolverLines(typed.asSequence())
        val (resolvers, fileSummary) = writeManualScan(typed)

        assertEquals(emptyList<String>(), resolvers)
        assertEquals(0, validation.normalizedResolvers.size)
        assertEquals(0, fileSummary.totalResolverCount)
        assertEquals(2, fileSummary.invalidEntryCount)
    }

    @Test
    fun pastedCommaListBecomesOneServerPerLineInOrder() {
        val pasted =
            "78.157.42.100:53,46.245.76.162:53,185.88.178.196:53,91.92.214.110:53,2.189.44.68:53," +
                "185.53.142.174:53,217.219.162.200:53,31.214.169.244:53,2.189.44.82:53,2.188.21.58:53," +
                "2.189.44.83:53,2.189.44.86:53,2.189.44.85:53,2.189.44.84:53,2.188.21.70:53," +
                "62.60.197.83:53,185.128.82.2:53,2.189.44.91:53,2.189.44.93:53,2.189.44.94:53," +
                "2.189.44.92:53,2.189.44.90:53,2.188.21.46:53,2.189.44.98:53,2.188.21.138:53," +
                "185.109.61.27:53,108.162.192.0:53,156.146.33.97:53,79.127.211.213:53,78.38.77.2:53," +
                "172.64.32.0:53,2.188.21.62:53,74.63.24.211:53,173.245.58.0:53,5.160.140.16:53," +
                "74.80.77.245:53,217.219.29.66:53,5.56.132.97:53,178.252.143.130:53,207.211.215.145:53," +
                "162.159.38.0:53,2.188.21.146:53,185.236.90.12:53,74.63.24.205:53,77.238.123.179:53," +
                "79.127.170.12:53,79.127.170.15:53,149.102.250.14:53,217.66.203.211:53,2.189.44.66:53"

        val lines = WhiteDnsScannerResultStore.formatResolverPaste(pasted).lines()

        assertEquals(50, lines.size)
        assertEquals("78.157.42.100:53", lines.first())
        assertEquals("2.189.44.66:53", lines.last())
        assertTrue(lines.none { ',' in it })
    }

    @Test
    fun semicolonsTabsAndSurroundingSpacesSplitAsWell() {
        val formatted = WhiteDnsScannerResultStore.formatResolverPaste(
            " 1.1.1.1:53 ; 8.8.8.8:5353 ,; 9.9.9.9 \t10.10.10.10 ",
        )

        assertEquals("1.1.1.1:53\n8.8.8.8:5353\n9.9.9.9\n10.10.10.10", formatted)
    }

    @Test
    fun commentLineStaysWholeWhileBlankLinesAreDropped() {
        val formatted = WhiteDnsScannerResultStore.formatResolverPaste(
            "\n  # Iran: 78.157.42.100:53, 2.189.44.66:53  \r\n\r\n1.1.1.1:53, 8.8.8.8:53\n   \n",
        )

        assertEquals(
            "# Iran: 78.157.42.100:53, 2.189.44.66:53\n1.1.1.1:53\n8.8.8.8:53",
            formatted,
        )
    }

    @Test
    fun alreadyOnePerLineInputIsLeftUnchanged() {
        val text = "# list\n1.1.1.1:53\n8.8.8.8:5353\n9.9.9.9"

        assertEquals(text, WhiteDnsScannerResultStore.formatResolverPaste(text))
    }
}
