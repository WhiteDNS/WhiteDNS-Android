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
}
