package shop.whitedns.client.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants

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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import shop.whitedns.client.R
import shop.whitedns.client.model.AdvancedSettingsProfile
import shop.whitedns.client.model.AutoTuneTrialResult
import shop.whitedns.client.model.Choice
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ConnectionProgressState
import shop.whitedns.client.model.ConnectionStats
import shop.whitedns.client.model.ConnectionStatus
import shop.whitedns.client.model.ConnectionVerificationState
import shop.whitedns.client.model.ConnectionVerificationStatus
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.ResolverRuntimeState
import shop.whitedns.client.model.WhiteDnsOptions
import shop.whitedns.client.model.WhiteDnsScanDefaults
import shop.whitedns.client.model.WhiteDnsScanState
import shop.whitedns.client.model.WhiteDnsScanStatus
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsUiState
import shop.whitedns.client.model.applyAdvancedProfile
import shop.whitedns.client.model.applyAutoTunePreset
import shop.whitedns.client.model.applyResolverProfileToSelectedConnection
import shop.whitedns.client.model.deleteConnectionProfile
import shop.whitedns.client.model.deleteDuplicateConnectionProfiles
import shop.whitedns.client.model.deleteAdvancedProfile
import shop.whitedns.client.model.deleteResolverProfile
import shop.whitedns.client.model.duplicateConnectionProfileCount
import shop.whitedns.client.model.exportAllStormDnsProfileLinks
import shop.whitedns.client.model.exportStormDnsProfileLink
import shop.whitedns.client.model.importStormDnsProfileLinks
import shop.whitedns.client.model.importAdvancedSettingsProfileFromToml
import shop.whitedns.client.model.matchesAdvancedProfile
import shop.whitedns.client.model.moveConnectionProfileToIndex
import shop.whitedns.client.model.moveAdvancedProfileToIndex
import shop.whitedns.client.model.moveResolverProfileToIndex
import shop.whitedns.client.model.normalizedAdvancedProfiles
import shop.whitedns.client.model.normalizedConnectionProfiles
import shop.whitedns.client.model.normalizedResolverProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.saveResolverProfileAs
import shop.whitedns.client.model.selectAdvancedProfile
import shop.whitedns.client.model.selectConnectionProfile
import shop.whitedns.client.model.selectedAdvancedProfile
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.model.selectedResolverProfile
import shop.whitedns.client.model.updateManualResolverText
import shop.whitedns.client.model.upsertConnectionProfile
import shop.whitedns.client.model.upsertAdvancedProfile
import shop.whitedns.client.model.upsertResolverProfile
import shop.whitedns.client.model.validateResolverText
import shop.whitedns.client.model.WhiteDnsAutoTunePresets
import shop.whitedns.client.model.WhiteDnsParallelTest
import shop.whitedns.client.model.syncSelectedConnectionProfileFields
import shop.whitedns.client.storm.StormDnsConfigRenderer
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import androidx.core.content.FileProvider

@Composable
fun WhiteDnsScreen(
    uiState: WhiteDnsUiState,
    onBatteryOptimizationClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onConnectClick: () -> Unit,
    onScanFileSelected: (Uri) -> Unit,
    onScanDefaultListSelected: () -> Unit,
    onScanStartClick: () -> Unit,
    onScanConnectionProfileChange: (String) -> Unit,
    onScanWorkerCountChange: (String) -> Unit,
    onScanStopClick: () -> Unit,
    onScanResumeClick: () -> Unit,
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
                WhiteDnsTab.SCAN -> ScanTabContent(
                    uiState = uiState,
                    onScanFileSelected = onScanFileSelected,
                    onScanDefaultListSelected = onScanDefaultListSelected,
                    onScanStartClick = onScanStartClick,
                    onScanConnectionProfileChange = onScanConnectionProfileChange,
                    onScanWorkerCountChange = onScanWorkerCountChange,
                    onScanStopClick = onScanStopClick,
                    onScanResumeClick = onScanResumeClick,
                    onSettingsChange = onSettingsChange,
                )
                WhiteDnsTab.LOGS -> LogsTabContent(
                    uiState = uiState,
                    onSettingsChange = onSettingsChange,
                )
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
    SCAN("Scan", Icons.Rounded.Search),
    LOGS("Logs", Icons.Rounded.Link),
}

private const val ScanWorkerMin = 1
private const val ScanWorkerMax = 32

private enum class CompactActionTone {
    Default,
    Accent,
    Success,
    Danger,
}

private enum class ProfileCreateRequest {
    CONNECTION,
    RESOLVER,
}

@Composable
private fun Modifier.whiteDnsPageBackground(): Modifier {
    val backgroundColor = WhiteDnsPalette.Background
    val accentColor = WhiteDnsPalette.Accent
    return drawBehind {
        drawRect(color = backgroundColor)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.30f),
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
    var showConnectionTomlDialog by rememberSaveable { mutableStateOf(false) }
    var showConnectionEditDialog by remember { mutableStateOf(false) }
    var showResolverEditDialog by remember { mutableStateOf(false) }
    var showAdvancedEditDialog by remember { mutableStateOf(false) }
    var autoTuneSaveResult by remember { mutableStateOf<AutoTuneTrialResult?>(null) }
    var parallelTestSelectionExpanded by rememberSaveable { mutableStateOf(true) }
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
    val context = LocalContext.current
    val splitTunnelApps = remember(context.packageName) {
        loadSplitTunnelAppOptions(context)
    }
    val splitTunnelAppLabels = remember(splitTunnelApps) {
        splitTunnelApps.associate { it.packageName to it.label }
    }
    val connectionSelectorItems = remember(connectionProfiles) {
        connectionProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = profile.name,
                detail = profile.customServerDomain.ifBlank { "Server route missing" },
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
    val showBatteryBanner = !uiState.batteryOptimizationIgnored &&
        !settings.batteryOptimizationWarningDismissed
    val shouldCollapseParallelTestSelection =
        settings.autoTuneEnabled &&
            resolvedSettings.connectionMode == "vpn" &&
            uiState.connectionStatus == ConnectionStatus.CONNECTED &&
            uiState.autoTuneTrialResults.any { it.selected }

    LaunchedEffect(
        shouldCollapseParallelTestSelection,
        settings.autoTuneEnabled,
        uiState.connectionStatus,
    ) {
        when {
            shouldCollapseParallelTestSelection -> parallelTestSelectionExpanded = false
            !settings.autoTuneEnabled || uiState.connectionStatus == ConnectionStatus.DISCONNECTED ->
                parallelTestSelectionExpanded = true
        }
    }

    fun normalizeManualResolverInput() {
        if (selectedResolverProfile != null || !resolverValidation.isValid) {
            return
        }
        if (resolverValidation.normalizedText != settings.resolverText) {
            onSettingsChange(settings.updateManualResolverText(resolverValidation.normalizedText))
        }
    }

    fun openSelector(type: HomeSelectorType) {
        selectorSheetType = type
        selectorSheetVisible = true
    }

    fun closeSelector() {
        selectorSheetVisible = false
    }

    BackHandler(enabled = selectorSheetVisible) {
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
            HeaderCard(
                themeMode = settings.themeMode,
                onThemeModeChange = { onSettingsChange(settings.copy(themeMode = it)) },
            )

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
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xl))
                }
            }
            AnimatedVisibility(
                visible = showBatteryBanner,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
            ) {
                Column {
                    BatteryOptimizationBanner(
                        onClick = onBatteryOptimizationClick,
                        onDismiss = {
                            onSettingsChange(settings.copy(batteryOptimizationWarningDismissed = true))
                        },
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xl))
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
                        onSettingsChange(
                            settings.copy(connectionMode = connectionMode),
                        )
                    },
                )
            AnimatedVisibility(
                visible = resolvedSettings.connectionMode == "vpn",
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
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
                            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                        }
                    }
                    SplitTunnelSettingsPanel(
                        settings = settings,
                        apps = splitTunnelApps,
                        onSettingsChange = onSettingsChange,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xl))
            ConnectButton(
                status = uiState.connectionStatus,
                progressState = uiState.connectionProgress,
                enabled = uiState.connectionStatus != ConnectionStatus.DISCONNECTED || hasResolvers,
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
                visible = resolvedSettings.connectionMode == "proxy" || resolvedSettings.connectionMode == "vpn",
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WhiteDnsSpacing.md),
                ) {
                    ToggleRow(
                        label = "Parallel Test",
                        enabled = settings.autoTuneEnabled,
                        onToggle = {
                            val selectedIds = WhiteDnsParallelTest.normalizeConfigIds(
                                configIds = settings.parallelTestSelectedConfigIds,
                                advancedProfiles = advancedProfiles,
                            )
                            onSettingsChange(
                                settings.copy(
                                    autoTuneEnabled = !settings.autoTuneEnabled,
                                    parallelTestSelectedConfigIds = selectedIds,
                                ),
                            )
                        },
                    )
                    AnimatedVisibility(
                        visible = settings.autoTuneEnabled,
                        enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
                    ) {
                        ParallelTestSelectionPanel(
                            settings = settings,
                            advancedProfiles = advancedProfiles,
                            expanded = parallelTestSelectionExpanded,
                            onExpandedChange = { parallelTestSelectionExpanded = it },
                            onSettingsChange = onSettingsChange,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = uiState.connectionStatus != ConnectionStatus.CONNECTED,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HomeSelectorCard(
                            label = "Connection",
                            value = selectedConnectionProfile.name,
                            detail = selectedConnectionProfile.customServerDomain.ifBlank { "Server route missing" },
                            selected = true,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.CONNECTION) },
                            onEditClick = {
                                showResolverRequiredMessage = false
                                showConnectionEditDialog = true
                            },
                        )
                        HomeSelectorCard(
                            label = "Resolver",
                            value = selectedResolverProfile?.name ?: "Resolver Profile",
                            detail = resolverSelectorDetail,
                            selected = selectedResolverProfile != null,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.RESOLVER) },
                            editEnabled = selectedResolverProfile?.id != ResolverProfile.DefaultId,
                            onEditClick = {
                                showResolverRequiredMessage = false
                                showResolverEditDialog = true
                            },
                        )
                        AdvancedProfileControls(
                            selectedProfile = selectedAdvancedProfile,
                            dirty = advancedProfileDirty,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onSelectClick = { openSelector(HomeSelectorType.ADVANCED) },
                            onEditClick = {
                                showResolverRequiredMessage = false
                                showAdvancedEditDialog = true
                            },
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
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xl))
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
                        actionsEnabled = uiState.connectionStatus != ConnectionStatus.CONNECTING,
                        onAddConnectionClick = onAddConnectionClick,
                        onAddResolverProfileClick = onAddResolverProfileClick,
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xl))
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
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED ||
                    uiState.autoTuneTrialResults.isNotEmpty(),
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
                        autoTuneResults = uiState.autoTuneTrialResults,
                        onSaveAutoTuneResult = { result ->
                            autoTuneSaveResult = result
                        },
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

            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sectionSpacing))
            FooterLink()
        }
        }

        selectorSheetType?.let { activeSelector ->
            HomeSelectorSheet(
                visible = selectorSheetVisible,
                title = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> "Connection Profiles"
                    HomeSelectorType.RESOLVER -> "Resolver Profiles"
                    HomeSelectorType.ADVANCED -> "Setting Profiles"
                },
                searchPlaceholder = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> "Search connections"
                    HomeSelectorType.RESOLVER -> "Search resolvers"
                    HomeSelectorType.ADVANCED -> "Search setting profiles"
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
                    HomeSelectorType.ADVANCED -> "No setting profiles found."
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

        if (showConnectionEditDialog) {
            ConnectionProfileDialog(
                profile = selectedConnectionProfile,
                onDismiss = { showConnectionEditDialog = false },
                onSave = { updatedProfile ->
                    val nextProfile = updatedProfile.copy(
                        id = selectedConnectionProfile.id,
                        serverMode = "custom",
                        resolverProfileId = selectedConnectionProfile.resolverProfileId,
                        connectionMode = selectedConnectionProfile.connectionMode,
                    )
                    onSettingsChange(
                        settings
                            .upsertConnectionProfile(nextProfile)
                            .selectConnectionProfile(selectedConnectionProfile.id),
                    )
                    showConnectionEditDialog = false
                },
            )
        }

        if (showResolverEditDialog) {
            ResolverProfileDialog(
                profile = selectedResolverProfile,
                initialResolverText = selectedResolverProfile?.resolverText ?: settings.resolverText,
                onDismiss = { showResolverEditDialog = false },
                onSave = { profile ->
                    val nextSettings = if (selectedResolverProfile == null) {
                        settings.upsertResolverProfile(profile)
                    } else {
                        settings.upsertResolverProfile(profile.copy(id = selectedResolverProfile.id))
                    }
                    onSettingsChange(nextSettings)
                    showResolverEditDialog = false
                },
            )
        }

        if (showAdvancedEditDialog) {
            val editingProfile = selectedAdvancedProfile.takeIf { it.id != AdvancedSettingsProfile.DefaultId }
            AdvancedSettingsProfileDialog(
                profile = editingProfile,
                initialName = editingProfile?.name ?: advancedSaveAsInitialName(selectedAdvancedProfile),
                initialSettings = settings,
                onDismiss = { showAdvancedEditDialog = false },
                onSave = { profile ->
                    val nextProfile = if (editingProfile == null) {
                        profile
                    } else {
                        profile.copy(id = editingProfile.id)
                    }
                    onSettingsChange(settings.upsertAdvancedProfile(nextProfile))
                    showAdvancedEditDialog = false
                },
            )
        }

        autoTuneSaveResult?.let { result ->
            val initialSettings = remember(result.configId, settings, advancedProfiles) {
                parallelTestInitialSettingsForResult(
                    result = result,
                    settings = settings,
                    advancedProfiles = advancedProfiles,
                )
            }
            if (initialSettings == null) {
                autoTuneSaveResult = null
            } else {
                AdvancedSettingsProfileDialog(
                    profile = null,
                    initialName = result.label,
                    initialSettings = initialSettings,
                    onDismiss = { autoTuneSaveResult = null },
                    onSave = { profile ->
                        onSettingsChange(settings.upsertAdvancedProfile(profile))
                        autoTuneSaveResult = null
                    },
                )
            }
        }
    }
}

