package shop.whitedns.client.ui

import android.graphics.Bitmap
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Build

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import shop.whitedns.client.model.AdvancedSettingsProfile
import shop.whitedns.client.model.Choice
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ConnectionProgressState
import shop.whitedns.client.model.ConnectionStats
import shop.whitedns.client.model.ConnectionStatus
import shop.whitedns.client.model.ConnectionVerificationState
import shop.whitedns.client.model.ConnectionVerificationStatus
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.ResolverRuntimeState
import shop.whitedns.client.model.SplitTunnelAppInfo
import shop.whitedns.client.model.WhiteDnsOptions
import shop.whitedns.client.model.WhiteDnsProxyExposurePolicy
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsUiState
import shop.whitedns.client.model.applyResolverProfileToSelectedConnection
import shop.whitedns.client.model.deleteConnectionProfile
import shop.whitedns.client.model.deleteResolverProfile
import shop.whitedns.client.model.exportAllStormDnsProfileLinks
import shop.whitedns.client.model.exportStormDnsProfileLink
import shop.whitedns.client.model.importStormDnsProfileLinks
import shop.whitedns.client.model.matchesAdvancedProfile
import shop.whitedns.client.model.moveConnectionProfileToIndex
import shop.whitedns.client.model.moveResolverProfileToIndex
import shop.whitedns.client.model.normalizeServerDomainText
import shop.whitedns.client.model.normalizeServerDomains
import shop.whitedns.client.model.normalizedAdvancedProfiles
import shop.whitedns.client.model.normalizedConnectionProfiles
import shop.whitedns.client.model.normalizedResolverProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.resetAdvancedSettings
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.saveCurrentAdvancedProfileAs
import shop.whitedns.client.model.saveSelectedAdvancedProfile
import shop.whitedns.client.model.selectAdvancedProfile
import shop.whitedns.client.model.selectConnectionProfile
import shop.whitedns.client.model.selectedAdvancedProfile
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.model.selectedResolverProfile
import shop.whitedns.client.model.updateManualResolverText
import shop.whitedns.client.model.upsertConnectionProfile
import shop.whitedns.client.model.upsertResolverProfile
import shop.whitedns.client.model.validateResolverText
import shop.whitedns.client.storm.StormDnsConfigRenderer
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Locale
import java.util.UUID

@Composable
fun WhiteDnsScreen(
    uiState: WhiteDnsUiState,
    onBatteryOptimizationClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(WhiteDnsTab.CONNECT) }
    var profileCreateRequest by rememberSaveable { mutableStateOf<ProfileCreateRequest?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .whiteDnsPageBackground(),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                WhiteDnsTab.PROFILES -> ProfilesTabContent(
                    uiState = uiState,
                    createRequest = profileCreateRequest,
                    onCreateRequestConsumed = {
                        profileCreateRequest = null
                    },
                    onSettingsChange = onSettingsChange,
                )
                WhiteDnsTab.CONNECT -> ConnectTabContent(
                    uiState = uiState,
                    onBatteryOptimizationClick = onBatteryOptimizationClick,
                    onNotificationPermissionClick = onNotificationPermissionClick,
                    onConnectClick = onConnectClick,
                    onAddConnectionClick = {
                        profileCreateRequest = ProfileCreateRequest.CONNECTION
                        selectedTab = WhiteDnsTab.PROFILES
                    },
                    onAddResolverProfileClick = {
                        profileCreateRequest = ProfileCreateRequest.RESOLVER
                        selectedTab = WhiteDnsTab.PROFILES
                    },
                    onSettingsChange = onSettingsChange,
                )
                WhiteDnsTab.LOGS -> LogsTabContent(uiState = uiState)
                WhiteDnsTab.GUIDE -> GuideTabContent()
            }
        }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
    }
}

private enum class WhiteDnsTab(
    val label: String,
    val icon: ImageVector,
) {
    PROFILES("Profiles", Icons.Filled.Apps),
    CONNECT("Connect", Icons.Rounded.PowerSettingsNew),
    LOGS("Logs", Icons.Rounded.Link),
    GUIDE("Guide", Icons.Rounded.Tune),
}

private enum class ProfileCreateRequest {
    CONNECTION,
    RESOLVER,
}

private data class GuideEntry(
    val label: String,
    val detail: String,
)

private fun Modifier.whiteDnsPageBackground(): Modifier {
    return drawBehind {
        drawRect(color = WhiteDnsPalette.Background)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    WhiteDnsPalette.Accent.copy(alpha = 0.30f),
                    Color(0xFF245D72).copy(alpha = 0.17f),
                    Color(0xFF111420).copy(alpha = 0.08f),
                    Color.Transparent,
                ),
                center = Offset(x = size.width, y = 0f),
                radius = size.maxDimension * 1.08f,
            ),
        )
    }
}

