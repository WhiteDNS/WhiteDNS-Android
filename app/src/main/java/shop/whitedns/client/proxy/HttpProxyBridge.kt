package shop.whitedns.client.proxy

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

private data class HostPort(
    val host: String,
    val port: Int,
)

data class HttpProxyBridgeLimits(
    val maxClients: Int = 64,
    val maxTunnelDirections: Int = 128,
    val maxHeaderLineBytes: Int = 8_192,
    val maxHeaderBytes: Int = 32_768,
    val maxHeaderCount: Int = 128,
    val maxHostLength: Int = 255,
    val tunnelIdleTimeoutMillis: Int = 60_000,
) {
    init {
        require(maxClients in 1..512) { "maxClients must be between 1 and 512" }
        require(maxTunnelDirections in 2..1024) { "maxTunnelDirections must be between 2 and 1024" }
        require(maxHeaderLineBytes in 256..65_536) { "maxHeaderLineBytes must be between 256 and 65536" }
        require(maxHeaderBytes in maxHeaderLineBytes..262_144) {
            "maxHeaderBytes must be at least maxHeaderLineBytes and at most 262144"
        }
        require(maxHeaderCount in 1..1024) { "maxHeaderCount must be between 1 and 1024" }
        require(maxHostLength in 1..255) { "maxHostLength must be between 1 and 255" }
        require(tunnelIdleTimeoutMillis in 5_000..3_600_000) {
            "tunnelIdleTimeoutMillis must be between 5000 and 3600000"
        }
    }
}

data class HttpProxyBridgeStats(
    val acceptedClients: Long,
    val rejectedClients: Long,
    val activeClients: Int,
    val activeTunnelDirections: Int,
    val uploadedBytes: Long,
    val downloadedBytes: Long,
    val authFailures: Long,
    val parseFailures: Long,
    val tunnelFailures: Long,
)

class HttpProxyBridge {
    @Volatile
    private var serverSocket: ServerSocket? = null
    @Volatile
    private var running = false
    @Volatile
    private var clientExecutor: ExecutorService? = null
    @Volatile
    private var tunnelExecutor: ExecutorService? = null
    @Volatile
    private var clientSlots: Semaphore? = null
    @Volatile
    private var tunnelSlots: Semaphore? = null
    @Volatile
    private var activeLimits = HttpProxyBridgeLimits()

    private val acceptedClients = AtomicLong()
    private val rejectedClients = AtomicLong()
    private val uploadedBytes = AtomicLong()
    private val downloadedBytes = AtomicLong()
    private val authFailures = AtomicLong()
    private val parseFailures = AtomicLong()
    private val tunnelFailures = AtomicLong()

    fun start(
        listenHost: String,
        listenPort: Int,
        socksHost: String,
        socksPort: Int,
        socksUsername: String? = null,
        socksPassword: String? = null,
        limits: HttpProxyBridgeLimits = HttpProxyBridgeLimits(),
        onOutput: (String) -> Unit = {},
    ) {
        stop()
        if (listenPort !in 1..65535 || socksPort !in 1..65535) {
            throw IllegalArgumentException("Invalid HTTP proxy or SOCKS port")
        }

        val nextClientExecutor = Executors.newFixedThreadPool(
            limits.maxClients,
            namedThreadFactory("whitedns-http-client"),
        )
        val nextTunnelExecutor = Executors.newFixedThreadPool(
            limits.maxTunnelDirections,
            namedThreadFactory("whitedns-http-tunnel"),
        )
        try {
            val bindHost = listenHost.trim().ifEmpty { "127.0.0.1" }
            val socket = ServerSocket().apply {
                reuseAddress = true
                bind(InetSocketAddress(InetAddress.getByName(bindHost), listenPort))
            }
            serverSocket = socket
            clientExecutor = nextClientExecutor
            tunnelExecutor = nextTunnelExecutor
            clientSlots = Semaphore(limits.maxClients)
            tunnelSlots = Semaphore(limits.maxTunnelDirections)
            activeLimits = limits
            running = true
            onOutput("HTTP proxy is listening on $bindHost:$listenPort")

            namedThreadFactory("whitedns-http-accept").newThread {
                acceptClients(socket, socksHost, socksPort, socksUsername, socksPassword, limits)
            }.start()
        } catch (error: Exception) {
            nextClientExecutor.shutdownNow()
            nextTunnelExecutor.shutdownNow()
            throw error
        }
    }

