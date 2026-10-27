package shop.whitedns.client.scan

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolverReachabilityProbeTest {
    @Test
    fun parseEndpointHandlesEveryNormalizedForm() {
        assertEquals("1.1.1.1" to 53, ResolverReachabilityProbe.parseEndpoint("1.1.1.1"))
        assertEquals("1.1.1.1" to 5353, ResolverReachabilityProbe.parseEndpoint("1.1.1.1:5353"))
        assertEquals(
            "2606:4700:4700::1111" to 53,
            ResolverReachabilityProbe.parseEndpoint("[2606:4700:4700::1111]"),
        )
        assertEquals(
            "2606:4700:4700::1111" to 5353,
            ResolverReachabilityProbe.parseEndpoint("[2606:4700:4700::1111]:5353"),
        )
        assertEquals(
            "2606:4700:4700::1111" to 53,
            ResolverReachabilityProbe.parseEndpoint("2606:4700:4700::1111"),
        )
        assertNull(ResolverReachabilityProbe.parseEndpoint("1.1.1.1:abc"))
    }

    @Test
    fun buildQueryWritesHeaderAndDefaultQuestion() {
        val query = ResolverReachabilityProbe.buildQuery(0x1234)

        assertEquals(29, query.size)
        assertEquals(0x12, query[0].toInt() and 0xFF)
        assertEquals(0x34, query[1].toInt() and 0xFF)
        assertEquals(0x01, query[2].toInt() and 0xFF)
        assertEquals(0x00, query[3].toInt() and 0xFF)
        assertEquals(1, word(query, 4))
        assertEquals(0, word(query, 6))
        assertEquals(0, word(query, 8))
        assertEquals(0, word(query, 10))
        assertEquals(7, query[12].toInt() and 0xFF)
        assertEquals("example", query.copyOfRange(13, 20).toString(Charsets.US_ASCII))
        assertEquals(3, query[20].toInt() and 0xFF)
        assertEquals("com", query.copyOfRange(21, 24).toString(Charsets.US_ASCII))
        assertEquals(0, query[24].toInt() and 0xFF)
        assertEquals(1, word(query, 25))
        assertEquals(1, word(query, 27))
    }

    @Test
    fun isResponseForRejectsShortWrongIdAndQueryShapedPackets() {
        val header = ByteArray(12)
        header[2] = 0x81.toByte()

        assertFalse(ResolverReachabilityProbe.isResponseFor(header, 11, 0))

        header[0] = 0x12
        header[1] = 0x35
        assertFalse(ResolverReachabilityProbe.isResponseFor(header, header.size, 0x1234))

        header[1] = 0x34
        val queryShaped = header.copyOf()
        queryShaped[2] = 0x01
        assertFalse(ResolverReachabilityProbe.isResponseFor(queryShaped, queryShaped.size, 0x1234))

        assertTrue(ResolverReachabilityProbe.isResponseFor(header, header.size, 0x1234))
    }

    @Test
    fun loopbackUdpServerProducesPositiveUdpResult() = runBlocking {
        val server = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
        val answerThread = thread(isDaemon = true) {
            server.use { socket ->
                val requestBuffer = ByteArray(512)
                val request = DatagramPacket(requestBuffer, requestBuffer.size)
                socket.receive(request)
                val reply = ByteArray(12)
                reply[0] = request.data[0]
                reply[1] = request.data[1]
                reply[2] = 0x81.toByte()
                reply[3] = 0x80.toByte()
                reply[5] = 1
                socket.send(DatagramPacket(reply, reply.size, request.address, request.port))
            }
        }
        val resolver = "127.0.0.1:${server.localPort}"

        val result = ResolverReachabilityProbe.probe(resolver, 1_000)
        answerThread.join(2_000)

        assertFalse(answerThread.isAlive)
        assertEquals(resolver, result.resolver)
        assertTrue(result.reachable)
        assertEquals("UDP", result.transport)
        assertNotNull(result.latencyMillis)
        assertTrue((result.latencyMillis ?: -1) >= 0)
        assertNull(result.failure)
    }

    @Test
    fun closedLoopbackPortIsReportedUnreachable() = runBlocking {
        val result = ResolverReachabilityProbe.probe("127.0.0.1:1", 250)

        assertFalse(result.reachable)
        assertNull(result.latencyMillis)
        assertNull(result.transport)
        assertEquals("No response", result.failure)
    }

    @Test
    fun malformedEndpointFailsWithoutTouchingTheNetwork() = runBlocking {
        val result = ResolverReachabilityProbe.probe("1.1.1.1:abc", 50)

        assertFalse(result.reachable)
        assertNull(result.latencyMillis)
        assertEquals("Unsupported address", result.failure)
    }

    @Test
    fun probeAllKeepsOrderAndReportsEachEntry() = runBlocking {
        val results = ResolverReachabilityProbe.probeAll(
            listOf("1.1.1.1:abc", "127.0.0.1:1"),
            timeoutMillis = 100,
        )

        assertEquals(listOf("1.1.1.1:abc", "127.0.0.1:1"), results.map { it.resolver })
        assertFalse(results[0].reachable)
        assertFalse(results[1].reachable)
    }

    @Test
    fun probeAllInvokesOnResultOncePerResolverInInputOrder() = runBlocking {
        val resolvers = listOf("127.0.0.1:1", "1.1.1.1:abc", "127.0.0.1:2")
        val reported = mutableListOf<String>()

        val results = ResolverReachabilityProbe.probeAll(
            resolvers,
            timeoutMillis = 100,
            maxConcurrency = 2,
            onResult = { reported += it.resolver },
        )

        assertEquals(resolvers, reported)
        assertEquals(resolvers, results.map { it.resolver })
    }

    private fun word(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
}