@Composable
private fun ConnectTabContent(
    uiState: WhiteDnsUiState,
    onBatteryOptimizationClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onConnectClick: () -> Unit,
    onAddConnectionClick: () -> Unit,
    onAddResolverProfileClick: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val settings = uiState.settings
    var showResolverRequiredMessage by rememberSaveable { mutableStateOf(false) }
    var selectorSheetType by rememberSaveable { mutableStateOf<HomeSelectorType?>(null) }
    var selectorSheetVisible by rememberSaveable { mutableStateOf(false) }
    var advancedEditorVisible by rememberSaveable { mutableStateOf(false) }
    var showAdvancedSaveAsDialog by rememberSaveable { mutableStateOf(false) }
    var showConnectionTomlDialog by rememberSaveable { mutableStateOf(false) }
    val runtimeSettings = remember(settings) { settings.runtimeConnectionSettings() }
    val resolvedSettings = remember(runtimeSettings) { runtimeSettings.resolve() }
    val connectionProfiles = remember(settings) { settings.normalizedConnectionProfiles() }
    val selectedConnectionProfile = remember(settings) { settings.selectedConnectionProfile() }
    val resolverProfiles = remember(settings) { settings.normalizedResolverProfiles() }
    val advancedProfiles = remember(settings) { settings.normalizedAdvancedProfiles() }
    val selectedAdvancedProfile = remember(settings) { settings.selectedAdvancedProfile() }
    val advancedProfileDirty = remember(settings, selectedAdvancedProfile) {
        !settings.matchesAdvancedProfile(selectedAdvancedProfile)
    }
    val hasInitialServerProfile = remember(connectionProfiles) {
        connectionProfiles.any { profile ->
            profile.customServerDomain.isNotBlank() &&
                profile.customServerEncryptionKey.isNotBlank()
        }
    }
    val hasInitialResolverProfile = resolverProfiles.isNotEmpty()
    val showInitialSetup = !hasInitialServerProfile || !hasInitialResolverProfile
    val selectedResolverProfile = remember(settings) { settings.selectedResolverProfile() }
    val resolverValidation = remember(settings.resolverText) { validateResolverText(settings.resolverText) }
    val splitTunnelApps = uiState.splitTunnelApps
    val splitTunnelAppLabels = remember(splitTunnelApps) {
        splitTunnelApps.associate { it.packageName to it.label }
    }
    val connectionSelectorItems = remember(connectionProfiles) {
        connectionProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = profile.name,
                detail = serverRouteSummary(profile.customServerDomain),
            )
        }
    }
    val resolverSelectorItems = remember(resolverProfiles) {
        resolverProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = profile.name,
                detail = resolverCountLabel(validateResolverText(profile.resolverText).normalizedResolvers.size),
            )
        }
    }
    val advancedSelectorItems = remember(advancedProfiles) {
        advancedProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = profile.name,
                detail = advancedProfileSummary(profile),
            )
        }
    }
    val resolverSelectorDetail = selectedResolverProfile?.resolverText?.let { resolverText ->
        resolverCountLabel(validateResolverText(resolverText).normalizedResolvers.size)
    } ?: if (resolverProfiles.isEmpty()) {
        "No saved lists"
    } else {
        "Not selected"
    }
    val hasResolvers = resolverValidation.isValid
    val proxyIpAddress = displayProxyIpAddress(
        listenIp = resolvedSettings.listenIp,
        networkIpAddress = uiState.networkIpAddress,
    )
    val proxyAddress = "$proxyIpAddress:${resolvedSettings.listenPort}"
    val httpProxyAddress = "$proxyIpAddress:${resolvedSettings.httpProxyPort}"
    val showNotificationBanner = resolvedSettings.connectionMode == "vpn" && !uiState.notificationsEnabled
    val showBatteryBanner = !uiState.batteryOptimizationIgnored

    fun normalizeManualResolverInput() {
        if (selectedResolverProfile != null || !resolverValidation.isValid) {
            return
        }
        if (resolverValidation.normalizedText != settings.resolverText) {
            onSettingsChange(settings.updateManualResolverText(resolverValidation.normalizedText))
        }
    }

    fun openSelector(type: HomeSelectorType) {
        if (type != HomeSelectorType.ADVANCED) {
            advancedEditorVisible = false
        }
        selectorSheetType = type
        selectorSheetVisible = true
    }

    fun closeSelector() {
        advancedEditorVisible = false
        selectorSheetVisible = false
    }

    BackHandler(enabled = advancedEditorVisible) {
        advancedEditorVisible = false
    }

    BackHandler(enabled = selectorSheetVisible && !advancedEditorVisible) {
        closeSelector()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderCard()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            AnimatedVisibility(
                visible = showNotificationBanner,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
            ) {
                Column {
                    NotificationPermissionBanner(onClick = onNotificationPermissionClick)
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            AnimatedVisibility(
                visible = showBatteryBanner,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
            ) {
                Column {
                    BatteryOptimizationBanner(onClick = onBatteryOptimizationClick)
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            Spacer(
                modifier = Modifier.height(
                    if (!showNotificationBanner && !showBatteryBanner) 36.dp else 18.dp,
                ),
            )
                ConnectionModeSegmentedControl(
                    modifier = Modifier.fillMaxWidth(),
                    selectedMode = resolvedSettings.connectionMode,
                    enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                    onModeChange = { connectionMode ->
                        onSettingsChange(settings.copy(connectionMode = connectionMode))
                    },
                )
            AnimatedVisibility(
                visible = resolvedSettings.connectionMode == "vpn",
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    AnimatedVisibility(
                        visible = !settings.fullVpnPerformanceWarningDismissed,
                        enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
                    ) {
                        Column {
                            FullVpnPerformanceWarning(
                                onDismiss = {
                                    onSettingsChange(settings.copy(fullVpnPerformanceWarningDismissed = true))
                                },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    SplitTunnelSettingsPanel(
                        settings = settings,
                        apps = splitTunnelApps,
                        appsLoading = uiState.splitTunnelAppsLoading,
                        onSettingsChange = onSettingsChange,
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            ConnectButton(
                status = uiState.connectionStatus,
                progressState = uiState.connectionProgress,
                enabled = uiState.connectionStatus != ConnectionStatus.DISCONNECTING &&
                    (uiState.connectionStatus != ConnectionStatus.DISCONNECTED || hasResolvers),
                onClick = {
                    if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED && !hasResolvers) {
                        showResolverRequiredMessage = true
                    } else {
                        showResolverRequiredMessage = false
                        if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED) {
                            normalizeManualResolverInput()
                        }
                        onConnectClick()
                    }
                },
            )
            AnimatedVisibility(
                visible = uiState.connectionStatus != ConnectionStatus.CONNECTED &&
                    uiState.connectionStatus != ConnectionStatus.DISCONNECTING,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HomeSelectorCard(
                            label = "Connection",
                            value = selectedConnectionProfile.name,
                            detail = serverRouteSummary(selectedConnectionProfile.customServerDomain),
                            selected = true,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.CONNECTION) },
                        )
                        HomeSelectorCard(
                            label = "Resolver",
                            value = selectedResolverProfile?.name ?: "Resolver Profile",
                            detail = resolverSelectorDetail,
                            selected = selectedResolverProfile != null,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.RESOLVER) },
                        )
                        AdvancedProfileControls(
                            selectedProfile = selectedAdvancedProfile,
                            dirty = advancedProfileDirty,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onSelectClick = { openSelector(HomeSelectorType.ADVANCED) },
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ResolverRuntimeSummary(
                        resolverState = uiState.resolverRuntimeState,
                        progressState = uiState.connectionProgress,
                        connectionStatus = uiState.connectionStatus,
                    )
                    ConnectionVerificationSummary(
                        modifier = Modifier.padding(top = 10.dp),
                        verification = uiState.connectionVerification,
                    )
                    AnimatedVisibility(
                        visible = uiState.connectionVerification.status == ConnectionVerificationStatus.Failed,
                        enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
                    ) {
                        RouteTroubleshootingCard(
                            modifier = Modifier.padding(top = 10.dp),
                            connectionMode = resolvedSettings.connectionMode,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = showResolverRequiredMessage &&
                    uiState.connectionStatus == ConnectionStatus.DISCONNECTED &&
                    !hasResolvers,
                enter = fadeIn(animationSpec = tween(160)) + expandVertically(animationSpec = tween(160)),
                exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
            ) {
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "You need resolvers to connect.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        color = WhiteDnsPalette.WarningText,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            AnimatedVisibility(
                visible = showInitialSetup,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(18.dp))
                    ConnectionSetupCard(
                        selectedConnectionProfile = selectedConnectionProfile,
                        selectedResolverProfile = selectedResolverProfile,
                        resolverCount = resolverValidation.normalizedResolvers.size,
                        resolverIssue = resolverValidation.invalidEntries.firstOrNull()?.let { invalidEntry ->
                            "Invalid resolver IP: $invalidEntry"
                        } ?: if (resolverValidation.normalizedResolvers.isEmpty()) {
                            "No resolvers configured"
                        } else {
                            null
                        },
                        actionsEnabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                        onAddConnectionClick = onAddConnectionClick,
                        onAddResolverProfileClick = onAddResolverProfileClick,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StarterWalkthroughCard()
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            AnimatedVisibility(
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180)),
            ) {
                LiveSpeedStrip(stats = uiState.connectionStats)
            }

            AnimatedVisibility(
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                enter = fadeIn(animationSpec = tween(260)) + expandVertically(animationSpec = tween(260)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                ) {
                    ConnectionInfoCard(
                        connectionProfileName = selectedConnectionProfile.name,
                        resolverProfileName = selectedResolverProfile?.name ?: "Manual resolvers",
                        settingProfileName = selectedAdvancedProfile.displayName(dirty = advancedProfileDirty),
                        listenAddress = proxyAddress,
                        httpProxyAddress = httpProxyAddress,
                        connectionMode = WhiteDnsOptions.connectionModeLabel(resolvedSettings.connectionMode),
                        httpProxyEnabled = resolvedSettings.httpProxyEnabled,
                        protocol = resolvedSettings.protocolType,
                        socksAuthEnabled = resolvedSettings.socks5Authentication,
                        username = resolvedSettings.socksUsername,
                        password = resolvedSettings.socksPassword,
                        stats = uiState.connectionStats,
                        showProxyDetails = resolvedSettings.connectionMode == "proxy",
                        splitTunnelMode = resolvedSettings.splitTunnelMode,
                        splitTunnelPackages = resolvedSettings.splitTunnelPackages,
                        splitTunnelAppLabels = splitTunnelAppLabels,
                        canDownloadToml = selectedConnectionProfile.customServerDomain.isNotBlank() &&
                            selectedConnectionProfile.customServerEncryptionKey.isNotBlank(),
                        onDownloadToml = {
                            showConnectionTomlDialog = true
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            FooterLink()
        }
        }

        selectorSheetType?.let { activeSelector ->
            HomeSelectorSheet(
                visible = selectorSheetVisible,
                title = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> "Connection Profiles"
                    HomeSelectorType.RESOLVER -> "Resolver Profiles"
                    HomeSelectorType.ADVANCED -> "Advanced Profiles"
                },
                searchPlaceholder = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> "Search connections"
                    HomeSelectorType.RESOLVER -> "Search resolvers"
                    HomeSelectorType.ADVANCED -> "Search advanced profiles"
                },
                items = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> connectionSelectorItems
                    HomeSelectorType.RESOLVER -> resolverSelectorItems
                    HomeSelectorType.ADVANCED -> advancedSelectorItems
                },
                selectedId = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> selectedConnectionProfile.id
                    HomeSelectorType.RESOLVER -> selectedResolverProfile?.id
                    HomeSelectorType.ADVANCED -> selectedAdvancedProfile.id
                },
                emptyMessage = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> "No connection profiles found."
                    HomeSelectorType.RESOLVER -> "No resolver profiles found."
                    HomeSelectorType.ADVANCED -> "No advanced profiles found."
                },
                onDismiss = { closeSelector() },
                onSelect = { itemId ->
                    showResolverRequiredMessage = false
                    when (activeSelector) {
                        HomeSelectorType.CONNECTION -> {
                            onSettingsChange(settings.selectConnectionProfile(itemId))
                        }
                        HomeSelectorType.RESOLVER -> {
                            onSettingsChange(settings.applyResolverProfileToSelectedConnection(itemId))
                        }
                        HomeSelectorType.ADVANCED -> {
                            onSettingsChange(settings.selectAdvancedProfile(itemId))
                        }
                    }
                    closeSelector()
                },
                onEdit = if (activeSelector == HomeSelectorType.ADVANCED) {
                    { itemId ->
                        showResolverRequiredMessage = false
                        onSettingsChange(settings.selectAdvancedProfile(itemId))
                        advancedEditorVisible = true
                    }
                } else {
                    null
                },
            )
        }

        AdvancedSettingsEditorSheet(
            visible = selectorSheetVisible && advancedEditorVisible,
            settings = settings,
            selectedProfile = selectedAdvancedProfile,
            dirty = advancedProfileDirty,
            showProxySettings = resolvedSettings.connectionMode == "proxy",
            canEdit = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
            onBack = { advancedEditorVisible = false },
            onSettingsChange = onSettingsChange,
            onSaveClick = {
                onSettingsChange(settings.saveSelectedAdvancedProfile())
            },
            onSaveAsClick = {
                showAdvancedSaveAsDialog = true
            },
            onResetClick = {
                onSettingsChange(settings.resetAdvancedSettings())
            },
        )

        if (showAdvancedSaveAsDialog) {
            AdvancedProfileSaveAsDialog(
                initialName = advancedSaveAsInitialName(selectedAdvancedProfile),
                onDismiss = { showAdvancedSaveAsDialog = false },
                onSave = { profileName ->
                    onSettingsChange(settings.saveCurrentAdvancedProfileAs(profileName))
                    showAdvancedSaveAsDialog = false
                },
            )
        }

        if (showConnectionTomlDialog) {
            ConnectionProfileExportDialog(
                title = "DOWNLOAD TOML",
                fieldLabel = "client_config.toml",
                placeholder = "client_config.toml",
                showQr = false,
                linkResult = remember(settings, selectedConnectionProfile, showConnectionTomlDialog) {
                    runCatching {
                        StormDnsConfigRenderer.renderClientToml(
                            connectionProfile = selectedConnectionProfile,
                            settings = settings,
                        )
                    }
                },
                onDismiss = { showConnectionTomlDialog = false },
                onShare = { toml ->
                    shareClientConfigToml(context, toml)
                },
            )
        }
    }
}

@Composable
private fun ProfilesTabContent(
    uiState: WhiteDnsUiState,
    createRequest: ProfileCreateRequest?,
    onCreateRequestConsumed: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    var selectedProfileTab by rememberSaveable { mutableStateOf(ProfileTab.CONNECTION) }
    var connectionCreateRequestId by rememberSaveable { mutableStateOf(0) }
    var resolverCreateRequestId by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(createRequest) {
        when (createRequest) {
            ProfileCreateRequest.CONNECTION -> {
                selectedProfileTab = ProfileTab.CONNECTION
                connectionCreateRequestId += 1
                onCreateRequestConsumed()
            }
            ProfileCreateRequest.RESOLVER -> {
                selectedProfileTab = ProfileTab.RESOLVER
                resolverCreateRequestId += 1
                onCreateRequestConsumed()
            }
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCard()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp),
        ) {
            ProfileTabSwitch(
                selectedTab = selectedProfileTab,
                onTabSelected = { selectedProfileTab = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = if (selectedProfileTab == ProfileTab.CONNECTION) {
                    "CONNECTION PROFILES"
                } else {
                    "RESOLVER PROFILES"
                },
            ) {
                when (selectedProfileTab) {
                    ProfileTab.CONNECTION -> ConnectionProfilesSettings(
                        settings = uiState.settings,
                        activeConnectionProfileId = uiState.activeConnectionProfileId,
                        connectionStatus = uiState.connectionStatus,
                        openCreateRequestId = connectionCreateRequestId,
                        onSettingsChange = onSettingsChange,
                    )
                    ProfileTab.RESOLVER -> ResolverProfilesSettings(
                        settings = uiState.settings,
                        connectionStatus = uiState.connectionStatus,
                        openCreateRequestId = resolverCreateRequestId,
                        onSettingsChange = onSettingsChange,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            FooterLink()
        }
    }
}

private enum class ProfileTab(val label: String) {
    CONNECTION("Connection Profile"),
    RESOLVER("Resolver Profile"),
}

@Composable
private fun LogsTabContent(uiState: WhiteDnsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCard()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp),
        ) {
            ConnectionLogsBlock(uiState = uiState, expanded = true)
            Spacer(modifier = Modifier.height(24.dp))
            FooterLink()
        }
    }
}

@Composable
private fun GuideTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeaderCard()
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StarterWalkthroughCard()
                GuideSection(
                    title = "CONNECTION BASICS",
                    intro = "WhiteDNS needs a server profile, a resolver list, and one runtime mode. Keep these three pieces separate while troubleshooting.",
                    entries = listOf(
                        GuideEntry(
                            label = "Domain(s)",
                            detail = "The public server route StormDNS will use. Multiple domains can improve fallback behavior, but every domain must be valid and controlled by the same setup.",
                        ),
                        GuideEntry(
                            label = "Encryption key",
                            detail = "The shared secret for the tunnel. A wrong key can still look like a connection attempt, but traffic will not become useful.",
                        ),
                        GuideEntry(
                            label = "Resolvers",
                            detail = "DNS resolvers carry the tunnel packets. More resolvers are not automatically better; invalid, private, or overloaded resolvers can slow startup and waste traffic.",
                        ),
                    ),
                )
                GuideSection(
                    title = "PROXY MODE",
                    intro = "Proxy mode starts a local SOCKS proxy and optionally an HTTP bridge. Use it when another app can point at a local proxy address.",
                    entries = listOf(
                        GuideEntry(
                            label = "Listen IP",
                            detail = "Loopback keeps the proxy on this device. A LAN address exposes the proxy to other devices and must use a nonblank username and password.",
                        ),
                        GuideEntry(
                            label = "SOCKS port",
                            detail = "The local port companion apps use. If another app already owns the port, startup should fail cleanly and release all worker threads.",
                        ),
                        GuideEntry(
                            label = "HTTP bridge",
                            detail = "Allows HTTP-proxy clients to reach the SOCKS tunnel. Client count, header size, host length, and idle tunnels are bounded to protect the app.",
                        ),
                    ),
                )
                GuideSection(
                    title = "VPN MODE",
                    intro = "VPN mode routes selected Android app traffic through WhiteDNS. It is simpler for users, but it is more sensitive to Android background and network-change behavior.",
                    entries = listOf(
                        GuideEntry(
                            label = "Split tunnel",
                            detail = "Choose which apps use the tunnel. Editing inactive profiles is safe while connected; active runtime changes should require reconnect or an explicit apply flow.",
                        ),
                        GuideEntry(
                            label = "IPv6 strategy",
                            detail = "Block is safest against leaks. Bypass can improve compatibility but should be treated as a warning. Experimental route needs network-specific testing.",
                        ),
                        GuideEntry(
                            label = "MTU",
                            detail = "Lower values such as 1280 favor compatibility. Higher values may improve throughput on clean networks but can break paths that fragment or filter packets.",
                        ),
                    ),
                )
                GuideSection(
                    title = "TRAFFIC AND BATTERY",
                    intro = "Warmup and keepalive traffic can make startup smoother, but they also create visible upload and battery cost.",
                    entries = listOf(
                        GuideEntry(
                            label = "Off",
                            detail = "No deliberate warmup traffic. Best for users who need the lowest possible background traffic.",
                        ),
                        GuideEntry(
                            label = "Startup-only",
                            detail = "A low-cost default. It helps the tunnel start, then stops instead of sending ongoing keepalive traffic.",
                        ),
                        GuideEntry(
                            label = "Balanced",
                            detail = "Keeps the tunnel warm when needed, but should skip keepalive if real traffic was seen recently.",
                        ),
                        GuideEntry(
                            label = "Aggressive",
                            detail = "Can help difficult networks, but costs more upload, battery, and resolver load. It should be used only when the user accepts that tradeoff.",
                        ),
                    ),
                )
                GuideSection(
                    title = "TUNNEL SHAPE",
                    intro = "These settings change how packets are split, repeated, compressed, or encoded before they travel through DNS.",
                    entries = listOf(
                        GuideEntry(
                            label = "Min and max upload/download",
                            detail = "These ranges control packet sizing. Smaller values favor compatibility on filtered or fragile networks; larger values can improve throughput when the path is clean.",
                        ),
                        GuideEntry(
                            label = "Upload/download duplication",
                            detail = "Duplication repeats packets to improve loss tolerance. It can help unstable resolvers but increases upload and resolver load.",
                        ),
                        GuideEntry(
                            label = "Upload/download compression",
                            detail = "Compression can reduce transferred bytes for compressible traffic, but it adds CPU cost and may not help already-compressed app data.",
                        ),
                        GuideEntry(
                            label = "DNS fragments",
                            detail = "Fragmenting can avoid size limits, but too many fragments increase request count and can make startup slower.",
                        ),
                        GuideEntry(
                            label = "Base encode data",
                            detail = "Encoding changes how payload bytes fit DNS-safe text. It can improve compatibility at the cost of extra overhead.",
                        ),
                    ),
                )
                GuideSection(
                    title = "RELIABILITY CONTROLS",
                    intro = "Retries, timeouts, workers, and queues decide how patient and parallel the client should be.",
                    entries = listOf(
                        GuideEntry(
                            label = "Resolver parallel and retries",
                            detail = "More parallelism can find working resolvers faster, but high values can create upload spikes. Retries help packet loss but delay failure detection.",
                        ),
                        GuideEntry(
                            label = "Resolver timeout and packet timeout",
                            detail = "Short timeouts fail faster on broken paths. Longer timeouts can help slow networks but make the app feel stuck.",
                        ),
                        GuideEntry(
                            label = "Process workers and RX/TX workers",
                            detail = "Workers control local concurrency. More workers may help high-throughput devices, but they can cost CPU, battery, and memory.",
                        ),
                        GuideEntry(
                            label = "RX/TX channels and stream queue",
                            detail = "Queues absorb bursts. Larger queues can smooth traffic but may increase memory use and delay failure visibility.",
                        ),
                        GuideEntry(
                            label = "Retry base, linear, step, and max",
                            detail = "These shape reconnect and retry pacing. Aggressive retries recover faster but can create repeated traffic on failing networks.",
                        ),
                    ),
                )
                GuideSection(
                    title = "RUNTIME HOUSEKEEPING",
                    intro = "These controls affect how WhiteDNS observes a running session and how much history it keeps.",
                    entries = listOf(
                        GuideEntry(
                            label = "Ping watchdog",
                            detail = "Detects sessions that look connected but stop responding. A low value reacts faster; a high value avoids false alarms on slow networks.",
                        ),
                        GuideEntry(
                            label = "Idle poll and busy retry",
                            detail = "These tune how often runtime loops wake up. Lower values can feel more responsive but may use more battery.",
                        ),
                        GuideEntry(
                            label = "Logs parallel, retries, and timeout",
                            detail = "These affect log collection and runtime observation, not secret handling. Logs still need redaction before sharing.",
                        ),
                        GuideEntry(
                            label = "Cancelled, terminal, and orphan retain",
                            detail = "Retain limits decide how much completed runtime state stays available for diagnostics. Higher values help debugging but should remain bounded.",
                        ),
                    ),
                )
                GuideSection(
                    title = "STATUS AND METRICS",
                    intro = "A useful connection state is more than a running process. The app should show which layer is healthy.",
                    entries = listOf(
                        GuideEntry(
                            label = "Startup progress",
                            detail = "Shows where startup is waiting. It should time out with a clear failure instead of remaining stuck.",
                        ),
                        GuideEntry(
                            label = "Resolver state",
                            detail = "Shows whether resolver activity is healthy. Bad resolver lists can look like server failure unless this layer is separated.",
                        ),
                        GuideEntry(
                            label = "Route check",
                            detail = "Confirms user traffic can actually leave through the local proxy or VPN route. If this fails, the app should not report ready.",
                        ),
                        GuideEntry(
                            label = "Upload/download",
                            detail = "Upload can include DNS tunnel requests, warmup, keepalive, and real user traffic. High upload with no download usually needs resolver or server-health checks.",
                        ),
                    ),
                )
                GuideSection(
                    title = "IMPORT, EXPORT, AND DIAGNOSTICS",
                    intro = "Profile sharing and support diagnostics have opposite safety goals: profile exports must stay usable, while diagnostics must be redacted by default.",
                    entries = listOf(
                        GuideEntry(
                            label = "Profile export",
                            detail = "Contains connection secrets and must remain the real importable value. Show a clear trust warning before copy, share, or QR display.",
                        ),
                        GuideEntry(
                            label = "Diagnostics",
                            detail = "Safe support output should redact keys, passwords, profile links, TOML secrets, runtime paths, generated files, and logs.",
                        ),
                        GuideEntry(
                            label = "Clipboard",
                            detail = "Copying sensitive values should require explicit intent and mark clipboard content sensitive on Android versions that support it.",
                        ),
                    ),
                )
                GuideSection(
                    title = "TROUBLESHOOTING PATH",
                    intro = "When the tunnel does not work, check one layer at a time instead of changing every setting together.",
                    entries = listOf(
                        GuideEntry(
                            label = "1. Server",
                            detail = "Confirm the domain, key, and server capacity. An expired or unreachable server can still produce confusing startup activity.",
                        ),
                        GuideEntry(
                            label = "2. Resolvers",
                            detail = "Use a smaller clean resolver list, remove duplicates, and avoid private/local addresses unless intentionally testing a local network.",
                        ),
                        GuideEntry(
                            label = "3. Local route",
                            detail = "In proxy mode, verify the companion app uses the displayed address and port. In VPN mode, verify the target app is included by split tunnel.",
                        ),
                        GuideEntry(
                            label = "4. Network",
                            detail = "Try Wi-Fi and mobile data separately. Operator filtering, IPv6, MTU, and battery restrictions can change the result.",
                        ),
                    ),
                )
                GuideSection(
                    title = "QA CHECKLIST",
                    intro = "Use this mini-checklist before sharing a build or profile with another user.",
                    entries = listOf(
                        GuideEntry(
                            label = "Secrets",
                            detail = "Diagnostics are redacted, backups exclude sensitive files, and profile exports show a clear warning.",
                        ),
                        GuideEntry(
                            label = "Connection",
                            detail = "Proxy and VPN both start, stop, reconnect, and reject stale runtime events.",
                        ),
                        GuideEntry(
                            label = "Usability",
                            detail = "Text remains visible after navigation, large font sizes do not overlap, and long profile names wrap cleanly.",
                        ),
                    ),
                )
                FooterLink()
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: WhiteDnsTab,
    onTabSelected: (WhiteDnsTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteDnsPalette.SurfaceAlt)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, WhiteDnsPalette.Border)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WhiteDnsTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val background by animateColorAsState(
                    targetValue = if (selected) WhiteDnsPalette.AccentSurface else Color.Transparent,
                    animationSpec = tween(180),
                    label = "bottomNavBackground",
                )
                val color by animateColorAsState(
                    targetValue = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Disabled,
                    animationSpec = tween(180),
                    label = "bottomNavColor",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(background)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 9.sp,
                            color = color,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTabSwitch(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.ControlBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ProfileTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) {
                            WhiteDnsPalette.Accent
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 8.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 9.sp,
                        color = if (selected) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FooterLink() {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Powered by WhiteDNS",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Description,
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = WhiteDnsTelegramUrl,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { openWhiteDnsTelegram(context) }
                .padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 9.sp,
                color = WhiteDnsPalette.AccentText,
            ),
        )
    }
}