private fun parallelTestInitialSettingsForResult(
    result: AutoTuneTrialResult,
    settings: WhiteDnsSettings,
    advancedProfiles: List<AdvancedSettingsProfile>,
): WhiteDnsSettings? {
    WhiteDnsParallelTest.presetIdFromConfigId(result.configId)?.let { presetId ->
        val preset = WhiteDnsAutoTunePresets.all.firstOrNull { it.id == presetId } ?: return null
        return settings
            .applyAutoTunePreset(preset)
            .copy(autoTuneEnabled = false)
            .syncSelectedConnectionProfileFields()
    }
    WhiteDnsParallelTest.settingProfileIdFromConfigId(result.configId)?.let { profileId ->
        val profile = advancedProfiles.firstOrNull { it.id == profileId } ?: return null
        return settings
            .applyAdvancedProfile(profile)
            .copy(autoTuneEnabled = false)
            .syncSelectedConnectionProfileFields()
    }
    return null
}

@Composable
private fun ParallelTestSelectionPanel(
    settings: WhiteDnsSettings,
    advancedProfiles: List<AdvancedSettingsProfile>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "parallelTestSelectionArrow",
    )
    val userProfiles = advancedProfiles.filter { it.id != AdvancedSettingsProfile.DefaultId }
    val selectedIds = WhiteDnsParallelTest.normalizeConfigIds(
        configIds = settings.parallelTestSelectedConfigIds,
        advancedProfiles = advancedProfiles,
    )
    val selectedSet = selectedIds.toSet()
    val whiteDnsConfigIds = WhiteDnsParallelTest.defaultConfigIds
    val selectedWhiteDnsCount = whiteDnsConfigIds.count { it in selectedSet }
    val allWhiteDnsSelected = selectedWhiteDnsCount == whiteDnsConfigIds.size
    val canAddWhiteDnsConfigs = allWhiteDnsSelected ||
        selectedIds.size - selectedWhiteDnsCount + whiteDnsConfigIds.size <= WhiteDnsParallelTest.MaxSelectedConfigs

    fun updateSelectedConfigIds(nextIds: List<String>) {
        val normalizedIds = WhiteDnsParallelTest.normalizeConfigIds(
            configIds = nextIds,
            advancedProfiles = advancedProfiles,
            defaultIfEmpty = false,
        )
        onSettingsChange(settings.copy(parallelTestSelectedConfigIds = normalizedIds))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WhiteDnsSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .semantics {
                    contentDescription = if (expanded) {
                        context.getString(R.string.cd_parallel_test_collapse)
                    } else {
                        context.getString(R.string.cd_parallel_test_expand)
                    }
                }
                .clickable {
                    haptic.performLight()
                    onExpandedChange(!expanded)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Selected ${selectedIds.size}/${WhiteDnsParallelTest.MaxSelectedConfigs}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.FieldLabel,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (expanded) WhiteDnsPalette.Accent else WhiteDnsPalette.SurfaceAlt)
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
                    contentDescription = if (expanded) {
                        "Collapse Parallel Test configs"
                    } else {
                        "Expand Parallel Test configs"
                    },
                    tint = if (expanded) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = arrowRotation),
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
            ) {
                Text(
                    text = "Parallel Test starts selected configs as temporary SOCKS proxies, measures speed and ping after each tunnel is ready, then connects with the best result. In Full VPN mode, the test still runs through SOCKS first and starts the Android VPN after a result is selected.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = WhiteDnsPalette.Muted,
                        lineHeight = 16.sp,
                    ),
                )
                ParallelTestConfigRow(
                    label = "WhiteDNS configs",
                    detail = "Adds 7 suggested configs",
                    checked = allWhiteDnsSelected,
                    enabled = canAddWhiteDnsConfigs,
                    onToggle = {
                        haptic.performLight()
                        val withoutWhiteDnsConfigs = selectedIds.filterNot { it in whiteDnsConfigIds }
                        if (allWhiteDnsSelected) {
                            if (withoutWhiteDnsConfigs.isNotEmpty()) {
                                updateSelectedConfigIds(withoutWhiteDnsConfigs)
                            }
                        } else if (canAddWhiteDnsConfigs) {
                            updateSelectedConfigIds(whiteDnsConfigIds + withoutWhiteDnsConfigs)
                        }
                    },
                )
                if (userProfiles.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(top = WhiteDnsSpacing.xs),
                        text = "Your configs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = WhiteDnsPalette.FieldLabel,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    userProfiles.forEach { profile ->
                        val configId = WhiteDnsParallelTest.settingConfigId(profile.id)
                        val checked = configId in selectedSet
                        val enabled = checked || selectedIds.size < WhiteDnsParallelTest.MaxSelectedConfigs
                        ParallelTestConfigRow(
                            label = profile.name.ifBlank { "Setting" },
                            detail = advancedProfileSummary(profile),
                            checked = checked,
                            enabled = enabled,
                            onToggle = {
                                haptic.performLight()
                                val nextIds = if (checked) {
                                    if (selectedIds.size == 1) {
                                        selectedIds
                                    } else {
                                        selectedIds.filterNot { it == configId }
                                    }
                                } else {
                                    selectedIds + configId
                                }
                                updateSelectedConfigIds(nextIds)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParallelTestConfigRow(
    label: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.46f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = WhiteDnsPalette.Accent,
                uncheckedColor = WhiteDnsPalette.ControlBorder,
                checkmarkColor = WhiteDnsPalette.OnAccent,
                disabledCheckedColor = WhiteDnsPalette.Accent.copy(alpha = 0.42f),
                disabledUncheckedColor = WhiteDnsPalette.ControlBorder.copy(alpha = 0.42f),
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Ink.copy(alpha = contentAlpha),
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.Muted.copy(alpha = contentAlpha),
                ),
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
        HeaderCard(
            themeMode = uiState.settings.themeMode,
            onThemeModeChange = { onSettingsChange(uiState.settings.copy(themeMode = it)) },
        )
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            InfoCard(
                title = when (selectedProfileTab) {
                    ProfileTab.CONNECTION -> "CONNECTION PROFILES"
                    ProfileTab.RESOLVER -> "RESOLVER PROFILES"
                    ProfileTab.SETTING -> "SETTING PROFILES"
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
                    ProfileTab.SETTING -> SettingProfilesSettings(
                        settings = uiState.settings,
                        connectionStatus = uiState.connectionStatus,
                        onSettingsChange = onSettingsChange,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sectionSpacing))
            FooterLink()
        }
    }
}

private enum class ProfileTab(val label: String) {
    CONNECTION("Connection"),
    RESOLVER("Resolver"),
    SETTING("Setting"),
}

@Composable
private fun LogsTabContent(
    uiState: WhiteDnsUiState,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCard(
            themeMode = uiState.settings.themeMode,
            onThemeModeChange = { onSettingsChange(uiState.settings.copy(themeMode = it)) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp),
        ) {
            ConnectionLogsBlock(uiState = uiState, expanded = true)
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sectionSpacing))
            FooterLink()
        }
    }
}

