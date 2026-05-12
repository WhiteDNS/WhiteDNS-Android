package shop.whitedns.client.quicksettings

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.app.NotificationManagerCompat
import java.util.UUID
import shop.whitedns.client.MainActivity
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.proxy.WhiteDnsProxyService
import shop.whitedns.client.runtime.WhiteDnsRuntimeState
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.vpn.WhiteDnsVpnService

class WhiteDnsTileService : TileService() {

    private val settingsStore by lazy {
        WhiteDnsSettingsStore(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val activeState = activeRuntimeState()
        if (activeState != null) {
            WhiteDnsVpnService.stop(applicationContext)
            WhiteDnsProxyService.stop(applicationContext)
            updateTile(subtitle = "Stopping", state = Tile.STATE_INACTIVE)
            return
        }

        val settings = settingsStore.load().runtimeConnectionSettings()
        val resolvedSettings = settings.resolve()
        val serverProfile = selectServerProfile(settings)
        if (resolvedSettings.resolverEntries.isEmpty() || serverProfile == null) {
            updateTile(subtitle = "Needs setup", state = Tile.STATE_UNAVAILABLE)
            openApp()
            return
        }
        val sessionId = UUID.randomUUID().toString()

        when (resolvedSettings.connectionMode) {
            WhiteDnsRuntimeStateStore.ModeVpn -> {
                if (
                    VpnService.prepare(this) != null ||
                    !NotificationManagerCompat.from(this).areNotificationsEnabled()
                ) {
                    updateTile(subtitle = "Needs permission", state = Tile.STATE_UNAVAILABLE)
                    openApp()
                    return
                }
                WhiteDnsVpnService.start(
                    context = applicationContext,
                    sessionId = sessionId,
                    serverProfile = serverProfile,
                    settings = settings,
                )
                updateTile(subtitle = "Starting VPN", state = Tile.STATE_ACTIVE)
            }
            else -> {
                WhiteDnsProxyService.start(
                    context = applicationContext,
                    sessionId = sessionId,
                    serverProfile = serverProfile,
                    settings = settings,
                )
                updateTile(subtitle = "Starting proxy", state = Tile.STATE_ACTIVE)
            }
        }
    }

    private fun activeRuntimeState(): WhiteDnsRuntimeState? {
        return WhiteDnsRuntimeStateStore.readAll(applicationContext)
            .firstOrNull { state ->
                state.status == WhiteDnsRuntimeStateStore.StatusReady ||
                    state.status == WhiteDnsRuntimeStateStore.StatusStarting
            }
    }

    private fun updateTile(
        subtitle: String? = null,
        state: Int? = null,
    ) {
        val tile = qsTile ?: return
        val activeState = activeRuntimeState()
        val resolvedState = state ?: if (activeState == null) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        val resolvedSubtitle = subtitle ?: when (activeState?.mode) {
            WhiteDnsRuntimeStateStore.ModeVpn -> "VPN active"
            WhiteDnsRuntimeStateStore.ModeProxy -> "Proxy active"
            else -> "Disconnected"
        }
        tile.label = "WhiteDNS"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = resolvedSubtitle
        }
        tile.state = resolvedState
        tile.updateTile()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun selectServerProfile(settings: WhiteDnsSettings): StormDnsServerProfile? {
        val connectionProfile = settings.selectedConnectionProfile()
        val domain = connectionProfile.customServerDomain
            .trim()
            .trimEnd('.')
        val encryptionKey = connectionProfile.customServerEncryptionKey.trim()
        if (domain.isBlank() || encryptionKey.isBlank()) {
            return null
        }
        return StormDnsServerProfile(
            id = "custom",
            label = "Custom StormDNS Server",
            domain = domain,
            encryptionKey = encryptionKey,
            encryptionMethod = connectionProfile.customServerEncryptionMethod.coerceIn(0, 5),
        )
    }
}