@Composable
private fun ConnectionModeSegmentedControl(
    selectedMode: String,
    enabled: Boolean,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FieldLabel("Mode")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) WhiteDnsPalette.Surface else WhiteDnsPalette.SurfaceAlt)
                .border(1.5.dp, WhiteDnsPalette.ControlBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            WhiteDnsOptions.connectionModes.forEach { mode ->
                val selected = selectedMode == mode.value
                val background by animateColorAsState(
                    targetValue = if (selected) {
                        WhiteDnsPalette.Accent
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(180),
                    label = "connectionModeSegmentBackground",
                )
                val textColor by animateColorAsState(
                    targetValue = when {
                        !enabled -> WhiteDnsPalette.Disabled
                        selected -> WhiteDnsPalette.OnAccent
                        else -> WhiteDnsPalette.Muted
                    },
                    animationSpec = tween(180),
                    label = "connectionModeSegmentText",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(background)
                        .clickable(enabled = enabled && !selected) {
                            onModeChange(mode.value)
                        }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mode.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 10.sp,
                            color = textColor,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.4.sp,
                        ),
                    )
                }
            }
        }
    }
}

private enum class HomeSelectorType {
    CONNECTION,
    RESOLVER,
    ADVANCED,
}

private data class HomeSelectorItem(
    val id: String,
    val title: String,
    val detail: String,
)

@Composable
private fun HomeSelectorCard(
    label: String,
    value: String,
    detail: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        !enabled -> WhiteDnsPalette.Divider
        selected -> WhiteDnsPalette.Accent.copy(alpha = 0.28f)
        else -> WhiteDnsPalette.Border
    }
    val textColor = if (enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled
    val detailColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        selected -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Column(modifier = modifier) {
        FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) WhiteDnsPalette.Surface else WhiteDnsPalette.SurfaceAlt)
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 11.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = detailColor,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) WhiteDnsPalette.Muted else WhiteDnsPalette.Disabled,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AdvancedProfileControls(
    selectedProfile: AdvancedSettingsProfile,
    dirty: Boolean,
    enabled: Boolean,
    onSelectClick: () -> Unit,
) {
    HomeSelectorCard(
        label = "Setting",
        value = selectedProfile.name,
        detail = if (dirty) "Unsaved changes" else advancedProfileSummary(selectedProfile),
        selected = !dirty,
        enabled = enabled,
        onClick = onSelectClick,
    )
}

@Composable
private fun AdvancedProfileActionRow(
    selectedProfile: AdvancedSettingsProfile,
    dirty: Boolean,
    enabled: Boolean,
    onSaveClick: () -> Unit,
    onSaveAsClick: () -> Unit,
) {
    val canSave = enabled && selectedProfile.id != AdvancedSettingsProfile.DefaultId && dirty
    val canSaveAs = enabled && (dirty || selectedProfile.id != AdvancedSettingsProfile.DefaultId)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResolverActionButton(
            modifier = Modifier.weight(1f),
            label = "SAVE",
            emphasized = canSave,
            enabled = canSave,
            onClick = onSaveClick,
        )
        ResolverActionButton(
            modifier = Modifier.weight(1f),
            label = "SAVE AS",
            emphasized = dirty && selectedProfile.id == AdvancedSettingsProfile.DefaultId,
            enabled = canSaveAs,
            onClick = onSaveAsClick,
        )
    }
}

@Composable
private fun AdvancedProfileSaveAsDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val canSave = name.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = "SAVE ADVANCED PROFILE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Fast tunnel",
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CANCEL",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "SAVE AS",
                    emphasized = true,
                    enabled = canSave,
                    onClick = {
                        onSave(name.trim())
                    },
                )
            }
        }
    }
}

@Composable
private fun AdvancedSettingsEditorSheet(
    visible: Boolean,
    settings: WhiteDnsSettings,
    selectedProfile: AdvancedSettingsProfile,
    dirty: Boolean,
    showProxySettings: Boolean,
    canEdit: Boolean,
    onBack: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    onSaveClick: () -> Unit,
    onSaveAsClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            initialOffsetX = { -it },
        ) + fadeIn(animationSpec = tween(180)),
        exit = slideOutHorizontally(
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            targetOffsetX = { it },
        ) + fadeOut(animationSpec = tween(160)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .whiteDnsPageBackground()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to advanced profiles",
                        emphasized = false,
                        enabled = true,
                        onClick = onBack,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedProfile.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = WhiteDnsPalette.Ink,
                            ),
                        )
                        Text(
                            text = if (dirty) "Unsaved changes" else advancedProfileSummary(selectedProfile),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 10.sp,
                                color = if (dirty) WhiteDnsPalette.WarningText else WhiteDnsPalette.Muted,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                AdvancedProfileActionRow(
                    selectedProfile = selectedProfile,
                    dirty = dirty,
                    enabled = canEdit,
                    onSaveClick = onSaveClick,
                    onSaveAsClick = onSaveAsClick,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    InfoCard(title = "EDIT ADVANCED SETTINGS", compact = true) {
                        AdvancedSettingsFields(
                            settings = settings,
                            showProxySettings = showProxySettings,
                            onSettingsChange = onSettingsChange,
                        )
                        SectionDivider()
                        ResolverActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = "RESET ADVANCED SETTINGS",
                            emphasized = true,
                            enabled = canEdit,
                            onClick = onResetClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsFields(
    settings: WhiteDnsSettings,
    showProxySettings: Boolean,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val context = LocalContext.current
    GroupLabel("MTU")
    MtuSettingsGroup(
        settings = settings,
        onSettingsChange = onSettingsChange,
    )

    SectionDivider()
    GroupLabel("Runtime Workers, Queues, and Timers")
    RuntimeWorkersSettingsGroup(
        settings = settings,
        onSettingsChange = onSettingsChange,
    )

    SectionDivider()
    if (showProxySettings) {
        val lanReachableProxy = WhiteDnsProxyExposurePolicy.isLanReachableListenIp(settings.listenIp)
        val lanProxyProtected = WhiteDnsProxyExposurePolicy.hasCompleteSocksCredentials(
            enabled = settings.socks5Authentication,
            username = settings.socksUsername,
            password = settings.socksPassword,
        )
        GroupLabel("Local Proxy")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = "Listen IP",
                value = settings.listenIp,
                onValueChange = { onSettingsChange(settings.copy(listenIp = it)) },
                placeholder = "127.0.0.1",
            )
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = "Listen Port",
                value = settings.listenPort,
                onValueChange = { onSettingsChange(settings.copy(listenPort = it.filter(Char::isDigit))) },
                placeholder = "10886",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None,
                ),
            )
        }

        ToggleRow(
            label = "HTTP Proxy",
            enabled = settings.httpProxyEnabled,
            onToggle = {
                onSettingsChange(settings.copy(httpProxyEnabled = !settings.httpProxyEnabled))
            },
        )
        AnimatedVisibility(
            visible = settings.httpProxyEnabled,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            WhiteDnsTextField(
                label = "HTTP Port",
                value = settings.httpProxyPort,
                onValueChange = { onSettingsChange(settings.copy(httpProxyPort = it.filter(Char::isDigit))) },
                placeholder = "10887",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None,
                ),
            )
        }

        AnimatedVisibility(
            visible = lanReachableProxy,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WhiteDnsPalette.WarningSurface)
                    .border(1.dp, WhiteDnsPalette.WarningText.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = WhiteDnsPalette.WarningText,
                    )
                    Text(
                        text = if (lanProxyProtected) {
                            "LAN-reachable proxy is protected by SOCKS5 authentication."
                        } else {
                            "LAN-reachable proxy requires a SOCKS5 username and password before connecting."
                        },
                        color = WhiteDnsPalette.WarningText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "ENABLE AUTH",
                        emphasized = !lanProxyProtected,
                        enabled = true,
                        onClick = {
                            onSettingsChange(
                                settings.copy(
                                    socks5Authentication = true,
                                    socksPassword = settings.socksPassword.ifBlank { generateSocksPassword() },
                                ),
                            )
                        },
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "COPY ADDRESS",
                        emphasized = false,
                        enabled = true,
                        onClick = {
                            copyTextToClipboard(
                                context = context,
                                label = "SOCKS5 proxy",
                                text = "${settings.listenIp.trim().ifBlank { "127.0.0.1" }}:${settings.listenPort}",
                                sensitive = false,
                            )
                        },
                    )
                }
            }
        }

        ToggleRow(
            label = "SOCKS5 Authentication",
            enabled = settings.socks5Authentication,
            onToggle = {
                onSettingsChange(settings.copy(socks5Authentication = !settings.socks5Authentication))
            },
        )

        AnimatedVisibility(
            visible = settings.socks5Authentication,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WhiteDnsTextField(
                        modifier = Modifier.weight(1f),
                        label = "Username",
                        value = settings.socksUsername,
                        onValueChange = { onSettingsChange(settings.copy(socksUsername = it)) },
                        placeholder = "master_dns_vpn",
                    )
                    WhiteDnsTextField(
                        modifier = Modifier.weight(1f),
                        label = "Password",
                        value = settings.socksPassword,
                        onValueChange = { onSettingsChange(settings.copy(socksPassword = it)) },
                        placeholder = "master_dns_vpn",
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "REGENERATE PASSWORD",
                        emphasized = false,
                        enabled = true,
                        onClick = {
                            onSettingsChange(settings.copy(socksPassword = generateSocksPassword()))
                        },
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "COPY PASSWORD",
                        emphasized = false,
                        enabled = settings.socksPassword.isNotBlank(),
                        onClick = {
                            copyTextToClipboard(
                                context = context,
                                label = "SOCKS5 password",
                                text = settings.socksPassword,
                                sensitive = true,
                            )
                        },
                    )
                }
            }
        }

        SectionDivider()
    }

    if (settings.connectionMode == "vpn") {
        GroupLabel("VPN Behavior")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteDnsDropdownField(
                modifier = Modifier.weight(1f),
                label = "IPv6 Strategy",
                value = settings.vpnIpv6Strategy,
                options = WhiteDnsOptions.vpnIpv6Strategies,
                onValueChange = { onSettingsChange(settings.copy(vpnIpv6Strategy = it)) },
            )
            WhiteDnsDropdownField(
                modifier = Modifier.weight(1f),
                label = "VPN MTU",
                value = settings.vpnMtuPreset,
                options = WhiteDnsOptions.vpnMtuPresets,
                onValueChange = { onSettingsChange(settings.copy(vpnMtuPreset = it)) },
            )
        }
        AnimatedVisibility(
            visible = settings.vpnMtuPreset == WhiteDnsOptions.VpnMtuPresetCustom,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            WhiteDnsTextField(
                label = "Custom VPN MTU",
                value = settings.vpnCustomMtu,
                onValueChange = { onSettingsChange(settings.copy(vpnCustomMtu = it.filter(Char::isDigit))) },
                placeholder = "1500",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None,
                ),
            )
        }
        VpnBehaviorNotice(settings.vpnIpv6Strategy)
        SectionDivider()
    }

    GroupLabel("Network Tuning")

    WhiteDnsDropdownField(
        label = "Balancing Strategy",
        value = settings.balancingStrategy,
        options = WhiteDnsOptions.balancingStrategies,
        onValueChange = { onSettingsChange(settings.copy(balancingStrategy = it)) },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Upload Dup",
            value = settings.uploadDuplication,
            onValueChange = {
                onSettingsChange(settings.copy(uploadDuplication = it.filter(Char::isDigit)))
            },
            placeholder = "3",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Download Dup",
            value = settings.downloadDuplication,
            onValueChange = {
                onSettingsChange(settings.copy(downloadDuplication = it.filter(Char::isDigit)))
            },
            placeholder = "7",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsDropdownField(
            modifier = Modifier.weight(1f),
            label = "Upload Compress",
            value = settings.uploadCompression,
            options = WhiteDnsOptions.compressionTypes,
            onValueChange = { onSettingsChange(settings.copy(uploadCompression = it)) },
        )
        WhiteDnsDropdownField(
            modifier = Modifier.weight(1f),
            label = "Download Compress",
            value = settings.downloadCompression,
            options = WhiteDnsOptions.compressionTypes,
            onValueChange = { onSettingsChange(settings.copy(downloadCompression = it)) },
        )
    }
    ToggleRow(
        label = "Base Encode Data",
        enabled = settings.baseEncodeData,
        onToggle = {
            onSettingsChange(settings.copy(baseEncodeData = !settings.baseEncodeData))
        },
    )

    SectionDivider()
    GroupLabel("Reliability")

    WhiteDnsTextField(
        label = "Ping Watchdog (s)",
        value = settings.pingWatchdogSeconds,
        onValueChange = {
            onSettingsChange(settings.copy(pingWatchdogSeconds = it.filter(Char::isDigit)))
        },
        placeholder = "300",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            capitalization = KeyboardCapitalization.None,
        ),
    )
    WhiteDnsDropdownField(
        label = "Traffic Warmup",
        value = settings.trafficWarmupMode,
        options = WhiteDnsOptions.trafficWarmupModes,
        onValueChange = {
            onSettingsChange(
                settings.copy(
                    trafficWarmupMode = it,
                    trafficWarmupEnabled = it != WhiteDnsOptions.TrafficWarmupOff,
                ),
            )
        },
    )
    AnimatedVisibility(
        visible = settings.trafficWarmupMode == WhiteDnsOptions.TrafficWarmupCustom,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = "Warmup Probes",
                value = settings.trafficWarmupProbeCount,
                onValueChange = {
                    onSettingsChange(settings.copy(trafficWarmupProbeCount = it.filter(Char::isDigit)))
                },
                placeholder = "4",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None,
                ),
            )
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = "Keepalive (s)",
                value = settings.trafficKeepaliveIntervalSeconds,
                onValueChange = {
                    onSettingsChange(
                        settings.copy(trafficKeepaliveIntervalSeconds = it.filter(Char::isDigit)),
                    )
                },
                placeholder = "5",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None,
                ),
            )
        }
    }
    WhiteDnsDropdownField(
        label = "Log Level",
        value = settings.logLevel,
        options = WhiteDnsOptions.logLevels,
        onValueChange = { onSettingsChange(settings.copy(logLevel = it)) },
    )
}