@Composable
private fun ScanTabContent(
    uiState: WhiteDnsUiState,
    onScanFileSelected: (Uri) -> Unit,
    onScanDefaultListSelected: () -> Unit,
    onScanStartClick: () -> Unit,
    onScanConnectionProfileChange: (String) -> Unit,
    onScanWorkerCountChange: (String) -> Unit,
    onScanStopClick: () -> Unit,
    onScanResumeClick: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val scanState = uiState.scanState
    val workerCount = (uiState.scanWorkerCount.toIntOrNull() ?: WhiteDnsScanDefaults.DefaultWorkerCount)
        .coerceIn(ScanWorkerMin, ScanWorkerMax)
    val connectionProfiles = remember(uiState.settings) {
        uiState.settings.normalizedConnectionProfiles()
    }
    val selectedScanConnectionProfile = connectionProfiles
        .firstOrNull { it.id == uiState.scanConnectionProfileId }
        ?: connectionProfiles.first()
    val scanConnectionProfileOptions = remember(connectionProfiles) {
        connectionProfiles.map { profile ->
            Choice(
                value = profile.id,
                label = profile.name.ifBlank { "Connection" },
            )
        }
    }
    val selectedScanProfileNeedsServer = selectedScanConnectionProfile.customServerDomain.isBlank() ||
        selectedScanConnectionProfile.customServerEncryptionKey.isBlank()
    var showScanInfoNotice by rememberSaveable { mutableStateOf(true) }
    var showSaveAsDialog by rememberSaveable(scanState.sessionId) { mutableStateOf(false) }
    val saveAsResolverEntries = remember(scanState.validResolverEntries) {
        scanState.validResolverEntries
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
    }
    val canSaveAsProfile = saveAsResolverEntries.isNotEmpty()
    val scanFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(onScanFileSelected)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCard(
            themeMode = uiState.settings.themeMode,
            onThemeModeChange = { onSettingsChange(uiState.settings.copy(themeMode = it)) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp)
                .padding(top = 36.dp),
            verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "DEFAULT LIST",
                    emphasized = !scanState.isRunning,
                    enabled = !scanState.isRunning,
                    onClick = onScanDefaultListSelected,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "SELECT FILE",
                    emphasized = false,
                    enabled = !scanState.isRunning,
                    onClick = {
                        scanFileLauncher.launch(ResolverImportMimeTypes)
                    },
                )
            }
            ScanWorkerSlider(
                workerCount = workerCount,
                enabled = !scanState.isRunning,
                onWorkerCountChange = { onScanWorkerCountChange(it.toString()) },
            )
            ScanNote(
                text = "Higher worker values increase battery usage and can impact phone performance.",
            )
            WhiteDnsDropdownField(
                label = "Scan Profile",
                value = selectedScanConnectionProfile.id,
                options = scanConnectionProfileOptions,
                onValueChange = onScanConnectionProfileChange,
                enabled = !scanState.isRunning,
            )
            if (selectedScanProfileNeedsServer) {
                ScanWarningBanner(
                    text = "${selectedScanConnectionProfile.name.ifBlank { "Scan profile" }} needs a server route and key.",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "START",
                    emphasized = true,
                    tone = CompactActionTone.Success,
                    enabled = scanCanStart(scanState) && !selectedScanProfileNeedsServer,
                    onClick = onScanStartClick,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "STOP",
                    emphasized = false,
                    tone = CompactActionTone.Danger,
                    enabled = scanState.isRunning,
                    onClick = onScanStopClick,
                )
            }
            if (showScanInfoNotice) {
                ScanInfoNotice(
                    text = "Scan results will be saved in profiles automatically.",
                    onDismiss = { showScanInfoNotice = false },
                )
            }

            SectionCard(
                title = "SCAN STATUS",
                expanded = true,
                icon = Icons.Filled.DataUsage,
                iconContentDescription = stringResource(R.string.cd_icon_data_usage),
                collapsible = false,
                onToggle = {},
            ) {
                CompactMetricRow(
                    metrics = listOf(
                        CompactMetric(
                            icon = Icons.Filled.DataUsage,
                            iconContentDescription = stringResource(R.string.cd_icon_data_usage),
                            label = "Total",
                            value = scanState.totalResolvers.toString(),
                        ),
                        CompactMetric(
                            icon = Icons.Rounded.Check,
                            iconContentDescription = stringResource(R.string.cd_icon_success),
                            label = "Valid",
                            value = scanState.validResolvers.toString(),
                        ),
                        CompactMetric(
                            icon = Icons.Rounded.Close,
                            iconContentDescription = stringResource(R.string.cd_icon_error),
                            label = "Rejected",
                            value = scanState.rejectedResolvers.toString(),
                        ),
                    ),
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                ScanProgressBar(fraction = scanState.fraction)
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                InfoRow(label = "Status", value = scanStatusLabel(scanState.status))
                InfoRow(label = "Source", value = scanState.sourceName.ifBlank { "No file selected" })
                InfoRow(label = "Workers", value = scanState.workerCount.toString())
                InfoRow(label = "Progress", value = "${scanState.completedResolvers}/${scanState.totalResolvers}")
                if (scanState.message.isNotBlank()) {
                    InfoRow(label = "Message", value = scanState.message, multilineValue = true)
                }
                if (scanState.workerFailures.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                    ScanWarningBanner(text = scanState.workerFailures.first())
                }
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "SAVE AS",
                        emphasized = false,
                        enabled = canSaveAsProfile,
                        onClick = {
                            showSaveAsDialog = true
                        },
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = "RESUME",
                        emphasized = true,
                        tone = CompactActionTone.Accent,
                        enabled = scanCanResume(scanState),
                        onClick = onScanResumeClick,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sectionSpacing))
        }
    }

    if (showSaveAsDialog) {
        ScanSaveAsProfileDialog(
            sourceName = scanState.sourceName,
            resolverCount = saveAsResolverEntries.size,
            initialName = scanResultProfileInitialName(scanState.sourceName),
            onDismiss = { showSaveAsDialog = false },
            onSave = { name ->
                onSettingsChange(
                    uiState.settings.saveResolverProfileAs(
                        name = name,
                        resolverText = saveAsResolverEntries.joinToString(separator = "\n"),
                    ),
                )
                showSaveAsDialog = false
            },
        )
    }
}

private fun scanCanResume(scanState: WhiteDnsScanState): Boolean {
    return !scanState.isRunning &&
        (scanState.status == WhiteDnsScanStatus.Stopped || scanState.status == WhiteDnsScanStatus.Failed) &&
        scanState.completedResolvers < scanState.totalResolvers
}

private fun scanCanStart(scanState: WhiteDnsScanState): Boolean {
    return !scanState.isRunning &&
        scanState.status == WhiteDnsScanStatus.Ready &&
        scanState.sessionId.isNotBlank() &&
        scanState.completedResolvers < scanState.totalResolvers
}

private fun scanResultProfileInitialName(sourceName: String): String {
    val trimmed = sourceName.trim()
    if (trimmed.isBlank()) {
        return "Scan Results"
    }
    val withoutExtension = trimmed.substringBeforeLast('.').trim().ifBlank { trimmed }
    return "$withoutExtension Results"
}

@Composable
private fun ScanInfoNotice(
    text: String,
    onDismiss: () -> Unit,
) {
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhiteDnsPalette.AccentSurface)
            .border(1.5.dp, WhiteDnsPalette.Accent.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(WhiteDnsPalette.Accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.cd_scan_autosave_enabled),
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SCAN AUTO SAVE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 9.sp,
                    color = WhiteDnsPalette.AccentText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = WhiteDnsPalette.AccentText,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable {
                    haptic.performLight()
                    onDismiss()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss scanner info",
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ScanSaveAsProfileDialog(
    sourceName: String,
    resolverCount: Int,
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(initialName, sourceName) { mutableStateOf(initialName) }
    val canSave = name.trim().isNotEmpty() && resolverCount > 0
    val scanLabel = sourceName.ifBlank { "Current scan" }

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
                text = "SAVE SCAN RESULTS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Text(
                text = "$resolverCount valid resolvers from $scanLabel will be saved as a new resolver profile.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Scan Results",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                        onSave(name.trim())
                    },
                )
            }
        }
    }
}

@Composable
private fun ScanWorkerSlider(
    workerCount: Int,
    enabled: Boolean,
    onWorkerCountChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FieldLabel("Workers")
            Text(
                text = workerCount.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                ),
            )
        }
        Slider(
            value = workerCount.toFloat(),
            onValueChange = { value ->
                onWorkerCountChange(value.roundToInt().coerceIn(ScanWorkerMin, ScanWorkerMax))
            },
            enabled = enabled,
            valueRange = ScanWorkerMin.toFloat()..ScanWorkerMax.toFloat(),
            steps = ScanWorkerMax - ScanWorkerMin - 1,
            colors = SliderDefaults.colors(
                thumbColor = WhiteDnsPalette.Accent,
                activeTrackColor = WhiteDnsPalette.Accent,
                inactiveTrackColor = WhiteDnsPalette.ControlBorder,
                disabledThumbColor = WhiteDnsPalette.Disabled,
                disabledActiveTrackColor = WhiteDnsPalette.ControlBorder,
                disabledInactiveTrackColor = WhiteDnsPalette.Input,
            ),
        )
    }
}

