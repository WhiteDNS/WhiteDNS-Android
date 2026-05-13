package shop.whitedns.client.runtime

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.min
import shop.whitedns.client.model.ResolvedWhiteDnsSettings

object WhiteDnsTrafficWarmup {
    const val RecentRealTrafficSuppressMillis = 30_000L
    const val ConstrainedKeepaliveBackoffMultiplier = 3

    fun runProbe(settings: ResolvedWhiteDnsSettings): Boolean {
        if (!settings.trafficWarmupEnabled) {
            return false
        }
        return runSocksHttpProbe(settings)
    }

    fun shouldSkipKeepaliveProbe(
        nowMillis: Long,
        lastRealTrafficSeenMillis: Long,
    ): Boolean {
        return lastRealTrafficSeenMillis > 0 &&
            nowMillis - lastRealTrafficSeenMillis < RecentRealTrafficSuppressMillis
    }

    fun keepaliveDelayMillis(
        settings: ResolvedWhiteDnsSettings,
        constrained: Boolean,
    ): Long {
        val baseDelay = settings.trafficKeepaliveIntervalSeconds.coerceAtLeast(1) * 1_000L
        return if (constrained) {
            baseDelay * ConstrainedKeepaliveBackoffMultiplier
        } else {
            baseDelay
        }
    }

    fun verifySocksRoute(settings: ResolvedWhiteDnsSettings): Boolean {
        return runSocksHttpProbe(settings)
    }

    private fun runSocksHttpProbe(settings: ResolvedWhiteDnsSettings): Boolean {
        return runCatching {
            Socket().use { socket ->
                socket.soTimeout = SocketTimeoutMillis
                socket.tcpNoDelay = true
                socket.connect(
                    InetSocketAddress(selectLocalSocksHost(settings.listenIp), settings.listenPort),
                    SocketTimeoutMillis,
                )
                val input = socket.getInputStream()
                val output = socket.getOutputStream()
                if (!negotiateSocks(input, output, settings)) {
                    return@runCatching false
                }
                if (!connectToProbeTarget(input, output)) {
                    return@runCatching false
                }
                output.write(ProbeHttpRequest)
                output.flush()
                runCatching {
                    input.read(ByteArray(ProbeReadBufferSize))
                }
                true
            }
        }.getOrDefault(false)
    }

    private fun negotiateSocks(
        input: InputStream,
        output: OutputStream,
        settings: ResolvedWhiteDnsSettings,
    ): Boolean {
        val method = if (settings.socks5Authentication) MethodUsernamePassword else MethodNoAuthentication
        output.write(byteArrayOf(SocksVersion, 1, method))
        output.flush()
        if (input.read() != SocksVersion.toInt()) {
            return false
        }
        return when (input.read()) {
            MethodNoAuthentication.toInt() -> true
            MethodUsernamePassword.toInt() -> authenticate(input, output, settings)
            else -> false
        }
    }

    private fun authenticate(
        input: InputStream,
        output: OutputStream,
        settings: ResolvedWhiteDnsSettings,
    ): Boolean {
        val username = settings.socksUsername.toByteArray(Charsets.UTF_8).limitedToSocksField()
        val password = settings.socksPassword.toByteArray(Charsets.UTF_8).limitedToSocksField()
        output.write(AuthVersion.toInt())
        output.write(username.size)
        output.write(username)
        output.write(password.size)
        output.write(password)
        output.flush()
        return input.read() == AuthVersion.toInt() && input.read() == AuthSuccess
    }

    private fun connectToProbeTarget(
        input: InputStream,
        output: OutputStream,
    ): Boolean {
        output.write(
            byteArrayOf(
                SocksVersion,
                CommandConnect,
                Reserved,
                AddressTypeIpv4,
                ProbeTargetIpA,
                ProbeTargetIpB,
                ProbeTargetIpC,
                ProbeTargetIpD,
                ProbeTargetPortHigh,
                ProbeTargetPortLow,
            ),
        )
        output.flush()
        if (input.read() != SocksVersion.toInt()) {
            return false
        }
        val reply = input.read()
        if (input.read() < 0) {
            return false
        }
        val addressType = input.read()
        val addressLength = when (addressType) {
            AddressTypeIpv4.toInt() -> 4
            AddressTypeDomain.toInt() -> input.read().takeIf { it >= 0 } ?: return false
            AddressTypeIpv6.toInt() -> 16
            else -> return false
        }
        if (!readAndDiscard(input, addressLength + PortLength)) {
            return false
        }
        return reply == ReplySucceeded
    }

    private fun readAndDiscard(input: InputStream, length: Int): Boolean {
        repeat(length) {
            if (input.read() < 0) {
                return false
            }
        }
        return true
    }

    private fun ByteArray.limitedToSocksField(): ByteArray {
        return copyOf(min(size, MaxSocksFieldLength))
    }

    private fun selectLocalSocksHost(listenIp: String): String {
        return when (listenIp.trim().removeSurrounding("[", "]")) {
            "", "0.0.0.0" -> "127.0.0.1"
            "::" -> "::1"
            else -> listenIp.trim().removeSurrounding("[", "]")
        }
    }

    private const val SocketTimeoutMillis = 4_000
    private const val ProbeReadBufferSize = 256
    private const val MaxSocksFieldLength = 255
    private const val PortLength = 2
    private const val AuthSuccess = 0
    private const val SocksVersion: Byte = 5
    private const val AuthVersion: Byte = 1
    private const val MethodNoAuthentication: Byte = 0
    private const val MethodUsernamePassword: Byte = 2
    private const val CommandConnect: Byte = 1
    private const val Reserved: Byte = 0
    private const val AddressTypeIpv4: Byte = 1
    private const val AddressTypeDomain: Byte = 3
    private const val AddressTypeIpv6: Byte = 4
    private const val ReplySucceeded = 0
    private const val ProbeTargetIpA: Byte = 1
    private const val ProbeTargetIpB: Byte = 1
    private const val ProbeTargetIpC: Byte = 1
    private const val ProbeTargetIpD: Byte = 1
    private const val ProbeTargetPortHigh: Byte = 0
    private const val ProbeTargetPortLow: Byte = 80
    private val ProbeHttpRequest = (
        "HEAD / HTTP/1.1\r\n" +
            "Host: 1.1.1.1\r\n" +
            "Connection: close\r\n" +
            "User-Agent: WhiteDNS/1\r\n" +
            "\r\n"
        ).toByteArray(Charsets.US_ASCII)
}