@Composable
private fun VpnBehaviorNotice(ipv6Strategy: String) {
    val strategyText = when (ipv6Strategy) {
        WhiteDnsOptions.VpnIpv6StrategyBypass ->
            "IPv6 traffic bypasses the tunnel. Use Android's Block connections without VPN setting when leak prevention matters."
        WhiteDnsOptions.VpnIpv6StrategyExperimentalRoute ->
            "Experimental IPv6 route is enabled. Some networks or apps may need fallback to Block IPv6."
        else ->
            "IPv6 is routed into WhiteDNS and blocked unless the VPN stack can forward it."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteDnsPalette.SurfaceAlt)
            .border(1.dp, WhiteDnsPalette.ControlBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = strategyText,
            color = WhiteDnsPalette.TextPrimary,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "For Always-on VPN, enable WhiteDNS in Android VPN settings and turn on Block connections without VPN.",
            color = WhiteDnsPalette.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun advancedProfileSummary(profile: AdvancedSettingsProfile): String {
    return "MTU ${profile.minUploadMtu}-${profile.maxUploadMtu}/${profile.minDownloadMtu}-${profile.maxDownloadMtu}, ${profile.logLevel}"
}

private fun advancedSaveAsInitialName(profile: AdvancedSettingsProfile): String {
    return if (profile.id == AdvancedSettingsProfile.DefaultId) {
        "Custom Advanced"
    } else {
        "${profile.name} Copy"
    }
}

private fun AdvancedSettingsProfile.displayName(dirty: Boolean): String {
    return if (dirty) {
        "$name (modified)"
    } else {
        name
    }
}

@Composable
private fun HomeSelectorSheet(
    visible: Boolean,
    title: String,
    searchPlaceholder: String,
    items: List<HomeSelectorItem>,
    selectedId: String?,
    emptyMessage: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onEdit: ((String) -> Unit)? = null,
) {
    var query by rememberSaveable(title) { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val filteredItems = remember(items, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            items
        } else {
            val lowerQuery = normalizedQuery.lowercase(Locale.getDefault())
            items.filter { item ->
                item.title.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    item.detail.lowercase(Locale.getDefault()).contains(lowerQuery)
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            initialOffsetX = { -it },
        ) + fadeIn(animationSpec = tween(180)),
        exit = slideOutHorizontally(
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            targetOffsetX = { it },
        ) + fadeOut(animationSpec = tween(160)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .whiteDnsPageBackground()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close selector",
                        emphasized = false,
                        enabled = true,
                        onClick = onDismiss,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = WhiteDnsPalette.Ink,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                WhiteDnsTextField(
                    label = "Search",
                    value = query,
                    onValueChange = { query = it },
                    placeholder = searchPlaceholder,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (filteredItems.isEmpty()) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                    } else {
                        filteredItems.forEach { item ->
                            HomeSelectorSheetRow(
                                item = item,
                                selected = item.id == selectedId,
                                onClick = { onSelect(item.id) },
                                onEdit = onEdit?.let { edit -> { edit(item.id) } },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSelectorSheetRow(
    item: HomeSelectorItem,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface)
            .border(
                1.5.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        onEdit?.let { edit ->
            ProfileIconButton(
                icon = Icons.Rounded.Edit,
                contentDescription = "Edit ${item.title}",
                emphasized = false,
                enabled = true,
                onClick = edit,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ConnectionSetupCard(
    selectedConnectionProfile: ConnectionProfile,
    selectedResolverProfile: ResolverProfile?,
    resolverCount: Int,
    resolverIssue: String?,
    actionsEnabled: Boolean,
    onAddConnectionClick: () -> Unit,
    onAddResolverProfileClick: () -> Unit,
) {
    val serverRoute = serverRouteSummary(selectedConnectionProfile.customServerDomain)
    val connectionIssue = when {
        selectedConnectionProfile.customServerDomain.isBlank() &&
            selectedConnectionProfile.customServerEncryptionKey.isBlank() -> "Server route and key missing"
        selectedConnectionProfile.customServerDomain.isBlank() -> "Server route missing"
        selectedConnectionProfile.customServerEncryptionKey.isBlank() -> "Encryption key missing"
        else -> null
    }
    val resolverSource = selectedResolverProfile?.name ?: "Manual resolvers"
    val resolverDetail = resolverIssue ?: resolverCountLabel(resolverCount)

    InfoCard(title = "SETUP", compact = true) {
        SetupInfoRow(
            icon = if (connectionIssue == null) Icons.Rounded.Link else Icons.Rounded.WarningAmber,
            label = "Connection",
            value = selectedConnectionProfile.name.ifBlank { "Connection" },
            detail = connectionIssue ?: serverRoute,
            color = if (connectionIssue == null) WhiteDnsPalette.AccentText else WhiteDnsPalette.WarningText,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(WhiteDnsPalette.Divider),
        )
        SetupInfoRow(
            icon = if (resolverIssue == null) Icons.Rounded.Check else Icons.Rounded.WarningAmber,
            label = "Resolvers",
            value = resolverSource,
            detail = resolverDetail,
            color = if (resolverIssue == null) WhiteDnsPalette.Success else WhiteDnsPalette.WarningText,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SetupActionButton(
            label = "Add Connection",
            supportingText = "Server domain and key",
            icon = Icons.Rounded.Add,
            emphasized = true,
            enabled = actionsEnabled,
            onClick = onAddConnectionClick,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SetupActionButton(
            label = "Add Resolver Profile",
            supportingText = "DNS resolver list",
            icon = Icons.Rounded.Add,
            emphasized = false,
            enabled = actionsEnabled,
            onClick = onAddResolverProfileClick,
        )
    }
}

@Composable
private fun StarterWalkthroughCard() {
    InfoCard(title = "STARTER WALKTHROUGH", compact = true) {
        GuideStepRow(
            number = "1",
            title = "Add a connection profile",
            detail = "Enter the server domain or domains and the encryption key from a source you trust.",
        )
        GuideStepDivider()
        GuideStepRow(
            number = "2",
            title = "Add a resolver profile",
            detail = "Start with a clean, smaller resolver list. Large duplicate-heavy lists can slow startup and increase upload.",
        )
        GuideStepDivider()
        GuideStepRow(
            number = "3",
            title = "Choose proxy or VPN mode",
            detail = "Use proxy mode for companion apps that can use a local proxy. Use VPN mode when Android apps should route directly through WhiteDNS.",
        )
        GuideStepDivider()
        GuideStepRow(
            number = "4",
            title = "Connect and read the status",
            detail = "A healthy session needs server startup, resolver activity, and a passing local route check before user traffic is truly ready.",
        )
    }
}

@Composable
private fun GuideSection(
    title: String,
    intro: String,
    entries: List<GuideEntry>,
) {
    InfoCard(title = title, compact = true) {
        Text(
            text = intro,
            style = MaterialTheme.typography.bodySmall.copy(
                color = WhiteDnsPalette.TextSecondary,
                lineHeight = 17.sp,
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        entries.forEachIndexed { index, entry ->
            GuideInfoRow(entry = entry)
            if (index != entries.lastIndex) {
                GuideStepDivider()
            }
        }
    }
}

@Composable
private fun GuideInfoRow(entry: GuideEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WhiteDnsPalette.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            ),
        )
        Text(
            text = entry.detail,
            style = MaterialTheme.typography.bodySmall.copy(
                color = WhiteDnsPalette.TextSecondary,
                lineHeight = 17.sp,
            ),
        )
    }
}

@Composable
private fun GuideStepRow(
    number: String,
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(WhiteDnsPalette.AccentSurface)
                .border(1.5.dp, WhiteDnsPalette.Accent.copy(alpha = 0.24f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WhiteDnsPalette.AccentText,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WhiteDnsPalette.TextSecondary,
                    lineHeight = 17.sp,
                ),
            )
        }
    }
}

@Composable
private fun GuideStepDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WhiteDnsPalette.Divider),
    )
}

@Composable
private fun RouteTroubleshootingCard(
    connectionMode: String,
    modifier: Modifier = Modifier,
) {
    val routeHint = if (connectionMode == "vpn") {
        "Check VPN permission, split tunnel selection, IPv6 strategy, MTU, and Android battery restrictions."
    } else {
        "Check that the companion app uses the displayed local proxy address and the correct SOCKS or HTTP port."
    }

    Column(modifier = modifier.fillMaxWidth()) {
        InfoCard(title = "WHAT TO CHECK NEXT", compact = true) {
            GuideInfoRow(
                GuideEntry(
                    label = "Server",
                    detail = "Confirm the domain, encryption key, server capacity, and whether the profile is still valid.",
                ),
            )
            GuideStepDivider()
            GuideInfoRow(
                GuideEntry(
                    label = "Resolvers",
                    detail = "Try a smaller clean resolver list. Remove duplicates, invalid rows, and private or loopback addresses.",
                ),
            )
            GuideStepDivider()
            GuideInfoRow(
                GuideEntry(
                    label = "Local route",
                    detail = routeHint,
                ),
            )
            GuideStepDivider()
            GuideInfoRow(
                GuideEntry(
                    label = "Network",
                    detail = "Test Wi-Fi and mobile data separately. If one network fails, try safer MTU and IPv6 settings before changing every advanced value.",
                ),
            )
        }
    }
}

@Composable
private fun SetupInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    detail: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(color.copy(alpha = 0.14f))
                .border(1.5.dp, color.copy(alpha = 0.22f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 9.sp,
                    color = WhiteDnsPalette.Muted,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
private fun SetupActionButton(
    label: String,
    supportingText: String,
    icon: ImageVector,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        emphasized -> WhiteDnsPalette.Accent
        else -> WhiteDnsPalette.SurfaceAlt
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        emphasized -> WhiteDnsPalette.AccentPressed
        else -> WhiteDnsPalette.Border
    }
    val foreground = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.OnAccent
        else -> WhiteDnsPalette.Ink
    }
    val secondary = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.OnAccent.copy(alpha = 0.78f)
        else -> WhiteDnsPalette.Muted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(11.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = foreground,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = supportingText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = secondary,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

private fun resolverCountLabel(count: Int): String {
    return "$count resolver${if (count == 1) "" else "s"} configured"
}

@Composable
private fun ConnectionProfilesSettings(
    settings: WhiteDnsSettings,
    activeConnectionProfileId: String?,
    connectionStatus: ConnectionStatus,
    openCreateRequestId: Int,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val profiles = settings.normalizedConnectionProfiles()
    val selectedProfile = settings.selectedConnectionProfile()
    val customProfiles = profiles.filter { it.serverMode == "custom" }
    val context = LocalContext.current
    var dialogProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var showExportAllDialog by remember { mutableStateOf(false) }
    var draggedProfileId by remember { mutableStateOf<String?>(null) }
    var dragStartIndex by remember { mutableStateOf(0) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var measuredItemHeightPx by remember { mutableStateOf(0) }
    val canManageProfiles = connectionStatus == ConnectionStatus.DISCONNECTED
    val draggedIndex = draggedProfileId?.let { profileId ->
        customProfiles.indexOfFirst { it.id == profileId }.takeIf { it >= 0 }
    }
    val dragTargetIndex = draggedIndex?.let {
        val indexOffset = dragOffsetToProfileIndexOffset(
            offsetY = dragOffsetY,
            itemHeightPx = measuredItemHeightPx.toFloat(),
        )
        (dragStartIndex + indexOffset).coerceIn(0, customProfiles.lastIndex)
    }

    fun clearDragState() {
        draggedProfileId = null
        dragStartIndex = 0
        dragOffsetY = 0f
    }

    fun finishDrag(commit: Boolean) {
        val profileId = draggedProfileId
        val targetIndex = if (profileId != null && customProfiles.isNotEmpty()) {
            val indexOffset = dragOffsetToProfileIndexOffset(
                offsetY = dragOffsetY,
                itemHeightPx = measuredItemHeightPx.toFloat(),
            )
            (dragStartIndex + indexOffset).coerceIn(0, customProfiles.lastIndex)
        } else {
            null
        }
        clearDragState()
        if (commit && profileId != null && targetIndex != null && canManageProfiles) {
            onSettingsChange(settings.moveConnectionProfileToIndex(profileId, targetIndex))
        }
    }

    LaunchedEffect(openCreateRequestId) {
        if (openCreateRequestId > 0 && canManageProfiles) {
            showCreateDialog = true
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResolverActionButton(
            modifier = Modifier.weight(1f),
            label = "CREATE",
            emphasized = true,
            enabled = canManageProfiles,
            onClick = {
                showCreateDialog = true
            },
        )
        ResolverActionButton(
            modifier = Modifier.weight(1f),
            label = "IMPORT",
            emphasized = false,
            enabled = canManageProfiles,
            onClick = {
                showImportDialog = true
            },
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    ResolverActionButton(
        modifier = Modifier.fillMaxWidth(),
        label = "EXPORT ALL",
        emphasized = false,
        enabled = customProfiles.any { it.customServerDomain.isNotBlank() && it.customServerEncryptionKey.isNotBlank() },
        onClick = {
            showExportAllDialog = true
        },
    )

    SectionDivider()
    GroupLabel("Custom Connections")
    if (customProfiles.isEmpty()) {
        Text(
            text = "No custom StormDNS connections yet.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
    customProfiles.forEachIndexed { index, profile ->
        val isActive = profile.id == activeConnectionProfileId &&
            connectionStatus != ConnectionStatus.DISCONNECTED
        val canEdit = canManageProfiles || !isActive
        val canDelete = !isActive && connectionStatus != ConnectionStatus.DISCONNECTING
        val isDragging = profile.id == draggedProfileId
        val targetTranslationY = profileDragTranslationY(
            itemIndex = index,
            draggedIndex = draggedIndex,
            targetIndex = dragTargetIndex,
            itemHeightPx = measuredItemHeightPx.toFloat(),
        )
        val animatedTranslationY by animateFloatAsState(
            targetValue = if (isDragging) 0f else targetTranslationY,
            animationSpec = spring(),
            label = "connectionProfileDragTranslation",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    translationY = if (isDragging) dragOffsetY else animatedTranslationY
                    shadowElevation = if (isDragging) 8f else 0f
                    alpha = if (isDragging) 0.96f else 1f
                }
                .onGloballyPositioned { coordinates ->
                    measuredItemHeightPx = coordinates.size.height.takeIf { it > 0 } ?: measuredItemHeightPx
                },
        ) {
            ConnectionProfileRow(
                profile = profile,
                selected = profile.id == selectedProfile.id,
                active = isActive,
                canEdit = canEdit,
                canDelete = canDelete,
                canDrag = canManageProfiles && customProfiles.size > 1,
                dragging = isDragging,
                onDragStart = {
                    if (canManageProfiles && customProfiles.size > 1) {
                        draggedProfileId = profile.id
                        dragStartIndex = index
                        dragOffsetY = 0f
                    }
                },
                onDrag = { deltaY ->
                    if (draggedProfileId == profile.id) {
                        dragOffsetY += deltaY
                    }
                },
                onDragEnd = {
                    finishDrag(commit = true)
                },
                onDragCancel = {
                    finishDrag(commit = false)
                },
                onExport = {
                    exportProfile = profile
                },
                onEdit = {
                    dialogProfile = profile
                },
                onDelete = {
                    if (canDelete) {
                        onSettingsChange(settings.deleteConnectionProfile(profile.id))
                    }
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showCreateDialog) {
        ConnectionProfileDialog(
            profile = null,
            onDismiss = { showCreateDialog = false },
            onSave = { profile ->
                val profileId = "profile-${System.currentTimeMillis()}"
                val nextProfile = profile.copy(id = profileId, serverMode = "custom")
                onSettingsChange(
                    settings
                        .upsertConnectionProfile(nextProfile)
                        .selectConnectionProfile(profileId),
                )
                showCreateDialog = false
            },
        )
    }

    if (showImportDialog) {
        ConnectionProfileImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { links ->
                runCatching {
                    settings.importStormDnsProfileLinks(links)
                }.onSuccess { importedSettings ->
                    onSettingsChange(importedSettings)
                    showImportDialog = false
                }
            },
        )
    }

    exportProfile?.let { profile ->
        ConnectionProfileExportDialog(
            title = "EXPORT CONNECTION",
            fieldLabel = "Profile Link",
            linkResult = remember(settings, profile) {
                runCatching { settings.exportStormDnsProfileLink(profile) }
            },
            onDismiss = { exportProfile = null },
            onShare = { link ->
                shareProfileLink(context, link)
            },
        )
    }

    if (showExportAllDialog) {
        ConnectionProfileExportDialog(
            title = "EXPORT ALL CONNECTIONS",
            fieldLabel = "Profile Links",
            linkResult = remember(settings, showExportAllDialog) {
                runCatching { settings.exportAllStormDnsProfileLinks() }
            },
            onDismiss = { showExportAllDialog = false },
            onShare = { links ->
                shareProfileLink(context, links)
            },
        )
    }

    dialogProfile?.let { profile ->
        ConnectionProfileDialog(
            profile = profile,
            onDismiss = { dialogProfile = null },
            onSave = { updatedProfile ->
                val nextProfile = updatedProfile.copy(id = profile.id, serverMode = "custom")
                onSettingsChange(
                    settings
                        .upsertConnectionProfile(nextProfile)
                        .selectConnectionProfile(profile.id),
                )
                dialogProfile = null
            },
        )
    }
}

@Composable
private fun ResolverProfilesSettings(
    settings: WhiteDnsSettings,
    connectionStatus: ConnectionStatus,
    openCreateRequestId: Int,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val profiles = settings.normalizedResolverProfiles()
    val selectedProfile = settings.selectedResolverProfile()
    var dialogProfile by remember { mutableStateOf<ResolverProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val canChangeProfiles = connectionStatus == ConnectionStatus.DISCONNECTED
    var draggedProfileId by remember { mutableStateOf<String?>(null) }
    var dragStartIndex by remember { mutableStateOf(0) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var measuredItemHeightPx by remember { mutableStateOf(0) }
    val draggedIndex = draggedProfileId?.let { profileId ->
        profiles.indexOfFirst { it.id == profileId }.takeIf { it >= 0 }
    }
    val dragTargetIndex = draggedIndex?.let {
        val indexOffset = dragOffsetToProfileIndexOffset(
            offsetY = dragOffsetY,
            itemHeightPx = measuredItemHeightPx.toFloat(),
        )
        (dragStartIndex + indexOffset).coerceIn(0, profiles.lastIndex)
    }

    fun clearDragState() {
        draggedProfileId = null
        dragStartIndex = 0
        dragOffsetY = 0f
    }

    fun finishDrag(commit: Boolean) {
        val profileId = draggedProfileId
        val targetIndex = if (profileId != null && profiles.isNotEmpty()) {
            val indexOffset = dragOffsetToProfileIndexOffset(
                offsetY = dragOffsetY,
                itemHeightPx = measuredItemHeightPx.toFloat(),
            )
            (dragStartIndex + indexOffset).coerceIn(0, profiles.lastIndex)
        } else {
            null
        }
        clearDragState()
        if (commit && profileId != null && targetIndex != null && canChangeProfiles) {
            onSettingsChange(settings.moveResolverProfileToIndex(profileId, targetIndex))
        }
    }

    LaunchedEffect(openCreateRequestId) {
        if (openCreateRequestId > 0 && canChangeProfiles) {
            showCreateDialog = true
        }
    }

    ResolverActionButton(
        modifier = Modifier.fillMaxWidth(),
        label = "CREATE RESOLVER PROFILE",
        emphasized = true,
        enabled = canChangeProfiles,
        onClick = { showCreateDialog = true },
    )

    if (settings.resolverText.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        ResolverActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "SAVE CURRENT RESOLVERS",
            enabled = canChangeProfiles,
            onClick = {
                showCreateDialog = true
            },
        )
    }

    SectionDivider()
    if (profiles.isEmpty()) {
        Text(
            text = "No saved resolver lists yet.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
    profiles.forEachIndexed { index, profile ->
        val isSelected = profile.id == selectedProfile?.id
        val canEditProfile = canChangeProfiles || !isSelected
        val canDeleteProfile = canEditProfile && connectionStatus != ConnectionStatus.DISCONNECTING
        val isDragging = profile.id == draggedProfileId
        val targetTranslationY = profileDragTranslationY(
            itemIndex = index,
            draggedIndex = draggedIndex,
            targetIndex = dragTargetIndex,
            itemHeightPx = measuredItemHeightPx.toFloat(),
        )
        val animatedTranslationY by animateFloatAsState(
            targetValue = if (isDragging) 0f else targetTranslationY,
            animationSpec = spring(),
            label = "resolverProfileDragTranslation",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    translationY = if (isDragging) dragOffsetY else animatedTranslationY
                    shadowElevation = if (isDragging) 8f else 0f
                    alpha = if (isDragging) 0.96f else 1f
                }
                .onGloballyPositioned { coordinates ->
                    measuredItemHeightPx = coordinates.size.height.takeIf { it > 0 } ?: measuredItemHeightPx
                },
        ) {
            ResolverProfileRow(
                profile = profile,
                selected = isSelected,
                canEdit = canEditProfile,
                canDelete = canDeleteProfile,
                canDrag = canChangeProfiles && profiles.size > 1,
                dragging = isDragging,
                onUse = {
                    if (canChangeProfiles) {
                        onSettingsChange(settings.applyResolverProfileToSelectedConnection(profile.id))
                    }
                },
                onDragStart = {
                    if (canChangeProfiles && profiles.size > 1) {
                        draggedProfileId = profile.id
                        dragStartIndex = index
                        dragOffsetY = 0f
                    }
                },
                onDrag = { deltaY ->
                    if (draggedProfileId == profile.id) {
                        dragOffsetY += deltaY
                    }
                },
                onDragEnd = {
                    finishDrag(commit = true)
                },
                onDragCancel = {
                    finishDrag(commit = false)
                },
                onEdit = { dialogProfile = profile },
                onDelete = {
                    if (canDeleteProfile) {
                        onSettingsChange(settings.deleteResolverProfile(profile.id))
                    }
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showCreateDialog) {
        ResolverProfileDialog(
            profile = null,
            initialResolverText = settings.resolverText,
            onDismiss = { showCreateDialog = false },
            onSave = { profile ->
                onSettingsChange(settings.upsertResolverProfile(profile))
                showCreateDialog = false
            },
        )
    }

    dialogProfile?.let { profile ->
        ResolverProfileDialog(
            profile = profile,
            initialResolverText = profile.resolverText,
            onDismiss = { dialogProfile = null },
            onSave = { updatedProfile ->
                onSettingsChange(settings.upsertResolverProfile(updatedProfile.copy(id = profile.id)))
                dialogProfile = null
            },
        )
    }
}

@Composable
private fun ResolverProfileDialog(
    profile: ResolverProfile?,
    initialResolverText: String,
    onDismiss: () -> Unit,
    onSave: (ResolverProfile) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(profile?.id) { mutableStateOf(profile?.name.orEmpty()) }
    var resolverText by remember(profile?.id) { mutableStateOf(profile?.resolverText ?: initialResolverText) }
    var importError by remember(profile?.id) { mutableStateOf<String?>(null) }
    val resolverValidation = remember(resolverText) { validateResolverText(resolverText) }
    val validationMessage = resolverValidationMessage(
        name = name,
        resolverText = resolverText,
        invalidEntries = resolverValidation.invalidEntries,
        validResolverCount = resolverValidation.normalizedResolvers.size,
    )
    val validationMessageIsError = validationMessage != null && (!resolverValidation.isValid || name.isBlank())
    val importResolverFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        readResolverTextFromUri(context, uri)
            .onSuccess { importedResolverText ->
                resolverText = importedResolverText
                importError = null
            }
            .onFailure { error ->
                importError = error.message ?: "Unable to import resolver file"
            }
    }
    val canSave = name.trim().isNotEmpty() && resolverValidation.isValid

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = if (profile == null) "CREATE RESOLVER PROFILE" else "EDIT RESOLVER PROFILE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Home resolvers",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "IMPORT FILE",
                    emphasized = false,
                    enabled = true,
                    onClick = {
                        importResolverFileLauncher.launch(ResolverImportMimeTypes)
                    },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CLEAR",
                    emphasized = false,
                    enabled = resolverText.isNotBlank(),
                    onClick = {
                        resolverText = ""
                        importError = null
                    },
                )
            }
            importError?.let { message ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            WhiteDnsTextField(
                label = "Resolvers",
                value = resolverText,
                onValueChange = {
                    resolverText = it
                    importError = null
                },
                placeholder = "1.1.1.1, 8.8.8.8 or one per line",
                singleLine = false,
                minLines = 6,
                maxLines = 10,
            )
            validationMessage?.let { message ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = if (validationMessageIsError) WhiteDnsPalette.Error else WhiteDnsPalette.Muted,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CANCEL",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "SAVE",
                    emphasized = true,
                    enabled = canSave,
                    onClick = {
                        onSave(
                            ResolverProfile(
                                id = profile?.id.orEmpty(),
                                name = name.trim(),
                                resolverText = resolverValidation.normalizedText,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ResolverProfileRow(
    profile: ResolverProfile,
    selected: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    canDrag: Boolean,
    dragging: Boolean,
    onUse: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val resolverCount = profile.resolverText
        .let { validateResolverText(it).normalizedResolvers.size }
    val resolverSummary = "$resolverCount resolver${if (resolverCount == 1) "" else "s"}" +
        if (selected) " - SELECTED" else ""
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.SurfaceAlt)
            .border(
                1.5.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.18f) else WhiteDnsPalette.Border,
                RoundedCornerShape(11.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileDragHandle(
                enabled = canDrag,
                dragging = dragging,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Ink,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = resolverSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileIconButton(
                icon = Icons.Rounded.Check,
                contentDescription = "Use resolver profile",
                emphasized = selected,
                enabled = canEdit,
                onClick = onUse,
            )
            ProfileIconButton(
                icon = Icons.Rounded.Edit,
                contentDescription = "Edit resolver profile",
                emphasized = false,
                enabled = canEdit,
                onClick = onEdit,
            )
            ProfileIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = "Delete resolver profile",
                emphasized = false,
                enabled = canDelete,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ConnectionProfileImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Result<WhiteDnsSettings>,
) {
    var profileLinks by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    val canImport = profileLinks.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = "IMPORT CONNECTION",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Profile Links",
                value = profileLinks,
                onValueChange = {
                    profileLinks = it
                    importError = null
                },
                placeholder = "stormdns://...\nstormdns://...",
                singleLine = false,
                minLines = 5,
                maxLines = 9,
            )
            importError?.let { message ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CANCEL",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "IMPORT",
                    emphasized = true,
                    enabled = canImport,
                    onClick = {
                        onImport(profileLinks)
                            .onFailure { error ->
                                importError = error.message ?: "Unable to import profile"
                            }
                    },
                )
            }
        }
    }
}

@Composable
private fun ConnectionProfileExportDialog(
    title: String,
    fieldLabel: String,
    linkResult: Result<String>,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    placeholder: String = "stormdns://...",
    showQr: Boolean = true,
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            val link = linkResult.getOrNull()
            if (link != null) {
                if (showQr && !link.contains('\n')) {
                    ProfileQrPreview(link = link)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                WhiteDnsTextField(
                    label = fieldLabel,
                    value = link,
                    onValueChange = {},
                    placeholder = placeholder,
                    singleLine = false,
                    minLines = if (link.contains('\n')) 7 else 5,
                    maxLines = 12,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "CLOSE",
                        emphasized = false,
                        enabled = true,
                        onClick = onDismiss,
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "COPY",
                        emphasized = false,
                        enabled = true,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(link))
                        },
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "SHARE",
                        emphasized = true,
                        enabled = true,
                        onClick = {
                            onShare(link)
                        },
                    )
                }
            } else {
                Text(
                    text = linkResult.exceptionOrNull()?.message ?: "Unable to export profile",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
                Spacer(modifier = Modifier.height(14.dp))
                CompactActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = "CLOSE",
                    emphasized = true,
                    enabled = true,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun ProfileQrPreview(link: String) {
    val qrBitmap = remember(link) {
        runCatching { buildQrBitmap(link, QrBitmapSizePx) }.getOrNull()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Profile QR code",
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(12.dp))
                    .padding(10.dp),
            )
        } else {
            Text(
                text = "QR code unavailable for this profile link.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.Error,
                ),
            )
        }
    }
}

private fun buildQrBitmap(
    value: String,
    sizePx: Int,
): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 2,
    )
    val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val pixels = IntArray(matrix.width * matrix.height)
    for (y in 0 until matrix.height) {
        val rowOffset = y * matrix.width
        for (x in 0 until matrix.width) {
            pixels[rowOffset + x] = if (matrix[x, y]) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
    }
}

@Composable
private fun ConnectionProfileDialog(
    profile: ConnectionProfile?,
    onDismiss: () -> Unit,
    onSave: (ConnectionProfile) -> Unit,
) {
    var name by remember(profile?.id) { mutableStateOf(profile?.name.orEmpty()) }
    var domain by remember(profile?.id) { mutableStateOf(profile?.customServerDomain.orEmpty()) }
    var encryptionKey by remember(profile?.id) { mutableStateOf(profile?.customServerEncryptionKey.orEmpty()) }
    var encryptionMethod by remember(profile?.id) {
        mutableStateOf(profile?.customServerEncryptionMethod ?: 1)
    }
    val normalizedDomains = remember(domain) { normalizeServerDomains(domain) }
    val canSave = name.isNotBlank() && normalizedDomains.isNotEmpty() && encryptionKey.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = if (profile == null) "CREATE NEW CONNECTION" else "EDIT CONNECTION",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "My StormDNS",
            )
            WhiteDnsTextField(
                label = "Domain(s)",
                value = domain,
                onValueChange = { domain = it },
                placeholder = "v1.example.com, v2.example.com",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
            )
            WhiteDnsTextField(
                label = "Encryption Key",
                value = encryptionKey,
                onValueChange = { encryptionKey = it.trim() },
                placeholder = "32-character key",
            )
            WhiteDnsDropdownField(
                label = "Encryption Method",
                value = encryptionMethod,
                options = WhiteDnsOptions.encryptionMethods,
                onValueChange = { encryptionMethod = it },
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CANCEL",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "SAVE",
                    emphasized = true,
                    enabled = canSave,
                    onClick = {
                        onSave(
                            ConnectionProfile(
                                id = profile?.id.orEmpty(),
                                name = name.trim(),
                                serverMode = "custom",
                                customServerDomain = normalizeServerDomainText(domain),
                                customServerEncryptionKey = encryptionKey.trim(),
                                customServerEncryptionMethod = encryptionMethod,
                                resolverProfileId = profile?.resolverProfileId.orEmpty(),
                            ),
                        )
                    },
                )
            }
        }
    }
}

private fun dragOffsetToProfileIndexOffset(
    offsetY: Float,
    itemHeightPx: Float,
): Int {
    if (itemHeightPx <= 0f) {
        return 0
    }
    return when {
        offsetY > 0f -> ((offsetY + itemHeightPx / 2f) / itemHeightPx).toInt()
        offsetY < 0f -> ((offsetY - itemHeightPx / 2f) / itemHeightPx).toInt()
        else -> 0
    }
}

private fun profileDragTranslationY(
    itemIndex: Int,
    draggedIndex: Int?,
    targetIndex: Int?,
    itemHeightPx: Float,
): Float {
    if (draggedIndex == null || targetIndex == null || itemHeightPx <= 0f) {
        return 0f
    }
    return when {
        draggedIndex < targetIndex && itemIndex in (draggedIndex + 1)..targetIndex -> -itemHeightPx
        draggedIndex > targetIndex && itemIndex in targetIndex until draggedIndex -> itemHeightPx
        else -> 0f
    }
}

@Composable
private fun ConnectionProfileRow(
    profile: ConnectionProfile,
    selected: Boolean,
    active: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    canDrag: Boolean,
    dragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val domain = profile.customServerDomain.ifBlank { "Custom StormDNS" }
    val connectionSummary = when {
        active -> "$domain - ACTIVE"
        selected -> "$domain - SELECTED"
        else -> domain
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(
                if (selected) {
                    WhiteDnsPalette.AccentSurface
                } else {
                    WhiteDnsPalette.SurfaceAlt
                },
            )
            .border(
                1.5.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.18f) else WhiteDnsPalette.Border,
                RoundedCornerShape(11.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileDragHandle(
                enabled = canDrag,
                dragging = dragging,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Ink,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = connectionSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = when {
                            active -> WhiteDnsPalette.Success
                            selected -> WhiteDnsPalette.AccentText
                            else -> WhiteDnsPalette.Muted
                        },
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileIconButton(
                icon = Icons.Rounded.Link,
                contentDescription = "Export connection profile",
                emphasized = false,
                enabled = profile.customServerDomain.isNotBlank() && profile.customServerEncryptionKey.isNotBlank(),
                onClick = onExport,
            )
            ProfileIconButton(
                icon = Icons.Rounded.Edit,
                contentDescription = "Edit connection profile",
                emphasized = selected,
                enabled = canEdit,
                onClick = onEdit,
            )
            ProfileIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = if (active) "Connected profile cannot be deleted" else "Delete connection profile",
                emphasized = false,
                enabled = canDelete,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ProfileDragHandle(
    enabled: Boolean,
    dragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        dragging -> WhiteDnsPalette.AccentSurface
        else -> WhiteDnsPalette.Surface
    }
    val border = if (dragging) {
        WhiteDnsPalette.Accent.copy(alpha = 0.40f)
    } else if (enabled) {
        WhiteDnsPalette.Border
    } else {
        WhiteDnsPalette.Divider
    }
    val iconColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        dragging -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .pointerInput(enabled) {
                if (!enabled) {
                    return@pointerInput
                }
                detectVerticalDragGestures(
                    onDragStart = {
                        onDragStart()
                    },
                    onDragCancel = {
                        onDragCancel()
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag to reorder profile",
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ProfileIconButton(
    icon: ImageVector,
    contentDescription: String,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        emphasized -> WhiteDnsPalette.Accent
        else -> WhiteDnsPalette.Surface
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        emphasized -> WhiteDnsPalette.AccentPressed
        else -> WhiteDnsPalette.Border
    }
    val iconColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.OnAccent
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun CompactActionButton(
    label: String,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        emphasized -> WhiteDnsPalette.Accent
        else -> WhiteDnsPalette.Surface
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        emphasized -> WhiteDnsPalette.AccentPressed
        else -> WhiteDnsPalette.Border
    }
    val textColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.OnAccent
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 8.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.9.sp,
            ),
        )
    }
}

@Composable
private fun MtuSettingsGroup(
    settings: WhiteDnsSettings,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Min Upload",
            value = settings.minUploadMtu,
            onValueChange = {
                onSettingsChange(settings.copy(minUploadMtu = it.filter(Char::isDigit)))
            },
            placeholder = "40",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Min Download",
            value = settings.minDownloadMtu,
            onValueChange = {
                onSettingsChange(settings.copy(minDownloadMtu = it.filter(Char::isDigit)))
            },
            placeholder = "300",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Max Upload",
            value = settings.maxUploadMtu,
            onValueChange = {
                onSettingsChange(settings.copy(maxUploadMtu = it.filter(Char::isDigit)))
            },
            placeholder = "140",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Max Download",
            value = settings.maxDownloadMtu,
            onValueChange = {
                onSettingsChange(settings.copy(maxDownloadMtu = it.filter(Char::isDigit)))
            },
            placeholder = "3000",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Resolver Retries",
            value = settings.mtuTestRetriesResolvers,
            onValueChange = {
                onSettingsChange(settings.copy(mtuTestRetriesResolvers = it.filter(Char::isDigit)))
            },
            placeholder = "3",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Resolver Timeout",
            value = settings.mtuTestTimeoutResolvers,
            onValueChange = {
                onSettingsChange(settings.copy(mtuTestTimeoutResolvers = filterDecimalInput(it)))
            },
            placeholder = "2.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    WhiteDnsTextField(
        label = "Resolver Parallel",
        value = settings.mtuTestParallelismResolvers,
        onValueChange = {
            onSettingsChange(settings.copy(mtuTestParallelismResolvers = it.filter(Char::isDigit)))
        },
        placeholder = "100",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            capitalization = KeyboardCapitalization.None,
        ),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Logs Retries",
            value = settings.mtuTestRetriesLogs,
            onValueChange = {
                onSettingsChange(settings.copy(mtuTestRetriesLogs = it.filter(Char::isDigit)))
            },
            placeholder = "5",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Logs Timeout",
            value = settings.mtuTestTimeoutLogs,
            onValueChange = {
                onSettingsChange(settings.copy(mtuTestTimeoutLogs = filterDecimalInput(it)))
            },
            placeholder = "2.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    WhiteDnsTextField(
        label = "Logs Parallel",
        value = settings.mtuTestParallelismLogs,
        onValueChange = {
            onSettingsChange(settings.copy(mtuTestParallelismLogs = it.filter(Char::isDigit)))
        },
        placeholder = "32",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            capitalization = KeyboardCapitalization.None,
        ),
    )
}

@Composable
private fun RuntimeWorkersSettingsGroup(
    settings: WhiteDnsSettings,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "RX/TX Workers",
            value = settings.rxTxWorkers,
            onValueChange = {
                onSettingsChange(settings.copy(rxTxWorkers = it.filter(Char::isDigit)))
            },
            placeholder = "4",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Process Workers",
            value = settings.tunnelProcessWorkers,
            onValueChange = {
                onSettingsChange(settings.copy(tunnelProcessWorkers = it.filter(Char::isDigit)))
            },
            placeholder = "4",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Packet Timeout",
            value = settings.tunnelPacketTimeoutSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(tunnelPacketTimeoutSeconds = filterDecimalInput(it)))
            },
            placeholder = "8.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Idle Poll",
            value = settings.dispatcherIdlePollIntervalSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(dispatcherIdlePollIntervalSeconds = filterDecimalInput(it)))
            },
            placeholder = "0.020",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "TX Channel",
            value = settings.txChannelSize,
            onValueChange = {
                onSettingsChange(settings.copy(txChannelSize = it.filter(Char::isDigit)))
            },
            placeholder = "2048",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "RX Channel",
            value = settings.rxChannelSize,
            onValueChange = {
                onSettingsChange(settings.copy(rxChannelSize = it.filter(Char::isDigit)))
            },
            placeholder = "2048",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "UDP Pool",
            value = settings.resolverUdpConnectionPoolSize,
            onValueChange = {
                onSettingsChange(settings.copy(resolverUdpConnectionPoolSize = it.filter(Char::isDigit)))
            },
            placeholder = "64",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Stream Queue",
            value = settings.streamQueueInitialCapacity,
            onValueChange = {
                onSettingsChange(settings.copy(streamQueueInitialCapacity = it.filter(Char::isDigit)))
            },
            placeholder = "128",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Orphan Queue",
            value = settings.orphanQueueInitialCapacity,
            onValueChange = {
                onSettingsChange(settings.copy(orphanQueueInitialCapacity = it.filter(Char::isDigit)))
            },
            placeholder = "32",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "DNS Fragments",
            value = settings.dnsResponseFragmentStoreCapacity,
            onValueChange = {
                onSettingsChange(settings.copy(dnsResponseFragmentStoreCapacity = it.filter(Char::isDigit)))
            },
            placeholder = "256",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "SOCKS UDP Timeout",
            value = settings.socksUdpAssociateReadTimeoutSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(socksUdpAssociateReadTimeoutSeconds = filterDecimalInput(it)))
            },
            placeholder = "30.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Terminal Retain",
            value = settings.clientTerminalStreamRetentionSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(clientTerminalStreamRetentionSeconds = filterDecimalInput(it)))
            },
            placeholder = "45.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Cancelled Retain",
            value = settings.clientCancelledSetupRetentionSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(clientCancelledSetupRetentionSeconds = filterDecimalInput(it)))
            },
            placeholder = "120.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Retry Base",
            value = settings.sessionInitRetryBaseSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(sessionInitRetryBaseSeconds = filterDecimalInput(it)))
            },
            placeholder = "1.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Retry Step",
            value = settings.sessionInitRetryStepSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(sessionInitRetryStepSeconds = filterDecimalInput(it)))
            },
            placeholder = "1.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Retry Linear",
            value = settings.sessionInitRetryLinearAfter,
            onValueChange = {
                onSettingsChange(settings.copy(sessionInitRetryLinearAfter = it.filter(Char::isDigit)))
            },
            placeholder = "5",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Retry Max",
            value = settings.sessionInitRetryMaxSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(sessionInitRetryMaxSeconds = filterDecimalInput(it)))
            },
            placeholder = "60.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = "Busy Retry",
            value = settings.sessionInitBusyRetryIntervalSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(sessionInitBusyRetryIntervalSeconds = filterDecimalInput(it)))
            },
            placeholder = "60.0",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
            ),
        )
    }
}