    fun stop() {
        running = false
        val socket = serverSocket
        val clients = clientExecutor
        val tunnels = tunnelExecutor
        serverSocket = null
        clientExecutor = null
        tunnelExecutor = null
        clientSlots = null
        tunnelSlots = null
        runCatching { socket?.close() }
        clients?.shutdownNow()
        tunnels?.shutdownNow()
    }

    fun snapshotStats(): HttpProxyBridgeStats {
        val limits = activeLimits
        val clients = clientSlots
        val tunnels = tunnelSlots
        return HttpProxyBridgeStats(
            acceptedClients = acceptedClients.get(),
            rejectedClients = rejectedClients.get(),
            activeClients = limits.maxClients - (clients?.availablePermits() ?: limits.maxClients),
            activeTunnelDirections = limits.maxTunnelDirections - (tunnels?.availablePermits() ?: limits.maxTunnelDirections),
            uploadedBytes = uploadedBytes.get(),
            downloadedBytes = downloadedBytes.get(),
            authFailures = authFailures.get(),
            parseFailures = parseFailures.get(),
            tunnelFailures = tunnelFailures.get(),
        )
    }

    private fun acceptClients(
        socket: ServerSocket,
        socksHost: String,
        socksPort: Int,
        socksUsername: String?,
        socksPassword: String?,
        limits: HttpProxyBridgeLimits,
    ) {
        while (running) {
            val client = try {
                socket.accept()
            } catch (_: IOException) {
                break
            }
            val slots = clientSlots
            val executor = clientExecutor
            if (slots == null || executor == null || !slots.tryAcquire()) {
                rejectedClients.incrementAndGet()
                writeCapacityErrorAndClose(client)
                continue
            }
            acceptedClients.incrementAndGet()
            try {
                executor.execute {
                    try {
                        handleClient(client, socksHost, socksPort, socksUsername, socksPassword, limits)
                    } finally {
                        slots.release()
                    }
                }
            } catch (_: RuntimeException) {
                slots.release()
                rejectedClients.incrementAndGet()
                writeCapacityErrorAndClose(client)
            }
        }
    }