@Composable
private fun ScanProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(WhiteDnsPalette.Input),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(10.dp)
                .background(WhiteDnsPalette.Accent),
        )
    }
}

@Composable
private fun ScanNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteDnsPalette.Input)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = stringResource(R.string.cd_icon_warning),
            tint = WhiteDnsPalette.Muted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = WhiteDnsPalette.Muted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScanWarningBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.26f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = stringResource(R.string.cd_icon_warning),
            tint = WhiteDnsPalette.WarningText,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.WarningText,
            ),
        )
    }
}

private fun scanStatusLabel(status: String): String {
    return when (status) {
        WhiteDnsScanStatus.Ready -> "Ready"
        WhiteDnsScanStatus.Starting -> "Starting"
        WhiteDnsScanStatus.Running -> "Running"
        WhiteDnsScanStatus.Completed -> "Completed"
        WhiteDnsScanStatus.Failed -> "Failed"
        WhiteDnsScanStatus.Stopped -> "Stopped"
        else -> "Idle"
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: WhiteDnsTab,
    onTabSelected: (WhiteDnsTab) -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

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
                        .semantics {
                            contentDescription = context.getString(
                                R.string.cd_navigate_to_tab, tab.label
                            )
                        }
                        .clickable {
                            haptic.performLight()
                            onTabSelected(tab)
                        }
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
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

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
                    .semantics {
                        contentDescription = if (selected) {
                            context.getString(R.string.cd_profile_tab_selected, tab.label)
                        } else {
                            context.getString(R.string.cd_profile_tab_unselected, tab.label)
                        }
                    }
                    .clickable {
                        haptic.performLight()
                        onTabSelected(tab)
                    }
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
    val haptic = rememberHapticFeedback()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Powered by WhiteDNS",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Description,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
        Text(
            text = WhiteDnsTelegramUrl,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .semantics {
                    contentDescription = context.getString(R.string.cd_telegram_link)
                }
                .clickable {
                    haptic.performLight()
                    openWhiteDnsTelegram(context)
                }
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
    val haptic = rememberHapticFeedback()

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
                            haptic.performLight()
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

@Composable
private fun ThemeModeSegmentedControl(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()

    Column(modifier = modifier) {
        FieldLabel("Theme")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.ControlBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            WhiteDnsOptions.themeModes.forEach { mode ->
                val selected = selectedMode == mode.value
                val background by animateColorAsState(
                    targetValue = if (selected) WhiteDnsPalette.Accent else Color.Transparent,
                    animationSpec = tween(180),
                    label = "themeModeSegmentBackground",
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                    animationSpec = tween(180),
                    label = "themeModeSegmentText",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(background)
                        .clickable(enabled = !selected) {
                            haptic.performLight()
                            onModeChange(mode.value)
                        }
                        .padding(horizontal = 6.dp, vertical = 9.dp),
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
    onEditClick: (() -> Unit)? = null,
    editEnabled: Boolean = enabled,
) {
    val haptic = rememberHapticFeedback()
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
                .clickable(enabled = enabled) {
                    haptic.performLight()
                    onClick()
                }
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
            if (onEditClick != null) {
                ProfileIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = "Edit $label",
                    emphasized = false,
                    enabled = editEnabled,
                    onClick = onEditClick,
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.cd_dropdown_advanced_settings),
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
    onEditClick: () -> Unit,
) {
    HomeSelectorCard(
        label = "Setting",
        value = selectedProfile.name,
        detail = if (dirty) "Unsaved changes" else advancedProfileSummary(selectedProfile),
        selected = !dirty,
        enabled = enabled,
        onClick = onSelectClick,
        onEditClick = onEditClick,
    )
}

@Composable
private fun AdvancedSettingsImportDialog(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Result<WhiteDnsSettings>,
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var tomlText by rememberSaveable { mutableStateOf("") }
    var importError by rememberSaveable { mutableStateOf<String?>(null) }
    val canImport = name.trim().isNotEmpty() && tomlText.trim().isNotEmpty()
    val importTomlFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        readTextFromUri(context, uri, "Unable to open settings file")
            .onSuccess { importedText ->
                tomlText = importedText
                importError = null
            }
            .onFailure { error ->
                importError = error.message ?: "Unable to import settings file"
            }
    }

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
                text = "IMPORT SETTINGS PROFILE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = {
                    name = it
                    importError = null
                },
                placeholder = "Imported settings",
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
                        importTomlFileLauncher.launch(SettingsImportMimeTypes)
                    },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = "CLEAR",
                    emphasized = false,
                    enabled = tomlText.isNotBlank(),
                    onClick = {
                        tomlText = ""
                        importError = null
                    },
                )
            }
            WhiteDnsTextField(
                label = "TOML",
                value = tomlText,
                onValueChange = {
                    tomlText = it
                    importError = null
                },
                placeholder = "LISTEN_PORT = 10886\nLOG_LEVEL = \"WARN\"",
                singleLine = false,
                minLines = 7,
                maxLines = 12,
            )
            importError?.let { message ->
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                        onImport(name, tomlText)
                            .onFailure { error ->
                                importError = error.message ?: "Unable to import settings"
                            }
                    },
                )
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
            }
        }

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
    ToggleRow(
        label = "Traffic Warmup",
        enabled = settings.trafficWarmupEnabled,
        onToggle = {
            onSettingsChange(settings.copy(trafficWarmupEnabled = !settings.trafficWarmupEnabled))
        },
    )
    AnimatedVisibility(
        visible = settings.trafficWarmupEnabled,
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.lg))
                WhiteDnsTextField(
                    label = "Search",
                    value = query,
                    onValueChange = { query = it },
                    placeholder = searchPlaceholder,
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

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
            .semantics {
                contentDescription = context.getString(R.string.cd_select_profile_item, item.title)
            }
            .clickable {
                haptic.performLight()
                onClick()
            }
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
    val serverRoute = selectedConnectionProfile.customServerDomain.ifBlank { "Server route missing" }
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
            iconContentDescription = stringResource(
                if (connectionIssue == null) R.string.cd_icon_link else R.string.cd_icon_warning
            ),
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
            iconContentDescription = stringResource(
                if (resolverIssue == null) R.string.cd_icon_check else R.string.cd_icon_warning
            ),
            label = "Resolvers",
            value = resolverSource,
            detail = resolverDetail,
            color = if (resolverIssue == null) WhiteDnsPalette.Success else WhiteDnsPalette.WarningText,
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
        SetupActionButton(
            label = "Add Connection",
            supportingText = "Server domain and key",
            icon = Icons.Rounded.Add,
            iconContentDescription = stringResource(R.string.cd_icon_add),
            emphasized = true,
            enabled = actionsEnabled,
            onClick = onAddConnectionClick,
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        SetupActionButton(
            label = "Add Resolver Profile",
            supportingText = "DNS resolver list",
            icon = Icons.Rounded.Add,
            iconContentDescription = stringResource(R.string.cd_icon_add),
            emphasized = false,
            enabled = actionsEnabled,
            onClick = onAddResolverProfileClick,
        )
    }
}