@Composable
private fun HeaderCard() {
    val context = LocalContext.current
    var showDonationDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(WhiteDnsPalette.SurfaceAlt)
                    .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(9.dp))
                    .clickable { openWhiteDnsTelegram(context) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "W",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteDnsPalette.AccentText,
                    ),
                )
            }
            Text(
                text = "WhiteDNS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = WhiteDnsPalette.Ink,
                ),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(7.dp))
                    .background(WhiteDnsPalette.Surface, RoundedCornerShape(7.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "v1.2.0",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = WhiteDnsPalette.Muted,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(WhiteDnsPalette.Accent)
                    .border(1.5.dp, WhiteDnsPalette.AccentPressed, RoundedCornerShape(7.dp))
                    .clickable { showDonationDialog = true }
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "DONATE",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 9.sp,
                        color = WhiteDnsPalette.OnAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                    ),
                )
            }
        }
    }

    if (showDonationDialog) {
        DonationDialog(onDismiss = { showDonationDialog = false })
    }
}

private fun openWhiteDnsTelegram(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(WhiteDnsTelegramUrl),
    )
    context.startActivity(intent)
}

@Composable
private fun DonationDialog(
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            Text(
                text = "SUPPORT WHITEDNS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Donations will be used for new servers and app development.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = WhiteDnsPalette.Description,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            DonationWallets.forEachIndexed { index, wallet ->
                DonationWalletField(
                    label = wallet.label,
                    address = wallet.address,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(wallet.address))
                    },
                )
                if (index != DonationWallets.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = "CLOSE",
                emphasized = true,
                enabled = true,
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun DonationWalletField(
    label: String,
    address: String,
    onCopy: () -> Unit,
) {
    Column {
        FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Input)
                .border(2.5.dp, WhiteDnsPalette.Divider, RoundedCornerShape(10.dp))
                .clickable(onClick = onCopy)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = address,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhiteDnsPalette.Ink,
                    fontSize = 12.sp,
                ),
            )
            Text(
                text = "COPY",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhiteDnsPalette.AccentText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                ),
            )
        }
    }
}

