package shop.whitedns.client.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Tun2SocksProcessManagerTest {
    @Test
    fun cliEnablesCapacityUdpLifetimeAndIpv6() {
        val args = buildTun2proxyCliArgs(
            proxyUrl = "socks5://user:pass@127.0.0.1:1080",
            tunFileDescriptor = 42,
            closeTunFileDescriptorOnDrop = false,
            settings = Tun2proxySettings(
                maxSessions = 1_024,
                tcpTimeoutSeconds = 300,
                udpTimeoutSeconds = 120,
                ipv6Enabled = true,
            ),
        )

        assertTrue(args.contains("--tun-fd 42"))
        assertTrue(args.contains("--max-sessions 1024"))
        assertTrue(args.contains("--tcp-timeout 300"))
        assertTrue(args.contains("--udp-timeout 120"))
        assertTrue(args.contains("--ipv6-enabled"))
        assertTrue(args.contains("--exit-on-fatal-error"))
    }

    @Test
    fun cliCanDisableIpv6AndClampsUnsafeValues() {
        val args = buildTun2proxyCliArgs(
            proxyUrl = "socks5://127.0.0.1:1080",
            tunFileDescriptor = 7,
            closeTunFileDescriptorOnDrop = true,
            settings = Tun2proxySettings(
                maxSessions = 1,
                tcpTimeoutSeconds = 1,
                udpTimeoutSeconds = 1,
                ipv6Enabled = false,
            ),
        )

        assertTrue(args.contains("--max-sessions 64"))
        assertTrue(args.contains("--tcp-timeout 30"))
        assertTrue(args.contains("--udp-timeout 10"))
        assertFalse(args.contains("--ipv6-enabled"))
    }
}