@Composable
private fun SetupInfoRow(
    icon: ImageVector,
    iconContentDescription: String,
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
                contentDescription = iconContentDescription,
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
    iconContentDescription: String,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticFeedback()
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
            .clickable(enabled = enabled) {
                haptic.performMedium()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
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

private data class ProfileTopAction(
    val label: String,
    val emphasized: Boolean,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun ProfileTopActionGrid(actions: List<ProfileTopAction>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
    ) {
        actions.chunked(2).forEach { rowActions ->
            if (rowActions.size == 1) {
                val action = rowActions.first()
                ResolverActionButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    label = action.label,
                    emphasized = action.emphasized,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowActions.forEach { action ->
                        ResolverActionButton(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp),
                            label = action.label,
                            emphasized = action.emphasized,
                            enabled = action.enabled,
                            onClick = action.onClick,
                        )
                    }
                }
            }
        }
    }
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
    var showDeleteDuplicatesDialog by remember { mutableStateOf(false) }
    var deleteProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var draggedProfileId by remember { mutableStateOf<String?>(null) }
    var dragStartIndex by remember { mutableStateOf(0) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var measuredItemHeightPx by remember { mutableStateOf(0) }
    val canManageProfiles = connectionStatus != ConnectionStatus.CONNECTING
    val duplicateProfileCount = settings.duplicateConnectionProfileCount()
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

    ProfileTopActionGrid(
        actions = listOf(
            ProfileTopAction(
                label = "CREATE",
                emphasized = true,
                enabled = canManageProfiles,
                onClick = {
                    showCreateDialog = true
                },
            ),
            ProfileTopAction(
                label = "IMPORT",
                emphasized = false,
                enabled = canManageProfiles,
                onClick = {
                    showImportDialog = true
                },
            ),
            ProfileTopAction(
                label = "DELETE DUPS",
                emphasized = false,
                enabled = canManageProfiles && duplicateProfileCount > 0,
                onClick = {
                    showDeleteDuplicatesDialog = true
                },
            ),
            ProfileTopAction(
                label = "EXPORT ALL",
                emphasized = false,
                enabled = customProfiles.any {
                    it.customServerDomain.isNotBlank() && it.customServerEncryptionKey.isNotBlank()
                },
                onClick = {
                    showExportAllDialog = true
                },
            ),
        )
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
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    }
    customProfiles.forEachIndexed { index, profile ->
        val isActive = profile.id == activeConnectionProfileId &&
            connectionStatus != ConnectionStatus.DISCONNECTED
        val canEdit = canManageProfiles
        val canDelete = canManageProfiles && !isActive
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
                        deleteProfile = profile
                    }
                },
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
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

    if (showDeleteDuplicatesDialog) {
        DeleteProfileConfirmationDialog(
            title = "DELETE DUPLICATE CONNECTIONS",
            message = "Delete $duplicateProfileCount duplicate connection profile" +
                if (duplicateProfileCount == 1) {
                    "? Duplicates are matched by server domain and encryption key. The active or selected profile is kept when possible."
                } else {
                    "s? Duplicates are matched by server domain and encryption key. The active or selected profile is kept when possible."
                },
            confirmLabel = "DELETE",
            onDismiss = { showDeleteDuplicatesDialog = false },
            onConfirm = {
                if (canManageProfiles && duplicateProfileCount > 0) {
                    onSettingsChange(settings.deleteDuplicateConnectionProfiles(activeConnectionProfileId))
                }
                showDeleteDuplicatesDialog = false
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

    deleteProfile?.let { profile ->
        val profileIsActive = profile.id == activeConnectionProfileId &&
            connectionStatus != ConnectionStatus.DISCONNECTED
        DeleteProfileConfirmationDialog(
            title = "DELETE CONNECTION PROFILE",
            message = "Delete \"${profile.name}\"? This removes the saved connection profile and cannot be undone.",
            confirmLabel = "DELETE",
            onDismiss = { deleteProfile = null },
            onConfirm = {
                if (canManageProfiles && !profileIsActive) {
                    onSettingsChange(settings.deleteConnectionProfile(profile.id))
                }
                deleteProfile = null
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
    var deleteProfile by remember { mutableStateOf<ResolverProfile?>(null) }
    val canChangeProfiles = connectionStatus != ConnectionStatus.CONNECTING
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

    ProfileTopActionGrid(
        actions = listOf(
            ProfileTopAction(
                label = "CREATE",
                emphasized = true,
                enabled = canChangeProfiles,
                onClick = { showCreateDialog = true },
            ),
            ProfileTopAction(
                label = "SAVE CURRENT",
                emphasized = false,
                enabled = canChangeProfiles && settings.resolverText.isNotBlank(),
                onClick = {
                    showCreateDialog = true
                },
            ),
        )
    )

    SectionDivider()
    if (profiles.isEmpty()) {
        Text(
            text = "No saved resolver lists yet.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    }
    profiles.forEachIndexed { index, profile ->
        val isDefaultProfile = profile.id == ResolverProfile.DefaultId
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
                selected = profile.id == selectedProfile?.id,
                canUse = canChangeProfiles,
                canEdit = canChangeProfiles && !isDefaultProfile,
                canDelete = canChangeProfiles && !isDefaultProfile,
                canDrag = canChangeProfiles &&
                    !isDefaultProfile &&
                    profiles.count { it.id != ResolverProfile.DefaultId } > 1,
                dragging = isDragging,
                onUse = {
                    if (canChangeProfiles) {
                        onSettingsChange(settings.applyResolverProfileToSelectedConnection(profile.id))
                    }
                },
                onDragStart = {
                    if (canChangeProfiles && !isDefaultProfile && profiles.size > 1) {
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
                    if (canChangeProfiles && !isDefaultProfile) {
                        deleteProfile = profile
                    }
                },
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
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

    deleteProfile?.let { profile ->
        DeleteProfileConfirmationDialog(
            title = "DELETE RESOLVER PROFILE",
            message = "Delete \"${profile.name}\"? This removes the saved resolver list and cannot be undone.",
            confirmLabel = "DELETE",
            onDismiss = { deleteProfile = null },
            onConfirm = {
                if (canChangeProfiles) {
                    onSettingsChange(settings.deleteResolverProfile(profile.id))
                }
                deleteProfile = null
            },
        )
    }
}

@Composable
private fun SettingProfilesSettings(
    settings: WhiteDnsSettings,
    connectionStatus: ConnectionStatus,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val profiles = settings.normalizedAdvancedProfiles()
    val defaultProfile = profiles.first()
    val customProfiles = profiles.filter { it.id != AdvancedSettingsProfile.DefaultId }
    val selectedProfile = settings.selectedAdvancedProfile()
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var editProfile by remember { mutableStateOf<AdvancedSettingsProfile?>(null) }
    var exportProfile by remember { mutableStateOf<AdvancedSettingsProfile?>(null) }
    var deleteProfile by remember { mutableStateOf<AdvancedSettingsProfile?>(null) }
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
            onSettingsChange(settings.moveAdvancedProfileToIndex(profileId, targetIndex))
        }
    }

    ProfileTopActionGrid(
        actions = listOf(
            ProfileTopAction(
                label = "CREATE",
                emphasized = true,
                enabled = canManageProfiles,
                onClick = { showCreateDialog = true },
            ),
            ProfileTopAction(
                label = "IMPORT",
                emphasized = false,
                enabled = canManageProfiles,
                onClick = { showImportDialog = true },
            ),
        )
    )

    SectionDivider()
    GroupLabel("Default")
    SettingProfileRow(
        profile = defaultProfile,
        selected = defaultProfile.id == selectedProfile.id,
        dirty = defaultProfile.id == selectedProfile.id && !settings.matchesAdvancedProfile(defaultProfile),
        canUse = canManageProfiles,
        canEdit = false,
        canDelete = false,
        canDrag = false,
        dragging = false,
        onUse = {
            if (canManageProfiles) {
                onSettingsChange(settings.selectAdvancedProfile(defaultProfile.id))
            }
        },
        onDragStart = {},
        onDrag = {},
        onDragEnd = {},
        onDragCancel = {},
        onExport = { exportProfile = defaultProfile },
        onEdit = {},
        onDelete = {},
    )
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))

    GroupLabel("Custom Settings")
    if (customProfiles.isEmpty()) {
        Text(
            text = "No saved setting profiles yet.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    }
    customProfiles.forEachIndexed { index, profile ->
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
            label = "advancedProfileDragTranslation",
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
            SettingProfileRow(
                profile = profile,
                selected = profile.id == selectedProfile.id,
                dirty = profile.id == selectedProfile.id && !settings.matchesAdvancedProfile(profile),
                canUse = canManageProfiles,
                canEdit = canManageProfiles,
                canDelete = canManageProfiles,
                canDrag = canManageProfiles && customProfiles.size > 1,
                dragging = isDragging,
                onUse = {
                    if (canManageProfiles) {
                        onSettingsChange(settings.selectAdvancedProfile(profile.id))
                    }
                },
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
                onExport = { exportProfile = profile },
                onEdit = { editProfile = profile },
                onDelete = {
                    if (canManageProfiles) {
                        deleteProfile = profile
                    }
                },
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        }
    }

    if (showCreateDialog) {
        AdvancedSettingsProfileDialog(
            profile = null,
            initialName = advancedSaveAsInitialName(selectedProfile),
            initialSettings = settings,
            onDismiss = { showCreateDialog = false },
            onSave = { profile ->
                onSettingsChange(settings.upsertAdvancedProfile(profile))
                showCreateDialog = false
            },
        )
    }

    if (showImportDialog) {
        AdvancedSettingsImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { profileName, toml ->
                runCatching {
                    settings.importAdvancedSettingsProfileFromToml(profileName, toml)
                }.onSuccess { importedSettings ->
                    onSettingsChange(importedSettings)
                    showImportDialog = false
                }
            },
        )
    }

    editProfile?.let { profile ->
        AdvancedSettingsProfileDialog(
            profile = profile,
            initialName = profile.name,
            initialSettings = settings.applyAdvancedProfile(profile),
            onDismiss = { editProfile = null },
            onSave = { updatedProfile ->
                onSettingsChange(settings.upsertAdvancedProfile(updatedProfile.copy(id = profile.id)))
                editProfile = null
            },
        )
    }

    exportProfile?.let { profile ->
        ConnectionProfileExportDialog(
            title = "EXPORT SETTINGS",
            fieldLabel = "advanced_settings.toml",
            placeholder = "advanced_settings.toml",
            showQr = false,
            linkResult = remember(settings, profile) {
                runCatching {
                    StormDnsConfigRenderer.renderAdvancedSettingsToml(settings.applyAdvancedProfile(profile))
                }
            },
            onDismiss = { exportProfile = null },
            onShare = { toml ->
                shareAdvancedSettingsToml(context, toml)
            },
        )
    }

    deleteProfile?.let { profile ->
        DeleteProfileConfirmationDialog(
            title = "DELETE SETTING PROFILE",
            message = "Delete \"${profile.name}\"? This removes the saved setting profile and cannot be undone.",
            confirmLabel = "DELETE",
            onDismiss = { deleteProfile = null },
            onConfirm = {
                if (canManageProfiles) {
                    onSettingsChange(settings.deleteAdvancedProfile(profile.id))
                }
                deleteProfile = null
            },
        )
    }
}

@Composable
private fun AdvancedSettingsProfileDialog(
    profile: AdvancedSettingsProfile?,
    initialName: String,
    initialSettings: WhiteDnsSettings,
    onDismiss: () -> Unit,
    onSave: (AdvancedSettingsProfile) -> Unit,
) {
    var name by remember(profile?.id) { mutableStateOf(initialName) }
    var draftSettings by remember(profile?.id) { mutableStateOf(initialSettings) }
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
                text = if (profile == null) "CREATE SETTING PROFILE" else "EDIT SETTING PROFILE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Fast tunnel",
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                AdvancedSettingsFields(
                    settings = draftSettings,
                    showProxySettings = true,
                    onSettingsChange = { draftSettings = it },
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                            AdvancedSettingsProfile.fromSettings(
                                settings = draftSettings,
                                id = profile?.id.orEmpty(),
                                name = name.trim(),
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingProfileRow(
    profile: AdvancedSettingsProfile,
    selected: Boolean,
    dirty: Boolean,
    canUse: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    canDrag: Boolean,
    dragging: Boolean,
    onUse: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val summarySuffix = when {
        dirty -> " - MODIFIED"
        selected -> " - SELECTED"
        else -> ""
    }
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = "${advancedProfileSummary(profile)}$summarySuffix",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = when {
                            dirty -> WhiteDnsPalette.WarningText
                            selected -> WhiteDnsPalette.AccentText
                            else -> WhiteDnsPalette.Muted
                        },
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileActionsMenu(
                contentDescription = "Setting profile actions",
                actions = listOf(
                    ProfileMenuAction(
                        label = if (selected) "Use profile (selected)" else "Use profile",
                        icon = Icons.Rounded.Check,
                        contentDescription = "Use setting profile",
                        enabled = canUse,
                        onClick = onUse,
                    ),
                    ProfileMenuAction(
                        label = "Export profile",
                        icon = Icons.Rounded.Link,
                        contentDescription = "Export setting profile",
                        enabled = true,
                        onClick = onExport,
                    ),
                    ProfileMenuAction(
                        label = "Edit profile",
                        icon = Icons.Rounded.Edit,
                        contentDescription = "Edit setting profile",
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = "Delete profile",
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete setting profile",
                        enabled = canDelete,
                        onClick = onDelete,
                    ),
                ),
            )
        }
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = if (validationMessageIsError) WhiteDnsPalette.Error else WhiteDnsPalette.Muted,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
    canUse: Boolean,
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
            ProfileActionsMenu(
                contentDescription = "Resolver profile actions",
                actions = listOf(
                    ProfileMenuAction(
                        label = if (selected) "Use profile (selected)" else "Use profile",
                        icon = Icons.Rounded.Check,
                        contentDescription = "Use resolver profile",
                        enabled = canUse,
                        onClick = onUse,
                    ),
                    ProfileMenuAction(
                        label = "Edit profile",
                        icon = Icons.Rounded.Edit,
                        contentDescription = "Edit resolver profile",
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = "Delete profile",
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete resolver profile",
                        enabled = canDelete,
                        onClick = onDelete,
                    ),
                ),
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
    val context = LocalContext.current

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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            val link = linkResult.getOrNull()
            if (link != null) {
                if (showQr && !link.contains('\n')) {
                    ProfileQrPreview(link = link)
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                            copyTextToClipboard(
                                context = context,
                                label = fieldLabel,
                                text = link,
                                sensitive = true,
                            )
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
    val canSave = name.isNotBlank() && domain.isNotBlank() && encryptionKey.isNotBlank()

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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "My StormDNS",
            )
            WhiteDnsTextField(
                label = "Domain",
                value = domain,
                onValueChange = { domain = it.trim() },
                placeholder = "v.example.com",
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                                customServerDomain = domain.trim().trimEnd('.'),
                                customServerEncryptionKey = encryptionKey.trim(),
                                customServerEncryptionMethod = encryptionMethod,
                                resolverProfileId = profile?.resolverProfileId.orEmpty(),
                                connectionMode = profile?.connectionMode ?: "proxy",
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
            ProfileActionsMenu(
                contentDescription = "Connection profile actions",
                actions = listOf(
                    ProfileMenuAction(
                        label = "Export profile",
                        icon = Icons.Rounded.Link,
                        contentDescription = "Export connection profile",
                        enabled = profile.customServerDomain.isNotBlank() && profile.customServerEncryptionKey.isNotBlank(),
                        onClick = onExport,
                    ),
                    ProfileMenuAction(
                        label = if (selected) "Edit profile (selected)" else "Edit profile",
                        icon = Icons.Rounded.Edit,
                        contentDescription = "Edit connection profile",
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = if (active) "Delete profile (active)" else "Delete profile",
                        icon = Icons.Rounded.Delete,
                        contentDescription = if (active) "Connected profile cannot be deleted" else "Delete connection profile",
                        enabled = canDelete,
                        onClick = onDelete,
                    ),
                ),
            )
        }
    }
}

private data class ProfileMenuAction(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun ProfileActionsMenu(
    contentDescription: String,
    actions: List<ProfileMenuAction>,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val enabled = actions.any { it.enabled }
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        expanded -> WhiteDnsPalette.AccentSurface
        else -> WhiteDnsPalette.Surface
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        expanded -> WhiteDnsPalette.Accent.copy(alpha = 0.28f)
        else -> WhiteDnsPalette.Border
    }
    val iconColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        expanded -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Box {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(background)
                .border(1.5.dp, border, RoundedCornerShape(10.dp))
                .clickable(enabled = enabled) {
                    haptic.performLight()
                    expanded = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 196.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WhiteDnsPalette.DropdownSurface),
        ) {
            actions.forEach { action ->
                val itemColor = if (action.enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    enabled = action.enabled,
                    leadingIcon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            tint = itemColor,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    text = {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = itemColor,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    },
                    onClick = {
                        haptic.performLight()
                        expanded = false
                        action.onClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun DeleteProfileConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = WhiteDnsPalette.Description,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                    label = confirmLabel,
                    emphasized = true,
                    enabled = true,
                    onClick = onConfirm,
                )
            }
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
    val haptic = rememberHapticFeedback()
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
                        haptic.performHeavy()
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
    val haptic = rememberHapticFeedback()
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
            .clickable(enabled = enabled) {
                haptic.performMedium()
                onClick()
            },
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
    tone: CompactActionTone = if (emphasized) CompactActionTone.Accent else CompactActionTone.Default,
) {
    val haptic = rememberHapticFeedback()
    val background = when {
        !enabled -> WhiteDnsPalette.SurfaceAlt
        tone == CompactActionTone.Accent -> WhiteDnsPalette.Accent
        tone == CompactActionTone.Success -> WhiteDnsPalette.Success
        tone == CompactActionTone.Danger -> WhiteDnsPalette.Error
        else -> WhiteDnsPalette.Surface
    }
    val border = when {
        !enabled -> WhiteDnsPalette.Divider
        tone == CompactActionTone.Accent -> WhiteDnsPalette.AccentPressed
        tone == CompactActionTone.Success -> WhiteDnsPalette.SuccessSurface
        tone == CompactActionTone.Danger -> WhiteDnsPalette.ErrorSurface
        else -> WhiteDnsPalette.Border
    }
    val textColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        tone == CompactActionTone.Default -> WhiteDnsPalette.Muted
        else -> WhiteDnsPalette.OnAccent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .border(1.5.dp, border, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled) {
                haptic.performMedium()
                onClick()
            }
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
            label = "Tunnel Packet Timeout",
            value = settings.tunnelPacketTimeoutSeconds,
            onValueChange = {
                onSettingsChange(settings.copy(tunnelPacketTimeoutSeconds = filterDecimalInput(it)))
            },
            placeholder = "10.0",
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
private fun HeaderCard(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    var overflowExpanded by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    val appVersion = remember(context.packageName) { appVersionName(context) }

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
                    .semantics { contentDescription = context.getString(R.string.cd_logo_telegram) }
                    .clickable {
                        haptic.performLight()
                        openWhiteDnsTelegram(context)
                    },
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

        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (overflowExpanded) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface,
                    )
                    .border(
                        1.5.dp,
                        if (overflowExpanded) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                        RoundedCornerShape(10.dp),
                    )
                    .semantics { contentDescription = context.getString(R.string.cd_menu_button) }
                    .clickable {
                        haptic.performLight()
                        overflowExpanded = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "App menu",
                    tint = if (overflowExpanded) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    modifier = Modifier.size(22.dp),
                )
            }

            DropdownMenu(
                expanded = overflowExpanded,
                onDismissRequest = { overflowExpanded = false },
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(WhiteDnsPalette.DropdownSurface),
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    enabled = false,
                    text = {
                        Column {
                            Text(
                                text = "Version",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 10.sp,
                                    color = WhiteDnsPalette.Muted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.7.sp,
                                ),
                            )
                            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                            Text(
                                text = "v${appVersion.ifBlank { "unknown" }}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = WhiteDnsPalette.Ink,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    },
                    onClick = {},
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(1.5.dp)
                        .background(WhiteDnsPalette.Divider),
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "App settings",
                            tint = WhiteDnsPalette.Ink,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    text = {
                        Text(
                            text = "App Settings",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = WhiteDnsPalette.Ink,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    },
                    onClick = {
                        haptic.performLight()
                        overflowExpanded = false
                        showAppSettingsDialog = true
                    },
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Donate",
                            tint = WhiteDnsPalette.Ink,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    text = {
                        Text(
                            text = "Donate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = WhiteDnsPalette.Ink,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    },
                    onClick = {
                        haptic.performLight()
                        overflowExpanded = false
                        showDonationDialog = true
                    },
                )
            }
        }
    }

    if (showAppSettingsDialog) {
        AppSettingsDialog(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onDismiss = { showAppSettingsDialog = false },
        )
    }
    if (showDonationDialog) {
        DonationDialog(onDismiss = { showDonationDialog = false })
    }
}

private fun appVersionName(context: Context): String {
    return runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty()
}

private fun openWhiteDnsTelegram(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(WhiteDnsTelegramUrl),
    )
    context.startActivity(intent)
}

@Composable
private fun AppSettingsDialog(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
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
                text = "APP SETTINGS",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            ThemeModeSegmentedControl(
                selectedMode = themeMode,
                onModeChange = onThemeModeChange,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.lg))
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
private fun DonationDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Text(
                text = "Donations will be used for new servers and app development.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = WhiteDnsPalette.Description,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            DonationWallets.forEachIndexed { index, wallet ->
                DonationWalletField(
                    label = wallet.label,
                    address = wallet.address,
                    onCopy = {
                        copyTextToClipboard(
                            context = context,
                            label = wallet.label,
                            text = wallet.address,
                            sensitive = false,
                        )
                    },
                )
                if (index != DonationWallets.lastIndex) {
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.lg))
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
    val context = LocalContext.current
    Column {
        FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Input)
                .border(2.5.dp, WhiteDnsPalette.Divider, RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_copy_address, address) }
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
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

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
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
        Text(
            text = "Enable WhiteDNS notifications so Android can keep the full VPN service visible and running in the background.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = WhiteDnsPalette.WarningText,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_enable_vpn_notification) }
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
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
private fun BatteryOptimizationBanner(
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "BACKGROUND VPN MAY STOP",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.WarningText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                ),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performLight()
                        onDismiss()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss battery optimization warning",
                    tint = WhiteDnsPalette.WarningText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
        Text(
            text = "Allow WhiteDNS to ignore battery optimization so the VPN keeps running after you leave the app.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = WhiteDnsPalette.WarningText,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_allow_background_vpn) }
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
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
    val haptic = rememberHapticFeedback()

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
            contentDescription = stringResource(R.string.cd_icon_warning),
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
                .clickable {
                    haptic.performLight()
                    onDismiss()
                },
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
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
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
                        label = "SELECT APPS",
                        emphasized = true,
                        enabled = apps.isNotEmpty(),
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Search",
                value = query,
                onValueChange = { query = it },
                placeholder = "App name or package",
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (visibleApps.isEmpty()) {
                    Text(
                        text = "No apps found.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            color = WhiteDnsPalette.Muted,
                        ),
                    )
                } else {
                    visibleApps.forEach { app ->
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable {
                haptic.performLight()
                onToggle()
            }
            .padding(vertical = 9.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                haptic.performLight()
                onToggle()
            },
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
    val haptic = rememberHapticFeedback()
    val ringColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Divider
            ConnectionStatus.CONNECTING -> WhiteDnsPalette.AccentPressed
            ConnectionStatus.CONNECTED -> WhiteDnsPalette.Success
        },
        animationSpec = tween(400),
        label = "connectRingColor",
    )
    val iconColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Disabled
            ConnectionStatus.CONNECTING -> WhiteDnsPalette.AccentPressed
            ConnectionStatus.CONNECTED -> WhiteDnsPalette.WarningText
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
        },
        animationSpec = tween(300),
        label = "connectProgressFraction",
    )
    val accentColor = WhiteDnsPalette.Accent
    val borderColor = WhiteDnsPalette.Border
    val successColor = WhiteDnsPalette.Success
    val circleSize = 156.dp
    val outerRingSize = 198.dp
    val label = when (status) {
        ConnectionStatus.DISCONNECTED -> "CONNECT"
        ConnectionStatus.CONNECTING -> "CONNECTING"
        ConnectionStatus.CONNECTED -> "STOP"
    }
    val labelColor = when (status) {
        ConnectionStatus.CONNECTED -> WhiteDnsPalette.WarningText
        ConnectionStatus.DISCONNECTED -> if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Disabled
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
                    accentColor.copy(alpha = pulseAlpha)
                } else {
                    ringColor.copy(alpha = if (status == ConnectionStatus.CONNECTED) 0.30f else 0.15f)
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
                    ConnectionStatus.CONNECTING -> {
                        drawArc(
                            color = borderColor.copy(alpha = 0.65f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth),
                        )
                        drawArc(
                            color = accentColor,
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
                                color = accentColor.copy(alpha = 0.22f),
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
                            color = successColor,
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performMedium()
                            onClick()
                        },
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
                        contentDescription = stringResource(
                            when (status) {
                                ConnectionStatus.DISCONNECTED -> R.string.cd_connect_button_disconnected
                                ConnectionStatus.CONNECTING -> R.string.cd_connect_button_connecting
                                ConnectionStatus.CONNECTED -> R.string.cd_connect_button_connected
                            }
                        ),
                        tint = iconColor,
                        modifier = Modifier.size(if (status == ConnectionStatus.CONNECTED) 30.dp else 34.dp),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                        ),
                    )
                    if (status == ConnectionStatus.CONNECTING) {
                        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                        Text(
                            text = progressState.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                        Text(
                            text = "${progressState.percent.coerceIn(0, 99)}%",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WhiteDnsPalette.Accent,
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
                // Enhanced progress indicator with percentage display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(20.dp)
                ) {
                    if (progressState.total > 0 && progressState.completed > 0) {
                        // Determinate progress when total is known
                        CircularProgressIndicator(
                            progress = { progressState.completed.toFloat() / progressState.total.toFloat() },
                            modifier = Modifier.size(20.dp),
                            color = WhiteDnsPalette.Accent,
                            strokeWidth = 2.5.dp,
                        )
                        // Show percentage inside circle
                        Text(
                            text = "${(progressState.completed * 100 / progressState.total)}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 7.sp,
                                color = WhiteDnsPalette.Accent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        // Indeterminate progress when total is unknown
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = WhiteDnsPalette.Accent,
                            strokeWidth = 2.5.dp,
                        )
                    }
                }
                Text(
                    text = if (progressState.completed > 0) {
                        "Background scanning ${progressState.completed}/${progressState.total}"
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
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
            contentDescription = stringResource(
                when (verification.status) {
                    ConnectionVerificationStatus.Verified -> R.string.cd_verification_success
                    ConnectionVerificationStatus.Failed -> R.string.cd_verification_failed
                    ConnectionVerificationStatus.Checking -> R.string.cd_verification_pending
                    else -> R.string.cd_icon_link
                }
            ),
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
    val context = LocalContext.current
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
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = "Resolvers",
                value = resolverText,
                onValueChange = {},
                placeholder = "No resolvers",
                singleLine = false,
                minLines = 6,
                maxLines = 12,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
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
                        copyTextToClipboard(
                            context = context,
                            label = "WhiteDNS resolvers",
                            text = resolverText,
                            sensitive = false,
                        )
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
            iconContentDescription = stringResource(R.string.cd_icon_download),
            label = "Down",
            value = formatDataSpeed(stats.downloadSpeedBytesPerSecond),
            modifier = Modifier.weight(1f),
        )
        SpeedIndicator(
            icon = Icons.Filled.Upload,
            iconContentDescription = stringResource(R.string.cd_icon_upload),
            label = "Up",
            value = formatDataSpeed(stats.uploadSpeedBytesPerSecond),
            modifier = Modifier.weight(1f),
        )
        SpeedIndicator(
            icon = Icons.Filled.DataUsage,
            iconContentDescription = stringResource(R.string.cd_icon_data_usage),
            label = "Total Usage",
            value = formatDataSize(stats.totalDataUsageBytes),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SpeedIndicator(
    icon: ImageVector,
    iconContentDescription: String,
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
            contentDescription = iconContentDescription,
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
    autoTuneResults: List<AutoTuneTrialResult>,
    onSaveAutoTuneResult: (AutoTuneTrialResult) -> Unit,
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
        if (autoTuneResults.isNotEmpty()) {
            AutoTuneResultsSection(
                results = autoTuneResults,
                onSaveResult = onSaveAutoTuneResult,
            )
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        CompactMetricRow(
            metrics = listOf(
                CompactMetric(
                    icon = Icons.Filled.Apps,
                    iconContentDescription = stringResource(R.string.cd_icon_apps),
                    label = "Apps",
                    value = stats.connectedApps.toString(),
                ),
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        InfoRow(label = "Connection Profile", value = connectionProfileName)
        InfoRow(label = "Resolver Profile", value = resolverProfileName)
        InfoRow(label = "Setting Profile", value = settingProfileName)
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        ResolverActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "DOWNLOAD TOML",
            emphasized = false,
            enabled = canDownloadToml,
            onClick = onDownloadToml,
        )
    }
}

@Composable
private fun AutoTuneResultsSection(
    results: List<AutoTuneTrialResult>,
    onSaveResult: (AutoTuneTrialResult) -> Unit,
) {
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
    GroupLabel("Parallel Test Results")
    results.forEachIndexed { index, result ->
        AutoTuneResultRow(
            result = result,
            onSaveResult = onSaveResult,
        )
        if (index < results.lastIndex) {
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(WhiteDnsPalette.Divider),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        }
    }
}

@Composable
private fun AutoTuneResultRow(
    result: AutoTuneTrialResult,
    onSaveResult: (AutoTuneTrialResult) -> Unit,
) {
    val inProgress = result.isAutoTuneInProgress()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (result.selected) WhiteDnsPalette.AccentSurface else Color.Transparent)
            .border(
                1.5.dp,
                if (result.selected) WhiteDnsPalette.Accent.copy(alpha = 0.18f) else Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = autoTuneResultLabel(result),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (inProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = WhiteDnsPalette.Accent,
                    strokeWidth = 2.dp,
                )
            }
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            AutoTuneMetricPill(
                metric = autoTuneMtuMetric(result),
                modifier = Modifier.weight(1f),
            )
            AutoTuneMetricPill(
                metric = AutoTuneDisplayMetric(
                    icon = Icons.Rounded.Speed,
                    iconContentDescription = "Parallel Test speed",
                    label = "Speed",
                    value = autoTuneSpeedValue(result),
                    color = WhiteDnsPalette.Success,
                    surface = WhiteDnsPalette.SuccessSurface,
                    loading = result.status == "measuring",
                ),
                modifier = Modifier.weight(1f),
            )
            AutoTuneMetricPill(
                metric = AutoTuneDisplayMetric(
                    icon = Icons.Rounded.NetworkPing,
                    iconContentDescription = "Parallel Test ping",
                    label = "Ping",
                    value = result.pingMillis?.let { "${it}ms" } ?: "-",
                    color = WhiteDnsPalette.AccentText,
                    surface = WhiteDnsPalette.AccentSurface,
                    loading = false,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        autoTuneStatusMessage(result)?.let { message ->
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = if (result.status == "failed") WhiteDnsPalette.WarningText else WhiteDnsPalette.Muted,
                    fontWeight = if (result.status == "failed") FontWeight.Medium else FontWeight.Normal,
                ),
            )
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        CompactActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "SAVE SETTING AS",
            emphasized = result.selected,
            enabled = true,
            onClick = { onSaveResult(result) },
        )
    }
}

@Composable
private fun AutoTuneMetricPill(
    metric: AutoTuneDisplayMetric,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(metric.surface)
            .border(1.5.dp, metric.color.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (metric.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = metric.color,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = metric.icon,
                contentDescription = metric.iconContentDescription,
                tint = metric.color,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 7.sp,
                    letterSpacing = 0.5.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Text(
                text = metric.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteDnsPalette.Ink,
                ),
            )
        }
    }
}

private data class AutoTuneDisplayMetric(
    val icon: ImageVector,
    val iconContentDescription: String,
    val label: String,
    val value: String,
    val color: Color,
    val surface: Color,
    val loading: Boolean = false,
)

@Composable
private fun autoTuneMtuMetric(result: AutoTuneTrialResult): AutoTuneDisplayMetric {
    return when (result.status) {
        "failed" -> AutoTuneDisplayMetric(
            icon = Icons.Rounded.Close,
            iconContentDescription = "MTU failed",
            label = "MTU",
            value = "Fail",
            color = WhiteDnsPalette.Error,
            surface = WhiteDnsPalette.ErrorSurface,
        )
        "listening", "measuring", "ready" -> AutoTuneDisplayMetric(
            icon = Icons.Rounded.Check,
            iconContentDescription = "MTU passed",
            label = "MTU",
            value = "Pass",
            color = WhiteDnsPalette.Success,
            surface = WhiteDnsPalette.SuccessSurface,
        )
        else -> AutoTuneDisplayMetric(
            icon = Icons.Rounded.Tune,
            iconContentDescription = "MTU testing",
            label = "MTU",
            value = "Test",
            color = WhiteDnsPalette.AccentText,
            surface = WhiteDnsPalette.AccentSurface,
            loading = true,
        )
    }
}

private fun autoTuneResultLabel(result: AutoTuneTrialResult): String {
    return if (result.selected) {
        "${result.label} - Selected"
    } else {
        result.label
    }
}

private fun autoTuneSpeedValue(result: AutoTuneTrialResult): String {
    return if (result.speedBytesPerSecond > 0L) {
        formatDataSpeed(result.speedBytesPerSecond)
    } else {
        "-"
    }
}

private fun autoTuneStatusMessage(result: AutoTuneTrialResult): String? {
    return when (result.status) {
        "starting" -> "Starting test"
        "listening" -> "Measuring speed"
        "measuring" -> "Measuring speed"
        "failed" -> result.message.ifBlank { "Failed" }
        else -> result.message.takeIf { it.isNotBlank() && it != "Measured" }
    }
}

private fun AutoTuneTrialResult.isAutoTuneInProgress(): Boolean {
    return status == "pending" || status == "starting" || status == "listening" || status == "measuring"
}

private data class CompactMetric(
    val icon: ImageVector,
    val iconContentDescription: String,
    val label: String,
    val value: String,
)

private data class SplitTunnelAppInfo(
    val packageName: String,
    val label: String,
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
            contentDescription = metric.iconContentDescription,
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
    multilineValue: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = if (multilineValue) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween,
        verticalAlignment = if (multilineValue) Alignment.Top else Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = if (multilineValue) Modifier.width(72.dp) else Modifier,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Text(
            text = value,
            modifier = if (multilineValue) Modifier.weight(1f) else Modifier,
            maxLines = if (multilineValue) Int.MAX_VALUE else 1,
            overflow = if (multilineValue) TextOverflow.Clip else TextOverflow.Ellipsis,
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
                        copyTextToClipboard(
                            context = context,
                            label = "WhiteDNS logs",
                            text = logs.joinToString(separator = "\n"),
                            sensitive = false,
                        )
                    },
                )
                LogActionButton(
                    label = "DIAGNOSTICS",
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            label = "WhiteDNS diagnostics",
                            text = buildDiagnosticsText(context, uiState),
                            sensitive = false,
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
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
    val haptic = rememberHapticFeedback()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(8.dp))
            .clickable {
                haptic.performLight()
                onClick()
            }
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
    shareTextExport(
        context = context,
        fileName = "whitedns-profile.txt",
        subject = "WhiteDNS profile",
        chooserTitle = "Export WhiteDNS profile",
        text = link,
    )
}

private fun shareClientConfigToml(context: Context, toml: String) {
    shareTextExport(
        context = context,
        fileName = "client_config.toml",
        subject = "client_config.toml",
        chooserTitle = "Export client_config.toml",
        text = toml,
    )
}

private fun shareAdvancedSettingsToml(context: Context, toml: String) {
    shareTextExport(
        context = context,
        fileName = "advanced_settings.toml",
        subject = "advanced_settings.toml",
        chooserTitle = "Export advanced_settings.toml",
        text = toml,
    )
}

private fun shareTextExport(
    context: Context,
    fileName: String,
    subject: String,
    chooserTitle: String,
    text: String,
) {
    val exportFile = writeCacheExportFile(
        context = context,
        fileName = fileName,
        text = text,
    )
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        exportFile,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun writeCacheExportFile(
    context: Context,
    fileName: String,
    text: String,
): File {
    val exportDir = File(context.cacheDir, CacheExportDirectory).apply {
        mkdirs()
    }
    cleanOldCacheExports(exportDir)
    val safeName = fileName
        .replace(Regex("""[^A-Za-z0-9._-]"""), "_")
        .ifBlank { "whitedns-export.txt" }
    val exportFile = File(exportDir, safeName)
    exportFile.writeText(text, Charsets.UTF_8)
    return exportFile
}

private fun cleanOldCacheExports(exportDir: File) {
    val now = System.currentTimeMillis()
    exportDir.listFiles()?.forEach { file ->
        if (file.isFile && now - file.lastModified() > CacheExportMaxAgeMillis) {
            runCatching { file.delete() }
        }
    }
}

private fun copyTextToClipboard(
    context: Context,
    label: String,
    text: String,
    sensitive: Boolean,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    val clip = ClipData.newPlainText(label, text)
    if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        clip.description.extras = (clip.description.extras ?: android.os.PersistableBundle()).apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)
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
    val appVersion = appVersionName(context)

    return buildString {
        appendLine("WhiteDNS diagnostics")
        appendLine("App version: ${appVersion.ifBlank { "unknown" }}")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("Status: ${uiState.connectionStatus}")
        appendLine("Mode: ${WhiteDnsOptions.connectionModeLabel(resolvedSettings.connectionMode)}")
        appendLine("Profile: ${selectedProfile.name.ifBlank { selectedProfile.id }}")
        appendLine("Server: ${selectedProfile.customServerDomain.ifBlank { "not configured" }}")
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
            appendLine(log)
        }
    }.trimEnd()
}

private fun readResolverTextFromUri(context: Context, uri: Uri): Result<String> {
    return runCatching {
        val rawText = readTextFromUri(context, uri, "Unable to open resolver file").getOrThrow()
        normalizeImportedResolverText(rawText)
    }
}

private fun readTextFromUri(
    context: Context,
    uri: Uri,
    errorMessage: String,
): Result<String> {
    return runCatching {
        context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { reader -> reader.readText() }
            ?: throw IllegalArgumentException(errorMessage)
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

private val SettingsImportMimeTypes = arrayOf(
    "text/*",
    "application/toml",
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
    val haptic = rememberHapticFeedback()
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
            .clickable(enabled = enabled) {
                haptic.performLight()
                onClick()
            }
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
    iconContentDescription: String,
    collapsible: Boolean = true,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = rememberHapticFeedback()
    val isOpen = expanded || !collapsible
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "sectionRotation",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isOpen) {
            WhiteDnsPalette.Accent.copy(alpha = 0.26f)
        } else {
            WhiteDnsPalette.Border
        },
        animationSpec = tween(220),
        label = "sectionBorderColor",
    )
    val iconBackground by animateColorAsState(
        targetValue = if (isOpen) {
            WhiteDnsPalette.AccentSurface
        } else {
            WhiteDnsPalette.SurfaceAlt
        },
        animationSpec = tween(220),
        label = "sectionIconBackground",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isOpen) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
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
                .let { modifier ->
                    if (collapsible) {
                        modifier.clickable {
                            haptic.performLight()
                            onToggle()
                        }
                    } else {
                        modifier
                    }
                }
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
                        contentDescription = iconContentDescription,
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
                    if (collapsible) {
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
            }
            if (collapsible) {
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
                        contentDescription = stringResource(
                            if (expanded) R.string.cd_advanced_settings_collapse else R.string.cd_advanced_settings_expand
                        ),
                        tint = if (expanded) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer(rotationZ = rotation),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isOpen,
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
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(WhiteDnsPalette.Divider, RoundedCornerShape(1.dp)),
    )
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
}

@Composable
private fun ToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performLight()
                onToggle()
            }
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
            onCheckedChange = {
                haptic.performLight()
                onToggle()
            },
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
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
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
    val haptic = rememberHapticFeedback()
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
                    .clickable(enabled = enabled) {
                        haptic.performLight()
                        expanded = true
                    }
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
                    contentDescription = stringResource(R.string.cd_dropdown_advanced_settings),
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
                                        contentDescription = stringResource(R.string.cd_icon_check),
                                        tint = WhiteDnsPalette.AccentText,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                        onClick = {
                            haptic.performLight()
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

@Suppress("DEPRECATION")
private fun loadSplitTunnelAppOptions(context: Context): List<SplitTunnelAppInfo> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(launcherIntent, 0)
        .asSequence()
        .mapNotNull { resolveInfo ->
            val appPackage = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            if (appPackage == context.packageName) {
                return@mapNotNull null
            }
            val label = resolveInfo.loadLabel(packageManager)
                .toString()
                .trim()
                .takeIf(String::isNotEmpty)
                ?: appPackage
            SplitTunnelAppInfo(
                packageName = appPackage,
                label = label,
            )
        }
        .distinctBy { it.packageName }
        .sortedWith(
            compareBy<SplitTunnelAppInfo> { it.label.lowercase(Locale.US) }
                .thenBy { it.packageName },
        )
        .toList()
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

// Haptic Feedback Utilities

/**
 * Remembers the haptic feedback instance from the current view.
 */
@Composable
private fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                when (hapticFeedbackType) {
                    HapticFeedbackType.LongPress -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    HapticFeedbackType.TextHandleMove -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
        }
    }
}

/**
 * Performs a light haptic feedback.
 * Use for: tab switches, toggle interactions, minor actions.
 */
private fun HapticFeedback.performLight() {
    performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

/**
 * Performs a medium haptic feedback.
 * Use for: button clicks, profile actions, important interactions.
 */
private fun HapticFeedback.performMedium() {
    performHapticFeedback(HapticFeedbackType.LongPress)
}

/**
 * Performs a heavy haptic feedback.
 * Use for: drag operations, destructive actions.
 */
private fun HapticFeedback.performHeavy() {
    performHapticFeedback(HapticFeedbackType.LongPress)
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

private const val CacheExportDirectory = "exports"
private const val CacheExportMaxAgeMillis = 60L * 60L * 1_000L