@Composable
private fun NotificationPermissionBanner(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "VPN NOTIFICATION BLOCKED",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.WarningText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enable WhiteDNS notifications so Android can keep the full VPN service visible and running in the background.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = WhiteDnsPalette.WarningText,
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ENABLE VPN NOTIFICATION",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 9.sp,
                    color = WhiteDnsPalette.WarningText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}

@Composable
private fun BatteryOptimizationBanner(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "BACKGROUND VPN MAY STOP",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.WarningText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Allow WhiteDNS to ignore battery optimization so the VPN keeps running after you leave the app.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = WhiteDnsPalette.WarningText,
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ALLOW BACKGROUND VPN",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 9.sp,
                    color = WhiteDnsPalette.WarningText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}

@Composable
private fun FullVpnPerformanceWarning(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = WhiteDnsPalette.WarningText,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "FULL VPN PERFORMANCE WARNING",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 9.sp,
                    color = WhiteDnsPalette.WarningText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Full VPN routes all device traffic through the DNS tunnel and may be slower or less stable. Proxy Mode is recommended for best performance.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = WhiteDnsPalette.WarningText,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss full VPN warning",
                tint = WhiteDnsPalette.WarningText,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SplitTunnelSettingsPanel(
    settings: WhiteDnsSettings,
    apps: List<SplitTunnelAppInfo>,
    appsLoading: Boolean,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    var showAppDialog by rememberSaveable { mutableStateOf(false) }
    val selectedPackages = settings.splitTunnelPackages
    val selectedLabels = selectedSplitTunnelLabels(selectedPackages, apps)
    val appSummary = splitTunnelAppsSummary(
        mode = settings.splitTunnelMode,
        appLabels = selectedLabels,
    )

    InfoCard(
        title = "SPLIT TUNNEL",
        compact = true,
    ) {
        WhiteDnsDropdownField(
            label = "App Routing",
            value = settings.splitTunnelMode,
            options = WhiteDnsOptions.splitTunnelModes,
            compact = true,
            onValueChange = { mode ->
                onSettingsChange(settings.copy(splitTunnelMode = mode))
            },
        )
        AnimatedVisibility(
            visible = settings.splitTunnelMode != WhiteDnsOptions.SplitTunnelModeOff,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            color = WhiteDnsPalette.Muted,
                        ),
                    )
                    Text(
                        text = appSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = WhiteDnsPalette.Ink,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    CompactActionButton(
                        modifier = Modifier.widthIn(min = 104.dp),
                        label = if (appsLoading) "LOADING" else "SELECT APPS",
                        emphasized = true,
                        enabled = !appsLoading && apps.isNotEmpty(),
                        onClick = { showAppDialog = true },
                    )
                }
            }
        }
    }

    if (showAppDialog) {
        SplitTunnelAppDialog(
            apps = apps,
            selectedPackages = selectedPackages,
            onDismiss = { showAppDialog = false },
            onSave = { packages ->
                onSettingsChange(settings.copy(splitTunnelPackages = packages))
                showAppDialog = false
            },
        )
    }
}

