package shop.whitedns.client

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import shop.whitedns.client.ui.WhiteDnsScreen
import shop.whitedns.client.ui.WhiteDnsTheme
import shop.whitedns.client.ui.WhiteDnsViewModel
import shop.whitedns.client.model.ConnectionStatus
import shop.whitedns.client.model.resolve

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<WhiteDnsViewModel>()

    override fun onResume() {
        super.onResume()
        viewModel.refreshBatteryOptimizationStatusWithRetry()
        viewModel.refreshNotificationStatus()
        viewModel.refreshRuntimeConnectionStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WhiteDnsTheme(
                themeMode = viewModel.uiState.settings.themeMode,
                languageCode = viewModel.uiState.settings.languageCode,
            ) {
                val context = LocalContext.current
                var shouldConnectAfterNotificationPermission by rememberSaveable { mutableStateOf(false) }
                val vpnPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        viewModel.beginConnection()
                    }
                }

                val requestVpnPermission = remember(context) {
                    {
                        val permissionIntent = VpnService.prepare(context)
                        if (permissionIntent == null) {
                            viewModel.beginConnection()
                        } else {
                            vpnPermissionLauncher.launch(permissionIntent)
                        }
                    }
                }
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    viewModel.refreshNotificationStatus()
                    val shouldConnect = shouldConnectAfterNotificationPermission
                    shouldConnectAfterNotificationPermission = false
                    if (granted) {
                        if (shouldConnect) {
                            requestVpnPermission()
                        }
                    } else {
                        openNotificationSettings()
                    }
                }

                val requestNotificationAccess = remember(context) {
                    request@{ connectAfterGrant: Boolean ->
                        viewModel.refreshNotificationStatus()
                        if (viewModel.uiState.notificationsEnabled) {
                            if (connectAfterGrant) {
                                requestVpnPermission()
                            }
                            return@request
                        }

                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            shouldConnectAfterNotificationPermission = connectAfterGrant
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            shouldConnectAfterNotificationPermission = false
                            openNotificationSettings()
                        }
                    }
                }

                WhiteDnsScreen(
                    uiState = viewModel.uiState,
                    onBatteryOptimizationClick = ::requestBatteryOptimizationExemption,
                    onNotificationPermissionClick = { requestNotificationAccess(false) },
                    onConnectClick = {
                        when (viewModel.uiState.connectionStatus) {
                            ConnectionStatus.DISCONNECTED -> {
                                if (viewModel.uiState.settings.resolve().connectionMode == "vpn") {
                                    requestNotificationAccess(true)
                                } else {
                                    viewModel.beginConnection()
                                }
                            }
                            ConnectionStatus.CONNECTING,
                            ConnectionStatus.CONNECTED -> viewModel.disconnect()
                        }
                    },
                    onScanFileSelected = viewModel::beginScanFromFile,
                    onScanDefaultListSelected = viewModel::beginScanFromDefaultResolvers,
                    onScanStartClick = viewModel::startPreparedScan,
                    onScanConnectionProfileChange = viewModel::updateScanConnectionProfile,
                    onScanWorkerCountChange = viewModel::updateScanWorkerCount,
                    onScanStopClick = viewModel::stopScan,
                    onScanResumeClick = viewModel::resumeScan,
                    onServerTestClick = { profileId -> viewModel.beginServerTest(profileId) },
                    onSettingsChange = viewModel::updateSettings,
                )
            }
        }
        handleProfileLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleProfileLinkIntent(intent)
    }

    private fun openNotificationSettings() {
        val packageUri = Uri.parse("package:$packageName")
        val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
        }
        runCatching {
            startActivity(settingsIntent)
        }.onFailure {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        }
    }

    private fun requestBatteryOptimizationExemption() {
        viewModel.refreshBatteryOptimizationStatus()
        if (viewModel.uiState.batteryOptimizationIgnored) {
            return
        }

        val packageUri = Uri.parse("package:$packageName")
        val requestIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
        runCatching {
            startActivity(requestIntent)
        }.onFailure {
            runCatching {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }.also {
            viewModel.refreshBatteryOptimizationStatusWithRetry()
        }
    }

    private fun handleProfileLinkIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW || intent.data?.scheme != StormDnsScheme) {
            return
        }
        if (intent.getBooleanExtra(ExtraProfileImportHandled, false)) {
            return
        }
        val link = intent.dataString?.takeIf(String::isNotBlank) ?: return
        intent.putExtra(ExtraProfileImportHandled, true)
        viewModel.importProfileLink(link)
    }

    private companion object {
        const val StormDnsScheme = "stormdns"
        const val ExtraProfileImportHandled = "shop.whitedns.client.extra.PROFILE_IMPORT_HANDLED"
    }
}