    private fun handleClient(
        client: Socket,
        socksHost: String,
        socksPort: Int,
        socksUsername: String?,
        socksPassword: String?,
        limits: HttpProxyBridgeLimits,
    ) {
        client.use { clientSocket ->
            try {
                clientSocket.soTimeout = ClientReadTimeoutMillis
                val input = clientSocket.getInputStream()
                val output = clientSocket.getOutputStream()

                val requestLine = readHttpLine(input, limits) ?: return
                val headers = readHeaders(input, limits) ?: run {
                    parseFailures.incrementAndGet()
                    return
                }
                if (!isAuthorized(headers, socksUsername, socksPassword)) {
                    authFailures.incrementAndGet()
                    output.write(
                        "HTTP/1.1 407 Proxy Authentication Required\r\nProxy-Authenticate: Basic realm=\"WhiteDNS\"\r\nConnection: close\r\n\r\n"
                            .toByteArray(StandardCharsets.ISO_8859_1),
                    )
                    output.flush()
                    return
                }

                val parts = requestLine.split(' ', limit = 3)
                if (parts.size < 3) {
                    parseFailures.incrementAndGet()
                    writeHttpError(output, 400, "Bad Request")
                    return
                }

                val method = parts[0].uppercase(Locale.US)
                if (method == "CONNECT") {
                    val target = parseHostPort(parts[1], defaultPort = 443, maxHostLength = limits.maxHostLength)
                    if (target == null) {
                        parseFailures.incrementAndGet()
                        writeHttpError(output, 400, "Bad Request")
                        return
                    }
                    val upstream = connectViaSocks(socksHost, socksPort, socksUsername, socksPassword, target.host, target.port)
                    output.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(StandardCharsets.ISO_8859_1))
                    output.flush()
                    tunnel(clientSocket, upstream, limits)
                    return
                }

                val rewrittenRequest = rewriteHttpRequest(parts, headers, limits) ?: run {
                    parseFailures.incrementAndGet()
                    writeHttpError(output, 400, "Bad Request")
                    return
                }
                val upstream = connectViaSocks(
                    socksHost = socksHost,
                    socksPort = socksPort,
                    socksUsername = socksUsername,
                    socksPassword = socksPassword,
                    host = rewrittenRequest.host,
                    port = rewrittenRequest.port,
                )
                upstream.getOutputStream().write(rewrittenRequest.headerBytes)
                upstream.getOutputStream().flush()
                tunnel(clientSocket, upstream, limits)
            } catch (_: Exception) {
                tunnelFailures.incrementAndGet()
                runCatching {
                    writeHttpError(clientSocket.getOutputStream(), 502, "Bad Gateway")
                }
            }
        }
    }

    private fun tunnel(client: Socket, upstream: Socket, limits: HttpProxyBridgeLimits) {
        upstream.use { upstreamSocket ->
            client.soTimeout = limits.tunnelIdleTimeoutMillis
            upstreamSocket.soTimeout = limits.tunnelIdleTimeoutMillis
            val done = CountDownLatch(2)
            submitTunnelCopy(client.getInputStream(), upstreamSocket, uploadedBytes, done)
            submitTunnelCopy(upstreamSocket.getInputStream(), client, downloadedBytes, done)
            try {
                done.await()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                tunnelFailures.incrementAndGet()
                runCatching { client.close() }
                runCatching { upstreamSocket.close() }
            }
        }
    }

    private fun submitTunnelCopy(
        input: InputStream,
        outputSocket: Socket,
        byteCounter: AtomicLong,
        done: CountDownLatch,
    ) {
        val slots = tunnelSlots ?: throw IOException("HTTP proxy bridge is stopped")
        val executor = tunnelExecutor ?: throw IOException("HTTP proxy bridge is stopped")
        if (!slots.tryAcquire()) {
            throw IOException("HTTP proxy tunnel capacity exhausted")
        }
        try {
            executor.execute {
                try {
                    copyAndCloseOutput(input, outputSocket, byteCounter)
                } finally {
                    slots.release()
                    done.countDown()
                }
            }
        } catch (error: RuntimeException) {
            slots.release()
            done.countDown()
            throw error
        }
    }

    private fun copyAndCloseOutput(input: InputStream, outputSocket: Socket, byteCounter: AtomicLong) {
        runCatching {
            val buffer = ByteArray(16 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) {
                    break
                }
                outputSocket.getOutputStream().write(buffer, 0, count)
                byteCounter.addAndGet(count.toLong())
            }
            outputSocket.getOutputStream().flush()
        }.onFailure {
            tunnelFailures.incrementAndGet()
        }
        runCatching { outputSocket.shutdownOutput() }
        runCatching { outputSocket.close() }
    }

    private fun connectViaSocks(
        socksHost: String,
        socksPort: Int,
        socksUsername: String?,
        socksPassword: String?,
        host: String,
        port: Int,
    ): Socket {
        val socket = Socket()
        socket.connect(InetSocketAddress(socksHost, socksPort), SocksConnectTimeoutMillis)
        socket.soTimeout = SocksReadTimeoutMillis
        val input = socket.getInputStream()
        val output = socket.getOutputStream()

        val useAuth = !socksUsername.isNullOrBlank() && !socksPassword.isNullOrBlank()
        output.write(byteArrayOf(0x05, 0x01, if (useAuth) 0x02 else 0x00))
        output.flush()
        val methodReply = input.readExactly(2)
        if (methodReply[0] != 0x05.toByte() || methodReply[1] == 0xFF.toByte()) {
            socket.close()
            throw IOException("SOCKS authentication method rejected")
        }

        if (methodReply[1] == 0x02.toByte()) {
            val user = socksUsername.orEmpty().toByteArray(StandardCharsets.UTF_8)
            val pass = socksPassword.orEmpty().toByteArray(StandardCharsets.UTF_8)
            if (user.size > 255 || pass.size > 255) {
                socket.close()
                throw IOException("SOCKS credentials are too long")
            }
            output.write(byteArrayOf(0x01, user.size.toByte()))
            output.write(user)
            output.write(pass.size)
            output.write(pass)
            output.flush()
            val authReply = input.readExactly(2)
            if (authReply[1] != 0x00.toByte()) {
                socket.close()
                throw IOException("SOCKS authentication failed")
            }
        }

        val request = ByteArrayOutputStream()
        request.write(byteArrayOf(0x05, 0x01, 0x00))
        val ipv4 = parseIpv4(host)
        if (ipv4 != null) {
            request.write(0x01)
            request.write(ipv4)
        } else {
            val hostBytes = host.removeSurrounding("[", "]").toByteArray(StandardCharsets.UTF_8)
            if (hostBytes.isEmpty() || hostBytes.size > 255) {
                socket.close()
                throw IOException("Target host is invalid")
            }
            request.write(0x03)
            request.write(hostBytes.size)
            request.write(hostBytes)
        }
        request.write((port ushr 8) and 0xFF)
        request.write(port and 0xFF)
        output.write(request.toByteArray())
        output.flush()

        val reply = input.readExactly(4)
        if (reply[0] != 0x05.toByte() || reply[1] != 0x00.toByte()) {
            socket.close()
            throw IOException("SOCKS connect failed: ${reply[1].toInt() and 0xFF}")
        }
        when (reply[3].toInt() and 0xFF) {
            0x01 -> input.readExactly(4)
            0x03 -> input.readExactly(input.read())
            0x04 -> input.readExactly(16)
            else -> Unit
        }
        input.readExactly(2)
        socket.soTimeout = 0
        return socket
    }

    private fun rewriteHttpRequest(
        parts: List<String>,
        headers: List<String>,
        limits: HttpProxyBridgeLimits,
    ): ProxiedRequest? {
        val uri = runCatching { URI(parts[1]) }.getOrNull()
        val host: String
        val port: Int
        val path: String

        if (uri?.scheme.equals("http", ignoreCase = true) && !uri?.host.isNullOrBlank()) {
            host = uri.host
            port = if (uri.port > 0) uri.port else 80
            path = buildString {
                append(uri.rawPath.takeIf { !it.isNullOrEmpty() } ?: "/")
                if (!uri.rawQuery.isNullOrEmpty()) {
                    append('?')
                    append(uri.rawQuery)
                }
            }
        } else {
            val hostHeader = headers.firstOrNull { it.startsWith("Host:", ignoreCase = true) }
                ?.substringAfter(':')
                ?.trim()
                ?: return null
            val target = parseHostPort(hostHeader, defaultPort = 80, maxHostLength = limits.maxHostLength) ?: return null
            host = target.host
            port = target.port
            path = parts[1].takeIf { it.startsWith("/") } ?: return null
        }
        if (host.length > limits.maxHostLength) {
            return null
        }

        val headerBytes = buildString {
            append(parts[0])
            append(' ')
            append(path)
            append(' ')
            append(parts[2])
            append("\r\n")
            for (header in headers) {
                val name = header.substringBefore(':').trim().lowercase(Locale.US)
                if (name == "proxy-connection" || name == "proxy-authorization") {
                    continue
                }
                append(header)
                append("\r\n")
            }
            append("\r\n")
        }.toByteArray(StandardCharsets.ISO_8859_1)

        return ProxiedRequest(host = host, port = port, headerBytes = headerBytes)
    }

    private fun isAuthorized(headers: List<String>, username: String?, password: String?): Boolean {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return true
        }
        val header = headers.firstOrNull { it.startsWith("Proxy-Authorization:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
            ?: return false
        if (!header.startsWith("Basic ", ignoreCase = true)) {
            return false
        }
        val decoded = runCatching {
            String(Base64.decode(header.substringAfter(' ').trim(), Base64.DEFAULT), StandardCharsets.UTF_8)
        }.getOrNull() ?: return false
        return decoded == "$username:$password"
    }

    private fun readHeaders(input: InputStream, limits: HttpProxyBridgeLimits): List<String>? {
        val headers = mutableListOf<String>()
        var totalBytes = 0
        while (true) {
            val line = readHttpLine(input, limits) ?: return null
            totalBytes += line.toByteArray(StandardCharsets.ISO_8859_1).size + 2
            if (totalBytes > limits.maxHeaderBytes) {
                return null
            }
            if (line.isEmpty()) {
                return headers
            }
            headers += line
            if (headers.size > limits.maxHeaderCount) {
                return null
            }
        }
    }

    private fun readHttpLine(input: InputStream, limits: HttpProxyBridgeLimits): String? {
        val buffer = ByteArrayOutputStream()
        while (buffer.size() <= limits.maxHeaderLineBytes) {
            val value = input.read()
            if (value == -1) {
                return if (buffer.size() == 0) null else buffer.toString(StandardCharsets.ISO_8859_1.name())
            }
            if (value == '\n'.code) {
                val bytes = buffer.toByteArray()
                val length = if (bytes.lastOrNull() == '\r'.code.toByte()) bytes.size - 1 else bytes.size
                return String(bytes, 0, length, StandardCharsets.ISO_8859_1)
            }
            buffer.write(value)
        }
        return null
    }

    private fun InputStream.readExactly(length: Int): ByteArray {
        if (length < 0) {
            throw IOException("Invalid read length")
        }
        val bytes = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val count = read(bytes, offset, length - offset)
            if (count == -1) {
                throw IOException("Unexpected end of stream")
            }
            offset += count
        }
        return bytes
    }

    private fun parseIpv4(host: String): ByteArray? {
        val parts = host.split('.')
        if (parts.size != 4) {
            return null
        }
        val bytes = ByteArray(4)
        for (idx in parts.indices) {
            val value = parts[idx].toIntOrNull() ?: return null
            if (value !in 0..255) {
                return null
            }
            bytes[idx] = value.toByte()
        }
        return bytes
    }

    private fun writeHttpError(output: java.io.OutputStream, code: Int, reason: String) {
        output.write("HTTP/1.1 $code $reason\r\nConnection: close\r\n\r\n".toByteArray(StandardCharsets.ISO_8859_1))
        output.flush()
    }

    private fun writeCapacityErrorAndClose(client: Socket) {
        runCatching {
            writeHttpError(client.getOutputStream(), 503, "Service Unavailable")
        }
        runCatching { client.close() }
    }

    private data class ProxiedRequest(
        val host: String,
        val port: Int,
        val headerBytes: ByteArray,
    )

    private companion object {
        const val ClientReadTimeoutMillis = 30_000
        const val SocksConnectTimeoutMillis = 3_000
        const val SocksReadTimeoutMillis = 10_000
    }
}