@Composable
private fun SplitTunnelAppDialog(
    apps: List<SplitTunnelAppInfo>,
    selectedPackages: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember(selectedPackages.joinToString("|")) {
        mutableStateOf(selectedPackages.toSet())
    }
    val normalizedQuery = query.trim().lowercase(Locale.US)
    val visibleApps = remember(apps, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            apps
        } else {
            apps.filter { app ->
                app.label.lowercase(Locale.US).contains(normalizedQuery) ||
                    app.packageName.lowercase(Locale.US).contains(normalizedQuery)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = "SELECT APPS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Search",
                value = query,
                onValueChange = { query = it },
                placeholder = "App name or package",
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
            ) {
                if (visibleApps.isEmpty()) {
                    item {
                        Text(
                            text = "No apps found.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                    }
                } else {
                    items(
                        items = visibleApps,
                        key = { it.packageName },
                    ) { app ->
                        val checked = app.packageName in selected
                        SplitTunnelAppRow(
                            app = app,
                            checked = checked,
                            onToggle = {
                                selected = if (checked) {
                                    selected - app.packageName
                                } else {
                                    selected + app.packageName
                                }
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CLEAR",
                    emphasized = false,
                    enabled = selected.isNotEmpty(),
                    onClick = { selected = emptySet() },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CANCEL",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "SAVE",
                    emphasized = true,
                    enabled = true,
                    onClick = {
                        val installedPackageOrder = apps.map { it.packageName }
                        onSave(
                            installedPackageOrder.filter { it in selected } +
                                selected.filterNot { it in installedPackageOrder }.sorted(),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SplitTunnelAppRow(
    app: SplitTunnelAppInfo,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 9.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = WhiteDnsPalette.Accent,
                uncheckedColor = WhiteDnsPalette.ControlBorder,
                checkmarkColor = WhiteDnsPalette.OnAccent,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = app.packageName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
        }
    }
}

@Composable
private fun ConnectButton(
    status: ConnectionStatus,
    progressState: ConnectionProgressState,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val ringColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Divider
            ConnectionStatus.CONNECTING -> WhiteDnsPalette.AccentPressed
            ConnectionStatus.CONNECTED -> WhiteDnsPalette.Success
            ConnectionStatus.DISCONNECTING -> WhiteDnsPalette.WarningText
        },
        animationSpec = tween(400),
        label = "connectRingColor",
    )
    val iconColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Disabled
            ConnectionStatus.CONNECTING -> WhiteDnsPalette.AccentPressed
            ConnectionStatus.CONNECTED -> WhiteDnsPalette.WarningText
            ConnectionStatus.DISCONNECTING -> WhiteDnsPalette.WarningText
        },
        animationSpec = tween(400),
        label = "connectIconColor",
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (status == ConnectionStatus.CONNECTED) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "connectButtonScale",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "connectButtonMotion")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "connectSpinAngle",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "connectPulseAlpha",
    )
    val progressFraction by animateFloatAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTING -> progressState.fraction.coerceIn(0.03f, 0.99f)
            ConnectionStatus.CONNECTED -> 1f
            ConnectionStatus.DISCONNECTED -> 0f
            ConnectionStatus.DISCONNECTING -> 1f
        },
        animationSpec = tween(300),
        label = "connectProgressFraction",
    )
    val circleSize = 156.dp
    val outerRingSize = 198.dp
    val label = when (status) {
        ConnectionStatus.DISCONNECTED -> "CONNECT"
        ConnectionStatus.CONNECTING -> "CONNECTING"
        ConnectionStatus.CONNECTED -> "STOP"
        ConnectionStatus.DISCONNECTING -> "STOPPING"
    }
    val labelColor = when (status) {
        ConnectionStatus.CONNECTED -> WhiteDnsPalette.WarningText
        ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Disabled
        ConnectionStatus.DISCONNECTING -> WhiteDnsPalette.WarningText
        else -> WhiteDnsPalette.AccentPressed
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(outerRingSize)
                .scale(buttonScale),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(outerRingSize)) {
                val strokeWidth = 3.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val color = if (status == ConnectionStatus.CONNECTING) {
                    WhiteDnsPalette.Accent.copy(alpha = pulseAlpha)
                } else {
                    ringColor.copy(alpha = if (
                        status == ConnectionStatus.CONNECTED ||
                        status == ConnectionStatus.DISCONNECTING
                    ) 0.30f else 0.15f)
                }
                drawCircle(
                    color = color,
                    radius = radius,
                    style = Stroke(width = strokeWidth),
                )
            }

            Canvas(modifier = Modifier.size(circleSize + 14.dp)) {
                val strokeWidth = 5.dp.toPx()
                val arcSize = Size(
                    width = size.width - strokeWidth,
                    height = size.height - strokeWidth,
                )
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                when (status) {
                    ConnectionStatus.CONNECTING,
                    ConnectionStatus.DISCONNECTING -> {
                        drawArc(
                            color = WhiteDnsPalette.Border.copy(alpha = 0.65f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth),
                        )
                        drawArc(
                            color = if (status == ConnectionStatus.DISCONNECTING) {
                                WhiteDnsPalette.WarningText
                            } else {
                                WhiteDnsPalette.Accent
                            },
                            startAngle = -90f,
                            sweepAngle = 360f * progressFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                            ),
                        )
                        rotate(spinAngle) {
                            drawArc(
                                color = WhiteDnsPalette.Accent.copy(alpha = 0.22f),
                                startAngle = 0f,
                                sweepAngle = 42f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                ),
                            )
                        }
                    }
                    ConnectionStatus.CONNECTED -> {
                        drawArc(
                            color = WhiteDnsPalette.Success,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                            ),
                        )
                    }
                    ConnectionStatus.DISCONNECTED -> Unit
                }
            }

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(if (enabled) WhiteDnsPalette.Surface else WhiteDnsPalette.SurfaceAlt)
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (status == ConnectionStatus.CONNECTED) {
                            Icons.Rounded.Stop
                        } else {
                            Icons.Rounded.PowerSettingsNew
                        },
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(if (status == ConnectionStatus.CONNECTED) 30.dp else 34.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                        ),
                    )
                    if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.DISCONNECTING) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = if (status == ConnectionStatus.DISCONNECTING) "Closing runtime" else progressState.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${progressState.percent.coerceIn(0, 99)}%",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (status == ConnectionStatus.DISCONNECTING) {
                                    WhiteDnsPalette.WarningText
                                } else {
                                    WhiteDnsPalette.Accent
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}

private enum class ResolverRuntimeDialogType {
    ACTIVE,
    VALID,
}

@Composable
private fun ResolverRuntimeSummary(
    resolverState: ResolverRuntimeState,
    progressState: ConnectionProgressState,
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    var selectedDialog by remember { mutableStateOf<ResolverRuntimeDialogType?>(null) }
    val activeResolverCount = resolverState.activeResolvers.size.takeIf { it > 0 }?.toString() ?: "Pending"
    val backgroundMtuScanInProgress = connectionStatus == ConnectionStatus.CONNECTED &&
        progressState.phase.lowercase() == "mtu" &&
        progressState.total > 0 &&
        progressState.completed < progressState.total

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ResolverRuntimeValue(
                modifier = Modifier.weight(1f),
                label = "Active Resolvers",
                value = activeResolverCount,
                onClick = { selectedDialog = ResolverRuntimeDialogType.ACTIVE },
            )
            ResolverRuntimeValue(
                modifier = Modifier.weight(1f),
                label = "Valid Resolvers",
                value = resolverState.validResolvers.size.toString(),
                onClick = { selectedDialog = ResolverRuntimeDialogType.VALID },
            )
        }
        AnimatedVisibility(
            visible = backgroundMtuScanInProgress,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(WhiteDnsPalette.AccentSurface)
                    .border(1.5.dp, WhiteDnsPalette.Accent.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = WhiteDnsPalette.Accent,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = if (progressState.completed > 0) {
                        "Background scanning in progress ${progressState.completed}/${progressState.total}"
                    } else {
                        "Background scanning in progress"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        color = WhiteDnsPalette.AccentText,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }

    selectedDialog?.let { dialog ->
        val title = when (dialog) {
            ResolverRuntimeDialogType.ACTIVE -> "ACTIVE RESOLVERS"
            ResolverRuntimeDialogType.VALID -> "VALID RESOLVERS"
        }
        val resolvers = when (dialog) {
            ResolverRuntimeDialogType.ACTIVE -> resolverState.activeResolvers
            ResolverRuntimeDialogType.VALID -> resolverState.validResolvers
        }
        ResolverRuntimeDialog(
            title = title,
            resolvers = resolvers,
            onDismiss = { selectedDialog = null },
        )
    }
}

@Composable
private fun ResolverRuntimeValue(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 9.sp,
                color = WhiteDnsPalette.Muted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            ),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Ink,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun ConnectionVerificationSummary(
    verification: ConnectionVerificationState,
    modifier: Modifier = Modifier,
) {
    val statusText = when (verification.status) {
        ConnectionVerificationStatus.Checking -> "Verifying"
        ConnectionVerificationStatus.Verified -> "Verified"
        ConnectionVerificationStatus.Failed -> "Needs Attention"
        else -> "Pending"
    }
    val message = verification.message.ifBlank {
        if (verification.status == ConnectionVerificationStatus.Idle) {
            "Connection verification has not run yet"
        } else {
            "Checking tunnel route"
        }
    }
    val color = when (verification.status) {
        ConnectionVerificationStatus.Verified -> WhiteDnsPalette.Success
        ConnectionVerificationStatus.Failed -> WhiteDnsPalette.WarningText
        ConnectionVerificationStatus.Checking -> WhiteDnsPalette.Accent
        else -> WhiteDnsPalette.Muted
    }
    val surface = when (verification.status) {
        ConnectionVerificationStatus.Verified -> WhiteDnsPalette.SuccessSurface
        ConnectionVerificationStatus.Failed -> WhiteDnsPalette.WarningSurface
        ConnectionVerificationStatus.Checking -> WhiteDnsPalette.AccentSurface
        else -> WhiteDnsPalette.Surface
    }
    val icon = when (verification.status) {
        ConnectionVerificationStatus.Verified -> Icons.Rounded.Check
        ConnectionVerificationStatus.Failed -> Icons.Rounded.WarningAmber
        ConnectionVerificationStatus.Checking -> Icons.Rounded.Tune
        else -> Icons.Rounded.Link
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(surface)
            .border(1.5.dp, color.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = statusText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = WhiteDnsPalette.Description,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
private fun ResolverRuntimeDialog(
    title: String,
    resolvers: List<String>,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val resolverText = resolvers.joinToString("\n")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            WhiteDnsTextField(
                label = "Resolvers",
                value = resolverText,
                onValueChange = {},
                placeholder = "No resolvers",
                singleLine = false,
                minLines = 6,
                maxLines = 12,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CLOSE",
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "COPY",
                    emphasized = true,
                    enabled = resolverText.isNotBlank(),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(resolverText))
                    },
                )
            }
        }
    }
}

@Composable
private fun LiveSpeedStrip(
    stats: ConnectionStats,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WhiteDnsPalette.Surface)
            .border(2.dp, WhiteDnsPalette.Border, RoundedCornerShape(18.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SpeedIndicator(
            icon = Icons.Filled.Download,
            label = "Down",
            value = formatDataSpeed(stats.downloadSpeedBytesPerSecond),
            modifier = Modifier.weight(1f),
        )
        SpeedIndicator(
            icon = Icons.Filled.Upload,
            label = "Up",
            value = formatDataSpeed(stats.uploadSpeedBytesPerSecond),
            modifier = Modifier.weight(1f),
        )
        SpeedIndicator(
            icon = Icons.Filled.DataUsage,
            label = "Total Usage",
            value = formatDataSize(stats.totalDataUsageBytes),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SpeedIndicator(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(WhiteDnsPalette.SuccessSurface)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WhiteDnsPalette.Success,
            modifier = Modifier.size(17.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 8.sp,
                    letterSpacing = 0.8.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Text(
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteDnsPalette.Ink,
                ),
            )
        }
    }
}

@Composable
private fun ConnectionInfoCard(
    connectionProfileName: String,
    resolverProfileName: String,
    settingProfileName: String,
    listenAddress: String,
    httpProxyAddress: String,
    connectionMode: String,
    httpProxyEnabled: Boolean,
    protocol: String,
    socksAuthEnabled: Boolean,
    username: String,
    password: String,
    stats: ConnectionStats,
    showProxyDetails: Boolean,
    splitTunnelMode: String,
    splitTunnelPackages: List<String>,
    splitTunnelAppLabels: Map<String, String>,
    canDownloadToml: Boolean,
    onDownloadToml: () -> Unit,
) {
    InfoCard(title = "CONNECTION INFO") {
        InfoRow(label = "Mode", value = connectionMode)
        if (showProxyDetails) {
            InfoRow(label = "SOCKS5 Proxy", value = listenAddress)
            if (httpProxyEnabled) {
                InfoRow(label = "HTTP Proxy", value = httpProxyAddress)
            }
            ProtocolRow(protocol = protocol, showDivider = true)
            InfoRow(label = "Auth", value = if (socksAuthEnabled) "On" else "Off")
            if (socksAuthEnabled) {
                InfoRow(label = "User", value = username)
                InfoRow(label = "Pass", value = password)
            }
        } else {
            ProtocolRow(protocol = protocol, showDivider = true)
            InfoRow(
                label = "Split Tunnel",
                value = splitTunnelConnectionSummary(
                    mode = splitTunnelMode,
                    packageNames = splitTunnelPackages,
                    labelsByPackage = splitTunnelAppLabels,
                ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        CompactMetricRow(
            metrics = listOf(
                CompactMetric(
                    icon = Icons.Filled.Apps,
                    label = "Apps",
                    value = stats.connectedApps.toString(),
                ),
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(label = "Connection Profile", value = connectionProfileName)
        InfoRow(label = "Resolver Profile", value = resolverProfileName)
        InfoRow(label = "Setting Profile", value = settingProfileName)
        Spacer(modifier = Modifier.height(10.dp))
        ResolverActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "DOWNLOAD TOML",
            emphasized = false,
            enabled = canDownloadToml,
            onClick = onDownloadToml,
        )
    }
}

private data class CompactMetric(
    val icon: ImageVector,
    val label: String,
    val value: String,
)

@Composable
private fun CompactMetricRow(
    metrics: List<CompactMetric>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        metrics.forEach { metric ->
            CompactMetricPill(
                metric = metric,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CompactMetricPill(
    metric: CompactMetric,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WhiteDnsPalette.SuccessSurface)
            .border(1.5.dp, WhiteDnsPalette.Success.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = WhiteDnsPalette.Success,
            modifier = Modifier.size(15.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = metric.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 8.sp,
                    letterSpacing = 0.6.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Text(
                text = metric.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteDnsPalette.Ink,
                ),
            )
        }
    }
}

@Composable
private fun ProtocolRow(
    protocol: String,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Protocol",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(WhiteDnsPalette.AccentSurface)
                .border(1.5.dp, WhiteDnsPalette.Accent.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text = protocol,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteDnsPalette.AccentText,
                ),
            )
        }
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(WhiteDnsPalette.Divider),
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(if (compact) 12.dp else 14.dp)
    val borderWidth = if (compact) 2.dp else 2.5.dp
    val contentPadding = if (compact) 14.dp else 18.dp
    val titleBottomSpacing = if (compact) 9.dp else 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(WhiteDnsPalette.Surface)
            .border(borderWidth, WhiteDnsPalette.Border, shape)
            .padding(contentPadding),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (compact) 12.sp else 13.sp,
                color = WhiteDnsPalette.SectionTitle,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            ),
        )
        Spacer(modifier = Modifier.height(titleBottomSpacing))
        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteDnsPalette.Ink,
            ),
        )
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(WhiteDnsPalette.Divider),
        )
    }
}

