package shop.whitedns.client.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualResolverTextTest {
    private val fiftyEntryPaste =
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

    @Test
    fun commaSeparatedLineBecomesOneEntryPerLineInPastedOrder() {
        val tidied = tidyManualResolverText("78.157.42.100:53, 46.245.76.162:53 ,185.88.178.196:53")

        assertEquals(
            listOf("78.157.42.100:53", "46.245.76.162:53", "185.88.178.196:53"),
            tidied.lines(),
        )
    }

    @Test
    fun semicolonsTabsAndSurroundingSpacesSplitAsWell() {
        val tidied = tidyManualResolverText(" 1.1.1.1:53 ; 8.8.8.8:5353 ,; 9.9.9.9 \t10.10.10.10 ")

        assertEquals("1.1.1.1:53\n8.8.8.8:5353\n9.9.9.9\n10.10.10.10", tidied)
    }

    @Test
    fun exactDuplicatesCollapseWhilePortVariantsBothSurvive() {
        val tidied = tidyManualResolverText("1.1.1.1\n1.1.1.1\n  1.1.1.1  \n1.1.1.1:53")

        assertEquals(listOf("1.1.1.1", "1.1.1.1:53"), tidied.lines())
    }

    @Test
    fun garbageTokensStayInTheBox() {
        val tidied = tidyManualResolverText("localhost, not-a-server, 78.157.42.100:")

        assertEquals(listOf("localhost", "not-a-server", "78.157.42.100:"), tidied.lines())
    }

    @Test
    fun survivingGarbageIsStillFlaggedInvalidByTheParser() {
        val tidied = tidyManualResolverText("localhost,1.1.1.1:53, not-a-server")
        val validation = WhiteDnsScannerResultStore.normalizeScanResolverLines(tidied.lineSequence())

        assertEquals(listOf("1.1.1.1"), validation.normalizedResolvers)
        assertEquals(listOf("localhost", "not-a-server"), validation.invalidEntries)
    }

    @Test
    fun commentLinesStayWholeWhileBlankLinesGo() {
        val tidied = tidyManualResolverText(
            "\n  # Iran: 78.157.42.100:53, 2.189.44.66:53  \r\n\r\n1.1.1.1:53, 8.8.8.8:53\n   \n",
        )

        assertEquals(
            "# Iran: 78.157.42.100:53, 2.189.44.66:53\n1.1.1.1:53\n8.8.8.8:53",
            tidied,
        )
    }

    @Test
    fun alreadyOnePerLineInputIsLeftUnchanged() {
        val text = "# list\n1.1.1.1:53\n8.8.8.8:5353\n9.9.9.9"

        assertEquals(text, tidyManualResolverText(text))
    }

    @Test
    fun quotedTokensLoseTheirQuotes() {
        val tidied = tidyManualResolverText("\"1.1.1.1:53\", '8.8.8.8:53', \" 9.9.9.9 \",")

        assertEquals(listOf("1.1.1.1:53", "8.8.8.8:53", "9.9.9.9"), tidied.lines())
    }

    @Test
    fun realWorldFiftyEntryPasteBecomesFiftyLines() {
        val lines = tidyManualResolverText(fiftyEntryPaste).lines()

        assertEquals(50, lines.size)
        assertEquals(50, lines.distinct().size)
        assertEquals("78.157.42.100:53", lines.first())
        assertEquals("2.189.44.66:53", lines.last())
        assertTrue(lines.none { ',' in it })
    }

    @Test
    fun tidiedBoxIsStableAndRepeatedPasteAddsNothingNew() {
        val once = tidyManualResolverText(fiftyEntryPaste)
        val merged = tidyManualResolverText("${once.trimEnd()}\n$fiftyEntryPaste")

        assertEquals(once, merged)
        assertFalse(merged.endsWith("\n"))
    }
}