internal fun parseHttpProxyHostPort(authority: String, defaultPort: Int, maxHostLength: Int = 255): Pair<String, Int>? {
    val trimmed = authority.trim()
    if (trimmed.isEmpty()) {
        return null
    }
    if (trimmed.startsWith("[")) {
        val end = trimmed.indexOf(']')
        if (end <= 1) {
            return null
        }
        val host = trimmed.substring(1, end)
        val port = if (trimmed.length > end + 1) {
            if (trimmed[end + 1] != ':') return null
            trimmed.substring(end + 2).toIntOrNull() ?: return null
        } else {
            defaultPort
        }
        return if (host.isNotBlank() && host.length <= maxHostLength && port in 1..65535) host to port else null
    }

    val colonIndex = trimmed.lastIndexOf(':')
    val host = if (colonIndex > 0 && trimmed.indexOf(':') == colonIndex) {
        trimmed.substring(0, colonIndex)
    } else {
        trimmed
    }
    val port = if (colonIndex > 0 && trimmed.indexOf(':') == colonIndex) {
        trimmed.substring(colonIndex + 1).toIntOrNull() ?: return null
    } else {
        defaultPort
    }
    return if (host.isNotBlank() && host.length <= maxHostLength && port in 1..65535) host to port else null
}

private fun parseHostPort(authority: String, defaultPort: Int, maxHostLength: Int): HostPort? {
    return parseHttpProxyHostPort(authority, defaultPort, maxHostLength)?.let { (host, port) ->
        HostPort(host, port)
    }
}

private fun namedThreadFactory(threadName: String): ThreadFactory {
    return ThreadFactory { runnable ->
        Thread(runnable, threadName).apply {
            isDaemon = true
        }
    }
}