@Composable
private fun ConnectionLogsBlock(
    uiState: WhiteDnsUiState,
    expanded: Boolean = false,
) {
    val logs = uiState.connectionLogs
    val visibleLogs = if (expanded) logs else logs.take(10)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CONNECTION LOGS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.SectionTitle,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogActionButton(
                    label = "COPY",
                    onClick = {
                        clipboardManager.setText(
                            AnnotatedString(logs.joinToString(separator = "\n")),
                        )
                    },
                )
                LogActionButton(
                    label = "DIAGNOSTICS",
                    onClick = {
                        clipboardManager.setText(
                            AnnotatedString(buildDiagnosticsText(context, uiState)),
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(12.dp)),
        ) {
            visibleLogs.forEachIndexed { index, logLine ->
                Text(
                    text = logLine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) WhiteDnsPalette.SurfaceAlt else WhiteDnsPalette.Surface)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        color = WhiteDnsPalette.Description,
                    ),
                )
                if (index != visibleLogs.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(WhiteDnsPalette.Divider),
                    )
                }
            }
        }
    }
}

@Composable
private fun LogActionButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 9.sp,
                color = WhiteDnsPalette.AccentText,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
        )
    }
}

private fun shareProfileLink(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "WhiteDNS profile")
        putExtra(Intent.EXTRA_TEXT, link)
    }
    context.startActivity(Intent.createChooser(intent, "Export WhiteDNS profile"))
}

private fun shareClientConfigToml(context: Context, toml: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "client_config.toml")
        putExtra(Intent.EXTRA_TEXT, toml)
    }
    context.startActivity(Intent.createChooser(intent, "Export client_config.toml"))
}

private fun buildDiagnosticsText(
    context: Context,
    uiState: WhiteDnsUiState,
): String {
    val settings = uiState.settings.runtimeConnectionSettings()
    val resolvedSettings = settings.resolve()
    val selectedProfile = settings.selectedConnectionProfile()
    val resolverProfile = settings.selectedResolverProfile()
    val verification = uiState.connectionVerification
    val appVersion = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty()

    return buildString {
        appendLine("WhiteDNS diagnostics")
        appendLine("App version: ${appVersion.ifBlank { "unknown" }}")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("Status: ${uiState.connectionStatus}")
        appendLine("Mode: ${WhiteDnsOptions.connectionModeLabel(resolvedSettings.connectionMode)}")
        appendLine("Profile: ${selectedProfile.name.ifBlank { selectedProfile.id }}")
        appendLine("Server: [redacted]")
        appendLine("Encryption key: ${selectedProfile.customServerEncryptionKey.ifBlank { "not configured" }}")
        appendLine("Resolver profile: ${resolverProfile?.name ?: "Manual resolvers"}")
        appendLine("Resolvers: ${resolvedSettings.resolverEntries.size}")
        appendLine("Split tunnel: ${WhiteDnsOptions.splitTunnelModeLabel(resolvedSettings.splitTunnelMode)}")
        appendLine("Split tunnel packages: ${resolvedSettings.splitTunnelPackages.size}")
        appendLine("Notifications enabled: ${uiState.notificationsEnabled}")
        appendLine("Battery optimization ignored: ${uiState.batteryOptimizationIgnored}")
        appendLine("Listen IP: ${resolvedSettings.listenIp}")
        appendLine("Listen port: ${resolvedSettings.listenPort}")
        appendLine("HTTP proxy: ${if (resolvedSettings.httpProxyEnabled) resolvedSettings.httpProxyPort else "off"}")
        appendLine("SOCKS auth: ${if (resolvedSettings.socks5Authentication) "on" else "off"}")
        appendLine("Traffic total: ${formatDataSize(uiState.connectionStats.totalDataUsageBytes)}")
        appendLine("Traffic down: ${formatDataSpeed(uiState.connectionStats.downloadSpeedBytesPerSecond)}")
        appendLine("Traffic up: ${formatDataSpeed(uiState.connectionStats.uploadSpeedBytesPerSecond)}")
        appendLine("Connected apps: ${uiState.connectionStats.connectedApps}")
        appendLine("Active resolvers: ${uiState.resolverRuntimeState.activeResolvers.size}")
        appendLine("Valid resolvers: ${uiState.resolverRuntimeState.validResolvers.size}")
        appendLine("Verification: ${verification.status}")
        if (verification.message.isNotBlank()) {
            appendLine("Verification message: ${verification.message}")
        }
        appendLine()
        appendLine("Recent logs:")
        uiState.connectionLogs.forEach { log ->
            appendLine(log.redactDiagnosticSecrets(selectedProfile))
        }
    }.trimEnd()
}

private fun String.redactDiagnosticSecrets(profile: ConnectionProfile): String {
    var redacted = this
    if (profile.customServerDomain.isNotBlank()) {
        redacted = redacted.replace(profile.customServerDomain, "[server route]")
    }
    return redacted.replace(Regex("""(?i)(password|pass|secret)\s*[:=]\s*\S+"""), "\$1=[redacted]")
}

private fun readResolverTextFromUri(context: Context, uri: Uri): Result<String> {
    return runCatching {
        val rawText = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { reader -> reader.readText() }
            ?: throw IllegalArgumentException("Unable to open resolver file")
        normalizeImportedResolverText(rawText)
    }
}

private fun normalizeImportedResolverText(rawText: String): String {
    val validation = validateResolverText(rawText)
    if (validation.invalidEntries.isNotEmpty()) {
        throw IllegalArgumentException("Invalid resolver IP: ${validation.invalidEntries.first()}")
    }
    if (validation.normalizedText.isBlank()) {
        throw IllegalArgumentException("No resolver entries found in file")
    }
    return validation.normalizedText
}

private fun resolverValidationMessage(
    name: String,
    resolverText: String,
    invalidEntries: List<String>,
    validResolverCount: Int,
): String? {
    return when {
        resolverText.isBlank() -> null
        invalidEntries.isNotEmpty() -> "Invalid resolver IP: ${invalidEntries.first()}"
        validResolverCount == 0 -> "Enter at least one valid resolver IP."
        name.isBlank() -> "Enter a profile name to save."
        else -> "$validResolverCount valid resolver${if (validResolverCount == 1) "" else "s"}."
    }
}

private val ResolverImportMimeTypes = arrayOf(
    "text/*",
    "application/json",
    "application/octet-stream",
)

private const val QrBitmapSizePx = 768
private const val WhiteDnsTelegramUrl = "https://t.me/whitedns"

private data class DonationWallet(
    val label: String,
    val address: String,
)

private val DonationWallets = listOf(
    DonationWallet(
        label = "USDT (TON / Jetton)",
        address = "UQCVUC-eZzxNkVVewFp9pz43JKd0XIc55KCdC5gbwxJKiqoL",
    ),
    DonationWallet(
        label = "USDT (TRC20 / TRON)",
        address = "TNvdayQydF8t8bNHMuBctxVdgiaWeNKhmR",
    ),
    DonationWallet(
        label = "USDT (ERC20 / Ethereum)",
        address = "0x87519c886F79d3935b9A45519f821519272D9967",
    ),
    DonationWallet(
        label = "USDT (SPL / Solana)",
        address = "7zKyVVnJRBEiw6vL6vnX1VKUTEkw5QvXu696QV5qLS94",
    ),
)

@Composable
private fun ResolverActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    enabled: Boolean = true,
) {
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        emphasized -> WhiteDnsPalette.Accent
        else -> WhiteDnsPalette.SurfaceAlt
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        emphasized -> WhiteDnsPalette.AccentPressed
        else -> WhiteDnsPalette.Border
    }
    val textColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.OnAccent
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 9.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    expanded: Boolean,
    icon: ImageVector = Icons.Rounded.Tune,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "sectionRotation",
    )
    val borderColor by animateColorAsState(
        targetValue = if (expanded) {
            WhiteDnsPalette.Accent.copy(alpha = 0.26f)
        } else {
            WhiteDnsPalette.Border
        },
        animationSpec = tween(220),
        label = "sectionBorderColor",
    )
    val iconBackground by animateColorAsState(
        targetValue = if (expanded) {
            WhiteDnsPalette.AccentSurface
        } else {
            WhiteDnsPalette.SurfaceAlt
        },
        animationSpec = tween(220),
        label = "sectionIconBackground",
    )
    val iconColor by animateColorAsState(
        targetValue = if (expanded) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
        animationSpec = tween(220),
        label = "sectionIconColor",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = WhiteDnsPalette.Ink,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                    Text(
                        text = if (expanded) "TAP TO COLLAPSE" else "TAP TO CONFIGURE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = WhiteDnsPalette.Description,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.1.sp,
                        ),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (expanded) {
                            WhiteDnsPalette.Accent
                        } else {
                            WhiteDnsPalette.SurfaceAlt
                        },
                    )
                    .padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (expanded) "OPEN" else "CLOSED",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 9.sp,
                        color = if (expanded) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                    ),
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (expanded) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = rotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteDnsPalette.Surface)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            color = WhiteDnsPalette.SectionTitle,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
        ),
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(WhiteDnsPalette.Divider, RoundedCornerShape(1.dp)),
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = WhiteDnsPalette.FieldLabel,
                    fontWeight = FontWeight.Medium,
                ),
            )
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = WhiteDnsPalette.OnAccent,
                checkedTrackColor = WhiteDnsPalette.Accent,
                checkedBorderColor = WhiteDnsPalette.Accent,
                uncheckedThumbColor = WhiteDnsPalette.Muted,
                uncheckedTrackColor = WhiteDnsPalette.Input,
                uncheckedBorderColor = WhiteDnsPalette.ControlBorder,
            ),
        )
    }
}

@Composable
private fun WhiteDnsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onFocusChange: (Boolean) -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) WhiteDnsPalette.Accent.copy(alpha = 0.60f) else WhiteDnsPalette.Divider
    val shape = RoundedCornerShape(10.dp)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = WhiteDnsPalette.Ink,
        fontSize = 14.sp,
    )

    Column(modifier = modifier) {
        FieldLabel(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    focused = it.isFocused
                    onFocusChange(it.isFocused)
                },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = textStyle,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(WhiteDnsPalette.Input)
                        .border(2.5.dp, borderColor, shape)
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(color = WhiteDnsPalette.Placeholder),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 13.sp,
            color = WhiteDnsPalette.FieldLabel,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.9.sp,
        ),
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun <T> WhiteDnsDropdownField(
    label: String,
    value: T,
    options: List<Choice<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == value }?.label.orEmpty()
    val shape = RoundedCornerShape(if (compact) 10.dp else 12.dp)
    val horizontalPadding = if (compact) 10.dp else 12.dp
    val verticalPadding = if (compact) 8.dp else 10.dp
    val borderColor by animateColorAsState(
        targetValue = if (!enabled) {
            WhiteDnsPalette.Divider
        } else if (expanded) {
            WhiteDnsPalette.Accent.copy(alpha = 0.60f)
        } else {
            WhiteDnsPalette.ControlBorder
        },
        animationSpec = tween(180),
        label = "dropdownBorderColor",
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> WhiteDnsPalette.SurfaceAlt
            expanded -> WhiteDnsPalette.DropdownSurface
            else -> WhiteDnsPalette.DropdownSurface
        },
        animationSpec = tween(180),
        label = "dropdownBackgroundColor",
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "dropdownArrowRotation",
    )

    Column(modifier = modifier) {
        FieldLabel(label)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(backgroundColor)
                    .border(1.5.dp, borderColor, shape)
                    .clickable(enabled = enabled, onClick = { expanded = true })
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedLabel.ifEmpty { "Select" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = if (enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = when {
                        !enabled -> WhiteDnsPalette.Disabled
                        expanded -> WhiteDnsPalette.Accent
                        else -> WhiteDnsPalette.Muted
                    },
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer(rotationZ = arrowRotation),
                )
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(WhiteDnsPalette.DropdownSurface),
            ) {
                options.forEach { choice ->
                    val selected = choice.value == value
                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) {
                                    WhiteDnsPalette.AccentSurface
                                } else {
                                    Color.Transparent
                                },
                            ),
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = choice.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Ink,
                                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = WhiteDnsPalette.AccentText,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onValueChange(choice.value)
                        },
                    )
                }
            }
        }
    }
}

private fun formatDataSpeed(bytesPerSecond: Long): String {
    return "${formatDataSize(bytesPerSecond)}/s"
}

private fun formatDataSize(bytes: Long): String {
    if (bytes <= 0) {
        return "0 B"
    }

    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }

    if (unitIndex == 0) {
        return "$bytes B"
    }

    val pattern = if (value >= 10.0) "%.0f %s" else "%.1f %s"
    return String.format(Locale.US, pattern, value, units[unitIndex])
}

private fun displayProxyIpAddress(
    listenIp: String,
    networkIpAddress: String,
): String {
    return when (listenIp.trim()) {
        "0.0.0.0", "::", "[::]" -> networkIpAddress.ifBlank { "127.0.0.1" }
        "" -> "127.0.0.1"
        else -> listenIp.trim()
    }
}

private fun generateSocksPassword(): String {
    return UUID.randomUUID().toString().replace("-", "")
}

private fun selectedSplitTunnelLabels(
    packageNames: List<String>,
    apps: List<SplitTunnelAppInfo>,
): List<String> {
    val labelsByPackage = apps.associate { it.packageName to it.label }
    return packageNames.map { packageName ->
        labelsByPackage[packageName] ?: packageName
    }
}

private fun splitTunnelAppsSummary(
    mode: String,
    appLabels: List<String>,
): String {
    if (mode == WhiteDnsOptions.SplitTunnelModeOff) {
        return "All apps"
    }
    if (appLabels.isEmpty()) {
        return "No apps"
    }
    return compactAppLabelSummary(appLabels)
}

private fun splitTunnelConnectionSummary(
    mode: String,
    packageNames: List<String>,
    labelsByPackage: Map<String, String>,
): String {
    val labels = packageNames.map { packageName ->
        labelsByPackage[packageName] ?: packageName
    }
    return when (mode) {
        WhiteDnsOptions.SplitTunnelModeInclude -> {
            if (labels.isEmpty()) "All apps" else "Only ${compactAppLabelSummary(labels)}"
        }
        WhiteDnsOptions.SplitTunnelModeExclude -> {
            if (labels.isEmpty()) "All apps" else "Bypass ${compactAppLabelSummary(labels)}"
        }
        else -> "All apps"
    }
}

private fun serverRouteSummary(raw: String): String {
    val domains = normalizeServerDomains(raw)
    return if (domains.isEmpty()) {
        "Server route missing"
    } else {
        domains.joinToString(separator = ", ")
    }
}

private fun compactAppLabelSummary(appLabels: List<String>): String {
    return when (appLabels.size) {
        0 -> "No apps"
        1 -> appLabels.first()
        2 -> appLabels.joinToString(", ")
        else -> "${appLabels.take(2).joinToString(", ")} +${appLabels.size - 2}"
    }
}

private fun filterDecimalInput(value: String): String {
    var hasDecimalPoint = false
    return buildString {
        value.forEach { character ->
            when {
                character.isDigit() -> append(character)
                character == '.' && !hasDecimalPoint -> {
                    hasDecimalPoint = true
                    append(character)
                }
            }
        }
    }
}
