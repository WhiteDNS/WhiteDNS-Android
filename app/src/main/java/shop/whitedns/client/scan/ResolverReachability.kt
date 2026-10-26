package shop.whitedns.client.scan

import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class ResolverReachability(
    val resolver: String,
    val reachable: Boolean,
    val latencyMillis: Int?,
    val transport: String?,
    val failure: String?,
)

object ResolverReachabilityProbe {
    const val MaxProbeCount = 24
    const val DefaultTimeoutMillis = 1_500
    private const val ProbeName = "example.com"

    suspend fun probeAll(
        resolvers: List<String>,
        timeoutMillis: Int = DefaultTimeoutMillis,
        maxConcurrency: Int = 8,
    ): List<ResolverReachability> = coroutineScope {
        resolvers.chunked(maxConcurrency.coerceAtLeast(1)).flatMap { chunk ->
            chunk.map { resolver ->
                async(Dispatchers.IO) { probe(resolver, timeoutMillis) }
            }.awaitAll()
        }
    }

    suspend fun probe(
        resolver: String,
        timeoutMillis: Int = DefaultTimeoutMillis,
    ): ResolverReachability = withContext(Dispatchers.IO) {
        val endpoint = parseEndpoint(resolver)
            ?: return@withContext ResolverReachability(resolver, false, null, null, "Unsupported address")
        val udp = probeUdp(endpoint.first, endpoint.second, timeoutMillis)
        if (udp != null) {
            return@withContext ResolverReachability(resolver, true, udp, "UDP", null)
        }
        val tcp = probeTcp(endpoint.first, endpoint.second, timeoutMillis)
        if (tcp != null) {
            return@withContext ResolverReachability(resolver, true, tcp, "TCP", null)
        }
        ResolverReachability(resolver, false, null, null, "No response")
    }

    internal fun parseEndpoint(resolver: String): Pair<String, Int>? {
        val text = resolver.trim()
        if (text.isEmpty()) return null
        if (text.startsWith("[")) {
            val end = text.indexOf(']')
            if (end <= 1) return null
            val host = text.substring(1, end)
            val rest = text.substring(end + 1)
            val port = if (rest.startsWith(":")) rest.drop(1).toIntOrNull() ?: return null else 53
            return host to port
        }
        val colonCount = text.count { it == ':' }
        if (colonCount == 1) {
            val host = text.substringBefore(':')
            val port = text.substringAfter(':').toIntOrNull() ?: return null
            return host to port
        }
        return text to 53
    }

    internal fun buildQuery(transactionId: Int, name: String = ProbeName): ByteArray {
        val out = ByteArrayOutputStream()
        out.write((transactionId shr 8) and 0xFF)
        out.write(transactionId and 0xFF)
        out.write(0x01); out.write(0x00)
        out.write(0x00); out.write(0x01)
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        out.write(0x00); out.write(0x00)
        name.split('.').forEach { label ->
            val bytes = label.toByteArray(Charsets.US_ASCII)
            out.write(bytes.size)
            out.write(bytes)
        }
        out.write(0x00)
        out.write(0x00); out.write(0x01)
        out.write(0x00); out.write(0x01)
        return out.toByteArray()
    }

    internal fun isResponseFor(packet: ByteArray, length: Int, transactionId: Int): Boolean {
        if (length < 12) return false
        val id = ((packet[0].toInt() and 0xFF) shl 8) or (packet[1].toInt() and 0xFF)
        if (id != transactionId) return false
        val flags = packet[2].toInt() and 0xFF
        return flags and 0x80 != 0
    }

    private fun probeUdp(host: String, port: Int, timeoutMillis: Int): Int? {
        return runCatching {
            DatagramSocket().use { socket ->
                socket.soTimeout = timeoutMillis
                val transactionId = Random.nextInt(0, 0x10000)
                val query = buildQuery(transactionId)
                val address = InetAddress.getByName(host)
                socket.send(DatagramPacket(query, query.size, address, port))
                val buffer = ByteArray(512)
                val response = DatagramPacket(buffer, buffer.size)
                val started = System.nanoTime()
                socket.receive(response)
                val elapsed = ((System.nanoTime() - started) / 1_000_000L).toInt()
                if (!isResponseFor(response.data, response.length, transactionId)) {
                    return@use null
                }
                elapsed.coerceAtLeast(0)
            }
        }.getOrNull()
    }

    private fun probeTcp(host: String, port: Int, timeoutMillis: Int): Int? {
        return runCatching {
            val started = System.nanoTime()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(InetAddress.getByName(host), port), timeoutMillis)
            }
            ((System.nanoTime() - started) / 1_000_000L).toInt().coerceAtLeast(0)
        }.getOrNull()
    }
}
