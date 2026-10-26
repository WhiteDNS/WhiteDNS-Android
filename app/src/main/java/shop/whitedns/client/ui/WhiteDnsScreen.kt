package shop.whitedns.client.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.intl.Locale as ComposeLocale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import shop.whitedns.client.model.CottenDnsProfileSettings
import shop.whitedns.client.model.canBootstrapResolversWithCottenMax
import shop.whitedns.client.model.SoarProfileSettings
import shop.whitedns.client.model.isValidSoarServer
import shop.whitedns.client.model.DnsClientEngine
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.ResolverRuntimeState
import shop.whitedns.client.model.ResolverTextValidation
import shop.whitedns.client.model.ServerDomains
import shop.whitedns.client.model.ServerTestResult
import shop.whitedns.client.model.ServerTestState
import shop.whitedns.client.model.ServerTestStatus
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
import shop.whitedns.client.model.exportAllResolverProfilesText
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
import shop.whitedns.client.scan.ResolverReachability
import shop.whitedns.client.scan.ResolverReachabilityProbe
import shop.whitedns.client.scan.WhiteDnsScannerResultStore
import shop.whitedns.client.storm.CottenDnsSettingsRenderer
import shop.whitedns.client.storm.StormDnsConfigRenderer
import com.google.zxing.BinaryBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.FileProvider

@Composable
fun WhiteDnsScreen(
    uiState: WhiteDnsUiState,
    onBatteryOptimizationClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onScanFileSelected: (Uri) -> Unit,
    onScanDefaultListSelected: () -> Unit,
    onScanManualEntriesSelected: (String) -> Unit,
    onScanStartClick: () -> Unit,
    onScanConnectionProfileChange: (String) -> Unit,
    onScanWorkerCountChange: (String) -> Unit,
    onScanStopClick: () -> Unit,
    onScanResumeClick: () -> Unit,
    onServerTestClick: (String?) -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    onImportProfiles: suspend (String, Boolean) -> Result<Unit>,
    onRefreshSubscriptions: suspend () -> Result<Unit>,
) {
    var selectedTab by rememberSaveable { mutableStateOf(WhiteDnsTab.CONNECT) }
    var homeDismissRequest by rememberSaveable { mutableStateOf(0) }
    var profileCreateRequest by rememberSaveable { mutableStateOf<ProfileCreateRequest?>(null) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    val tabState = rememberSaveableStateHolder()
    val expandedLayout = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() } >= 600.dp
    val homeKeyboardVisible = selectedTab == WhiteDnsTab.CONNECT && WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = selectedTab != WhiteDnsTab.CONNECT) { selectedTab = WhiteDnsTab.CONNECT }

    fun selectTab(tab: WhiteDnsTab) {
        if (selectedTab == WhiteDnsTab.CONNECT) {
            homeDismissRequest++
            profileCreateRequest = null
            showAppSettingsDialog = false
            showDonationDialog = false
        }
        selectedTab = tab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
              .whiteDnsPageBackground()
              .imePadding()
              .then(if (homeKeyboardVisible) Modifier.statusBarsPadding() else Modifier),
    ) {
      if (!homeKeyboardVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteDnsPalette.Background)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            HeaderCard(
                onSettingsClick = { showDonationDialog = false; showAppSettingsDialog = true },
                onDonateClick = { showAppSettingsDialog = false; showDonationDialog = true },
            )
        }
      }
        Row(modifier = Modifier.weight(1f)) {
            if (expandedLayout) {
                WhiteDnsNavigationRail(selectedTab, onTabSelected = ::selectTab)
            }
            Box(modifier = Modifier.weight(1f)
                .then(if (expandedLayout) Modifier.navigationBarsPadding() else Modifier)) {
                CompositionLocalProvider(LocalHomeDialogHost provides (selectedTab == WhiteDnsTab.CONNECT)) {
                    tabState.SaveableStateProvider(selectedTab) {
                        when (selectedTab) {
                            WhiteDnsTab.PROFILES -> ProfilesTabContent(
                                uiState = uiState,
                                onServerTestClick = onServerTestClick,
                                onSettingsChange = onSettingsChange,
                                onImportProfiles = onImportProfiles,
                                onRefreshSubscriptions = onRefreshSubscriptions,
                            )
                            WhiteDnsTab.CONNECT -> ConnectTabContent(
                                uiState = uiState,
                                dismissRequest = homeDismissRequest,
                                rootDialogVisible = profileCreateRequest != null || showAppSettingsDialog || showDonationDialog,
                                onBatteryOptimizationClick = onBatteryOptimizationClick,
                                onNotificationPermissionClick = onNotificationPermissionClick,
                                onNotificationSettingsClick = onNotificationSettingsClick,
                                onConnectClick = onConnectClick,
                                onLogsClick = { selectTab(WhiteDnsTab.LOGS) },
                                onAddConnectionClick = {
                                    profileCreateRequest = ProfileCreateRequest.CONNECTION
                                },
                                onAddResolverProfileClick = {
                                    profileCreateRequest = ProfileCreateRequest.RESOLVER
                                },
                                onSettingsChange = onSettingsChange,
                            )
                            WhiteDnsTab.SCAN -> ScanTabContent(
                                uiState = uiState,
                                onScanFileSelected = onScanFileSelected,
                                onScanDefaultListSelected = onScanDefaultListSelected,
                                onScanManualEntriesSelected = onScanManualEntriesSelected,
                                onScanStartClick = onScanStartClick,
                                onScanConnectionProfileChange = onScanConnectionProfileChange,
                                onScanWorkerCountChange = onScanWorkerCountChange,
                                onScanStopClick = onScanStopClick,
                                onScanResumeClick = onScanResumeClick,
                                onSettingsChange = onSettingsChange,
                            )
                            WhiteDnsTab.LOGS -> LogsTabContent(uiState = uiState)
                        }
                    }
                    when (profileCreateRequest) {
                        ProfileCreateRequest.CONNECTION -> ConnectionProfileDialog(
                            profile = null,
                            onDismiss = { profileCreateRequest = null },
                            onSave = { profile ->
                                val profileId = "profile-${System.currentTimeMillis()}"
                                onSettingsChange(uiState.settings.upsertConnectionProfile(profile.copy(id = profileId))
                                    .selectConnectionProfile(profileId))
                                profileCreateRequest = null
                            },
                        )
                        ProfileCreateRequest.RESOLVER -> ResolverProfileDialog(
                            profile = null,
                            initialResolverText = uiState.settings.resolverText,
                            onDismiss = { profileCreateRequest = null },
                            onSave = { profile ->
                                onSettingsChange(uiState.settings.upsertResolverProfile(profile))
                                profileCreateRequest = null
                            },
                        )
                        null -> Unit
                    }
                    if (showAppSettingsDialog) {
                        AppSettingsDialog(
                            themeMode = uiState.settings.themeMode,
                            onThemeModeChange = { onSettingsChange(uiState.settings.copy(themeMode = it)) },
                            languageCode = uiState.settings.languageCode,
                            onLanguageCodeChange = { onSettingsChange(uiState.settings.copy(languageCode = it)) },
                            onDismiss = { showAppSettingsDialog = false },
                        )
                    }
                    if (showDonationDialog) {
                        DonationDialog(onDismiss = { showDonationDialog = false })
                    }
                }
            }
        }
        if (!expandedLayout) {
            BottomNavigationBar(selectedTab = selectedTab, onTabSelected = ::selectTab)
        }
    }
}

private enum class WhiteDnsTab { PROFILES, CONNECT, SCAN, LOGS }

private const val ScanWorkerMin = 1
private const val ScanWorkerMax = 32
private const val MtuParallelismMin = 50
private const val MtuParallelismMax = 1000
private const val MtuParallelismStep = 50
private const val MtuParallelismDefault = 100
private const val ReachabilityDebounceMillis = 600L

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

private val LocalHomeDialogHost = staticCompositionLocalOf { false }
private val LocalCompactHomeDialog = staticCompositionLocalOf { false }

@Composable
private fun Modifier.whiteDnsPageBackground(): Modifier {
    return background(WhiteDnsPalette.Background)
}

@Composable
private fun ConnectTabContent(
    uiState: WhiteDnsUiState,
    dismissRequest: Int,
    rootDialogVisible: Boolean,
    onBatteryOptimizationClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onLogsClick: () -> Unit,
    onAddConnectionClick: () -> Unit,
    onAddResolverProfileClick: () -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val settings = uiState.settings
    var showResolverRequiredMessage by rememberSaveable { mutableStateOf(false) }
    var tuningExpanded by rememberSaveable { mutableStateOf(false) }
    var selectorSheetType by rememberSaveable { mutableStateOf<HomeSelectorType?>(null) }
    var selectorSheetVisible by rememberSaveable { mutableStateOf(false) }
    var showConnectionTomlDialog by rememberSaveable { mutableStateOf(false) }
    var showConnectionEditDialog by remember { mutableStateOf(false) }
    var showResolverEditDialog by remember { mutableStateOf(false) }
    var showAdvancedEditDialog by remember { mutableStateOf(false) }
    var showSplitTunnelDialog by rememberSaveable { mutableStateOf(false) }
    var resolverRuntimeDialog by remember { mutableStateOf<ResolverRuntimeDialogType?>(null) }
    var autoTuneSaveResult by remember { mutableStateOf<AutoTuneTrialResult?>(null) }
    var handledDismissRequest by rememberSaveable { mutableStateOf(dismissRequest) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(dismissRequest) {
        if (handledDismissRequest != dismissRequest) {
            selectorSheetVisible = false
            showConnectionTomlDialog = false
            showConnectionEditDialog = false
            showResolverEditDialog = false
            showAdvancedEditDialog = false
            showSplitTunnelDialog = false
            resolverRuntimeDialog = null
            autoTuneSaveResult = null
            focusManager.clearFocus()
            keyboard?.hide()
            handledDismissRequest = dismissRequest
        }
    }
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
    val hasInitialServerProfile = selectedConnectionProfile.customServerDomain.isNotBlank() &&
        selectedConnectionProfile.customServerEncryptionKey.isNotBlank()
    val selectedResolverProfile = remember(settings) { settings.selectedResolverProfile() }
    val resolverValidation = remember(settings.resolverText) { validateResolverText(settings.resolverText) }
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val shouldLoadSplitTunnelApps = resolvedSettings.connectionMode == "vpn" ||
        resolvedSettings.splitTunnelPackages.isNotEmpty()
    var splitTunnelApps by remember(context.packageName) {
        mutableStateOf(emptyList<SplitTunnelAppInfo>())
    }
    LaunchedEffect(context.packageName, shouldLoadSplitTunnelApps) {
        splitTunnelApps = if (shouldLoadSplitTunnelApps) {
            withContext(Dispatchers.IO) {
                loadSplitTunnelAppOptions(appContext)
            }
        } else {
            emptyList()
        }
    }
    val splitTunnelAppLabels = remember(splitTunnelApps) {
        splitTunnelApps.associate { it.packageName to it.label }
    }
    val serverRouteMissingStr = WhiteDnsL10n.serverRouteMissing
    val shareChooserClientConfigLabel = WhiteDnsL10n.shareChooserClientConfig
    val advancedProfileModifiedSuffixLabel = WhiteDnsL10n.advancedProfileModifiedSuffix
    val resolverCountSingular = WhiteDnsL10n.resolverCountOneTemplate
    val resolverCountPlural = WhiteDnsL10n.resolverCountTemplate
    val noSavedListsStr = WhiteDnsL10n.homeSelectorNoSavedLists
    val notSelectedStr = WhiteDnsL10n.homeSelectorNotSelected
    val defaultConnectionLabel = WhiteDnsL10n.setupDefaultConnection
    val defaultResolverLabel = WhiteDnsL10n.setupDefaultResolver
    val defaultAdvancedLabel = WhiteDnsL10n.setupDefaultAdvanced
    val connectionSelectorItems = remember(connectionProfiles, serverRouteMissingStr, defaultConnectionLabel) {
        connectionProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = if (profile.id == ConnectionProfile.DefaultId) defaultConnectionLabel else profile.name,
                detail = profile.customServerDomain.ifBlank { serverRouteMissingStr },
            )
        }
    }
    val resolverSelectorItems = remember(resolverProfiles, resolverCountSingular, resolverCountPlural, defaultResolverLabel) {
        resolverProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = if (profile.id == ResolverProfile.DefaultId) defaultResolverLabel else profile.name,
                detail = resolverCountLabel(
                    validateResolverText(profile.resolverText).normalizedResolvers.size,
                    resolverCountSingular,
                    resolverCountPlural,
                ),
            )
        }
    }
    val advancedSelectorItems = remember(advancedProfiles, defaultAdvancedLabel) {
        advancedProfiles.map { profile ->
            HomeSelectorItem(
                id = profile.id,
                title = if (profile.id == AdvancedSettingsProfile.DefaultId) defaultAdvancedLabel else profile.name,
                detail = advancedProfileSummary(profile),
            )
        }
    }
    val resolverSelectorDetail = selectedResolverProfile?.resolverText?.let { resolverText ->
        resolverCountLabel(
            validateResolverText(resolverText).normalizedResolvers.size,
            resolverCountSingular,
            resolverCountPlural,
        )
    } ?: if (resolverProfiles.isEmpty()) {
        noSavedListsStr
    } else {
        notSelectedStr
    }
    val hasResolvers = resolverValidation.isValid || settings.canBootstrapResolversWithCottenMax() ||
        (selectedConnectionProfile.engine == DnsClientEngine.Soar && selectedConnectionProfile.soarSettings.discovery && settings.resolverText.isBlank())
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
        focusManager.clearFocus()
        keyboard?.hide()
    }

    BackHandler(enabled = selectorSheetVisible) {
        closeSelector()
    }

    val homeOverlayVisible = selectorSheetVisible || rootDialogVisible || showConnectionTomlDialog ||
        showConnectionEditDialog || showResolverEditDialog || showAdvancedEditDialog || showSplitTunnelDialog ||
        resolverRuntimeDialog != null || autoTuneSaveResult != null
    Box(modifier = Modifier.fillMaxSize().testTag("connect-content")
        .then(if (rootDialogVisible) Modifier.clearAndSetSemantics {} else Modifier)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (homeOverlayVisible) Modifier.clearAndSetSemantics {} else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = WhiteDnsLayout.contentMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            ConnectButton(
                status = uiState.connectionStatus,
                progressState = uiState.connectionProgress,
                enabled = uiState.connectionStatus != ConnectionStatus.DISCONNECTED ||
                    (hasResolvers && selectedConnectionProfile.customServerDomain.isNotBlank() &&
                        selectedConnectionProfile.customServerEncryptionKey.isNotBlank()),
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
                visible = uiState.connectionStatus == ConnectionStatus.DISCONNECTED &&
                    (!hasInitialServerProfile || !hasResolvers),
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)),
            ) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    ConnectionSetupSlider(
                        connection = selectedConnectionProfile,
                        connectionReady = hasInitialServerProfile,
                        resolverProfile = selectedResolverProfile,
                        resolverCount = resolverValidation.normalizedResolvers.size,
                        resolversReady = hasResolvers,
                        automaticDiscovery = selectedConnectionProfile.engine == DnsClientEngine.Soar &&
                            selectedConnectionProfile.soarSettings.discovery && settings.resolverText.isBlank(),
                        resolverIssue = resolverValidation.invalidEntries.firstOrNull()?.let {
                            "${WhiteDnsL10n.setupInvalidResolverIp}: $it"
                        } ?: WhiteDnsL10n.setupNoResolversConfigured,
                        connectionActionLabel = when {
                            hasInitialServerProfile -> soarText("Change connection", "تغییر اتصال")
                            selectedConnectionProfile.id != ConnectionProfile.DefaultId -> soarText("Edit connection", "ویرایش اتصال")
                            connectionProfiles.any { it.customServerDomain.isNotBlank() && it.customServerEncryptionKey.isNotBlank() } ->
                                soarText("Choose connection", "انتخاب اتصال")
                            else -> WhiteDnsL10n.setupAddConnection
                        },
                        onConnectionClick = {
                            when {
                                hasInitialServerProfile -> openSelector(HomeSelectorType.CONNECTION)
                                selectedConnectionProfile.id != ConnectionProfile.DefaultId -> showConnectionEditDialog = true
                                connectionProfiles.any { it.customServerDomain.isNotBlank() && it.customServerEncryptionKey.isNotBlank() } ->
                                    openSelector(HomeSelectorType.CONNECTION)
                                else -> onAddConnectionClick()
                            }
                        },
                        onResolverClick = {
                            when {
                                hasResolvers -> openSelector(HomeSelectorType.RESOLVER)
                                selectedResolverProfile != null && selectedResolverProfile.id != ResolverProfile.DefaultId ->
                                    showResolverEditDialog = true
                                else -> onAddResolverProfileClick()
                            }
                        },
                    )
                }
            }
            HomeTrafficSummary(uiState.connectionStatus, uiState.connectionStats,
                uiState.connectionLogs.firstOrNull { it.isNotBlank() && it != "Idle" }, onLogsClick)
            Spacer(Modifier.height(16.dp))
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
            if (showNotificationBanner || showBatteryBanner) {
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            }
            AnimatedVisibility(
                visible = showNotificationBanner,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)),
            ) {
                Column {
                    NotificationPermissionBanner(
                        onClick = onNotificationPermissionClick,
                        onSettingsClick = onNotificationSettingsClick,
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
                }
            }
            AnimatedVisibility(
                visible = showBatteryBanner,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)),
            ) {
                Column {
                    BatteryOptimizationBanner(
                        onClick = onBatteryOptimizationClick,
                        onDismiss = {
                            onSettingsChange(settings.copy(batteryOptimizationWarningDismissed = true))
                        },
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
                }
            }
            Spacer(
                modifier = Modifier.height(
                    if (!showNotificationBanner && !showBatteryBanner) WhiteDnsSpacing.md else WhiteDnsSpacing.sm,
                ),
            )
            AnimatedVisibility(
                visible = resolvedSettings.connectionMode == "vpn",
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(140)),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                    AnimatedVisibility(
                        visible = !settings.fullVpnPerformanceWarningDismissed,
                        enter = fadeIn(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(140)),
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
                        onSelectAppsClick = { showSplitTunnelDialog = true },
                    )
                }
            }
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            HomeOptionsGroup { stacked ->
                AnimatedVisibility(
                    visible = uiState.connectionStatus != ConnectionStatus.CONNECTED,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(140)),
                ) {
                    Column {
                        HomeOptionRow(
                            modifier = Modifier.testTag("home-option-connection"),
                            label = WhiteDnsL10n.sectionConnection,
                            value = if (selectedConnectionProfile.id == ConnectionProfile.DefaultId) WhiteDnsL10n.setupDefaultConnection else selectedConnectionProfile.name,
                            detail = selectedConnectionProfile.customServerDomain.ifBlank { WhiteDnsL10n.serverRouteMissing },
                            stacked = stacked,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.CONNECTION) },
                            onEditClick = {
                                showResolverRequiredMessage = false
                                showConnectionEditDialog = true
                            },
                        )
                        HomeOptionDivider()
                        HomeOptionRow(
                            modifier = Modifier.testTag("home-option-resolver"),
                            label = WhiteDnsL10n.sectionResolver,
                            value = when {
                                selectedResolverProfile == null -> WhiteDnsL10n.homeSelectorResolverProfileFallback
                                selectedResolverProfile.id == ResolverProfile.DefaultId -> WhiteDnsL10n.setupDefaultResolver
                                else -> selectedResolverProfile.name
                            },
                            detail = resolverSelectorDetail,
                            stacked = stacked,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.RESOLVER) },
                            onEditClick = if (selectedResolverProfile?.id != ResolverProfile.DefaultId) ({
                                showResolverRequiredMessage = false
                                showResolverEditDialog = true
                            }) else null,
                        )
                        HomeOptionDivider()
                        HomeOptionRow(
                            modifier = Modifier.testTag("home-option-setting"),
                            label = WhiteDnsL10n.homeSelectorSettingLabel,
                            value = (if (selectedAdvancedProfile.id == AdvancedSettingsProfile.DefaultId) WhiteDnsL10n.setupDefaultAdvanced else selectedAdvancedProfile.name) +
                                if (advancedProfileDirty) " · ${WhiteDnsL10n.homeSelectorUnsavedChanges}" else "",
                            detail = if (advancedProfileDirty) WhiteDnsL10n.homeSelectorUnsavedChanges else advancedProfileSummary(selectedAdvancedProfile),
                            stacked = stacked,
                            enabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED,
                            onClick = { openSelector(HomeSelectorType.ADVANCED) },
                            onEditClick = {
                                showResolverRequiredMessage = false
                                showAdvancedEditDialog = true
                            },
                        )
                        HomeOptionDivider()
                    }
                }
                HomeOptionRow(
                    modifier = Modifier.testTag("home-option-tuning"),
                    label = WhiteDnsL10n.groupNetworkTuning,
                    value = "",
                    stacked = stacked,
                    enabled = true,
                    expanded = tuningExpanded,
                    onClick = { tuningExpanded = !tuningExpanded },
                )
                AnimatedVisibility(
                    visible = tuningExpanded,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(120)),
                ) {
                    Column(Modifier.fillMaxWidth().background(WhiteDnsPalette.SurfaceAlt).padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    val parallelTestControlsEnabled = uiState.connectionStatus == ConnectionStatus.DISCONNECTED
                    ToggleRow(
                        label = WhiteDnsL10n.parallelTest,
                        enabled = settings.autoTuneEnabled,
                        interactiveEnabled = parallelTestControlsEnabled,
                        onToggle = {
                            val selectedIds = WhiteDnsParallelTest.normalizeConfigIds(
                                configIds = settings.parallelTestSelectedConfigIds,
                                advancedProfiles = advancedProfiles,
                                includeAggressive = settings.parallelTestAggressivePresetsEnabled,
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
                        enter = fadeIn(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(140)),
                    ) {
                        ParallelTestSelectionPanel(
                            settings = settings,
                            advancedProfiles = advancedProfiles,
                            controlsEnabled = parallelTestControlsEnabled,
                            expanded = parallelTestSelectionExpanded,
                            onExpandedChange = { parallelTestSelectionExpanded = it },
                            onSettingsChange = onSettingsChange,
                        )
                    }
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
                    MtuParallelismSlider(
                        parallelism = settings.mtuTestParallelismResolvers.toMtuParallelismSliderValue(),
                        enabled = parallelTestControlsEnabled,
                        onParallelismChange = { parallelism ->
                            onSettingsChange(settings.copy(mtuTestParallelismResolvers = parallelism.toString()))
                        },
                    )
                }
                    }
                }
            }
            AnimatedVisibility(
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(120)),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ResolverRuntimeSummary(
                        resolverState = uiState.resolverRuntimeState,
                        progressState = uiState.connectionProgress,
                        connectionStatus = uiState.connectionStatus,
                        onShowResolvers = { resolverRuntimeDialog = it },
                    )
                    ConnectionVerificationSummary(
                        modifier = Modifier.padding(top = 8.dp),
                        verification = uiState.connectionVerification,
                    )
                }
            }
            AnimatedVisibility(
                visible = showResolverRequiredMessage &&
                    uiState.connectionStatus == ConnectionStatus.DISCONNECTED &&
                    !hasResolvers,
                enter = fadeIn(animationSpec = tween(160)),
                exit = fadeOut(animationSpec = tween(120)),
            ) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = WhiteDnsL10n.connectNeedResolvers,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.WarningText,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }


            AnimatedVisibility(
                visible = uiState.connectionStatus == ConnectionStatus.CONNECTED ||
                    uiState.autoTuneTrialResults.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(260)),
                exit = fadeOut(animationSpec = tween(180)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                ) {
                    ConnectionInfoCard(
                        connectionProfileName = if (selectedConnectionProfile.id == ConnectionProfile.DefaultId) WhiteDnsL10n.setupDefaultConnection else selectedConnectionProfile.name,
                        resolverProfileName = when {
                            selectedResolverProfile == null -> WhiteDnsL10n.setupManualResolvers
                            selectedResolverProfile.id == ResolverProfile.DefaultId -> WhiteDnsL10n.setupDefaultResolver
                            else -> selectedResolverProfile.name
                        },
                        settingProfileName = if (selectedAdvancedProfile.id == AdvancedSettingsProfile.DefaultId) {
                            selectedAdvancedProfile.copy(name = WhiteDnsL10n.setupDefaultAdvanced).displayName(dirty = advancedProfileDirty, modifiedSuffix = advancedProfileModifiedSuffixLabel)
                        } else {
                            selectedAdvancedProfile.displayName(dirty = advancedProfileDirty, modifiedSuffix = advancedProfileModifiedSuffixLabel)
                        },
                        listenAddress = proxyAddress,
                        httpProxyAddress = httpProxyAddress,
                        connectionMode = if (resolvedSettings.connectionMode == "vpn") WhiteDnsL10n.connectionModeVpn else WhiteDnsL10n.connectionModeProxy,
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
                        isSoar = selectedConnectionProfile.engine == DnsClientEngine.Soar,
                        canDownloadToml = selectedConnectionProfile.customServerDomain.isNotBlank() &&
                            selectedConnectionProfile.customServerEncryptionKey.isNotBlank(),
                        onDownloadToml = {
                            showConnectionTomlDialog = true
                        },
                    )
                }
            }
        }
        }

        selectorSheetType?.let { activeSelector ->
            HomeSelectorSheet(
                visible = selectorSheetVisible,
                title = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> WhiteDnsL10n.selectorConnectionProfiles
                    HomeSelectorType.RESOLVER -> WhiteDnsL10n.selectorResolverProfiles
                    HomeSelectorType.ADVANCED -> WhiteDnsL10n.selectorSettingProfiles
                },
                searchPlaceholder = when (activeSelector) {
                    HomeSelectorType.CONNECTION -> WhiteDnsL10n.homeSelectorSearchConnections
                    HomeSelectorType.RESOLVER -> WhiteDnsL10n.homeSelectorSearchResolvers
                    HomeSelectorType.ADVANCED -> WhiteDnsL10n.homeSelectorSearchSettings
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
                    HomeSelectorType.CONNECTION -> WhiteDnsL10n.homeSelectorNoConnectionProfiles
                    HomeSelectorType.RESOLVER -> WhiteDnsL10n.homeSelectorNoResolverProfiles
                    HomeSelectorType.ADVANCED -> WhiteDnsL10n.homeSelectorNoSettingProfiles
                },
                editContentDescription = "${WhiteDnsL10n.cdEditPrefix} ${when (activeSelector) {
                    HomeSelectorType.CONNECTION -> WhiteDnsL10n.sectionConnection
                    HomeSelectorType.RESOLVER -> WhiteDnsL10n.sectionResolver
                    HomeSelectorType.ADVANCED -> WhiteDnsL10n.homeSelectorSettingLabel
                }}",
                onEditClick = if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED &&
                    (activeSelector != HomeSelectorType.RESOLVER || selectedResolverProfile?.id != ResolverProfile.DefaultId)) ({
                    closeSelector()
                    showResolverRequiredMessage = false
                    when (activeSelector) {
                        HomeSelectorType.CONNECTION -> showConnectionEditDialog = true
                        HomeSelectorType.RESOLVER -> showResolverEditDialog = true
                        HomeSelectorType.ADVANCED -> showAdvancedEditDialog = true
                    }
                }) else null,
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

        if (showSplitTunnelDialog) {
            SplitTunnelAppDialog(
                apps = splitTunnelApps,
                selectedPackages = settings.splitTunnelPackages,
                onDismiss = { showSplitTunnelDialog = false },
                onSave = { packages ->
                    onSettingsChange(settings.copy(splitTunnelPackages = packages))
                    showSplitTunnelDialog = false
                },
            )
        }
        resolverRuntimeDialog?.let { dialog ->
            ResolverRuntimeDialog(
                title = if (dialog == ResolverRuntimeDialogType.ACTIVE) WhiteDnsL10n.resolverActiveResolvers
                    else WhiteDnsL10n.resolverValidResolvers,
                resolvers = if (dialog == ResolverRuntimeDialogType.ACTIVE) uiState.resolverRuntimeState.activeResolvers
                    else uiState.resolverRuntimeState.validResolvers,
                onDismiss = { resolverRuntimeDialog = null },
            )
        }

        if (showConnectionTomlDialog) {
            ConnectionProfileExportDialog(
                title = if (selectedConnectionProfile.engine == DnsClientEngine.Soar) soarText("EXPORT SOAR SETTINGS", "خروجی تنظیمات Soar") else WhiteDnsL10n.downloadTomlTitle,
                fieldLabel = if (selectedConnectionProfile.engine == DnsClientEngine.Soar) "soar_config.json" else "client_config.toml",
                placeholder = if (selectedConnectionProfile.engine == DnsClientEngine.Soar) "soar_config.json" else "client_config.toml",
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
                    if (selectedConnectionProfile.engine == DnsClientEngine.Soar) {
                        shareTextExport(context, "soar_config.json", "Soar configuration", "Export Soar configuration", toml)
                    } else shareClientConfigToml(context, toml, shareChooserClientConfigLabel)
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
                initialName = editingProfile?.name ?: advancedSaveAsInitialName(selectedAdvancedProfile, WhiteDnsL10n.homeSelectorCustomAdvanced, WhiteDnsL10n.profileNameCopySuffix),
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
    controlsEnabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    val resources = LocalResources.current
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
        includeAggressive = settings.parallelTestAggressivePresetsEnabled,
    )
    val selectedSet = selectedIds.toSet()
    val stableWhiteDnsConfigIds = WhiteDnsParallelTest.stableConfigIds
    val aggressiveWhiteDnsConfigIds = WhiteDnsParallelTest.aggressiveConfigIds
    val selectedStableWhiteDnsCount = stableWhiteDnsConfigIds.count { it in selectedSet }
    val allStableWhiteDnsSelected = selectedStableWhiteDnsCount == stableWhiteDnsConfigIds.size
    val canAddStableWhiteDnsConfigs = allStableWhiteDnsSelected ||
        selectedIds.size - selectedStableWhiteDnsCount + stableWhiteDnsConfigIds.size <=
        WhiteDnsParallelTest.MaxSelectedConfigs
    val selectedAggressiveWhiteDnsCount = aggressiveWhiteDnsConfigIds.count { it in selectedSet }
    val allAggressiveWhiteDnsSelected = settings.parallelTestAggressivePresetsEnabled &&
        selectedAggressiveWhiteDnsCount == aggressiveWhiteDnsConfigIds.size
    val canAddAggressiveWhiteDnsConfigs = allAggressiveWhiteDnsSelected ||
        selectedIds.size - selectedAggressiveWhiteDnsCount + aggressiveWhiteDnsConfigIds.size <=
        WhiteDnsParallelTest.MaxSelectedConfigs

    fun updateSelectedConfigIds(
        nextIds: List<String>,
        includeAggressive: Boolean = settings.parallelTestAggressivePresetsEnabled,
    ) {
        val normalizedIds = WhiteDnsParallelTest.normalizeConfigIds(
            configIds = nextIds,
            advancedProfiles = advancedProfiles,
            defaultIfEmpty = false,
            includeAggressive = includeAggressive,
        )
        val keepsAggressive = includeAggressive && aggressiveWhiteDnsConfigIds.any { it in normalizedIds }
        onSettingsChange(
            settings.copy(
                parallelTestSelectedConfigIds = normalizedIds,
                parallelTestAggressivePresetsEnabled = keepsAggressive,
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WhiteDnsSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
    ) {
        FlowRow(
            maxItemsInEachRow = if (LocalConfiguration.current.fontScale > 1.5f) 1 else 2,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.control))
                .semantics {
                    contentDescription = if (expanded) {
                        resources.getString(R.string.cd_parallel_test_collapse)
                    } else {
                        resources.getString(R.string.cd_parallel_test_expand)
                    }
                }
                .clickable(role = Role.Button) {
                    haptic.performLight()
                    onExpandedChange(!expanded)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${WhiteDnsL10n.connectSelectedCount} ${selectedIds.size}/${WhiteDnsParallelTest.MaxSelectedConfigs}",
                modifier = if (LocalConfiguration.current.fontScale > 1.5f) Modifier.fillMaxWidth() else Modifier,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.FieldLabel,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (expanded) WhiteDnsPalette.Accent else WhiteDnsPalette.SurfaceAlt)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (expanded) WhiteDnsL10n.parallelTestOpenLabel else WhiteDnsL10n.parallelTestClosedLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = if (expanded) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
                        fontWeight = FontWeight.Normal,
                    ),
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) {
                        WhiteDnsL10n.parallelTestCollapseDescription
                    } else {
                        WhiteDnsL10n.parallelTestExpandDescription
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
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
            ) {
                Text(
                    text = WhiteDnsL10n.parallelTestDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Muted,
                        lineHeight = 20.sp,
                    ),
                )
                ParallelTestConfigRow(
                    label = WhiteDnsL10n.whiteDnsConfigsLabel,
                    detail = WhiteDnsL10n.whiteDnsConfigsDescription,
                    checked = allStableWhiteDnsSelected,
                    enabled = controlsEnabled && canAddStableWhiteDnsConfigs,
                    onToggle = {
                        haptic.performLight()
                        val withoutStableWhiteDnsConfigs = selectedIds.filterNot { it in stableWhiteDnsConfigIds }
                        if (allStableWhiteDnsSelected) {
                            if (withoutStableWhiteDnsConfigs.isNotEmpty()) {
                                updateSelectedConfigIds(withoutStableWhiteDnsConfigs)
                            }
                        } else if (canAddStableWhiteDnsConfigs) {
                            updateSelectedConfigIds(stableWhiteDnsConfigIds + withoutStableWhiteDnsConfigs)
                        }
                    },
                )
                ParallelTestConfigRow(
                    label = WhiteDnsL10n.whiteDnsAggressiveConfigsLabel,
                    detail = WhiteDnsL10n.whiteDnsAggressiveConfigsDescription,
                    checked = allAggressiveWhiteDnsSelected,
                    enabled = controlsEnabled && canAddAggressiveWhiteDnsConfigs,
                    onToggle = {
                        haptic.performLight()
                        val withoutAggressiveWhiteDnsConfigs = selectedIds.filterNot {
                            it in aggressiveWhiteDnsConfigIds
                        }
                        if (allAggressiveWhiteDnsSelected) {
                            updateSelectedConfigIds(
                                withoutAggressiveWhiteDnsConfigs,
                                includeAggressive = false,
                            )
                        } else if (canAddAggressiveWhiteDnsConfigs) {
                            updateSelectedConfigIds(
                                withoutAggressiveWhiteDnsConfigs + aggressiveWhiteDnsConfigIds,
                                includeAggressive = true,
                            )
                        }
                    },
                )
                if (userProfiles.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(top = WhiteDnsSpacing.xs),
                        text = WhiteDnsL10n.parallelTestYourConfigs,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = WhiteDnsPalette.FieldLabel,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    userProfiles.forEach { profile ->
                        val configId = WhiteDnsParallelTest.settingConfigId(profile.id)
                        val checked = configId in selectedSet
                        val enabled = checked || selectedIds.size < WhiteDnsParallelTest.MaxSelectedConfigs
                        ParallelTestConfigRow(
                            label = profile.name.ifBlank { WhiteDnsL10n.genericSettingFallback },
                            detail = advancedProfileSummary(profile),
                            checked = checked,
                            enabled = controlsEnabled && enabled,
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
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Ink.copy(alpha = contentAlpha),
                    fontWeight = FontWeight.Normal,
                ),
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Muted.copy(alpha = contentAlpha),
                    lineHeight = 18.sp,
                ),
            )
        }
    }
}

@Composable
private fun ProfilesTabContent(
    uiState: WhiteDnsUiState,
    onServerTestClick: (String?) -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    onImportProfiles: suspend (String, Boolean) -> Result<Unit>,
    onRefreshSubscriptions: suspend () -> Result<Unit>,
) {
    var selectedProfileTab by rememberSaveable { mutableStateOf(ProfileTab.CONNECTION) }
    var showSettingGuide by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = WhiteDnsLayout.contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding)
                .padding(top = WhiteDnsSpacing.sm),
        ) {
            val header: @Composable (@Composable () -> Unit) -> Unit = { actions ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = WhiteDnsL10n.tabProfiles,
                        style = MaterialTheme.typography.headlineMedium,
                        color = WhiteDnsPalette.Ink,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    if (selectedProfileTab == ProfileTab.SETTING) {
                        SettingProfileGuideButton(onClick = { showSettingGuide = true })
                    }
                    actions()
                }
                ProfileTabSwitch(
                    selectedTab = selectedProfileTab,
                    onTabSelected = { selectedProfileTab = it },
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            }
            when (selectedProfileTab) {
                ProfileTab.CONNECTION -> ConnectionProfilesSettings(
                    settings = uiState.settings,
                    activeConnectionProfileId = uiState.activeConnectionProfileId,
                    connectionStatus = uiState.connectionStatus,
                    serverTestState = uiState.serverTestState,
                    onServerTestClick = onServerTestClick,
                    onSettingsChange = onSettingsChange,
                    onImportProfiles = onImportProfiles,
                    onRefreshSubscriptions = onRefreshSubscriptions,
                    header = header,
                )
                ProfileTab.RESOLVER -> ResolverProfilesSettings(
                    settings = uiState.settings,
                    connectionStatus = uiState.connectionStatus,
                    onSettingsChange = onSettingsChange,
                    header = header,
                )
                ProfileTab.SETTING -> SettingProfilesSettings(
                    settings = uiState.settings,
                    connectionStatus = uiState.connectionStatus,
                    onSettingsChange = onSettingsChange,
                    header = header,
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sectionSpacing))
        }
    }

    if (showSettingGuide) {
        SettingProfileGuideDialog(
            onDismiss = { showSettingGuide = false },
        )
    }
}

private enum class ProfileTab(val label: String) {
    CONNECTION("Connection"),
    RESOLVER("Resolver"),
    SETTING("Setting"),
}

@Composable
private fun tabLabel(tab: WhiteDnsTab): String = when (tab) {
    WhiteDnsTab.PROFILES -> WhiteDnsL10n.tabProfiles
    WhiteDnsTab.CONNECT -> WhiteDnsL10n.tabConnect
    WhiteDnsTab.SCAN -> WhiteDnsL10n.tabScan
    WhiteDnsTab.LOGS -> WhiteDnsL10n.tabLogs
}

@Composable
private fun profileTabLabel(tab: ProfileTab): String = when (tab) {
    ProfileTab.CONNECTION -> WhiteDnsL10n.profileTabConnection
    ProfileTab.RESOLVER -> WhiteDnsL10n.profileTabResolver
    ProfileTab.SETTING -> WhiteDnsL10n.profileTabSetting
}

@Composable
private fun SettingProfileGuideButton(
    onClick: () -> Unit,
) {
    val haptic = rememberHapticFeedback()
    Box(
        modifier = Modifier
            .size(WhiteDnsLayout.minimumTouchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button) {
                haptic.performLight()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
            contentDescription = WhiteDnsL10n.cdSettingGuide,
            tint = WhiteDnsPalette.AccentText,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingProfileGuideDialog(
    onDismiss: () -> Unit,
) {
    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(WhiteDnsL10n.settingGuideTitle, onDismiss)
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = WhiteDnsL10n.settingGuideIntro,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = WhiteDnsPalette.Description,
                    ),
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = WhiteDnsL10n.settingGuideSource,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = WhiteDnsPalette.Pale,
                    ),
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                WhiteDnsL10n.settingGuideSections.forEachIndexed { index, section ->
                    if (index > 0) {
                        SectionDivider()
                    }
                    SettingsGuideSectionView(section = section)
                }
            }
        }
    }
}

@Composable
private fun SettingsGuideSectionView(
    section: SettingsGuideSection,
) {
    Text(
        text = section.title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            color = WhiteDnsPalette.SectionTitle,
            fontWeight = FontWeight.Bold,
        ),
    )
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    section.entries.forEachIndexed { index, entry ->
        if (index > 0) {
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        }
        SettingsGuideEntryView(entry = entry)
    }
}

@Composable
private fun SettingsGuideEntryView(
    entry: SettingsGuideEntry,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(WhiteDnsPalette.SurfaceAlt)
            .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.control))
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = WhiteDnsPalette.Ink,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
        Text(
            text = entry.body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 19.sp,
                color = WhiteDnsPalette.Description,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
        Text(
            text = "${WhiteDnsL10n.settingGuideEffectLabel}: ${entry.effect}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 19.sp,
                color = WhiteDnsPalette.AccentText,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun LogsTabContent(
    uiState: WhiteDnsUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = WhiteDnsLayout.contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding)
                .padding(top = WhiteDnsSpacing.sm),
        ) {
            Text(
                text = WhiteDnsL10n.tabLogs,
                style = MaterialTheme.typography.headlineMedium,
                color = WhiteDnsPalette.Ink,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
            )
            ConnectionLogsBlock(uiState = uiState, expanded = true)
        }
    }
}

@Composable
private fun ScanTabContent(
    uiState: WhiteDnsUiState,
    onScanFileSelected: (Uri) -> Unit,
    onScanDefaultListSelected: () -> Unit,
    onScanManualEntriesSelected: (String) -> Unit,
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
    val connectionFallback = WhiteDnsL10n.genericConnectionFallback
    val scanConnectionProfileOptions = remember(connectionProfiles, connectionFallback) {
        connectionProfiles.map { profile ->
            Choice(
                value = profile.id,
                label = profile.name.ifBlank { connectionFallback },
            )
        }
    }
    val selectedScanProfileNeedsServer = selectedScanConnectionProfile.customServerDomain.isBlank() ||
        selectedScanConnectionProfile.customServerEncryptionKey.isBlank()
    var showScanInfoNotice by rememberSaveable { mutableStateOf(true) }
    var showSaveAsDialog by rememberSaveable(scanState.sessionId) { mutableStateOf(false) }
    var showManualEntryDialog by rememberSaveable { mutableStateOf(false) }
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
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = WhiteDnsLayout.contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding)
                .padding(top = WhiteDnsSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.md),
        ) {
            Text(
                text = WhiteDnsL10n.tabScan,
                style = MaterialTheme.typography.headlineMedium,
                color = WhiteDnsPalette.Ink,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.scanDefaultList,
                    emphasized = !scanState.isRunning,
                    enabled = !scanState.isRunning,
                    onClick = onScanDefaultListSelected,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.scanSelectFile,
                    emphasized = false,
                    enabled = !scanState.isRunning,
                    onClick = {
                        scanFileLauncher.launch(ResolverImportMimeTypes)
                    },
                )
            }
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = WhiteDnsL10n.scanManualEntry,
                emphasized = false,
                enabled = !scanState.isRunning,
                onClick = { showManualEntryDialog = true },
            )
            ScanWorkerSlider(
                workerCount = workerCount,
                enabled = !scanState.isRunning,
                onWorkerCountChange = { onScanWorkerCountChange(it.toString()) },
            )
            ScanNote(
                text = WhiteDnsL10n.scanWorkerWarning,
            )
            WhiteDnsDropdownField(
                label = WhiteDnsL10n.scanProfileLabel,
                value = selectedScanConnectionProfile.id,
                options = scanConnectionProfileOptions,
                onValueChange = onScanConnectionProfileChange,
                enabled = !scanState.isRunning,
            )
            if (selectedScanProfileNeedsServer) {
                val scanProfileFallback = WhiteDnsL10n.scanProfileFallback
                val needsServerSuffix = WhiteDnsL10n.scanProfileNeedsServer
                ScanWarningBanner(
                    text = "${selectedScanConnectionProfile.name.ifBlank { scanProfileFallback }} $needsServerSuffix",
                )
            }
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = if (scanState.isRunning) WhiteDnsL10n.scanBtnStop else WhiteDnsL10n.scanBtnStart,
                emphasized = true,
                tone = if (scanState.isRunning) CompactActionTone.Danger else CompactActionTone.Success,
                enabled = if (scanState.isRunning) {
                    true
                } else {
                    scanCanStart(scanState) && !selectedScanProfileNeedsServer
                },
                onClick = if (scanState.isRunning) onScanStopClick else onScanStartClick,
            )
            if (showScanInfoNotice) {
                ScanInfoNotice(
                    text = WhiteDnsL10n.scanAutoSave,
                    onDismiss = { showScanInfoNotice = false },
                )
            }

            SectionCard(
                title = WhiteDnsL10n.scanStatusTitle,
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
                            label = WhiteDnsL10n.scanLabelTotal,
                            value = scanState.totalResolvers.toString(),
                        ),
                        CompactMetric(
                            icon = Icons.Rounded.Check,
                            iconContentDescription = stringResource(R.string.cd_icon_success),
                            label = WhiteDnsL10n.scanLabelValid,
                            value = scanState.validResolvers.toString(),
                        ),
                        CompactMetric(
                            icon = Icons.Rounded.Close,
                            iconContentDescription = stringResource(R.string.cd_icon_error),
                            label = WhiteDnsL10n.scanLabelRejected,
                            value = scanState.rejectedResolvers.toString(),
                        ),
                    ),
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                ScanProgressBar(fraction = scanState.fraction)
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                InfoRow(label = WhiteDnsL10n.scanLabelStatus, value = scanStatusLabel(scanState.status))
                InfoRow(label = WhiteDnsL10n.scanLabelSource, value = scanState.sourceName.ifBlank { WhiteDnsL10n.scanNoFileSelected })
                InfoRow(label = WhiteDnsL10n.scanLabelWorkers, value = scanState.workerCount.toString())
                InfoRow(label = WhiteDnsL10n.scanLabelProgress, value = "${scanState.completedResolvers}/${scanState.totalResolvers}")
                if (scanState.testingPaths.isNotEmpty()) {
                    InfoRow(label = "Testing DNS paths", value = scanState.testingPaths.takeLast(12).joinToString("\n"), multilineValue = true)
                }
                if (scanState.workingPaths.isNotEmpty()) {
                    InfoRow(label = "Verified DNS paths", value = scanState.workingPaths.takeLast(24).joinToString("\n"), multilineValue = true)
                }
                if (scanState.message.isNotBlank()) {
                    InfoRow(label = WhiteDnsL10n.scanMessageLabel, value = scanState.message, multilineValue = true)
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
                        label = WhiteDnsL10n.scanBtnSaveAs,
                        emphasized = false,
                        enabled = canSaveAsProfile,
                        onClick = {
                            showSaveAsDialog = true
                        },
                    )
                    CompactActionButton(
                        modifier = Modifier.weight(1f),
                        label = WhiteDnsL10n.scanBtnResume,
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
        val scanResultsName = WhiteDnsL10n.scanResultsTitle
        val scanResultsSuffix = WhiteDnsL10n.scanResultsSuffix
        ScanSaveAsProfileDialog(
            sourceName = scanState.sourceName,
            resolverCount = saveAsResolverEntries.size,
            initialName = scanResultProfileInitialName(scanState.sourceName, scanResultsName, scanResultsSuffix),
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

    if (showManualEntryDialog) {
        ScanManualEntryDialog(
            onDismiss = { showManualEntryDialog = false },
            onSubmit = { text ->
                showManualEntryDialog = false
                onScanManualEntriesSelected(text)
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

private fun scanResultProfileInitialName(
    sourceName: String,
    defaultName: String,
    suffix: String,
): String {
    val trimmed = sourceName.trim()
    if (trimmed.isBlank()) {
        return defaultName
    }
    val withoutExtension = trimmed.substringBeforeLast('.').trim().ifBlank { trimmed }
    return "$withoutExtension $suffix"
}

@Composable
private fun ScanInfoNotice(
    text: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WhiteDnsRadii.card))
            .background(WhiteDnsPalette.AccentSurface)
            .border(1.dp, WhiteDnsPalette.Accent.copy(alpha = 0.22f), RoundedCornerShape(WhiteDnsRadii.card))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                text = WhiteDnsL10n.scanAutoSaveTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.AccentText,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = WhiteDnsPalette.AccentText,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
        WhiteDnsCloseButton(onClick = onDismiss,
            contentDescription = WhiteDnsL10n.cdDismissScannerInfo, tint = WhiteDnsPalette.AccentText)
    }
}

@Composable
private fun ScanManualEntryDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var pasteMessage by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val validation by produceState(ResolverTextValidation(emptyList(), emptyList()), text) {
        value = withContext(Dispatchers.Default) {
            WhiteDnsScannerResultStore.normalizeScanResolverLines(text.lineSequence())
        }
    }
    var probeRequest by remember { mutableIntStateOf(0) }
    val reachability by produceState(
        initialValue = emptyMap<String, ResolverReachability>(),
        validation.normalizedResolvers,
        probeRequest,
    ) {
        val servers = validation.normalizedResolvers.take(ResolverReachabilityProbe.MaxProbeCount)
        if (servers.isEmpty()) {
            value = emptyMap()
            return@produceState
        }
        value = emptyMap()
        delay(ReachabilityDebounceMillis)
        value = ResolverReachabilityProbe.probeAll(servers).associateBy { it.resolver }
    }
    val probeServers = remember(validation.normalizedResolvers) {
        validation.normalizedResolvers.take(ResolverReachabilityProbe.MaxProbeCount)
    }
    val readyCount = validation.normalizedResolvers.size
    val invalidCount = validation.invalidEntries.size
    val summarySeparator = " · "
    val clipboardEmptyMessage = WhiteDnsL10n.scanManualEntryClipboardEmpty

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = WhiteDnsL10n.scanManualEntryTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Text(
                text = WhiteDnsL10n.scanManualEntryBody,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = WhiteDnsL10n.scanManualEntryTitle,
                value = text,
                onValueChange = {
                    text = it
                    pasteMessage = ""
                },
                placeholder = WhiteDnsL10n.scanManualEntryPlaceholder,
                singleLine = false,
                minLines = 6,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.None,
                ),
                rawValue = true,
                showLabel = false,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = WhiteDnsL10n.scanManualEntryPaste,
                emphasized = false,
                enabled = true,
                onClick = {
                    val pasted = runCatching {
                        context.getSystemService(ClipboardManager::class.java)
                            ?.primaryClip
                            ?.takeIf { clip ->
                                clip.itemCount > 0 && clip.description?.hasMimeType("text/*") == true
                            }
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                    }.getOrNull()?.trim().orEmpty()
                    if (pasted.isEmpty()) {
                        pasteMessage = clipboardEmptyMessage
                    } else {
                        val block = pasted.replace('\t', '\n')
                        text = if (text.isBlank()) block else "${text.trimEnd()}\n$block"
                        pasteMessage = ""
                    }
                },
            )
            if (pasteMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = pasteMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            val countsText = "$readyCount ${WhiteDnsL10n.scanManualEntryReadySuffix}" +
                summarySeparator +
                "$invalidCount ${WhiteDnsL10n.scanManualEntryInvalidSuffix}"
            val nothingParsed = text.isNotBlank() && readyCount == 0
            Text(
                text = if (nothingParsed) WhiteDnsL10n.scanManualEntryEmpty else countsText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = if (nothingParsed) WhiteDnsPalette.Error else WhiteDnsPalette.Muted,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Text(
                text = WhiteDnsL10n.scanReachabilityTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteDnsPalette.Ink,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
            Text(
                text = WhiteDnsL10n.scanReachabilityHint,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            probeServers.forEach { server ->
                val result = reachability[server]
                val reachable = result?.reachable == true
                val detail = when {
                    result == null -> WhiteDnsL10n.scanReachabilityChecking
                    reachable -> "${result.latencyMillis ?: 0} ms"
                    else -> WhiteDnsL10n.scanReachabilityNoResponse
                }
                Text(
                    text = "$server · $detail",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = when {
                            result == null -> WhiteDnsPalette.Muted
                            reachable -> WhiteDnsPalette.AccentText
                            else -> WhiteDnsPalette.Error
                        },
                    ),
                )
            }
            if (probeServers.isNotEmpty() && reachability.size >= probeServers.size) {
                val reachableCount = probeServers.count { reachability[it]?.reachable == true }
                val unreachableCount = probeServers.size - reachableCount
                Text(
                    text = "$reachableCount ${WhiteDnsL10n.scanReachabilityReachableSuffix}" +
                        summarySeparator +
                        "$unreachableCount ${WhiteDnsL10n.scanReachabilityUnreachableSuffix}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = WhiteDnsPalette.Muted,
                    ),
                )
            }
            if (probeServers.size < validation.normalizedResolvers.size) {
                Text(
                    text = "${probeServers.size}/${validation.normalizedResolvers.size} " +
                        WhiteDnsL10n.scanReachabilityTestedSuffix,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = WhiteDnsPalette.Muted,
                    ),
                )
            }
            if (probeServers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                CompactActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = WhiteDnsL10n.scanReachabilityAgain,
                    emphasized = false,
                    enabled = true,
                    onClick = { probeRequest += 1 },
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnAdd,
                    emphasized = true,
                    enabled = readyCount > 0,
                    onClick = { onSubmit(text) },
                )
            }
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
    val scanLabel = sourceName.ifBlank { WhiteDnsL10n.scanCurrentScan }
    val saveBodyTemplate = WhiteDnsL10n.scanSaveBodyTemplate

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = WhiteDnsL10n.scanSaveAsTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Text(
                text = "$resolverCount · $scanLabel · $saveBodyTemplate",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = WhiteDnsL10n.scanSaveAsName,
                value = name,
                onValueChange = { name = it },
                placeholder = WhiteDnsL10n.scanResultsTitle,
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
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnSave,
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
private fun SliderLabelValue(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WhiteDnsPalette.FieldLabel))
        Text(value, maxLines = 1, softWrap = false, style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WhiteDnsPalette.Ink,
            textDirection = TextDirection.Ltr))
    }
}

@Composable
private fun ScanWorkerSlider(
    workerCount: Int,
    enabled: Boolean,
    onWorkerCountChange: (Int) -> Unit,
) {
    val resources = LocalResources.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderLabelValue(WhiteDnsL10n.scanFieldWorkers, workerCount.toString())
        Slider(
            value = workerCount.toFloat(),
            onValueChange = { value ->
                onWorkerCountChange(value.roundToInt().coerceIn(ScanWorkerMin, ScanWorkerMax))
            },
            enabled = enabled,
            valueRange = ScanWorkerMin.toFloat()..ScanWorkerMax.toFloat(),
            steps = ScanWorkerMax - ScanWorkerMin - 1,
            modifier = Modifier.semantics {
                contentDescription = resources.getString(R.string.cd_worker_count_slider, workerCount)
            },
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
private fun MtuParallelismSlider(
    parallelism: Int,
    enabled: Boolean,
    onParallelismChange: (Int) -> Unit,
) {
    val resources = LocalResources.current
    var sliderValue by remember(parallelism) { mutableStateOf(parallelism.toMtuParallelismSliderValue().toFloat()) }
    val displayedParallelism = sliderValue.toMtuParallelismSliderValue()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderLabelValue(WhiteDnsL10n.settingResolverParallel, displayedParallelism.toString())
        Slider(
            value = sliderValue,
            onValueChange = { value ->
                sliderValue = value.toMtuParallelismSliderValue().toFloat()
            },
            onValueChangeFinished = {
                val committedParallelism = sliderValue.toMtuParallelismSliderValue()
                if (committedParallelism != parallelism) {
                    onParallelismChange(committedParallelism)
                }
            },
            enabled = enabled,
            valueRange = MtuParallelismMin.toFloat()..MtuParallelismMax.toFloat(),
            steps = ((MtuParallelismMax - MtuParallelismMin) / MtuParallelismStep) - 1,
            modifier = Modifier.semantics {
                contentDescription = resources.getString(R.string.cd_mtu_parallelism_slider, displayedParallelism)
            },
            colors = SliderDefaults.colors(
                thumbColor = WhiteDnsPalette.Accent,
                activeTrackColor = WhiteDnsPalette.Accent,
                inactiveTrackColor = WhiteDnsPalette.ControlBorder,
                disabledThumbColor = WhiteDnsPalette.Disabled,
                disabledActiveTrackColor = WhiteDnsPalette.ControlBorder,
                disabledInactiveTrackColor = WhiteDnsPalette.Input,
            ),
        )
        Text(
            text = WhiteDnsL10n.settingResolverParallelNote,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
    }
}

private fun String.toMtuParallelismSliderValue(): Int {
    return toIntOrNull()?.toMtuParallelismSliderValue() ?: MtuParallelismDefault
}

private fun Float.toMtuParallelismSliderValue(): Int {
    return roundToInt().toMtuParallelismSliderValue()
}

private fun Int.toMtuParallelismSliderValue(): Int {
    val rounded = (this.toFloat() / MtuParallelismStep).roundToInt() * MtuParallelismStep
    return rounded.coerceIn(MtuParallelismMin, MtuParallelismMax)
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                lineHeight = 20.sp,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScanWarningBanner(text: String) {
    CompactWarningNotice(text = text, modifier = Modifier.testTag("scan-warning"))
}

@Composable
private fun scanStatusLabel(status: String): String {
    return when (status) {
        WhiteDnsScanStatus.Ready -> WhiteDnsL10n.scanStatusReady
        WhiteDnsScanStatus.Starting -> WhiteDnsL10n.scanStatusStarting
        WhiteDnsScanStatus.Running -> WhiteDnsL10n.scanStatusRunning
        WhiteDnsScanStatus.Completed -> WhiteDnsL10n.scanStatusCompleted
        WhiteDnsScanStatus.Failed -> WhiteDnsL10n.scanStatusFailed
        WhiteDnsScanStatus.Stopped -> WhiteDnsL10n.scanStatusStopped
        else -> WhiteDnsL10n.scanStatusIdle
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: WhiteDnsTab,
    onTabSelected: (WhiteDnsTab) -> Unit,
) {
    val haptic = rememberHapticFeedback()
    val largeText = LocalConfiguration.current.fontScale > 1.5f
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.widthIn(max = WhiteDnsLayout.contentMaxWidth)
                .fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(WhiteDnsPalette.Surface).selectableGroup(),
        ) {
            WhiteDnsTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val label = tabLabel(tab)
                val color by animateColorAsState(
                    if (selected) WhiteDnsPalette.Accent else WhiteDnsPalette.Muted,
                    animationSpec = tween(180), label = "navigationColor",
                )
                Box(
                    modifier = Modifier.weight(if (largeText && selected && !keyboardVisible) 1.5f else 1f)
                        .heightIn(min = if (keyboardVisible) 56.dp else 72.dp)
                        .selectable(selected = selected, role = Role.Tab, onClick = {
                            haptic.performLight()
                            onTabSelected(tab)
                        }).semantics(mergeDescendants = true) { if (keyboardVisible || (largeText && !selected)) contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = if (keyboardVisible) 8.dp else 12.dp),
                    ) {
                        val iconResource = when (tab) {
                            WhiteDnsTab.PROFILES -> R.drawable.ic_subscriptions_tab
                            WhiteDnsTab.CONNECT -> R.drawable.ic_vpn_tab
                            WhiteDnsTab.SCAN -> R.drawable.ic_connection_test
                            WhiteDnsTab.LOGS -> R.drawable.ic_logs
                        }
                        Icon(painterResource(iconResource), null, tint = color, modifier = Modifier.size(22.dp))
                        if (!keyboardVisible && (!largeText || selected)) {
                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                                color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    if (selected) {
                        Box(Modifier.align(Alignment.BottomCenter).width(28.dp).height(3.dp)
                            .background(WhiteDnsPalette.Accent, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun segmentedChoiceLabelsFit(labels: List<String>, width: Dp): Boolean {
    val style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    val measurer = rememberTextMeasurer()
    val cellWidth = (width - 8.dp - 4.dp * (labels.size - 1)) / labels.size - 16.dp
    val availablePixels = with(LocalDensity.current) { cellWidth.toPx() }
    return labels.all { measurer.measure(it, style, softWrap = false).size.width <= availablePixels }
}

@Composable
private fun ProfileTabSwitch(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
) {
    val haptic = rememberHapticFeedback()
    val labels = ProfileTab.entries.map { profileTabLabel(it) }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val labelsFit = segmentedChoiceLabelsFit(labels, maxWidth)
        FlowRow(
            maxItemsInEachRow = if (labelsFit) 3 else 1,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WhiteDnsPalette.SurfaceAlt)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ProfileTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val localizedProfileLabel = profileTabLabel(tab)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = WhiteDnsLayout.controlHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                WhiteDnsPalette.AccentSurface
                            } else {
                                Color.Transparent
                            },
                        )
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = {
                                haptic.performLight()
                                onTabSelected(tab)
                            },
                        )
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = localizedProfileLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
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

    val modeOptions: List<Pair<String, String>> = listOf(
        "proxy" to WhiteDnsL10n.connectionModeProxy,
        "vpn" to WhiteDnsL10n.connectionModeVpn,
    )
    BoxWithConstraints(modifier = modifier) {
        val labelsFit = segmentedChoiceLabelsFit(modeOptions.map { it.second }, maxWidth)
        FlowRow(
            maxItemsInEachRow = if (labelsFit) 2 else 1,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) WhiteDnsPalette.Surface else WhiteDnsPalette.SurfaceAlt)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            modeOptions.forEach { (modeValue, modeLabel) ->
                val selected = selectedMode == modeValue
                val background by animateColorAsState(
                    targetValue = if (selected) {
                        WhiteDnsPalette.AccentSurface
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(180),
                    label = "connectionModeSegmentBackground",
                )
                val textColor by animateColorAsState(
                    targetValue = when {
                        !enabled -> WhiteDnsPalette.Disabled
                        selected -> WhiteDnsPalette.AccentText
                        else -> WhiteDnsPalette.Muted
                    },
                    animationSpec = tween(180),
                    label = "connectionModeSegmentText",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = WhiteDnsLayout.controlHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(background)
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = {
                                haptic.performLight()
                                onModeChange(modeValue)
                            },
                        )
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = modeLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
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

    val themeOptions: List<Pair<String, String>> = listOf(
        "dark" to WhiteDnsL10n.themeModeDark,
        "light" to WhiteDnsL10n.themeModeLight,
        "system" to WhiteDnsL10n.themeModeAuto,
    )
    BoxWithConstraints(modifier = modifier) {
        val labelsFit = segmentedChoiceLabelsFit(themeOptions.map { it.second }, maxWidth)
        Column {
            FieldLabel(WhiteDnsL10n.fieldTheme)
            FlowRow(
                maxItemsInEachRow = if (labelsFit) 3 else 1,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WhiteDnsPalette.Surface)
                    .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                themeOptions.forEach { (modeValue, modeLabel) ->
                    val selected = selectedMode == modeValue
                    val background by animateColorAsState(
                        targetValue = if (selected) WhiteDnsPalette.AccentSurface else Color.Transparent,
                        animationSpec = tween(180),
                        label = "themeModeSegmentBackground",
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                        animationSpec = tween(180),
                        label = "themeModeSegmentText",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = WhiteDnsLayout.controlHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(background)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = {
                                    haptic.performLight()
                                    onModeChange(modeValue)
                                },
                            )
                            .semantics(mergeDescendants = true) {}
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = modeLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = textColor,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                    }
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
private fun HomeOptionsGroup(content: @Composable ColumnScope.(Boolean) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val labels = listOf(WhiteDnsL10n.sectionConnection, WhiteDnsL10n.sectionResolver,
        WhiteDnsL10n.homeSelectorSettingLabel, WhiteDnsL10n.groupNetworkTuning)
    BoxWithConstraints(
        Modifier.fillMaxWidth().testTag("home-options")
            .clip(shape)
            .background(WhiteDnsPalette.Surface.copy(alpha = 200f / 255f))
            .border(1.dp, WhiteDnsPalette.Border.copy(alpha = 180f / 255f), shape),
    ) {
        val density = LocalDensity.current
        val labelWidth = with(density) { 104.dp.roundToPx() }
        val stacked = maxWidth < 320.dp || labels.any {
            measurer.measure(it.uppercase(Locale.ROOT), labelStyle, softWrap = false).size.width > labelWidth
        }
        Column(Modifier.fillMaxWidth()) { content(stacked) }
    }
}

@Composable
private fun HomeOptionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        thickness = 1.dp,
        color = WhiteDnsPalette.Border.copy(alpha = 150f / 255f),
    )
}

@Composable
private fun HomeOptionRow(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    stacked: Boolean = false,
    detail: String? = null,
    onEditClick: (() -> Unit)? = null,
    expanded: Boolean? = null,
) {
    val haptic = rememberHapticFeedback()
    val resources = LocalResources.current
    val editLabel = "${WhiteDnsL10n.cdEditPrefix} $label"
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val forwardAngle = if (isRtl) 90f else -90f
    val angle by animateFloatAsState(
        targetValue = if (expanded == true) 0f else forwardAngle,
        animationSpec = tween(260, easing = FastOutSlowInEasing), label = "homeOptionChevron",
    )
    val labelStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal, color = WhiteDnsPalette.Muted,
    )
    val valueStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = WhiteDnsPalette.Ink,
        textDirection = TextDirection.Content,
    )
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
            .graphicsLayer(alpha = if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(22.dp))
            .semantics {
                if (!detail.isNullOrBlank()) stateDescription = detail
                if (expanded != null) contentDescription = resources.getString(
                    if (expanded) R.string.cd_section_collapse else R.string.cd_section_expand, label)
            }
            .combinedClickable(
                enabled = enabled, role = Role.Button,
                onClick = { haptic.performLight(); onClick() },
                onLongClickLabel = if (onEditClick != null) editLabel else null,
                onLongClick = onEditClick?.let { edit -> { haptic.performMedium(); edit() } },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (stacked) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label.uppercase(Locale.ROOT), Modifier.fillMaxWidth(), style = labelStyle)
                if (value.isNotBlank()) Text(value, Modifier.fillMaxWidth(), style = valueStyle,
                    textAlign = if (isRtl) TextAlign.Right else TextAlign.Left)
            }
        } else {
            Text(label.uppercase(Locale.ROOT), Modifier.width(104.dp), style = labelStyle)
            Spacer(Modifier.width(12.dp))
            if (value.isNotBlank()) Text(value, Modifier.weight(1f), style = valueStyle,
                textAlign = if (isRtl) TextAlign.Left else TextAlign.Right)
            else Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = WhiteDnsPalette.Tertiary,
            modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = angle),
        )
    }
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
    val errorImportSettingsFileLabel = WhiteDnsL10n.errorImportSettingsFile
    val errorImportSettingsLabel = WhiteDnsL10n.errorImportSettings
    val importTomlFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        readTextFromUri(context, uri, errorImportSettingsFileLabel)
            .onSuccess { importedText ->
                tomlText = importedText
                importError = null
            }
            .onFailure { error ->
                importError = error.message ?: errorImportSettingsFileLabel
            }
    }

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = WhiteDnsL10n.profileDialogImportSettings,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldName,
                value = name,
                onValueChange = {
                    name = it
                    importError = null
                },
                placeholder = WhiteDnsL10n.settingProfileImportedSettingsPlaceholder,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.profileBtnImportFile,
                    emphasized = false,
                    enabled = true,
                    onClick = {
                        importTomlFileLauncher.launch(SettingsImportMimeTypes)
                    },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.profileBtnClear,
                    emphasized = false,
                    enabled = tomlText.isNotBlank(),
                    onClick = {
                        tomlText = ""
                        importError = null
                    },
                )
            }
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldToml,
                rawValue = true,
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
                        fontSize = 12.sp,
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
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnImport,
                    emphasized = true,
                    enabled = canImport,
                    onClick = {
                        onImport(name, tomlText)
                            .onFailure { error ->
                                importError = error.message ?: errorImportSettingsLabel
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
    GroupLabel(WhiteDnsL10n.groupMtu)
    MtuSettingsGroup(
        settings = settings,
        onSettingsChange = onSettingsChange,
    )

    SectionDivider()
    GroupLabel(WhiteDnsL10n.groupRuntimeWorkers)
    RuntimeWorkersSettingsGroup(
        settings = settings,
        onSettingsChange = onSettingsChange,
    )

    SectionDivider()
    if (showProxySettings) {
        GroupLabel(WhiteDnsL10n.groupLocalProxy)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = WhiteDnsL10n.settingListenIp,
                rawValue = true,
                value = settings.listenIp,
                onValueChange = { onSettingsChange(settings.copy(listenIp = it)) },
                placeholder = "127.0.0.1",
            )
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = WhiteDnsL10n.settingListenPort,
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
            label = WhiteDnsL10n.settingHttpProxy,
            enabled = settings.httpProxyEnabled,
            onToggle = {
                onSettingsChange(settings.copy(httpProxyEnabled = !settings.httpProxyEnabled))
            },
        )
        AnimatedVisibility(
            visible = settings.httpProxyEnabled,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)),
        ) {
            WhiteDnsTextField(
                label = WhiteDnsL10n.settingHttpPort,
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
            label = WhiteDnsL10n.settingSocks5Auth,
            enabled = settings.socks5Authentication,
            onToggle = {
                onSettingsChange(settings.copy(socks5Authentication = !settings.socks5Authentication))
            },
        )

        AnimatedVisibility(
            visible = settings.socks5Authentication,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(160)),
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    WhiteDnsTextField(
                        modifier = Modifier.weight(1f),
                        label = WhiteDnsL10n.settingUsername,
                        value = settings.socksUsername,
                        onValueChange = { onSettingsChange(settings.copy(socksUsername = it)) },
                        placeholder = "master_dns_vpn",
                    )
                    WhiteDnsTextField(
                        modifier = Modifier.weight(1f),
                        label = WhiteDnsL10n.settingPassword,
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

    GroupLabel(WhiteDnsL10n.groupNetworkTuning)

    WhiteDnsDropdownField(
        label = WhiteDnsL10n.settingBalancingStrategy,
        value = settings.balancingStrategy,
        options = localizedBalancingStrategies(),
        onValueChange = { onSettingsChange(settings.copy(balancingStrategy = it)) },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingUploadDup,
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
            label = WhiteDnsL10n.settingDownloadDup,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsDropdownField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingUploadCompress,
            value = settings.uploadCompression,
            options = localizedCompressionTypes(),
            onValueChange = { onSettingsChange(settings.copy(uploadCompression = it)) },
        )
        WhiteDnsDropdownField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingDownloadCompress,
            value = settings.downloadCompression,
            options = localizedCompressionTypes(),
            onValueChange = { onSettingsChange(settings.copy(downloadCompression = it)) },
        )
    }
    ToggleRow(
        label = WhiteDnsL10n.settingBaseEncodeData,
        enabled = settings.baseEncodeData,
        onToggle = {
            onSettingsChange(settings.copy(baseEncodeData = !settings.baseEncodeData))
        },
    )

    SectionDivider()
    GroupLabel(WhiteDnsL10n.groupReliability)

    WhiteDnsTextField(
        label = WhiteDnsL10n.settingPingWatchdog,
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
        label = WhiteDnsL10n.settingTrafficWarmup,
        enabled = settings.trafficWarmupEnabled,
        onToggle = {
            onSettingsChange(settings.copy(trafficWarmupEnabled = !settings.trafficWarmupEnabled))
        },
    )
    AnimatedVisibility(
        visible = settings.trafficWarmupEnabled,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(160)),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            WhiteDnsTextField(
                modifier = Modifier.weight(1f),
                label = WhiteDnsL10n.settingWarmupProbes,
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
                label = WhiteDnsL10n.settingKeepalive,
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
        label = WhiteDnsL10n.settingLogLevel,
        value = settings.logLevel,
        options = WhiteDnsOptions.logLevels,
        onValueChange = { onSettingsChange(settings.copy(logLevel = it)) },
    )
}

private fun advancedProfileSummary(profile: AdvancedSettingsProfile): String {
    return "MTU ${profile.minUploadMtu}-${profile.maxUploadMtu}/${profile.minDownloadMtu}-${profile.maxDownloadMtu}, ${profile.logLevel}"
}

private fun advancedSaveAsInitialName(profile: AdvancedSettingsProfile, customAdvancedLabel: String, copySuffix: String): String {
    return if (profile.id == AdvancedSettingsProfile.DefaultId) {
        customAdvancedLabel
    } else {
        "${profile.name} $copySuffix"
    }
}

private fun AdvancedSettingsProfile.displayName(dirty: Boolean, modifiedSuffix: String): String {
    return if (dirty) {
        "$name $modifiedSuffix"
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
    editContentDescription: String,
    onEditClick: (() -> Unit)?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var query by rememberSaveable(title, visible) { mutableStateOf("") }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
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
            initialOffsetX = { it * direction },
        ) + fadeIn(animationSpec = tween(180)),
        exit = slideOutHorizontally(
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            targetOffsetX = { it * direction },
        ) + fadeOut(animationSpec = tween(160)),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .whiteDnsPageBackground()
                .testTag("home-selector")
                .semantics { paneTitle = title }
                .pointerInput(Unit) { detectTapGestures { /* Block the covered home controls. */ } },
        ) {
            val compactHeight = maxHeight < 240.dp
            Column(
                modifier = Modifier
                    .widthIn(max = WhiteDnsLayout.contentMaxWidth)
                    .fillMaxSize().align(Alignment.TopCenter)
                    .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding, vertical = if (compactHeight) 4.dp else 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("home-selector-header"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f).semantics { heading() },
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = WhiteDnsPalette.Ink,
                        ),
                    )
                    if (onEditClick != null) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Rounded.Edit, editContentDescription,
                                tint = WhiteDnsPalette.Muted, modifier = Modifier.size(20.dp))
                        }
                    }
                    WhiteDnsCloseButton(onClick = onDismiss, contentDescription = WhiteDnsL10n.cdCloseSelector)
                }
                Spacer(modifier = Modifier.height(if (compactHeight) 4.dp else WhiteDnsSpacing.md))
                val search: @Composable () -> Unit = {
                    WhiteDnsTextField(
                        label = WhiteDnsL10n.settingSearch, value = query, onValueChange = { query = it },
                        placeholder = searchPlaceholder, showLabel = !compactHeight,
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    search()
                    if (filteredItems.isEmpty()) {
                        Text(
                            text = if (normalizedQuery.isEmpty()) emptyMessage else
                                soarText("No profiles match your search.", "هیچ پروفایلی با جست‌وجوی شما مطابقت ندارد."),
                            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                        if (normalizedQuery.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            CompactActionButton(
                                label = soarText("Clear search", "پاک کردن جست‌وجو"),
                                emphasized = false, enabled = true, onClick = { query = "" },
                            )
                        }
                    } else {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(WhiteDnsPalette.Surface)
                                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(16.dp))
                                .selectableGroup(),
                        ) {
                            filteredItems.forEachIndexed { index, item ->
                                if (index > 0) HomeOptionDivider()
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
}

@Composable
private fun HomeSelectorSheetRow(
    item: HomeSelectorItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val resources = LocalResources.current
    val haptic = rememberHapticFeedback()
    val textAlignment = if (LocalLayoutDirection.current == LayoutDirection.Rtl) TextAlign.Right else TextAlign.Left

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = WhiteDnsLayout.minimumTouchTarget)
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface)
            .semantics {
                contentDescription = resources.getString(R.string.cd_select_profile_item, item.title)
            }
            .selectable(selected = selected, role = Role.RadioButton, onClick = {
                haptic.performLight()
                onClick()
            })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = textAlignment,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Content,
                ),
            )
            Text(
                text = item.detail,
                modifier = Modifier.fillMaxWidth(),
                textAlign = textAlignment,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    fontWeight = FontWeight.Normal,
                    textDirection = TextDirection.Content,
                ),
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ConnectionSetupSlider(
    connection: ConnectionProfile,
    connectionReady: Boolean,
    resolverProfile: ResolverProfile?,
    resolverCount: Int,
    resolversReady: Boolean,
    automaticDiscovery: Boolean,
    resolverIssue: String,
    connectionActionLabel: String,
    onConnectionClick: () -> Unit,
    onResolverClick: () -> Unit,
) {
    val nextStep = when {
        !connectionReady -> 0
        !resolversReady -> 1
        else -> 2
    }
    val pager = rememberPagerState(initialPage = nextStep) { 3 }
    val scope = rememberCoroutineScope()
    val stepLabels = listOf(
        WhiteDnsL10n.sectionConnection,
        WhiteDnsL10n.setupResolversLabel,
        soarText("Ready", "آماده"),
    )
    val completeLabel = soarText("Complete", "تکمیل‌شده")
    val incompleteLabel = soarText("Needs attention", "نیازمند تکمیل")
    val connectionIssue = when {
        connection.customServerDomain.isBlank() && connection.customServerEncryptionKey.isBlank() ->
            WhiteDnsL10n.setupServerRouteAndKey
        connection.customServerDomain.isBlank() -> WhiteDnsL10n.serverRouteMissing
        else -> WhiteDnsL10n.setupEncryptionKeyMissing
    }
    val resolverName = when {
        automaticDiscovery -> soarText("Automatic discovery", "کشف خودکار")
        resolverProfile == null -> WhiteDnsL10n.setupManualResolvers
        resolverProfile.id == ResolverProfile.DefaultId -> WhiteDnsL10n.setupDefaultResolver
        else -> resolverProfile.name
    }
    val resolverDetail = if (automaticDiscovery) {
        soarText("Resolvers are discovered when you connect.", "ریزالورها هنگام اتصال به‌صورت خودکار پیدا می‌شوند.")
    } else {
        resolverCountLabel(resolverCount, WhiteDnsL10n.resolverCountOneTemplate, WhiteDnsL10n.resolverCountTemplate)
    }

    // Follow saved configuration changes, while leaving manual browsing alone between changes.
    LaunchedEffect(nextStep, connection, resolverProfile, resolverCount, resolverIssue, automaticDiscovery) {
        pager.animateScrollToPage(nextStep)
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(22.dp))
            .padding(12.dp).testTag("setup-card"),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(WhiteDnsL10n.setupSectionSetup, style = MaterialTheme.typography.titleSmall,
                color = WhiteDnsPalette.Ink, modifier = Modifier.semantics { heading() })
            Text(soarText("Step ${pager.currentPage + 1} of 3", "مرحلهٔ ${pager.currentPage + 1} از ۳"),
                style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            stepLabels.forEachIndexed { index, label ->
                val complete = when (index) {
                    0 -> connectionReady
                    1 -> resolversReady
                    else -> connectionReady && resolversReady
                }
                val selected = pager.currentPage == index
                Column(
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) WhiteDnsPalette.AccentSurface else Color.Transparent)
                        .selectable(selected = selected, role = Role.Tab,
                            onClick = { scope.launch { pager.animateScrollToPage(index) } })
                        .semantics { stateDescription = if (complete) completeLabel else incompleteLabel }
                        .testTag("setup-step-$index").padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(modifier = Modifier.heightIn(min = 24.dp).widthIn(min = 24.dp)
                        .clip(CircleShape)
                        .background(if (complete) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.SurfaceAlt)
                        .padding(2.dp), contentAlignment = Alignment.Center) {
                        if (complete) {
                            Icon(Icons.Rounded.Check, contentDescription = null,
                                tint = WhiteDnsPalette.AccentText, modifier = Modifier.size(16.dp))
                        } else {
                            Text("${index + 1}", style = MaterialTheme.typography.labelSmall,
                                color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted)
                        }
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Ink,
                        textAlign = TextAlign.Center)
                }
            }
        }
        HorizontalPager(state = pager, verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().animateContentSize(tween(180)).testTag("setup-slider")) { page ->
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("setup-page-$page"),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val title = when (page) {
                    0 -> if (connectionReady) soarText("Connection ready", "اتصال آماده است")
                        else soarText("Set up your connection", "اتصال را تنظیم کنید")
                    1 -> if (resolversReady) soarText("Resolvers ready", "ریزالورها آماده‌اند")
                        else soarText("Set up your resolvers", "ریزالورها را تنظیم کنید")
                    else -> if (nextStep == 2) soarText("Ready to connect", "آمادهٔ اتصال")
                        else soarText("Complete your setup", "راه‌اندازی را کامل کنید")
                }
                Text(title, style = MaterialTheme.typography.titleSmall, color = WhiteDnsPalette.Ink,
                    modifier = Modifier.testTag("setup-slide-title-$page").semantics { heading() })
                val detail = when (page) {
                    0 -> if (connectionReady) connection.name else connectionIssue
                    1 -> if (resolversReady) "$resolverName · $resolverDetail" else resolverIssue
                    else -> if (nextStep == 2)
                        soarText("Your connection and resolvers are set. Tap Connect above to start.",
                            "اتصال و ریزالورها تنظیم شده‌اند. برای شروع، دکمهٔ اتصال بالا را بزنید.")
                        else if (!connectionReady) WhiteDnsL10n.setupAddConnectionSupportingText
                        else WhiteDnsL10n.setupAddResolverSupportingText
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Description)
                if (page == 0 && connectionReady) {
                    Text(connection.customServerDomain, style = MaterialTheme.typography.bodySmall.copy(
                        textDirection = TextDirection.Ltr), color = WhiteDnsPalette.Muted)
                }
                when (page) {
                    0 -> CompactActionButton(modifier = Modifier.fillMaxWidth().testTag("setup-action-0"),
                        label = connectionActionLabel, emphasized = !connectionReady, enabled = true,
                        onClick = onConnectionClick)
                    1 -> CompactActionButton(modifier = Modifier.fillMaxWidth().testTag("setup-action-1"),
                        label = when {
                            resolversReady -> soarText("Change resolvers", "تغییر ریزالورها")
                            resolverProfile != null && resolverProfile.id != ResolverProfile.DefaultId ->
                                soarText("Edit resolvers", "ویرایش ریزالورها")
                            else -> WhiteDnsL10n.setupAddResolver
                        }, emphasized = !resolversReady, enabled = true, onClick = onResolverClick)
                    else -> if (nextStep != 2) {
                        CompactActionButton(modifier = Modifier.fillMaxWidth().testTag("setup-action-2"),
                            label = soarText("Continue setup", "ادامهٔ راه‌اندازی"), emphasized = true, enabled = true,
                            onClick = { scope.launch { pager.animateScrollToPage(nextStep) } })
                    }
                }
            }
        }
    }
}

private fun resolverCountLabel(
    count: Int,
    singular: String,
    plural: String,
): String {
    return "$count ${if (count == 1) singular else plural}"
}

private data class ProfileTopAction(
    val label: String,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun ProfileTopActionMenu(
    actions: List<ProfileTopAction>,
    header: @Composable (@Composable () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = actions.any { it.enabled }
    val haptic = rememberHapticFeedback()
    val addLabel = soarText("+ Add", "+ افزودن")
    val description = soarText("Add profile options", "گزینه‌های افزودن پروفایل")
    val expandedState = if (expanded) soarText("Expanded", "باز شده") else soarText("Collapsed", "بسته")
    header {
        Box {
            Box(
                modifier = Modifier.testTag("profile-add-options")
                    .widthIn(min = 84.dp).heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WhiteDnsPalette.Surface)
                    .border(1.dp, if (enabled) WhiteDnsPalette.Accent else WhiteDnsPalette.Border, RoundedCornerShape(8.dp))
                    .semantics { contentDescription = description; stateDescription = expandedState }
                    .clickable(enabled = enabled, role = Role.Button) {
                        haptic.performLight()
                        expanded = true
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(addLabel, style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold,
                    color = if (enabled) WhiteDnsPalette.AccentText else WhiteDnsPalette.Disabled,
                ))
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.testTag("profile-add-menu").widthIn(min = 196.dp),
                shape = RoundedCornerShape(0.dp),
                containerColor = WhiteDnsPalette.Surface,
            ) {
                actions.forEach { action ->
                    DropdownMenuItem(
                        enabled = action.enabled,
                        text = { Text(action.label, style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp, lineHeight = 24.sp,
                            color = if (action.enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled,
                        )) },
                        onClick = {
                            expanded = false
                            haptic.performLight()
                            action.onClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ConnectionProfilesSettings(
    settings: WhiteDnsSettings,
    activeConnectionProfileId: String?,
    connectionStatus: ConnectionStatus,
    serverTestState: ServerTestState,
    onServerTestClick: (String?) -> Unit,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    onImportProfiles: suspend (String, Boolean) -> Result<Unit>,
    onRefreshSubscriptions: suspend () -> Result<Unit>,
    header: @Composable (@Composable () -> Unit) -> Unit = { it() },
) {
    val profiles = settings.normalizedConnectionProfiles()
    val selectedProfile = settings.selectedConnectionProfile()
    val customProfiles = profiles.filter { it.serverMode == "custom" }
    val serverTestScores = remember(serverTestState.results) { buildServerTestScores(serverTestState.results) }
    val serverTestResultsById = remember(serverTestState.results) {
        serverTestState.results.associateBy { it.serverId }
    }
    val context = LocalContext.current
    var dialogProfile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var revealProfileId by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var scannedSubscription by remember { mutableStateOf("") }
    var subscriptionBusy by remember { mutableStateOf(false) }
    val subscriptionScope = rememberCoroutineScope()
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
    val shareSubjectProfileLabel = WhiteDnsL10n.shareSubjectProfile
    val shareChooserProfileLabel = WhiteDnsL10n.shareChooserProfile
    val subscriptionSuccessLabel = soarText("Subscriptions refreshed", "ساب‌ها تازه‌سازی شدند")
    val importSuccessLabel = WhiteDnsL10n.profileImportSuccess
    val importErrorProfileLabel = WhiteDnsL10n.errorImportProfile
    var importNotice by remember { mutableStateOf<String?>(null) }
    var importNoticeIsError by remember { mutableStateOf(false) }
    val qrScanner = rememberQrProfileImportLauncher(
        onDecoded = { decodedLink ->
            if (decodedLink.startsWith("https://", true) || decodedLink.startsWith("http://", true)) {
                scannedSubscription = decodedLink
                showImportDialog = true
            } else runCatching {
                settings.importStormDnsProfileLinks(decodedLink)
            }.onSuccess { importedSettings ->
                importNoticeIsError = false
                importNotice = importSuccessLabel
                onSettingsChange(importedSettings)
            }.onFailure { error ->
                importNoticeIsError = true
                importNotice = error.message ?: importErrorProfileLabel
            }
        },
        onError = { message ->
            importNoticeIsError = true
            importNotice = message
        },
    )
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

    ProfileTopActionMenu(
        header = header,
        actions = listOf(
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnCreate,
                enabled = canManageProfiles,
                onClick = {
                    showCreateDialog = true
                },
            ),
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnImport,
                enabled = canManageProfiles,
                onClick = {
                    showImportDialog = true
                },
            ),
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnScanQr,
                enabled = canManageProfiles,
                onClick = qrScanner,
            ),
        )
    )
    val subscriptionSources = profiles.mapNotNull { it.subscription }.distinctBy { it.url }
    if (subscriptionSources.isNotEmpty()) {
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        CompactActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = if (subscriptionBusy) soarText("Refreshing…", "در حال تازه‌سازی…") else soarText("Refresh subscriptions", "تازه‌سازی ساب‌ها"),
            emphasized = false,
            enabled = !subscriptionBusy && connectionStatus == ConnectionStatus.DISCONNECTED,
            onClick = {
                subscriptionScope.launch {
                    subscriptionBusy = true
                    try {
                        onRefreshSubscriptions().onSuccess {
                            importNoticeIsError = false
                            importNotice = subscriptionSuccessLabel
                        }.onFailure {
                            importNoticeIsError = true
                            importNotice = it.message
                        }
                    } finally { subscriptionBusy = false }
                }
            },
        )
        Text(soarText("Automatic refresh when the app opens and the interval is due. Disconnect to refresh. Failed downloads keep your saved profiles.", "ساب‌ها هنگام بازشدن اپ و رسیدن زمان تازه‌سازی بررسی می‌شوند. برای تازه‌سازی قطع اتصال کنید. در خطای دریافت، پروفایل ذخیره‌شده حفظ می‌شود."),
            style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
        subscriptionSources.forEach { source ->
            val used = source.upload.toDouble() + source.download.toDouble()
            val usage = "%.2f / %s GiB".format(java.util.Locale.US, used / (1024 * 1024 * 1024), if (source.total == 0L) "∞" else "%.2f".format(java.util.Locale.US, source.total.toDouble() / (1024 * 1024 * 1024)))
            val dateFormat = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
            val expires = if (source.expiresAt == 0L) soarText("No expiry", "بدون انقضا") else dateFormat.format(java.util.Date(source.expiresAt))
            val updated = dateFormat.format(java.util.Date(source.updatedAt))
            Text("${java.net.URI(source.url).host} · $usage", style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
            Text(soarText("Expires: $expires · Updated: $updated", "انقضا: $expires · تازه‌سازی: $updated"), style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
        }
    }
    importNotice?.let { message ->
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = if (importNoticeIsError) WhiteDnsPalette.Error else WhiteDnsPalette.Success,
                fontWeight = FontWeight.Normal,
            ),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = WhiteDnsL10n.groupCustomConnections,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.SectionTitle,
                fontWeight = FontWeight.Bold,
            ),
        )
        ProfileActionsMenu(
            contentDescription = "${WhiteDnsL10n.groupCustomConnections}: ${WhiteDnsL10n.connectionProfileMenuActions}",
            alwaysEnabled = true,
            actions = listOf(
                ProfileMenuAction(
                    label = if (serverTestState.isRunning) {
                        WhiteDnsL10n.serverTestRunning
                    } else {
                        WhiteDnsL10n.serverTestButton
                    },
                    icon = Icons.Rounded.Speed,
                    contentDescription = WhiteDnsL10n.serverTestButton,
                    enabled = connectionStatus == ConnectionStatus.CONNECTED && !serverTestState.isRunning,
                    onClick = { onServerTestClick(null) },
                ),
                ProfileMenuAction(
                    label = WhiteDnsL10n.profileBtnDeleteDups,
                    icon = Icons.Rounded.Delete,
                    contentDescription = WhiteDnsL10n.profileBtnDeleteDups,
                    enabled = canManageProfiles && duplicateProfileCount > 0,
                    onClick = { showDeleteDuplicatesDialog = true },
                ),
                ProfileMenuAction(
                    label = WhiteDnsL10n.profileBtnExportAll,
                    icon = Icons.Rounded.Link,
                    contentDescription = WhiteDnsL10n.profileBtnExportAll,
                    enabled = customProfiles.any {
                        it.customServerDomain.isNotBlank() && it.customServerEncryptionKey.isNotBlank()
                    },
                    onClick = { showExportAllDialog = true },
                ),
            ),
        )
    }
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    if (serverTestState.results.isEmpty() && serverTestState.message.isNotBlank()) {
        Text(
            text = serverTestDisplayMessage(serverTestState),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
                fontWeight = FontWeight.Normal,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
    }
    if (customProfiles.isEmpty()) {
        Text(
            text = WhiteDnsL10n.customConnectionsEmpty,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
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
        val canTest = connectionStatus == ConnectionStatus.CONNECTED &&
            !serverTestState.isRunning &&
            profile.customServerDomain.isNotBlank() &&
            profile.customServerEncryptionKey.isNotBlank()
        val isDragging = profile.id == draggedProfileId
        val serverTestResult = serverTestResultsById[profile.id]
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
                reveal = profile.id == revealProfileId,
                onRevealed = { revealProfileId = null },
                reorderActions = profileReorderActions(
                    enabled = canManageProfiles, index = index, count = customProfiles.size,
                    onMove = { target -> onSettingsChange(settings.moveConnectionProfileToIndex(profile.id, target)) },
                ),
                selected = profile.id == selectedProfile.id,
                active = isActive,
                canUse = canManageProfiles && !isDragging,
                canEdit = canEdit,
                canDelete = canDelete,
                canTest = canTest,
                canDrag = canManageProfiles && customProfiles.size > 1,
                dragging = isDragging,
                serverTestResult = serverTestResult,
                serverTestScore = serverTestResult?.let { serverTestScores[it.serverId] },
                onUse = {
                    onSettingsChange(settings.selectConnectionProfile(profile.id))
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
                onExport = {
                    exportProfile = profile
                },
                onTest = {
                    onServerTestClick(profile.id)
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
                revealProfileId = profileId
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
            initialValue = scannedSubscription,
            onImport = { links, allowHttp ->
                onImportProfiles(links, allowHttp).onSuccess {
                    scannedSubscription = ""
                    showImportDialog = false
                }
            },
        )
    }

    exportProfile?.let { profile ->
        ConnectionProfileExportDialog(
            title = WhiteDnsL10n.profileDialogExportConnection,
            fieldLabel = WhiteDnsL10n.profileFieldProfileLinkSingle,
            linkResult = remember(settings, profile) {
                runCatching { settings.exportStormDnsProfileLink(profile) }
            },
            onDismiss = { exportProfile = null },
            onShare = { link ->
                shareProfileLink(context, link, shareSubjectProfileLabel, shareChooserProfileLabel)
            },
        )
    }

    if (showExportAllDialog) {
        ConnectionProfileExportDialog(
            title = WhiteDnsL10n.profileDialogExportAllConnections,
            fieldLabel = WhiteDnsL10n.profileFieldProfileLinksLabel,
            linkResult = remember(settings, showExportAllDialog) {
                runCatching { settings.exportAllStormDnsProfileLinks() }
            },
            onDismiss = { showExportAllDialog = false },
            onShare = { links ->
                shareProfileLink(context, links, shareSubjectProfileLabel, shareChooserProfileLabel)
            },
        )
    }

    if (showDeleteDuplicatesDialog) {
        DeleteProfileConfirmationDialog(
            title = WhiteDnsL10n.deleteDupsTitle,
            message = if (duplicateProfileCount == 1) {
                WhiteDnsL10n.deleteDupsMessageSingleConnection
            } else {
                WhiteDnsL10n.deleteDupsMessageManyConnection
            },
            confirmLabel = WhiteDnsL10n.dialogDeleteConfirm,
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
            title = WhiteDnsL10n.deleteConnectionTitle,
            message = WhiteDnsL10n.deleteConnectionMessageTemplate,
            confirmLabel = WhiteDnsL10n.dialogDeleteConfirm,
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
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    header: @Composable (@Composable () -> Unit) -> Unit = { it() },
) {
    val profiles = settings.normalizedResolverProfiles()
    val selectedProfile = settings.selectedResolverProfile()
    val context = LocalContext.current
    var dialogProfile by remember { mutableStateOf<ResolverProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var revealProfileId by remember { mutableStateOf<String?>(null) }
    var showExportAllDialog by remember { mutableStateOf(false) }
    var deleteProfile by remember { mutableStateOf<ResolverProfile?>(null) }
    val canChangeProfiles = connectionStatus != ConnectionStatus.CONNECTING
    val shareChooserResolversLabel = WhiteDnsL10n.shareChooserResolvers
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

    ProfileTopActionMenu(
        header = header,
        actions = listOf(
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnCreate,
                enabled = canChangeProfiles,
                onClick = { showCreateDialog = true },
            ),
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnSaveCurrent,
                enabled = canChangeProfiles && settings.resolverText.isNotBlank(),
                onClick = {
                    showCreateDialog = true
                },
            ),
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnExportAll,
                enabled = profiles.isNotEmpty(),
                onClick = {
                    showExportAllDialog = true
                },
            ),
        )
    )

    if (profiles.isEmpty()) {
        Text(
            text = WhiteDnsL10n.profileNoResolverLists,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
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
                reveal = profile.id == revealProfileId,
                onRevealed = { revealProfileId = null },
                reorderActions = profileReorderActions(
                    enabled = canChangeProfiles && !isDefaultProfile, index = profiles.filter { it.id != ResolverProfile.DefaultId }.indexOfFirst { it.id == profile.id }, count = profiles.count { it.id != ResolverProfile.DefaultId },
                    onMove = { target -> onSettingsChange(settings.moveResolverProfileToIndex(profile.id, target)) },
                ),
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
                val updated = settings.upsertResolverProfile(profile)
                revealProfileId = updated.normalizedResolverProfiles().last().id
                onSettingsChange(updated)
                showCreateDialog = false
            },
        )
    }

    if (showExportAllDialog) {
        ResolverProfilesExportDialog(
            settings = settings,
            onDismiss = { showExportAllDialog = false },
            onShare = { exportFile ->
                shareCachedExportFile(
                    context = context,
                    exportFile = exportFile,
                    subject = ResolverProfilesExportFileName,
                    chooserTitle = shareChooserResolversLabel,
                )
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
            title = WhiteDnsL10n.deleteResolverTitle,
            message = WhiteDnsL10n.deleteResolverMessageTemplate,
            confirmLabel = WhiteDnsL10n.dialogDeleteConfirm,
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
    header: @Composable (@Composable () -> Unit) -> Unit = { it() },
) {
    val profiles = settings.normalizedAdvancedProfiles()
    val defaultProfile = profiles.first()
    val customProfiles = profiles.filter { it.id != AdvancedSettingsProfile.DefaultId }
    val selectedProfile = settings.selectedAdvancedProfile()
    val context = LocalContext.current
    val shareChooserAdvancedSettingsLabel = WhiteDnsL10n.shareChooserAdvancedSettings
    var showCreateDialog by remember { mutableStateOf(false) }
    var revealProfileId by remember { mutableStateOf<String?>(null) }
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

    ProfileTopActionMenu(
        header = header,
        actions = listOf(
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnCreate,
                enabled = canManageProfiles,
                onClick = { showCreateDialog = true },
            ),
            ProfileTopAction(
                label = WhiteDnsL10n.profileBtnImport,
                enabled = canManageProfiles,
                onClick = { showImportDialog = true },
            ),
        )
    )

    GroupLabel(WhiteDnsL10n.groupDefault)
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

    GroupLabel(WhiteDnsL10n.groupCustomSettings)
    if (customProfiles.isEmpty()) {
        Text(
            text = WhiteDnsL10n.profileNoSettingProfiles,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
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
                reveal = profile.id == revealProfileId,
                onRevealed = { revealProfileId = null },
                reorderActions = profileReorderActions(
                    enabled = canManageProfiles, index = index, count = customProfiles.size,
                    onMove = { target -> onSettingsChange(settings.moveAdvancedProfileToIndex(profile.id, target)) },
                ),
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
            initialName = advancedSaveAsInitialName(selectedProfile, WhiteDnsL10n.homeSelectorCustomAdvanced, WhiteDnsL10n.profileNameCopySuffix),
            initialSettings = settings,
            onDismiss = { showCreateDialog = false },
            onSave = { profile ->
                val updated = settings.upsertAdvancedProfile(profile)
                revealProfileId = updated.normalizedAdvancedProfiles().last().id
                onSettingsChange(updated)
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
            title = WhiteDnsL10n.profileDialogExportSettings,
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
                shareAdvancedSettingsToml(context, toml, shareChooserAdvancedSettingsLabel)
            },
        )
    }

    deleteProfile?.let { profile ->
        DeleteProfileConfirmationDialog(
            title = WhiteDnsL10n.deleteSettingTitle,
            message = WhiteDnsL10n.deleteSettingMessageTemplate,
            confirmLabel = WhiteDnsL10n.dialogDeleteConfirm,
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

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(
                if (profile == null) WhiteDnsL10n.profileDialogCreateSetting else WhiteDnsL10n.profileDialogEditSetting,
                onDismiss,
            )
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false))
                    .dialogBodyScroll(),
            ) {
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldName,
                value = name,
                onValueChange = { name = it },
                placeholder = WhiteDnsL10n.settingProfileFastTunnelPlaceholder,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
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
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnSave,
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
    reorderActions: List<CustomAccessibilityAction> = emptyList(),
    reveal: Boolean = false,
    onRevealed: () -> Unit = {},
) {
    val summarySuffix = when {
        dirty -> " - ${WhiteDnsL10n.profileStatusModified}"
        selected -> " - ${WhiteDnsL10n.profileStatusSelected}"
        else -> ""
    }
    val displayName = if (profile.id == AdvancedSettingsProfile.DefaultId) WhiteDnsL10n.setupDefaultAdvanced else profile.name
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(profileRevealModifier(reveal, onRevealed))
            .semantics { customActions = reorderActions }
            .selectable(selected = selected, enabled = canUse, role = Role.RadioButton, onClick = onUse)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.SurfaceAlt)
            .border(
                1.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.18f) else Color.Transparent,
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Ink,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = "${advancedProfileSummary(profile)}$summarySuffix",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = when {
                            dirty -> WhiteDnsPalette.WarningText
                            selected -> WhiteDnsPalette.AccentText
                            else -> WhiteDnsPalette.Muted
                        },
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileActionsMenu(
                contentDescription = WhiteDnsL10n.settingProfileMenuActions,
                actions = listOf(
                    ProfileMenuAction(
                        label = if (selected) WhiteDnsL10n.profileMenuUseSelected else WhiteDnsL10n.profileMenuUse,
                        icon = Icons.Rounded.Check,
                        contentDescription = WhiteDnsL10n.useSettingProfile,
                        enabled = canUse,
                        onClick = onUse,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuExport,
                        icon = Icons.Rounded.Link,
                        contentDescription = WhiteDnsL10n.exportSettingProfileAction,
                        enabled = true,
                        onClick = onExport,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuEdit,
                        icon = Icons.Rounded.Edit,
                        contentDescription = WhiteDnsL10n.editSettingProfileAction,
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuDelete,
                        icon = Icons.Rounded.Delete,
                        contentDescription = WhiteDnsL10n.deleteSettingProfileAction,
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
    val invalidResolverIpTemplate = WhiteDnsL10n.errorInvalidResolverIpTemplate
    val enterValidResolverIpLabel = WhiteDnsL10n.errorEnterValidResolverIp
    val enterProfileNameToSaveLabel = WhiteDnsL10n.errorEnterProfileNameToSave
    val resolverValidSingularTemplate = WhiteDnsL10n.resolverValidSingularTemplate
    val resolverValidPluralTemplate = WhiteDnsL10n.resolverValidPluralTemplate
    val unableToOpenResolverLabel = WhiteDnsL10n.errorUnableToOpenResolverFile
    val noResolverEntriesLabel = WhiteDnsL10n.errorNoResolverEntries
    val validationMessage = resolverValidationMessage(
        name = name,
        resolverText = resolverText,
        invalidEntries = resolverValidation.invalidEntries,
        validResolverCount = resolverValidation.normalizedResolvers.size,
        invalidResolverIpTemplate = invalidResolverIpTemplate,
        enterValidResolverIpLabel = enterValidResolverIpLabel,
        enterProfileNameToSaveLabel = enterProfileNameToSaveLabel,
        validResolverSingularTemplate = resolverValidSingularTemplate,
        validResolverPluralTemplate = resolverValidPluralTemplate,
    )
    val validationMessageIsError = validationMessage != null && (!resolverValidation.isValid || name.isBlank())
    val errorImportResolverLabel = WhiteDnsL10n.errorImportResolver
    val importResolverFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        readResolverTextFromUri(
            context = context,
            uri = uri,
            unableToOpenLabel = unableToOpenResolverLabel,
            invalidResolverIpTemplate = invalidResolverIpTemplate,
            noResolverEntriesLabel = noResolverEntriesLabel,
        )
            .onSuccess { importedResolverText ->
                resolverText = importedResolverText
                importError = null
            }
            .onFailure { error ->
                importError = error.message ?: errorImportResolverLabel
            }
    }
    val canSave = name.trim().isNotEmpty() && resolverValidation.isValid

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(
                if (profile == null) WhiteDnsL10n.profileDialogCreateResolver else WhiteDnsL10n.profileDialogEditResolver,
                onDismiss,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Column(Modifier.fillMaxWidth().then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false)).dialogBodyScroll()) {
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldName,
                value = name,
                onValueChange = { name = it },
                placeholder = WhiteDnsL10n.resolverProfileHomeResolversPlaceholder,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.profileBtnImportFile,
                    emphasized = false,
                    enabled = true,
                    onClick = {
                        importResolverFileLauncher.launch(ResolverImportMimeTypes)
                    },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.profileBtnClear,
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
                        fontSize = 12.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldResolvers,
                rawValue = true,
                value = resolverText,
                onValueChange = {
                    resolverText = it
                    importError = null
                },
                placeholder = WhiteDnsL10n.resolverFieldPlaceholder,
                singleLine = false,
                minLines = 6,
                maxLines = 10,
            )
            validationMessage?.let { message ->
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = if (validationMessageIsError) WhiteDnsPalette.Error else WhiteDnsPalette.Muted,
                    ),
                )
            }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnSave,
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
    reorderActions: List<CustomAccessibilityAction> = emptyList(),
    reveal: Boolean = false,
    onRevealed: () -> Unit = {},
) {
    val resolverCount = profile.resolverText
        .let { validateResolverText(it).normalizedResolvers.size }
    val resolverSummary = WhiteDnsL10n.resolverProfileSummary(resolverCount) +
        if (selected) " - ${WhiteDnsL10n.profileStatusSelected}" else ""
    val displayName = if (profile.id == ResolverProfile.DefaultId) WhiteDnsL10n.setupDefaultResolver else profile.name
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(profileRevealModifier(reveal, onRevealed))
            .semantics { customActions = reorderActions }
            .selectable(selected = selected, enabled = canUse, role = Role.RadioButton, onClick = onUse)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.SurfaceAlt)
            .border(
                1.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.18f) else Color.Transparent,
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Ink,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = resolverSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileActionsMenu(
                contentDescription = WhiteDnsL10n.resolverProfileMenuActions,
                actions = listOf(
                    ProfileMenuAction(
                        label = if (selected) WhiteDnsL10n.profileMenuUseSelected else WhiteDnsL10n.profileMenuUse,
                        icon = Icons.Rounded.Check,
                        contentDescription = WhiteDnsL10n.useResolverProfile,
                        enabled = canUse,
                        onClick = onUse,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuEdit,
                        icon = Icons.Rounded.Edit,
                        contentDescription = WhiteDnsL10n.editResolverProfileAction,
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuDelete,
                        icon = Icons.Rounded.Delete,
                        contentDescription = WhiteDnsL10n.deleteResolverProfileAction,
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
    onImport: suspend (String, Boolean) -> Result<Unit>,
    initialValue: String = "",
) {
    var profileLinks by remember { mutableStateOf(initialValue) }
    var importError by remember { mutableStateOf<String?>(null) }
    val errorRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(importError) {
        if (importError != null) {
            withFrameNanos { }
            errorRequester.bringIntoView()
        }
    }
    var busy by remember { mutableStateOf(false) }
    var allowHttp by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isHttp = profileLinks.trim().startsWith("http://", true)
    val canImport = !busy && profileLinks.trim().isNotEmpty() && (!isHttp || allowHttp)
    val errorImportProfileLabel = WhiteDnsL10n.errorImportProfile
    val qrScanner = rememberQrProfileImportLauncher(
        onDecoded = { decodedLink ->
            profileLinks = decodedLink
            importError = null

        },
        onError = { message ->
            importError = message
        },
    )

    WhiteDnsDialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            Column(Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Text(
                text = WhiteDnsL10n.profileDialogImportConnection,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            WhiteDnsTextField(
                label = WhiteDnsL10n.profileFieldProfileLinks,
                rawValue = true,
                value = profileLinks,
                onValueChange = {
                    if (!busy) profileLinks = it
                    importError = null
                },
                placeholder = "https://panel.example.com/sub/...\nsoar://...",
                singleLine = false,
                minLines = 5,
                maxLines = 9,
            )
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = WhiteDnsL10n.profileBtnScanQr,
                emphasized = false,
                enabled = !busy,
                onClick = qrScanner,
            )
            if (isHttp) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .toggleable(value = allowHttp, enabled = !busy, role = Role.Checkbox,
                        onValueChange = { allowHttp = it }), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = allowHttp, onCheckedChange = null, enabled = !busy,
                        modifier = Modifier.clearAndSetSemantics {})
                    Text(soarText("Allow unencrypted HTTP for this subscription. Its address and keys can be exposed. HTTPS is recommended.", "اجازهٔ HTTP بدون رمزنگاری برای این ساب؛ آدرس و کلیدها ممکن است افشا شوند. HTTPS پیشنهاد می‌شود."), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
            if (busy) Text(soarText("Downloading subscription…", "در حال دریافت ساب…"), color = WhiteDnsPalette.Muted)
            importError?.let { message ->
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.iconSpacing))
                Text(
                    text = message,
                    modifier = Modifier.bringIntoViewRequester(errorRequester)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = WhiteDnsPalette.Error,
                    ),
                )
            }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = !busy,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnImport,
                    emphasized = true,
                    enabled = canImport,
                    onClick = {
                        scope.launch {
                            busy = true
                            try {
                                onImport(profileLinks, allowHttp).onFailure { error ->
                                    importError = error.message ?: errorImportProfileLabel
                                }
                            } finally { busy = false }
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
    val link = linkResult.getOrNull()

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(title, onDismiss)
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            BoxWithConstraints(Modifier.fillMaxWidth().then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false))) {
                val qrSize = minOf(210.dp, maxWidth, (maxHeight - 8.dp).coerceAtLeast(0.dp))
                Column(Modifier.fillMaxWidth().dialogBodyScroll()) {
                    if (link != null) {
                        if (showQr && !link.contains('\n')) {
                            ProfileQrPreview(link, qrSize)
                            Spacer(Modifier.height(WhiteDnsSpacing.md))
                        }
                        WhiteDnsTextField(
                            label = fieldLabel, value = link, onValueChange = {}, placeholder = placeholder,
                            singleLine = false, minLines = if (link.contains('\n')) 7 else 5, maxLines = 12,
                            rawValue = true, readOnly = true,
                        )
                    } else {
                        Text(linkResult.exceptionOrNull()?.message ?: WhiteDnsL10n.errorExportProfile,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = WhiteDnsPalette.Error))
                    }
                }
            }
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            if (link != null) {
                val labels = listOf(WhiteDnsL10n.btnClose, WhiteDnsL10n.btnCopy, WhiteDnsL10n.btnShare)
                val measurer = rememberTextMeasurer()
                val style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal)
                val density = LocalDensity.current
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val labelWidth = (maxWidth - 16.dp) / 3 - 24.dp
                    val fitInRow = labels.all {
                        measurer.measure(it, style, softWrap = false).size.width <= with(density) { labelWidth.toPx() }
                    }
                    FlowRow(maxItemsInEachRow = if (fitInRow) 3 else 1,
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        CompactActionButton(labels[0], emphasized = false, enabled = true,
                            modifier = Modifier.weight(1f), onClick = onDismiss)
                        CompactActionButton(labels[1], emphasized = false, enabled = true,
                            modifier = Modifier.weight(1f), onClick = {
                                copyTextToClipboard(context, fieldLabel, link, sensitive = true)
                            })
                        CompactActionButton(labels[2], emphasized = true, enabled = true,
                            modifier = Modifier.weight(1f), onClick = { onShare(link) })
                    }
                }
            } else {
                CompactActionButton(WhiteDnsL10n.btnClose, emphasized = true, enabled = true,
                    modifier = Modifier.fillMaxWidth(), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ResolverProfilesExportDialog(
    settings: WhiteDnsSettings,
    onDismiss: () -> Unit,
    onShare: (File) -> Unit,
) {
    val context = LocalContext.current
    val errorExportProfileLabel = WhiteDnsL10n.errorExportProfile
    var exportState by remember(settings) { mutableStateOf(ResolverProfilesExportDialogState()) }

    LaunchedEffect(settings) {
        exportState = ResolverProfilesExportDialogState()
        val result = withContext(Dispatchers.IO) {
            runCatching {
                writeResolverProfilesExport(
                    context = context.applicationContext,
                    settings = settings,
                )
            }
        }
        exportState = result.fold(
            onSuccess = { exportResult ->
                ResolverProfilesExportDialogState(
                    saving = false,
                    resolverCount = exportResult.resolverCount,
                    savedLocation = exportResult.savedLocation,
                    shareFile = exportResult.shareFile,
                )
            },
            onFailure = { error ->
                ResolverProfilesExportDialogState(
                    saving = false,
                    errorMessage = error.message ?: errorExportProfileLabel,
                )
            },
        )
    }

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = WhiteDnsL10n.profileDialogExportAllResolvers,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            when {
                exportState.saving -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = WhiteDnsPalette.Accent,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = WhiteDnsL10n.profileExportSavingFile,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                    CompactActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = WhiteDnsL10n.btnClose,
                        emphasized = false,
                        enabled = true,
                        onClick = onDismiss,
                    )
                }
                exportState.errorMessage != null -> {
                    Text(
                        text = exportState.errorMessage ?: WhiteDnsL10n.errorExportProfile,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = WhiteDnsPalette.Error,
                        ),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                    CompactActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = WhiteDnsL10n.btnClose,
                        emphasized = true,
                        enabled = true,
                        onClick = onDismiss,
                    )
                }
                else -> {
                    Text(
                        text = WhiteDnsL10n.profileExportResolverTotalTemplate.format(
                            WhiteDnsL10n.resolverProfileSummary(exportState.resolverCount),
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = WhiteDnsPalette.Ink,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
                    Text(
                        text = WhiteDnsL10n.profileExportSavedToTemplate.format(exportState.savedLocation),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = WhiteDnsPalette.Muted,
                        ),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompactActionButton(
                            modifier = Modifier.weight(1f),
                            label = WhiteDnsL10n.btnClose,
                            emphasized = false,
                            enabled = true,
                            onClick = onDismiss,
                        )
                        CompactActionButton(
                            modifier = Modifier.weight(1f),
                            label = WhiteDnsL10n.btnShare,
                            emphasized = true,
                            enabled = exportState.shareFile != null,
                            onClick = {
                                exportState.shareFile?.let(onShare)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileQrPreview(link: String, size: Dp = 210.dp) {
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
                contentDescription = WhiteDnsL10n.cdProfileQrCode,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WhiteDnsPalette.OnAccent)
                    .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(12.dp))
                    .padding(8.dp),
            )
        } else {
            Text(
                text = WhiteDnsL10n.profileQrUnavailable,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Error,
                ),
            )
        }
    }
}

@Composable
private fun rememberQrProfileImportLauncher(
    onDecoded: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val noCodeMessage = WhiteDnsL10n.qrScanNoCode
    val cancelledMessage = WhiteDnsL10n.qrScanCancelled
    var capturePath by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val currentOnDecoded by rememberUpdatedState(onDecoded)
    val currentOnError by rememberUpdatedState(onError)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val path = capturePath
        if (!captured || path == null) {
            path?.let { File(it).delete() }
            capturePath = null
            currentOnError(cancelledMessage)
        } else {
            scope.launch {
                val photo = File(path)
                try {
                    val result = withContext(Dispatchers.IO) { decodeQrPhoto(photo) }
                    result.onSuccess(currentOnDecoded).onFailure { currentOnError(noCodeMessage) }
                } finally {
                    photo.delete()
                    capturePath = null
                }
            }
        }
    }
    return remember(context, launcher, cancelledMessage) {
        capture@{
            if (capturePath != null) return@capture
            runCatching {
                val directory = File(context.cacheDir, CacheExportDirectory).apply { mkdirs() }
                cleanOldCacheExports(directory)
                val photo = File.createTempFile("qr-capture-", ".jpg", directory)
                capturePath = photo.absolutePath
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo)
                launcher.launch(uri)
            }.onFailure {
                capturePath?.let { File(it).delete() }
                capturePath = null
                currentOnError(cancelledMessage)
            }
        }
    }
}

// Camera thumbnails lose the small modules in exported profile QR codes.
internal fun decodeQrPhoto(photo: File): Result<String> = runCatching {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(photo.absolutePath, options)
    require(options.outWidth > 0 && options.outHeight > 0) { "Unable to read QR photo" }
    var sample = 1
    while (options.outWidth / sample > 2048 || options.outHeight / sample > 2048) sample *= 2
    options.inSampleSize = sample
    options.inJustDecodeBounds = false
    val bitmap = requireNotNull(BitmapFactory.decodeFile(photo.absolutePath, options))
    try {
        decodeQrBitmap(bitmap).getOrThrow()
    } finally {
        bitmap.recycle()
    }
}

private fun decodeQrBitmap(bitmap: Bitmap): Result<String> {
    return runCatching {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.CHARACTER_SET to "UTF-8",
        )
        MultiFormatReader().decode(binaryBitmap, hints).text
            .trim()
            .takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException("QR code is empty")
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
    var engine by remember(profile?.id) {
        mutableStateOf(DnsClientEngine.normalize(profile?.engine ?: DnsClientEngine.StormDns))
    }
    var cotten by remember(profile?.id) {
        mutableStateOf(profile?.cottenSettings?.normalized() ?: CottenDnsProfileSettings())
    }
    var soar by remember(profile?.id) { mutableStateOf(profile?.soarSettings?.normalized() ?: SoarProfileSettings()) }
    val effectiveEngine = DnsClientEngine.normalize(engine)
    val canSave = name.isNotBlank() && !(effectiveEngine == DnsClientEngine.CottenDns && cotten.hasUnsupportedMaxRateBudget) && if (effectiveEngine == DnsClientEngine.Soar) {
        isValidSoarServer(domain, encryptionKey)
    } else ServerDomains.isValid(domain) && encryptionKey.isNotBlank()

    // Bound the dialog to the viewport rather than a fixed height: a tall phone,
    // a tablet, landscape and large font scaling all give very different room,
    // and anything taller than the screen clips the Save button off the bottom.
    val maxDialogHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } * 0.9f

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.heightIn(max = maxDialogHeight))
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(
                if (profile == null) WhiteDnsL10n.profileDialogCreateConnection else WhiteDnsL10n.profileDialogEditConnection,
                onDismiss,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            // Takes the space left over after the title and the Cancel/Save row,
            // so those two stay reachable at any height. fill = false lets a
            // short profile still render a compact dialog.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false))
                    .dialogBodyScroll(),
                verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.inputSpacing),
            ) {
                // Editable on existing profiles too. Gating this to creation meant an
                // existing profile could never be moved to CottenDNS, and so could
                // never reach the CottenDNS settings below without being deleted and
                // recreated from scratch.
                WhiteDnsDropdownField(
                    label = WhiteDnsL10n.profileFieldEngine,
                    value = engine,
                    options = localizedDnsClientEngines(),
                    onValueChange = { engine = it },
                )
                WhiteDnsTextField(
                    label = WhiteDnsL10n.profileFieldName,
                    value = name,
                    onValueChange = { name = it },
                    placeholder = WhiteDnsL10n.profileMyStormDnsPlaceholder,
                )
                WhiteDnsTextField(
                    label = if (effectiveEngine == DnsClientEngine.Soar) soarText("Tunnel domain (one zone)", "دامنهٔ تونل (یک زیردامنه)") else WhiteDnsL10n.profileFieldDomain,
                    // Not trimmed on input: a comma-separated list needs the space
                    // after each comma to survive while it is being typed.
                    value = domain,
                    rawValue = true,
                    onValueChange = { domain = it },
                    placeholder = WhiteDnsL10n.profileDomainPlaceholder,
                )
                WhiteDnsTextField(
                    label = if (effectiveEngine == DnsClientEngine.Soar) soarText("Server PUBLIC key", "کلید عمومی سرور") else WhiteDnsL10n.profileFieldEncryptionKey,
                    value = encryptionKey,
                    onValueChange = { encryptionKey = it.trim() },
                    placeholder = if (effectiveEngine == DnsClientEngine.Soar) "Base64 Ed25519 public key" else WhiteDnsL10n.profileEncryptionKeyPlaceholder,
                    rawValue = true,
                )
                if (effectiveEngine != DnsClientEngine.Soar) {
                    WhiteDnsDropdownField(
                        label = WhiteDnsL10n.profileFieldEncryptionMethod,
                        value = encryptionMethod,
                        options = localizedEncryptionMethods(),
                        onValueChange = { encryptionMethod = it },
                    )
                } else {
                    SoarSettingsPanel(soar) { soar = it }
                    if (!isValidSoarServer(domain, encryptionKey)) {
                        Text(soarText("Enter one ASCII domain (up to 45 characters) and the server public key from soar-manager. Do not use the private key.",
                            "یک دامنهٔ ASCII تا ۴۵ نویسه و کلید عمومی خروجی soar-manager را وارد کنید؛ کلید خصوصی را وارد نکنید."),
                            style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
                    }
                }
                if (effectiveEngine == DnsClientEngine.CottenDns) {
                    Text(
                        text = WhiteDnsL10n.profileCottenSectionTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = WhiteDnsPalette.FieldLabel,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    WhiteDnsDropdownField(
                        label = WhiteDnsL10n.profileFieldConfigPreset,
                        value = cotten.configPreset,
                        options = localizedCottenConfigPresets(),
                        onValueChange = { cotten = cotten.copy(configPreset = it) },
                    )
                    if (!cotten.isCompatibility) {
                        WhiteDnsDropdownField(
                            label = "CottenDNS mode",
                            value = cotten.tunnelMode,
                            options = listOf(Choice("legacy", WhiteDnsL10n.cottenDnsNormalMode), Choice("cottenmax", WhiteDnsL10n.cottenMaxBetaMode)),
                            onValueChange = { cotten = cotten.copy(tunnelMode = it) },
                        )
                        Text(WhiteDnsL10n.cottenMaxSharedCredentialHint, style = MaterialTheme.typography.bodySmall)
                        Text(WhiteDnsL10n.cottenMaxReconnectHint, style = MaterialTheme.typography.bodySmall)
                        if (cotten.usesCottenMax) {
                            WhiteDnsDropdownField(
                                label = WhiteDnsL10n.cottenMaxTrafficProfileLabel,
                                value = cotten.cottenMaxOptions.trafficProfile,
                                options = listOf(Choice("full", WhiteDnsL10n.cottenMaxTrafficFull), Choice("low", WhiteDnsL10n.cottenMaxTrafficLow)),
                                onValueChange = { cotten = cotten.copy(cottenMaxOptions = cotten.cottenMaxOptions.copy(trafficProfile = it)) },
                            )
                            Text(WhiteDnsL10n.cottenMaxTrafficProfileHint, style = MaterialTheme.typography.bodySmall)
                            if (cotten.cottenMaxOptions.trafficProfile == "low") {
                                CottenMaxLowReductionSlider(cotten.cottenMaxOptions.lowReductionPercent) {
                                    cotten = cotten.copy(cottenMaxOptions = cotten.cottenMaxOptions.copy(lowReductionPercent = it))
                                }
                            }
                            Text(WhiteDnsL10n.cottenMaxCipherHint, style = MaterialTheme.typography.bodySmall)
                            Text(WhiteDnsL10n.cottenMaxAdaptiveHint, style = MaterialTheme.typography.bodySmall)
                        }
                        CottenDnsOptionalConnectionFields(cotten, profile?.id.orEmpty()) { cotten = it }
                    }
                    CottenDnsBackgroundScanSlider(
                        parallelism = cotten.backgroundScanParallelism,
                        onParallelismChange = { cotten = cotten.copy(backgroundScanParallelism = it) },
                    )
                    // Client-side only, so it stays available in Compatibility too.
                    WhiteDnsDropdownField(
                        label = WhiteDnsL10n.profileFieldQueryRateLimit,
                        value = cotten.queryRateLimit,
                        options = localizedCottenQueryRateLimits(),
                        onValueChange = { cotten = cotten.copy(queryRateLimit = it) },
                    )
                    if (cotten.hasUnsupportedMaxRateBudget) {
                        Text("CottenMax does not support a hard DNS query-rate cap yet. Select Normal mode to keep the cap, or explicitly turn the cap off before saving Max.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (cotten.queryRateLimit != CottenDnsProfileSettings.RateOff) {
                        WhiteDnsDropdownField(
                            label = WhiteDnsL10n.profileFieldQueryRateScope,
                            value = cotten.queryRateScope,
                            options = localizedCottenQueryRateScopes(),
                            onValueChange = { cotten = cotten.copy(queryRateScope = it) },
                        )
                    }
                    WhiteDnsDropdownField(
                        label = WhiteDnsL10n.profileFieldTimingMask,
                        value = cotten.timingMask,
                        options = localizedCottenTimingMasks(),
                        onValueChange = { cotten = cotten.copy(timingMask = it) },
                    )
                    Text(
                        text = WhiteDnsL10n.cottenRateLimitHint,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = WhiteDnsPalette.Muted,
                        ),
                    )
                    // Compatibility pins TXT/UDP/63 regardless, so the overrides
                    // below would be misleading if they stayed interactive.
                    if (!cotten.isCompatibility) {
                        WhiteDnsDropdownField(
                            label = WhiteDnsL10n.profileFieldTransportMode,
                            value = cotten.transportMode,
                            options = localizedCottenTransportModes(),
                            onValueChange = { cotten = cotten.copy(transportMode = it) },
                        )
                        WhiteDnsDropdownField(
                            label = WhiteDnsL10n.profileFieldDeliveryMode,
                            value = cotten.deliveryMode,
                            options = localizedCottenDeliveryModes(),
                            onValueChange = { cotten = cotten.copy(deliveryMode = it) },
                        )
                        WhiteDnsDropdownField(
                            label = WhiteDnsL10n.profileFieldIpMode,
                            value = cotten.ipMode,
                            options = localizedCottenIpModes(),
                            onValueChange = { cotten = cotten.copy(ipMode = it) },
                        )
                        WhiteDnsDropdownField(
                            label = WhiteDnsL10n.profileFieldQnameMode,
                            value = cotten.qnameMode,
                            options = localizedCottenQnameModes(),
                            onValueChange = { cotten = cotten.copy(qnameMode = it) },
                        )
                        Text(
                            text = WhiteDnsL10n.cottenOverrideHint,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = WhiteDnsPalette.Muted,
                            ),
                        )
                        if (cotten.transportMode in setOf("dot", "doh", "doh3", "doq")) {
                            WhiteDnsTextField(
                                label = WhiteDnsL10n.profileFieldResolverTlsServerName,
                                rawValue = true,
                                value = cotten.resolverTlsServerName,
                                onValueChange = { cotten = cotten.copy(resolverTlsServerName = it.trim()) },
                                placeholder = WhiteDnsL10n.profileResolverTlsServerNamePlaceholder,
                            )
                            WhiteDnsTextField(
                                label = WhiteDnsL10n.profileFieldResolverTlsPin,
                                rawValue = true,
                                value = cotten.resolverTlsPin,
                                onValueChange = { cotten = cotten.copy(resolverTlsPin = it.trim()) },
                                placeholder = WhiteDnsL10n.profileResolverTlsPinPlaceholder,
                            )
                            if (cotten.transportMode == "dot" || cotten.transportMode == "doq") {
                                WhiteDnsTextField(
                                    label = WhiteDnsL10n.profileFieldResolverDoTPort,
                                    value = cotten.resolverDoTPort,
                                    onValueChange = { cotten = cotten.copy(resolverDoTPort = it.trim()) },
                                    placeholder = "853",
                                )
                            } else {
                                WhiteDnsTextField(
                                    label = WhiteDnsL10n.profileFieldResolverDoHPort,
                                    value = cotten.resolverDoHPort,
                                    onValueChange = { cotten = cotten.copy(resolverDoHPort = it.trim()) },
                                    placeholder = "443",
                                )
                                WhiteDnsTextField(
                                    label = WhiteDnsL10n.profileFieldResolverDoHPath,
                                    rawValue = true,
                                    value = cotten.resolverDoHPath,
                                    onValueChange = { cotten = cotten.copy(resolverDoHPath = it.trim()) },
                                    placeholder = "/dns-query",
                                )
                            }
                        }
                    }
                    if (!cotten.usesCottenMax) CottenDnsPresetSummaryPanel(cotten)
                }
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnSave,
                    emphasized = true,
                    enabled = canSave,
                    onClick = {
                        onSave(
                            ConnectionProfile(
                                id = profile?.id.orEmpty(),
                                name = name.trim(),
                                serverMode = "custom",
                                customServerDomain = ServerDomains.normalize(domain),
                                customServerEncryptionKey = encryptionKey.trim(),
                                customServerEncryptionMethod = encryptionMethod,
                                resolverProfileId = profile?.resolverProfileId.orEmpty(),
                                connectionMode = profile?.connectionMode ?: "proxy",
                                engine = engine,
                                cottenSettings = cotten.normalized(),
                                soarSettings = soar.normalized(),
                                subscription = profile?.subscription,
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
    canUse: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    canTest: Boolean,
    canDrag: Boolean,
    dragging: Boolean,
    serverTestResult: ServerTestResult?,
    serverTestScore: ServerTestScore?,
    onUse: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onExport: () -> Unit,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    reorderActions: List<CustomAccessibilityAction> = emptyList(),
    reveal: Boolean = false,
    onRevealed: () -> Unit = {},
) {
    // Multi-domain profiles show the primary route plus a count, so the row does
    // not stretch with every extra domain.
    val domainList = ServerDomains.split(profile.customServerDomain)
    val domain = when {
        domainList.isEmpty() -> WhiteDnsL10n.profileDomainFallback
        domainList.size == 1 -> domainList.first()
        else -> "${domainList.first()} +${domainList.size - 1}"
    }
    val engine = DnsClientEngine.displayName(profile.engine)
    val activeStatus = WhiteDnsL10n.profileStatusActive
    val selectedStatus = WhiteDnsL10n.profileStatusSelected
    val connectionSummary = when {
        active -> "$engine · $domain - $activeStatus"
        selected -> "$engine · $domain - $selectedStatus"
        else -> "$engine · $domain"
    }
    val displayName = if (profile.id == ConnectionProfile.DefaultId) WhiteDnsL10n.setupDefaultConnection else profile.name
    val testRating = serverTestResult?.let { result ->
        serverTestRatingDisplay(
            result = result,
            score = serverTestScore ?: ServerTestScore(ServerTestScoreBucket.Pending, 0f),
        )
    }
    val rowSurface = when {
        testRating != null -> testRating.surface
        selected -> WhiteDnsPalette.AccentSurface
        else -> WhiteDnsPalette.SurfaceAlt
    }
    val rowBorder = when {
        testRating != null -> testRating.color.copy(alpha = 0.24f)
        selected -> WhiteDnsPalette.Accent.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(profileRevealModifier(reveal, onRevealed))
            .semantics { customActions = reorderActions }
            .clip(RoundedCornerShape(16.dp))
            .background(rowSurface)
            .selectable(
                selected = selected,
                enabled = canUse,
                role = Role.RadioButton,
                onClick = onUse,
            )
            .border(
                1.dp,
                rowBorder,
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.Ink,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                Text(
                    text = connectionSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = when {
                            active -> WhiteDnsPalette.Success
                            selected -> WhiteDnsPalette.AccentText
                            else -> WhiteDnsPalette.Muted
                        },
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProfileActionsMenu(
                contentDescription = WhiteDnsL10n.connectionProfileMenuActions,
                actions = listOf(
                    ProfileMenuAction(
                        label = if (selected) WhiteDnsL10n.profileMenuUseSelected else WhiteDnsL10n.profileMenuUse,
                        icon = Icons.Rounded.Check,
                        contentDescription = WhiteDnsL10n.profileMenuUse,
                        enabled = canUse,
                        onClick = onUse,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.serverTestSingleButton,
                        icon = Icons.Rounded.Speed,
                        contentDescription = WhiteDnsL10n.serverTestSingleButton,
                        enabled = canTest,
                        onClick = onTest,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuExport,
                        icon = Icons.Rounded.Link,
                        contentDescription = WhiteDnsL10n.exportConnectionProfileAction,
                        enabled = profile.customServerDomain.isNotBlank() && profile.customServerEncryptionKey.isNotBlank(),
                        onClick = onExport,
                    ),
                    ProfileMenuAction(
                        label = WhiteDnsL10n.profileMenuEdit,
                        icon = Icons.Rounded.Edit,
                        contentDescription = WhiteDnsL10n.editConnectionProfileAction,
                        enabled = canEdit,
                        onClick = onEdit,
                    ),
                    ProfileMenuAction(
                        label = if (active) WhiteDnsL10n.profileMenuDelete else WhiteDnsL10n.profileMenuDelete,
                        icon = Icons.Rounded.Delete,
                        contentDescription = if (active) WhiteDnsL10n.deleteConnectionProfileBlockedAction else WhiteDnsL10n.deleteConnectionProfileAction,
                        enabled = canDelete,
                        onClick = onDelete,
                    ),
                ),
            )
        }
        if (serverTestResult != null && testRating != null) {
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            ServerTestInlineResult(rating = testRating)
        }
    }
}

@Composable
private fun ServerTestInlineResult(rating: ServerTestRatingDisplay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(rating.color.copy(alpha = 0.12f))
            .border(1.dp, rating.color.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (rating.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                color = rating.color,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = rating.icon,
                contentDescription = rating.label,
                tint = rating.color,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = WhiteDnsL10n.serverTestTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Ink,
                fontWeight = FontWeight.Bold,
            ),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(rating.color.copy(alpha = 0.13f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = rating.icon,
                contentDescription = rating.label,
                tint = rating.color,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = rating.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = rating.color,
                    fontWeight = FontWeight.Bold,
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
    alwaysEnabled: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val enabled = alwaysEnabled || actions.any { it.enabled }
    val background = when {
        !enabled -> Color.Transparent
        expanded -> WhiteDnsPalette.AccentSurface
        else -> Color.Transparent
    }
    val iconColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        expanded -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Box {
        Box(
            modifier = Modifier
                .size(WhiteDnsLayout.minimumTouchTarget)
                .clip(RoundedCornerShape(WhiteDnsRadii.control))
                .background(background)
                .clickable(enabled = enabled, role = Role.Button) {
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
                .clip(RoundedCornerShape(WhiteDnsRadii.card))
                .background(WhiteDnsPalette.DropdownSurface),
        ) {
            actions.forEach { action ->
                val itemColor = if (action.enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(WhiteDnsRadii.control)),
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
                                fontWeight = FontWeight.Normal,
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
    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
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
                    label = WhiteDnsL10n.btnCancel,
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
        !enabled -> Color.Transparent
        dragging -> WhiteDnsPalette.AccentSurface
        else -> Color.Transparent
    }
    val iconColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        dragging -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = Modifier
            .size(WhiteDnsLayout.minimumTouchTarget)
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(background)
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
            contentDescription = WhiteDnsL10n.cdDragToReorder,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun WhiteDnsCloseButton(
    onClick: () -> Unit,
    contentDescription: String = WhiteDnsL10n.btnClose,
    tint: Color = WhiteDnsPalette.Ink,
) {
    val haptic = rememberHapticFeedback()
    IconButton(
        onClick = { haptic.performLight(); onClick() },
        modifier = Modifier.size(WhiteDnsLayout.minimumTouchTarget),
    ) {
        Icon(Icons.Rounded.Close, contentDescription, tint = tint,
            modifier = Modifier.size(20.dp).testTag("close-icon"))
    }
}

@Composable
private fun WhiteDnsDialogHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("dialog-header").semantics { paneTitle = title },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleLarge, color = WhiteDnsPalette.Ink)
        WhiteDnsCloseButton(onClick = onDismiss)
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
            .size(WhiteDnsLayout.minimumTouchTarget)
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(WhiteDnsRadii.control))
            .clickable(enabled = enabled, role = Role.Button) {
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
        tone == CompactActionTone.Success -> WhiteDnsPalette.SuccessSurface
        tone == CompactActionTone.Danger -> WhiteDnsPalette.ErrorSurface
        else -> WhiteDnsPalette.Surface
    }
    val border = when {
        !enabled -> Color.Transparent
        tone == CompactActionTone.Accent -> WhiteDnsPalette.AccentPressed
        tone == CompactActionTone.Success -> WhiteDnsPalette.Success
        tone == CompactActionTone.Danger -> WhiteDnsPalette.Error
        else -> WhiteDnsPalette.Border
    }
    val textColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        tone == CompactActionTone.Default -> WhiteDnsPalette.Muted
        tone == CompactActionTone.Success -> WhiteDnsPalette.Success
        tone == CompactActionTone.Danger -> WhiteDnsPalette.Error
        else -> WhiteDnsPalette.OnAccent
    }

    Box(
        modifier = modifier
            .heightIn(min = WhiteDnsLayout.controlHeight)
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(WhiteDnsRadii.control))
            .clickable(enabled = enabled, role = Role.Button) {
                haptic.performMedium()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun MtuSettingsGroup(
    settings: WhiteDnsSettings,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingMinUpload,
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
            label = WhiteDnsL10n.settingMinDownload,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingMaxUpload,
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
            label = WhiteDnsL10n.settingMaxDownload,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingResolverRetries,
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
            label = WhiteDnsL10n.settingResolverTimeout,
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
    MtuParallelismSlider(
        parallelism = settings.mtuTestParallelismResolvers.toMtuParallelismSliderValue(),
        enabled = true,
        onParallelismChange = { parallelism ->
            onSettingsChange(settings.copy(mtuTestParallelismResolvers = parallelism.toString()))
        },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingLogsRetries,
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
            label = WhiteDnsL10n.settingLogsTimeout,
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
        label = WhiteDnsL10n.settingLogsParallel,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingRxTxWorkers,
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
            label = WhiteDnsL10n.settingProcessWorkers,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingTunnelPacketTimeout,
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
            label = WhiteDnsL10n.settingIdlePoll,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingTxChannel,
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
            label = WhiteDnsL10n.settingRxChannel,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingUdpPool,
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
            label = WhiteDnsL10n.settingStreamQueue,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingOrphanQueue,
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
            label = WhiteDnsL10n.settingDnsFragments,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingSocksUdpTimeout,
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
            label = WhiteDnsL10n.settingTerminalRetain,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingCancelledRetain,
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
            label = WhiteDnsL10n.settingRetryBase,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingRetryStep,
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
            label = WhiteDnsL10n.settingRetryLinear,
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        WhiteDnsTextField(
            modifier = Modifier.weight(1f),
            label = WhiteDnsL10n.settingRetryMax,
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
            label = WhiteDnsL10n.settingBusyRetry,
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
    onSettingsClick: () -> Unit,
    onDonateClick: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    var overflowExpanded by remember { mutableStateOf(false) }
    val menuLabel = WhiteDnsL10n.cdAppMenu
    val appVersion = remember(context.packageName) { appVersionName(context) }

    Row(
        modifier = Modifier
            .widthIn(max = WhiteDnsLayout.contentMaxWidth)
            .fillMaxWidth()
            .padding(
                horizontal = WhiteDnsLayout.screenHorizontalPadding,
                vertical = WhiteDnsSpacing.xs,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(WhiteDnsLayout.minimumTouchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button) {
                    haptic.performLight()
                    openWhiteDnsTelegram(context)
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_telegram), stringResource(R.string.cd_logo_telegram),
                tint = WhiteDnsPalette.Accent, modifier = Modifier.size(22.dp))
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("WhiteDNS", style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                    color = WhiteDnsPalette.Ink, fontWeight = FontWeight.Bold)
                if (LocalConfiguration.current.fontScale <= 1.5f) {
                    Spacer(Modifier.width(9.dp))
                    Text(appVersion, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = WhiteDnsPalette.Muted)
                }
            }

        }
        Box {
            Box(
                modifier = Modifier
                    .size(WhiteDnsLayout.minimumTouchTarget)
                    .clip(RoundedCornerShape(WhiteDnsRadii.control))
                    .background(
                        if (overflowExpanded) WhiteDnsPalette.AccentSurface else Color.Transparent,
                    )
                    .semantics { contentDescription = menuLabel }
                    .clickable(role = Role.Button) {
                        haptic.performLight()
                        overflowExpanded = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = if (overflowExpanded) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                    modifier = Modifier.size(22.dp),
                )
            }

            DropdownMenu(
                containerColor = WhiteDnsPalette.Surface,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(0.dp),
                expanded = overflowExpanded,
                onDismissRequest = { overflowExpanded = false },
                modifier = Modifier.widthIn(min = 196.dp, max = 280.dp),
            ) {
                DropdownMenuItem(
                    enabled = false,
                    text = {
                        Column {
                            Text(
                                text = WhiteDnsL10n.menuVersion,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = WhiteDnsPalette.Muted,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                            Text(
                                text = "v${appVersion.ifBlank { "unknown" }}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = WhiteDnsPalette.Ink,
                                    fontWeight = FontWeight.Normal,
                                ),
                            )
                        }
                    },
                    onClick = {},
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(1.dp)
                        .background(WhiteDnsPalette.Divider),
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = WhiteDnsL10n.cdAppSettings,
                            tint = WhiteDnsPalette.Ink,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    text = {
                        Text(
                            text = WhiteDnsL10n.menuAppSettings,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = WhiteDnsPalette.Ink,
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    },
                    onClick = {
                        haptic.performLight()
                        overflowExpanded = false
                        onSettingsClick()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = WhiteDnsL10n.cdDonate,
                            tint = WhiteDnsPalette.Ink,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    text = {
                        Text(
                            text = WhiteDnsL10n.menuDonate,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = WhiteDnsPalette.Ink,
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    },
                    onClick = {
                        haptic.performLight()
                        overflowExpanded = false
                        onDonateClick()
                    },
                )
            }
        }
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
private fun LanguageModeSegmentedControl(
    selectedCode: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    val options: List<Pair<String, String>> = listOf(
        "en" to WhiteDnsL10n.languageEn,
        "fa" to WhiteDnsL10n.languageFa,
    )

    BoxWithConstraints(modifier = modifier) {
        val labelsFit = segmentedChoiceLabelsFit(options.map { it.second }, maxWidth)
        Column {
            FieldLabel(WhiteDnsL10n.fieldLanguage)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                FlowRow(
                    maxItemsInEachRow = if (labelsFit) 2 else 1,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WhiteDnsPalette.Surface)
                        .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    options.forEach { (code, label) ->
                        val selected = selectedCode == code
                        val background by animateColorAsState(
                            targetValue = if (selected) WhiteDnsPalette.AccentSurface else Color.Transparent,
                            animationSpec = tween(180),
                            label = "langSegmentBackground",
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (selected) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                            animationSpec = tween(180),
                            label = "langSegmentText",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = WhiteDnsLayout.controlHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(background)
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        haptic.performLight()
                                        onCodeChange(code)
                                    },
                                )
                                .semantics(mergeDescendants = true) {}
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = textColor,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingsDialog(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    languageCode: String,
    onLanguageCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(WhiteDnsL10n.appSettingsTitle, onDismiss)
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Column(Modifier.fillMaxWidth().then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false)).dialogBodyScroll()) {
                ThemeModeSegmentedControl(
                    selectedMode = themeMode,
                    onModeChange = onThemeModeChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                LanguageModeSegmentedControl(
                    selectedCode = languageCode,
                    onCodeChange = onLanguageCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.lg))
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = WhiteDnsL10n.btnClose,
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

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.heightIn(max = 560.dp))
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(WhiteDnsL10n.supportTitle, onDismiss)
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Column(Modifier.fillMaxWidth().then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false)).dialogBodyScroll()) {
                Text(
                    text = WhiteDnsL10n.supportBody,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
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
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.lg))
            CompactActionButton(
                modifier = Modifier.fillMaxWidth(),
                label = WhiteDnsL10n.btnClose,
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
    val resources = LocalResources.current
    Column {
        FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.control))
                .background(WhiteDnsPalette.Input)
                .border(1.dp, WhiteDnsPalette.Divider, RoundedCornerShape(WhiteDnsRadii.control))
                .semantics { contentDescription = resources.getString(R.string.cd_copy_address, address) }
                .clickable(role = Role.Button, onClick = onCopy)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = address,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.Ltr,
                    color = WhiteDnsPalette.Ink,
                    fontSize = 12.sp,
                ),
            )
            Text(
                text = WhiteDnsL10n.btnCopy,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhiteDnsPalette.AccentText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun CompactWarningNotice(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    actionLabel: String? = null,
    actionDescription: String? = null,
    onAction: (() -> Unit)? = null,
    dismissDescription: String? = null,
    onDismiss: (() -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val haptic = rememberHapticFeedback()
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(shape)
            .background(WhiteDnsPalette.WarningSurface)
            .border(1.dp, WhiteDnsPalette.Warning.copy(alpha = 0.26f), shape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (title == null) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = stringResource(R.string.cd_icon_warning),
                tint = WhiteDnsPalette.WarningText,
                modifier = Modifier.padding(top = 8.dp).size(16.dp),
            )
        }
        // The complete notice is the action target, so compact text keeps a generous hit area.
        Column(
            modifier = Modifier.weight(1f)
                .then(if (onAction != null) Modifier
                    .heightIn(min = WhiteDnsLayout.minimumTouchTarget)
                    .semantics(mergeDescendants = true) { contentDescription = actionDescription ?: actionLabel.orEmpty() }
                    .clickable(role = Role.Button) { haptic.performMedium(); onAction() }
                else Modifier)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp, lineHeight = 18.sp,
                        color = WhiteDnsPalette.WarningText, fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp, lineHeight = 18.sp, color = WhiteDnsPalette.WarningText,
                ),
            )
            if (actionLabel != null && onAction != null) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp, lineHeight = 18.sp,
                        color = WhiteDnsPalette.WarningText, fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    ),
                )
            }
        }
        trailingAction?.invoke()
        if (onDismiss != null) {
            WhiteDnsCloseButton(onClick = onDismiss,
                contentDescription = dismissDescription ?: WhiteDnsL10n.btnClose,
                tint = WhiteDnsPalette.WarningText)
        }
    }
}

@Composable
private fun NotificationPermissionBanner(onClick: () -> Unit, onSettingsClick: () -> Unit) {
    CompactWarningNotice(
        modifier = Modifier.testTag("notification-notice"),
        title = WhiteDnsL10n.bannerNotificationBlockedTitle,
        text = WhiteDnsL10n.bannerNotificationBlockedBody,
        actionLabel = WhiteDnsL10n.bannerEnableNotification,
        actionDescription = stringResource(R.string.cd_enable_vpn_notification),
        onAction = onClick,
        trailingAction = {
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(WhiteDnsLayout.minimumTouchTarget)) {
                Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.cd_notification_settings),
                    tint = WhiteDnsPalette.WarningText, modifier = Modifier.size(20.dp))
            }
        },
    )
}

@Composable
private fun BatteryOptimizationBanner(onClick: () -> Unit, onDismiss: () -> Unit) {
    CompactWarningNotice(
        modifier = Modifier.testTag("battery-notice"),
        title = WhiteDnsL10n.bannerBatteryTitle,
        text = WhiteDnsL10n.bannerBatteryBody,
        actionLabel = WhiteDnsL10n.bannerAllowBackground,
        actionDescription = stringResource(R.string.cd_allow_background_vpn),
        onAction = onClick,
        dismissDescription = WhiteDnsL10n.cdDismissBatteryWarning,
        onDismiss = onDismiss,
    )
}

@Composable
private fun FullVpnPerformanceWarning(onDismiss: () -> Unit) {
    CompactWarningNotice(
        modifier = Modifier.testTag("vpn-performance-notice"),
        title = WhiteDnsL10n.bannerFullVpnWarningTitle,
        text = WhiteDnsL10n.bannerFullVpnWarningBody,
        dismissDescription = WhiteDnsL10n.cdDismissVpnWarning,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SplitTunnelSettingsPanel(
    settings: WhiteDnsSettings,
    apps: List<SplitTunnelAppInfo>,
    onSettingsChange: (WhiteDnsSettings) -> Unit,
    onSelectAppsClick: () -> Unit,
) {
    val selectedPackages = settings.splitTunnelPackages
    val selectedLabels = selectedSplitTunnelLabels(selectedPackages, apps)
    val splitAllAppsLabel = WhiteDnsL10n.splitTunnelAllApps
    val splitNoAppsLabel = WhiteDnsL10n.splitTunnelNoApps
    val appSummary = splitTunnelAppsSummary(
        mode = settings.splitTunnelMode,
        appLabels = selectedLabels,
        allAppsLabel = splitAllAppsLabel,
        noAppsLabel = splitNoAppsLabel,
    )

    InfoCard(
        title = WhiteDnsL10n.splitTunnelTitle,
        compact = true,
    ) {
        WhiteDnsDropdownField(
            label = WhiteDnsL10n.splitTunnelAppRouting,
            value = settings.splitTunnelMode,
            options = localizedSplitTunnelModes(),
            compact = true,
            onValueChange = { mode ->
                onSettingsChange(settings.copy(splitTunnelMode = mode))
            },
        )
        AnimatedVisibility(
            visible = settings.splitTunnelMode != WhiteDnsOptions.SplitTunnelModeOff,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140)),
        ) {
            Column {
                Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = WhiteDnsL10n.splitTunnelSelected,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = WhiteDnsPalette.Muted,
                        ),
                    )
                    Text(
                        text = appSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = WhiteDnsPalette.Ink,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    CompactActionButton(
                        modifier = Modifier.widthIn(min = 104.dp),
                        label = WhiteDnsL10n.splitTunnelSelectApps,
                        emphasized = true,
                        enabled = apps.isNotEmpty(),
                        onClick = onSelectAppsClick,
                    )
                }
            }
        }
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

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.heightIn(max = 620.dp))
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(WhiteDnsL10n.splitTunnelDialogTitle, onDismiss)
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false))
                    .dialogBodyScroll(),
            ) {
            WhiteDnsTextField(
                label = WhiteDnsL10n.splitTunnelSearchLabel,
                value = query,
                onValueChange = { query = it },
                placeholder = WhiteDnsL10n.appsSearchPlaceholder,
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
                if (visibleApps.isEmpty()) {
                    Text(
                        text = WhiteDnsL10n.splitTunnelNoAppsFound,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
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
                    label = WhiteDnsL10n.profileBtnClear,
                    emphasized = false,
                    enabled = selected.isNotEmpty(),
                    onClick = { selected = emptySet() },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCancel,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnSave,
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
    val resources = LocalResources.current
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = {
                    haptic.performLight()
                    onToggle()
                },
            )
            .semantics {
                contentDescription = resources.getString(R.string.cd_split_tunnel_app_toggle, app.label)
            }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
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
                    fontWeight = FontWeight.Normal,
                ),
            )
            Text(
                text = app.packageName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
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
    val colors = WhiteDnsOrbColors(
        amber = WhiteDnsPalette.Warning.toArgb(),
        amberGradientEnd = WhiteDnsPalette.WarningGradientEnd.toArgb(),
        amberGradientStart = WhiteDnsPalette.WarningGradientStart.toArgb(),
        background = WhiteDnsPalette.Background.toArgb(),
        isDark = WhiteDnsPalette.isDark,
        neutral = WhiteDnsPalette.Neutral.toArgb(),
        onAccent = WhiteDnsPalette.OnAccent.toArgb(),
        onProminent = WhiteDnsPalette.OnProminent.toArgb(),
        onStateFill = WhiteDnsPalette.OnStateFill.toArgb(),
        teal = WhiteDnsPalette.Accent.toArgb(),
        tealGradientEnd = WhiteDnsPalette.AccentGradientEnd.toArgb(),
        tealGradientStart = WhiteDnsPalette.Accent.toArgb(),
        textPrimary = WhiteDnsPalette.Ink.toArgb(),
        textSecondary = WhiteDnsPalette.Muted.toArgb(),
    )
    val action = when (status) {
        ConnectionStatus.DISCONNECTED -> WhiteDnsL10n.btnConnect
        ConnectionStatus.CONNECTING -> WhiteDnsL10n.btnConnecting
        ConnectionStatus.CONNECTED -> WhiteDnsL10n.btnStop
    }
    val stateLabel = connectionStatusLabel(status)
    val click by rememberUpdatedState(onClick)
    val configuration = LocalConfiguration.current
    val orbSize = when {
        configuration.fontScale > 1.5f -> 256.dp
        configuration.screenHeightDp < 600 -> 216.dp
        else -> 240.dp
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().testTag("connect-control")) {
        key(colors, configuration.fontScale, orbSize) {
            AndroidView(
                factory = { context ->
                    WhiteDnsConnectionOrb(context, colors, orbSize.value).apply { setOnClickListener { click() } }
                },
                update = { view ->
                    view.setConnectionState(status, action)
                    view.isEnabled = enabled
                    view.contentDescription = action
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) view.stateDescription = stateLabel
                },
                modifier = Modifier.size(orbSize),
            )
        }
        if (status == ConnectionStatus.CONNECTING) {
            Text(
                text = "${progressState.label} · ${progressState.percent.coerceIn(0, 99)}%",
                color = WhiteDnsPalette.Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
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
    onShowResolvers: (ResolverRuntimeDialogType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeResolverCount = resolverState.activeResolvers.size.takeIf { it > 0 }?.toString() ?: WhiteDnsL10n.resolverPending
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
                label = WhiteDnsL10n.resolverActiveResolvers,
                value = activeResolverCount,
                onClick = { onShowResolvers(ResolverRuntimeDialogType.ACTIVE) },
            )
            ResolverRuntimeValue(
                modifier = Modifier.weight(1f),
                label = WhiteDnsL10n.resolverValidResolvers,
                value = resolverState.validResolvers.size.toString(),
                onClick = { onShowResolvers(ResolverRuntimeDialogType.VALID) },
            )
        }
        AnimatedVisibility(
            visible = backgroundMtuScanInProgress,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(120)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(WhiteDnsRadii.control))
                    .background(WhiteDnsPalette.AccentSurface)
                    .border(1.dp, WhiteDnsPalette.Accent.copy(alpha = 0.20f), RoundedCornerShape(WhiteDnsRadii.control))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        "${WhiteDnsL10n.backgroundScanningInProgress} ${progressState.completed}/${progressState.total}"
                    } else {
                        WhiteDnsL10n.backgroundScanningInProgress
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = WhiteDnsPalette.AccentText,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
    }

}

@Composable
private fun ResolverRuntimeValue(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(WhiteDnsPalette.Surface)
            .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.control))
            .semantics {
                contentDescription = resources.getString(R.string.cd_stat_card_detail, label, value)
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Ink,
                fontWeight = FontWeight.Bold,
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
        ConnectionVerificationStatus.Checking -> WhiteDnsL10n.verificationVerifying
        ConnectionVerificationStatus.Verified -> WhiteDnsL10n.verificationVerified
        ConnectionVerificationStatus.Failed -> WhiteDnsL10n.verificationNeedsAttention
        else -> WhiteDnsL10n.verificationPending
    }
    val message = verificationDisplayMessage(verification)
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
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(surface)
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(WhiteDnsRadii.control))
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = WhiteDnsPalette.Description,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
private fun verificationDisplayMessage(verification: ConnectionVerificationState): String {
    val rawMessage = verification.message
    if (rawMessage.isBlank()) {
        return if (verification.status == ConnectionVerificationStatus.Idle) {
            WhiteDnsL10n.verificationNotRunYet
        } else {
            WhiteDnsL10n.verificationCheckingRoute
        }
    }
    return when (rawMessage) {
        "Connection verified: proxy tunnel can reach the internet" -> WhiteDnsL10n.verificationProxyReachable
        "Connection verified: VPN tunnel can reach the internet" -> WhiteDnsL10n.verificationVpnReachable
        "Connection ready: proxy tunnel is active; outbound probe is still warming up" -> {
            WhiteDnsL10n.verificationProxyWarming
        }
        "Connection ready: VPN tunnel is active; outbound probe is still warming up" -> {
            WhiteDnsL10n.verificationVpnWarming
        }
        "Connection mode changed before verification finished" -> WhiteDnsL10n.verificationModeChanged
        "Connection verification failed: local SOCKS listener is not reachable" -> {
            WhiteDnsL10n.verificationSocksNotReachable
        }
        "Connection verification failed: VPN interface is not active" -> WhiteDnsL10n.verificationVpnInterfaceInactive
        else -> rawMessage
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
    val whiteDnsResolversLabel = WhiteDnsL10n.whiteDnsResolversLabel

    WhiteDnsDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WhiteDnsRadii.dialog))
                .background(WhiteDnsPalette.Surface)
                .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(WhiteDnsRadii.dialog))
                .padding(16.dp),
        ) {
            WhiteDnsDialogHeader(title, onDismiss)
            Spacer(Modifier.height(WhiteDnsSpacing.md))
            Column(Modifier.fillMaxWidth().then(if (LocalCompactHomeDialog.current) Modifier else Modifier.weight(1f, fill = false)).dialogBodyScroll()) {
                WhiteDnsTextField(
                    label = WhiteDnsL10n.profileFieldResolvers,
                    rawValue = true,
                    value = resolverText,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = WhiteDnsL10n.noResolversPlaceholder,
                    singleLine = false,
                    minLines = 6,
                    maxLines = 12,
                )
            }
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnClose,
                    emphasized = false,
                    enabled = true,
                    onClick = onDismiss,
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCopy,
                    emphasized = true,
                    enabled = resolverText.isNotBlank(),
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            label = whiteDnsResolversLabel,
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
    val labels = listOf(WhiteDnsL10n.speedDown, WhiteDnsL10n.speedUp, WhiteDnsL10n.speedTotalUsage)
    val values = listOf(formatDataSpeed(stats.downloadSpeedBytesPerSecond),
        formatDataSpeed(stats.uploadSpeedBytesPerSecond), formatDataSize(stats.totalDataUsageBytes))
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
    val valueStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("home-traffic")) {
        val density = LocalDensity.current
        val cellWidth = with(density) { ((maxWidth - 16.dp) / 3).roundToPx().coerceAtLeast(1) }
        val labelLayouts = labels.map { measurer.measure(it, labelStyle, maxLines = 2,
            constraints = Constraints(maxWidth = cellWidth)) }
        // Reflow when the actual translated/scaled content needs it, not at a language-blind font threshold.
        val columnsFit = labelLayouts.none { it.hasVisualOverflow } && values.all {
            measurer.measure(it, valueStyle, softWrap = false).size.width <= cellWidth
        }
        if (columnsFit) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                labels.indices.forEach { index ->
                    SpeedIndicator(labels[index], values[index],
                        Modifier.weight(1f), labelLines = labelLayouts.maxOf { it.lineCount })
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                labels.indices.forEach { index ->
                    SpeedIndicator(labels[index], values[index], Modifier.fillMaxWidth(), inline = true)
                }
            }
        }
    }
}

@Composable
private fun serverTestDisplayMessage(state: ServerTestState): String {
    val completed = state.results.count {
        it.status == ServerTestStatus.Ready || it.status == ServerTestStatus.Failed
    }
    val ready = state.results.count { it.status == ServerTestStatus.Ready }
    val failed = state.results.count { it.status == ServerTestStatus.Failed }
    val total = state.results.size

    return when {
        state.isRunning && total > 0 -> WhiteDnsL10n.serverTestProgressTemplate.format(completed, total)
        total > 0 && completed == total -> WhiteDnsL10n.serverTestSummaryTemplate.format(ready, failed)
        state.message == "Server Test: no saved server profiles are configured" ->
            WhiteDnsL10n.serverTestNoSavedServers
        state.message == "Server Test: no connected resolvers are available" ->
            WhiteDnsL10n.serverTestNoConnectedResolvers
        state.message.startsWith("Server Test failed:") ->
            WhiteDnsL10n.serverTestFailedTemplate.format(state.message.substringAfter(':').trim())
        else -> state.message.ifBlank { WhiteDnsL10n.serverTestIdle }
    }
}

@Composable
private fun serverTestRatingDisplay(
    result: ServerTestResult,
    score: ServerTestScore,
): ServerTestRatingDisplay {
    return when {
        result.status == ServerTestStatus.Failed ->
            ServerTestRatingDisplay(
                label = WhiteDnsL10n.serverTestScoreUnavailable,
                icon = Icons.Rounded.Close,
                color = WhiteDnsPalette.Error,
                surface = WhiteDnsPalette.ErrorSurface,
            )
        score.bucket == ServerTestScoreBucket.Poor ->
            ServerTestRatingDisplay(
                label = WhiteDnsL10n.serverTestScorePoor,
                icon = Icons.Rounded.Close,
                color = WhiteDnsPalette.Error,
                surface = WhiteDnsPalette.ErrorSurface,
            )
        score.bucket == ServerTestScoreBucket.Good -> ServerTestRatingDisplay(
            label = WhiteDnsL10n.serverTestScoreGood,
            icon = Icons.Rounded.Check,
            color = WhiteDnsPalette.Success,
            surface = WhiteDnsPalette.SuccessSurface,
        )
        score.bucket == ServerTestScoreBucket.Fair -> ServerTestRatingDisplay(
            label = WhiteDnsL10n.serverTestScoreFair,
            icon = Icons.Rounded.WarningAmber,
            color = WhiteDnsPalette.Warning,
            surface = WhiteDnsPalette.WarningSurface,
        )
        else -> ServerTestRatingDisplay(
            label = when (result.status) {
                ServerTestStatus.Starting -> WhiteDnsL10n.serverTestStarting
                ServerTestStatus.Measuring -> WhiteDnsL10n.serverTestMeasuring
                else -> WhiteDnsL10n.serverTestPending
            },
            icon = Icons.Rounded.Tune,
            color = WhiteDnsPalette.Warning,
            surface = WhiteDnsPalette.WarningSurface,
            loading = result.status == ServerTestStatus.Starting || result.status == ServerTestStatus.Measuring,
        )
    }
}

private fun buildServerTestScores(results: List<ServerTestResult>): Map<String, ServerTestScore> {
    val measuredResults = results.filter { it.status == ServerTestStatus.Ready }
    val speeds = measuredResults
        .map { it.speedBytesPerSecond }
        .filter { it > 0L }
    val pings = measuredResults
        .mapNotNull { it.pingMillis }
        .filter { it > 0L }

    return results.associate { result ->
        val score = when (result.status) {
            ServerTestStatus.Failed -> ServerTestScore(ServerTestScoreBucket.Poor, 0f)
            ServerTestStatus.Ready -> {
                val metricScores = listOfNotNull(
                    result.speedBytesPerSecond
                        .takeIf { it > 0L }
                        ?.let { percentileScore(value = it, values = speeds, higherIsBetter = true) },
                    result.pingMillis
                        ?.takeIf { it > 0L }
                        ?.let { percentileScore(value = it, values = pings, higherIsBetter = false) },
                )
                val percentile = if (metricScores.isEmpty()) {
                    0f
                } else {
                    metricScores.average().toFloat().coerceIn(0f, 1f)
                }
                ServerTestScore(
                    bucket = serverTestScoreBucket(percentile),
                    percentile = percentile,
                )
            }
            else -> ServerTestScore(ServerTestScoreBucket.Pending, 0f)
        }
        result.serverId to score
    }
}

private fun percentileScore(
    value: Long,
    values: List<Long>,
    higherIsBetter: Boolean,
): Float {
    if (values.isEmpty()) {
        return 0f
    }
    val favorableCount = if (higherIsBetter) {
        values.count { value >= it }
    } else {
        values.count { value <= it }
    }
    return (favorableCount.toFloat() / values.size.toFloat()).coerceIn(0f, 1f)
}

private fun serverTestScoreBucket(percentile: Float): ServerTestScoreBucket {
    return when {
        percentile >= 0.67f -> ServerTestScoreBucket.Good
        percentile >= 0.34f -> ServerTestScoreBucket.Fair
        else -> ServerTestScoreBucket.Poor
    }
}

private data class ServerTestRatingDisplay(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val surface: Color,
    val loading: Boolean = false,
)

private data class ServerTestScore(
    val bucket: ServerTestScoreBucket,
    val percentile: Float,
)

private enum class ServerTestScoreBucket {
    Good,
    Fair,
    Poor,
    Pending,
}

@Composable
private fun SpeedIndicator(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    inline: Boolean = false,
    labelLines: Int = 1,
) {
    val labelStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = WhiteDnsPalette.Muted)
    val valueStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold,
        textDirection = TextDirection.Ltr, color = WhiteDnsPalette.Ink)
    val valueContent: @Composable () -> Unit = { Text(value, style = valueStyle, maxLines = 1, softWrap = false) }
    if (inline) {
        Row(modifier.padding(vertical = 4.dp).semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = labelStyle, modifier = Modifier.weight(1f))
            valueContent()
        }
    } else {
        Column(modifier.padding(vertical = 4.dp).semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = labelStyle, minLines = labelLines, maxLines = 2,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            valueContent()
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
    isSoar: Boolean = false,
) {
    InfoCard(title = WhiteDnsL10n.infoCardConnection) {
        InfoRow(label = WhiteDnsL10n.infoLabelMode, value = connectionMode)
        if (showProxyDetails) {
            InfoRow(label = WhiteDnsL10n.infoLabelSocks5Proxy, value = listenAddress)
            if (httpProxyEnabled) {
                InfoRow(label = WhiteDnsL10n.infoLabelHttpProxy, value = httpProxyAddress)
            }
            ProtocolRow(protocol = protocol, showDivider = true)
            InfoRow(label = WhiteDnsL10n.infoLabelAuth, value = if (socksAuthEnabled) WhiteDnsL10n.infoLabelAuthOn else WhiteDnsL10n.infoLabelAuthOff)
            if (socksAuthEnabled) {
                InfoRow(label = WhiteDnsL10n.infoLabelUser, value = username)
                InfoRow(label = WhiteDnsL10n.infoLabelPass, value = password)
            }
        } else {
            ProtocolRow(protocol = protocol, showDivider = true)
            InfoRow(
                label = WhiteDnsL10n.infoLabelSplitTunnel,
                value = splitTunnelConnectionSummary(
                    mode = splitTunnelMode,
                    packageNames = splitTunnelPackages,
                    labelsByPackage = splitTunnelAppLabels,
                    allAppsLabel = WhiteDnsL10n.splitTunnelAllApps,
                    noAppsLabel = WhiteDnsL10n.splitTunnelNoApps,
                    onlyPrefix = WhiteDnsL10n.splitTunnelOnlyPrefix,
                    bypassPrefix = WhiteDnsL10n.splitTunnelBypassPrefix,
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
                    label = WhiteDnsL10n.infoLabelApps,
                    value = stats.connectedApps.toString(),
                ),
            ),
        )
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        InfoRow(label = WhiteDnsL10n.infoLabelConnectionProfile, value = connectionProfileName)
        InfoRow(label = WhiteDnsL10n.infoLabelResolverProfile, value = resolverProfileName)
        InfoRow(label = WhiteDnsL10n.infoLabelSettingProfile, value = settingProfileName)
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.inputSpacing))
        ResolverActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = if (isSoar) soarText("EXPORT SETTINGS", "خروجی تنظیمات") else WhiteDnsL10n.downloadTomlBtn,
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
    GroupLabel(WhiteDnsL10n.groupParallelTestResults)
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
                    .height(1.dp)
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
                    fontWeight = FontWeight.Normal,
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
        val metrics = listOf(
            autoTuneMtuMetric(result),
            AutoTuneDisplayMetric(Icons.Rounded.Speed, WhiteDnsL10n.cdParallelTestSpeed,
                WhiteDnsL10n.autoTuneSpeedLabel, autoTuneSpeedValue(result),
                WhiteDnsPalette.Success, WhiteDnsPalette.SuccessSurface, result.status == "measuring"),
            AutoTuneDisplayMetric(Icons.Rounded.NetworkPing, WhiteDnsL10n.cdParallelTestPing,
                WhiteDnsL10n.autoTunePingLabel, result.pingMillis?.let { "${it}ms" } ?: "-",
                WhiteDnsPalette.AccentText, WhiteDnsPalette.AccentSurface),
        )
        BoxWithConstraints {
            if (maxWidth < 330.dp || LocalConfiguration.current.fontScale > 1.2f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    metrics.forEach { AutoTuneMetricPill(it, Modifier.fillMaxWidth()) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    metrics.forEach { AutoTuneMetricPill(it, Modifier.weight(1f)) }
                }
            }
        }
        autoTuneStatusMessage(
            result = result,
            startingLabel = WhiteDnsL10n.autoTuneStartingTest,
            measuringLabel = WhiteDnsL10n.autoTuneMeasuringSpeed,
            failedFallback = WhiteDnsL10n.autoTuneFailedFallback,
            measuredKeyword = WhiteDnsL10n.autoTuneMeasuredKeyword,
        )?.let { message ->
            Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = if (result.status == "failed") WhiteDnsPalette.WarningText else WhiteDnsPalette.Muted,
                    fontWeight = if (result.status == "failed") FontWeight.Normal else FontWeight.Normal,
                ),
            )
        }
        Spacer(modifier = Modifier.height(WhiteDnsSpacing.sm))
        CompactActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = WhiteDnsL10n.saveSettingAs,
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
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(metric.surface)
            .border(1.dp, metric.color.copy(alpha = 0.16f), RoundedCornerShape(WhiteDnsRadii.control))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.ContentOrLtr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
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
            iconContentDescription = WhiteDnsL10n.cdAutoTuneMtuFailed,
            label = WhiteDnsL10n.groupMtu,
            value = WhiteDnsL10n.autoTuneMtuFail,
            color = WhiteDnsPalette.Error,
            surface = WhiteDnsPalette.ErrorSurface,
        )
        "listening", "measuring", "ready" -> AutoTuneDisplayMetric(
            icon = Icons.Rounded.Check,
            iconContentDescription = WhiteDnsL10n.cdAutoTuneMtuPassed,
            label = WhiteDnsL10n.groupMtu,
            value = WhiteDnsL10n.autoTuneMtuPass,
            color = WhiteDnsPalette.Success,
            surface = WhiteDnsPalette.SuccessSurface,
        )
        else -> AutoTuneDisplayMetric(
            icon = Icons.Rounded.Tune,
            iconContentDescription = WhiteDnsL10n.cdAutoTuneMtuTesting,
            label = WhiteDnsL10n.groupMtu,
            value = WhiteDnsL10n.autoTuneMtuTest,
            color = WhiteDnsPalette.AccentText,
            surface = WhiteDnsPalette.AccentSurface,
            loading = true,
        )
    }
}

@Composable
private fun autoTuneResultLabel(result: AutoTuneTrialResult): String {
    return if (result.selected) {
        "${result.label} · ${WhiteDnsL10n.profileStatusSelected}"
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

private fun autoTuneStatusMessage(
    result: AutoTuneTrialResult,
    startingLabel: String,
    measuringLabel: String,
    failedFallback: String,
    measuredKeyword: String,
): String? {
    return when (result.status) {
        "starting" -> startingLabel
        "listening" -> measuringLabel
        "measuring" -> measuringLabel
        "failed" -> result.message.ifBlank { failedFallback }
        else -> result.message.takeIf { it.isNotBlank() && it != measuredKeyword }
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
    FlowRow(
        maxItemsInEachRow = if (LocalConfiguration.current.fontScale > 1.2f) 1 else metrics.size.coerceAtLeast(1),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(WhiteDnsPalette.SuccessSurface)
            .border(1.dp, WhiteDnsPalette.Success.copy(alpha = 0.16f), RoundedCornerShape(WhiteDnsRadii.control))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Muted,
                ),
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.ContentOrLtr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = WhiteDnsL10n.infoLabelProtocol,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(WhiteDnsPalette.AccentSurface)
                .border(1.dp, WhiteDnsPalette.Accent.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = protocol,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = WhiteDnsPalette.AccentText,
                ),
            )
        }
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WhiteDnsPalette.Divider),
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    compact: Boolean = false,
    titleAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val contentPadding = if (compact) WhiteDnsSpacing.md else 0.dp
    val titleBottomSpacing = if (compact) WhiteDnsSpacing.sm else WhiteDnsSpacing.md

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (compact) 13.sp else 15.sp,
                    color = WhiteDnsPalette.Ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
            titleAction?.invoke()
        }
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
            .padding(vertical = 8.dp),
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
                    textDirection = TextDirection.ContentOrLtr,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = WhiteDnsPalette.Ink,
            ),
        )
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
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
    val meaningfulLogs = logs.filter { it.isNotBlank() && it != "Idle" }
    val visibleLogs = if (expanded) meaningfulLogs else meaningfulLogs.take(10)
    val context = LocalContext.current
    val logsClipboardLabel = WhiteDnsL10n.whiteDnsLogsLabel
    val diagnosticsClipboardLabel = WhiteDnsL10n.whiteDnsDiagnosticsLabel
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
        ) {
            Text(
                text = WhiteDnsL10n.logsInlineTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = WhiteDnsPalette.SectionTitle,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
            ) {
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.btnCopy,
                    emphasized = false,
                    enabled = meaningfulLogs.isNotEmpty(),
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            label = logsClipboardLabel,
                            text = meaningfulLogs.joinToString(separator = "\n"),
                            sensitive = false,
                        )
                    },
                )
                CompactActionButton(
                    modifier = Modifier.weight(1f),
                    label = WhiteDnsL10n.logsDiagnostics,
                    emphasized = false,
                    enabled = true,
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            label = diagnosticsClipboardLabel,
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
                .heightIn(min = 240.dp)
                .clip(RoundedCornerShape(WhiteDnsRadii.card))
                .background(WhiteDnsPalette.SurfaceAlt),
        ) {
            if (visibleLogs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .padding(WhiteDnsSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        tint = WhiteDnsPalette.AccentText,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
                    Text(
                        text = WhiteDnsL10n.logsEmptyTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = WhiteDnsPalette.Ink,
                        ),
                    )
                    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
                    Text(
                        text = WhiteDnsL10n.logsEmptyBody,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WhiteDnsPalette.Description,
                        ),
                    )
                }
            } else {
                visibleLogs.forEach { logLine ->
                    Text(
                        text = logLine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WhiteDnsPalette.Description,
                        ),
                    )
                }
            }
        }
    }
}

private fun shareProfileLink(
    context: Context,
    link: String,
    subject: String,
    chooserTitle: String,
) {
    shareTextExport(
        context = context,
        fileName = "whitedns-profile.txt",
        subject = subject,
        chooserTitle = chooserTitle,
        text = link,
    )
}

private fun shareClientConfigToml(
    context: Context,
    toml: String,
    chooserTitle: String,
) {
    shareTextExport(
        context = context,
        fileName = "client_config.toml",
        subject = "client_config.toml",
        chooserTitle = chooserTitle,
        text = toml,
    )
}

private fun shareAdvancedSettingsToml(
    context: Context,
    toml: String,
    chooserTitle: String,
) {
    shareTextExport(
        context = context,
        fileName = "advanced_settings.toml",
        subject = "advanced_settings.toml",
        chooserTitle = chooserTitle,
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
    shareCachedExportFile(
        context = context,
        exportFile = exportFile,
        subject = subject,
        chooserTitle = chooserTitle,
    )
}

private fun shareCachedExportFile(
    context: Context,
    exportFile: File,
    subject: String,
    chooserTitle: String,
) {
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

private data class ResolverProfilesExportDialogState(
    val saving: Boolean = true,
    val resolverCount: Int = 0,
    val savedLocation: String = "",
    val shareFile: File? = null,
    val errorMessage: String? = null,
)

private data class ResolverProfilesExportResult(
    val resolverCount: Int,
    val savedLocation: String,
    val shareFile: File,
)

private data class DeviceExportFile(
    val fileName: String,
    val savedLocation: String,
)

private fun writeResolverProfilesExport(
    context: Context,
    settings: WhiteDnsSettings,
): ResolverProfilesExportResult {
    val resolverText = settings.exportAllResolverProfilesText()
    val resolverCount = resolverText
        .lineSequence()
        .count { it.isNotBlank() }
    val savedFile = writeDeviceDownloadsExportFile(
        context = context,
        // Downloads stops finding duplicate-name suffixes after 32 repeated exports.
        fileName = "${ResolverProfilesExportFileName.removeSuffix(".txt")}_${System.currentTimeMillis()}.txt",
        text = resolverText,
    )
    val shareFile = writeCacheExportFile(
        context = context,
        fileName = savedFile.fileName,
        text = resolverText,
    )
    return ResolverProfilesExportResult(
        resolverCount = resolverCount,
        savedLocation = savedFile.savedLocation,
        shareFile = shareFile,
    )
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
    val safeName = safeExportFileName(fileName)
    val exportFile = File(exportDir, safeName)
    exportFile.writeText(text, Charsets.UTF_8)
    return exportFile
}

private fun writeDeviceDownloadsExportFile(
    context: Context,
    fileName: String,
    text: String,
): DeviceExportFile {
    val safeName = safeExportFileName(fileName)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, safeName)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Unable to create export file")
        runCatching {
            resolver.openOutputStream(uri)
                ?.bufferedWriter(Charsets.UTF_8)
                ?.use { writer -> writer.write(text) }
                ?: throw IllegalStateException("Unable to write export file")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.getOrElse { error ->
            runCatching { resolver.delete(uri, null, null) }
            throw error
        }
        return DeviceExportFile(
            fileName = safeName,
            savedLocation = "${Environment.DIRECTORY_DOWNLOADS}/$safeName",
        )
    }

    val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?: File(context.filesDir, Environment.DIRECTORY_DOWNLOADS)
    exportDir.mkdirs()
    val exportFile = File(exportDir, safeName)
    exportFile.writeText(text, Charsets.UTF_8)
    return DeviceExportFile(
        fileName = safeName,
        savedLocation = exportFile.absolutePath,
    )
}

private fun safeExportFileName(fileName: String): String {
    return fileName
        .replace(Regex("""[^A-Za-z0-9._-]"""), "_")
        .ifBlank { "whitedns-export.txt" }
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
        if (selectedProfile.engine == DnsClientEngine.CottenDns) {
            val cotten = selectedProfile.cottenSettings.normalized()
            appendLine("CottenDNS mode: ${cotten.tunnelMode}")
            if (cotten.usesCottenMax) {
                appendLine("CottenMax traffic profile: ${cotten.cottenMaxOptions.trafficProfile}")
                if (cotten.cottenMaxOptions.trafficProfile == "low") appendLine("CottenMax speed reduction target: ${cotten.cottenMaxOptions.lowReductionPercent}%")
            }
        }
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
        appendLine("Measured loss: ${String.format(Locale.US, "%.1f%%", uiState.connectionStats.lossPercent)}")
        appendLine("Transport state: ${uiState.connectionStats.transportSummary.ifBlank { "not reported" }}")
        appendLine(
            "Path events: explore=${uiState.connectionStats.transportExplorationCount} " +
                "restore=${uiState.connectionStats.transportRestorationCount} " +
                "switch=${uiState.connectionStats.transportSwitchCount} " +
                "stripe=${uiState.connectionStats.pathStripeCount} " +
                "saved=${uiState.connectionStats.redundancySavedCount}",
        )
        appendLine(
            "Engine queues: ${uiState.connectionStats.txQueueDepth}/" +
                "${uiState.connectionStats.encodedTxQueueDepth}/" +
                "${uiState.connectionStats.rxQueueDepth}",
        )
        appendLine(
            "Engine drops: rx=${uiState.connectionStats.rxDropCount} " +
                "tx=${uiState.connectionStats.txDropCount}",
        )
        appendLine("Transport recoveries: ${uiState.connectionStats.transportRecoveryCount}")
        appendLine(
            "Stream failures: dial=${uiState.connectionStats.streamDialFailureCount} " +
                "write=${uiState.connectionStats.streamWriteFailureCount}",
        )
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

private fun readResolverTextFromUri(
    context: Context,
    uri: Uri,
    unableToOpenLabel: String,
    invalidResolverIpTemplate: String,
    noResolverEntriesLabel: String,
): Result<String> {
    return runCatching {
        val rawText = readTextFromUri(context, uri, unableToOpenLabel).getOrThrow()
        normalizeImportedResolverText(rawText, invalidResolverIpTemplate, noResolverEntriesLabel)
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

private fun normalizeImportedResolverText(
    rawText: String,
    invalidResolverIpTemplate: String,
    noResolverEntriesLabel: String,
): String {
    val validation = validateResolverText(rawText)
    if (validation.invalidEntries.isNotEmpty()) {
        throw IllegalArgumentException(invalidResolverIpTemplate.format(validation.invalidEntries.first()))
    }
    if (validation.normalizedText.isBlank()) {
        throw IllegalArgumentException(noResolverEntriesLabel)
    }
    return validation.normalizedText
}

private fun resolverValidationMessage(
    name: String,
    resolverText: String,
    invalidEntries: List<String>,
    validResolverCount: Int,
    invalidResolverIpTemplate: String,
    enterValidResolverIpLabel: String,
    enterProfileNameToSaveLabel: String,
    validResolverSingularTemplate: String,
    validResolverPluralTemplate: String,
): String? {
    return when {
        resolverText.isBlank() -> null
        invalidEntries.isNotEmpty() -> invalidResolverIpTemplate.format(invalidEntries.first())
        validResolverCount == 0 -> enterValidResolverIpLabel
        name.isBlank() -> enterProfileNameToSaveLabel
        else -> {
            val template = if (validResolverCount == 1) validResolverSingularTemplate else validResolverPluralTemplate
            template.format(validResolverCount)
        }
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
        emphasized -> WhiteDnsPalette.AccentSurface
        else -> WhiteDnsPalette.SurfaceAlt
    }
    val textColor = when {
        !enabled -> WhiteDnsPalette.Disabled
        emphasized -> WhiteDnsPalette.AccentText
        else -> WhiteDnsPalette.Muted
    }

    Box(
        modifier = modifier
            .heightIn(min = WhiteDnsLayout.controlHeight)
            .clip(RoundedCornerShape(WhiteDnsRadii.control))
            .background(background)
            .clickable(enabled = enabled, role = Role.Button) {
                haptic.performLight()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Normal,
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
    val resources = LocalResources.current
    val haptic = rememberHapticFeedback()
    val isOpen = expanded || !collapsible
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "sectionRotation",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isOpen) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
        animationSpec = tween(220),
        label = "sectionIconColor",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WhiteDnsRadii.card))
            .background(WhiteDnsPalette.Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WhiteDnsLayout.navigationItemHeight)
                .let { modifier ->
                    if (collapsible) {
                        modifier
                            .semantics {
                                contentDescription = if (expanded) {
                                    resources.getString(R.string.cd_section_collapse, title)
                                } else {
                                    resources.getString(R.string.cd_section_expand, title)
                                }
                            }
                            .clickable(role = Role.Button) {
                                haptic.performLight()
                                onToggle()
                            }
                    } else {
                        modifier
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WhiteDnsSpacing.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (collapsible) iconContentDescription else null,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = WhiteDnsPalette.Ink,
                    ),
                )
            }
            if (collapsible) {
                Box(
                    modifier = Modifier
                        .size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = stringResource(
                            if (expanded) R.string.cd_advanced_settings_collapse else R.string.cd_advanced_settings_expand
                        ),
                        tint = if (expanded) WhiteDnsPalette.AccentText else WhiteDnsPalette.Muted,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer(rotationZ = rotation),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(animationSpec = tween(WhiteDnsAnimations.DURATION_FAST)),
            exit = fadeOut(animationSpec = tween(WhiteDnsAnimations.DURATION_INSTANT)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteDnsPalette.SurfaceAlt)
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
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            color = WhiteDnsPalette.SectionTitle,
            fontWeight = FontWeight.Bold,
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
            .height(1.dp)
            .background(WhiteDnsPalette.Divider),
    )
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.md))
}

@Composable
private fun ToggleRow(
    label: String,
    enabled: Boolean,
    interactiveEnabled: Boolean = true,
    onToggle: () -> Unit,
) {
    val haptic = rememberHapticFeedback()
    val contentAlpha = if (interactiveEnabled) 1f else 0.46f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = enabled,
                enabled = interactiveEnabled,
                role = Role.Switch,
                onValueChange = {
                    haptic.performLight()
                    onToggle()
                },
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = WhiteDnsPalette.FieldLabel.copy(alpha = contentAlpha),
                fontWeight = FontWeight.Normal,
            ),
        )
        Switch(
            checked = enabled,
            onCheckedChange = null,
            enabled = interactiveEnabled,
            modifier = Modifier.clearAndSetSemantics {},
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

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
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
    rawValue: Boolean = false,
    readOnly: Boolean = false,
    showLabel: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val fieldBringIntoView = remember { BringIntoViewRequester() }
    val keyboardBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(focused, keyboardBottom) {
        if (focused && keyboardBottom > 0) {
            withFrameNanos { }
            fieldBringIntoView.bringIntoView()
        }
    }
    val borderColor = if (focused) WhiteDnsPalette.Accent else WhiteDnsPalette.Divider
    val shape = RoundedCornerShape(WhiteDnsRadii.control)
    val fieldCopyAlignment = if (LocalLayoutDirection.current == LayoutDirection.Rtl) TextAlign.Right else TextAlign.Left
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = WhiteDnsPalette.Ink,
        fontSize = 14.sp,
    )
    val leftToRight = rawValue || keyboardOptions.keyboardType == KeyboardType.Number ||
        keyboardOptions.keyboardType == KeyboardType.Decimal
    val fieldTextStyle = if (leftToRight) {
        textStyle.copy(
            localeList = LocaleList(ComposeLocale("en-US")),
            textDirection = TextDirection.Ltr,
        )
    } else {
        textStyle
    }

    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        if (showLabel) FieldLabel(label)
        val textField: @Composable () -> Unit = {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(fieldBringIntoView)
                    .semantics { contentDescription = label }
                    .onFocusChanged {
                        focused = it.isFocused
                        onFocusChange(it.isFocused)
                    },
                singleLine = singleLine,
                readOnly = readOnly,
                minLines = minLines,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                textStyle = fieldTextStyle,
                cursorBrush = SolidColor(WhiteDnsPalette.Accent),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = WhiteDnsLayout.controlHeight)
                            .clip(shape)
                            .background(WhiteDnsPalette.Input)
                            .border(1.dp, borderColor, shape)
                            .padding(
                                horizontal = 12.dp,
                                vertical = if (singleLine) 8.dp else 12.dp,
                            ),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = fieldCopyAlignment,
                                style = textStyle.copy(
                                    color = WhiteDnsPalette.Placeholder,
                                    textDirection = TextDirection.Content,
                                ),
                                maxLines = if (singleLine) 1 else maxLines,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        InterceptPlatformTextInput(interceptor = { request, next ->
            next.startInputMethod(object : PlatformTextInputMethodRequest {
                override fun createInputConnection(outAttributes: EditorInfo): InputConnection {
                    val connection = request.createInputConnection(outAttributes)
                    // Keep landscape editors and their navigation visible while typing.
                    outAttributes.imeOptions = outAttributes.imeOptions or EditorInfo.IME_FLAG_NO_FULLSCREEN
                    return connection
                }
            })
        }) {
            if (leftToRight) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) { textField() }
            } else {
                textField()
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) TextAlign.Right else TextAlign.Left,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 13.sp,
            color = WhiteDnsPalette.FieldLabel,
            fontWeight = FontWeight.Bold,
        ),
    )
    Spacer(modifier = Modifier.height(WhiteDnsSpacing.xs))
}

@Composable
private fun localizedBalancingStrategies(): List<Choice<Int>> = listOf(
    Choice(1, WhiteDnsL10n.balancingStrategyRandom),
    Choice(2, WhiteDnsL10n.balancingStrategyRoundRobin),
    Choice(3, WhiteDnsL10n.balancingStrategyLeastLoss),
    Choice(4, WhiteDnsL10n.balancingStrategyLowestLatency),
)

@Composable
private fun localizedCompressionTypes(): List<Choice<Int>> = listOf(
    Choice(0, WhiteDnsL10n.compressionOff),
    Choice(1, WhiteDnsL10n.compressionZstd),
    Choice(2, WhiteDnsL10n.compressionLz4),
    Choice(3, WhiteDnsL10n.compressionZlib),
)

@Composable
private fun localizedSplitTunnelModes(): List<Choice<String>> = listOf(
    Choice(WhiteDnsOptions.SplitTunnelModeOff, WhiteDnsL10n.splitTunnelAllAppsChoice),
    Choice(WhiteDnsOptions.SplitTunnelModeInclude, WhiteDnsL10n.splitTunnelOnlySelectedChoice),
    Choice(WhiteDnsOptions.SplitTunnelModeExclude, WhiteDnsL10n.splitTunnelBypassSelectedChoice),
)

@Composable
private fun localizedDnsClientEngines(): List<Choice<String>> = listOf(
    Choice(DnsClientEngine.StormDns, WhiteDnsL10n.profileEngineStormDns),
    Choice(DnsClientEngine.CottenDns, WhiteDnsL10n.profileEngineCottenDns),
    Choice(DnsClientEngine.Soar, "Soar (experimental)"),
)

@Composable
private fun CottenMaxLowReductionSlider(
    percent: Int,
    onPercentChange: (Int) -> Unit,
) {
    var sliderValue by remember(percent) { mutableStateOf(percent.coerceIn(10, 90).toFloat()) }
    val displayed = sliderValue.roundToInt().coerceIn(10, 90)
    val label = WhiteDnsL10n.cottenMaxLowReductionLabel
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderLabelValue(label, "$displayed%")
        Slider(
            modifier = Modifier.semantics { contentDescription = label },
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onPercentChange(displayed) },
            valueRange = 10f..90f,
            steps = 79,
            colors = SliderDefaults.colors(
                thumbColor = WhiteDnsPalette.Accent,
                activeTrackColor = WhiteDnsPalette.Accent,
                inactiveTrackColor = WhiteDnsPalette.ControlBorder,
            ),
        )
        Text(WhiteDnsL10n.cottenMaxLowReductionHint, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * CottenDNS-only: how many resolvers the background sweep probes at once after
 * the tunnel is already up. The initial pre-connection scan is governed by the
 * app-wide resolver-parallelism setting instead, so this never slows connecting.
 */
@Composable
private fun CottenDnsBackgroundScanSlider(
    parallelism: Int,
    onParallelismChange: (Int) -> Unit,
) {
    val min = CottenDnsProfileSettings.MinBackgroundScanParallelism
    val max = CottenDnsProfileSettings.MaxBackgroundScanParallelism
    var sliderValue by remember(parallelism) {
        mutableStateOf(parallelism.coerceIn(min, max).toFloat())
    }
    val displayed = sliderValue.roundToInt().coerceIn(min, max)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderLabelValue(WhiteDnsL10n.profileFieldScanParallelism, displayed.toString())
        val accessibilityLabel = WhiteDnsL10n.profileFieldScanParallelism
        Slider(
            modifier = Modifier.semantics { contentDescription = accessibilityLabel },
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                if (displayed != parallelism) {
                    onParallelismChange(displayed)
                }
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
            colors = SliderDefaults.colors(
                thumbColor = WhiteDnsPalette.Accent,
                activeTrackColor = WhiteDnsPalette.Accent,
                inactiveTrackColor = WhiteDnsPalette.ControlBorder,
            ),
        )
        Text(
            text = WhiteDnsL10n.profileScanParallelismNote,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
            ),
        )
    }
}

/**
 * Read-only recap of what the profile will actually emit. Computed by the
 * renderer itself, so it cannot drift from the generated TOML.
 */
@Composable
private fun CottenDnsOptionalConnectionFields(
    cotten: CottenDnsProfileSettings,
    profileId: String,
    onSettingsChange: (CottenDnsProfileSettings) -> Unit,
) {
    val options = cotten.cottenMaxOptions
    val max = cotten.usesCottenMax
    val encrypted = cotten.transportMode in setOf("dot", "doh", "doh3", "doq")
    fun update(next: shop.whitedns.client.model.CottenMaxOptions) {
        onSettingsChange(cotten.copy(cottenMaxOptions = next))
    }
    if (encrypted) {
        WhiteDnsDropdownField(
            label = "Encrypted DNS padding",
            value = options.dnsPaddingBlockSize,
            options = (listOf(0, 128, 256, 512) + options.dnsPaddingBlockSize).distinct().map { Choice(it, if (it == 0) "Off" else "$it-byte blocks") },
            onValueChange = { update(options.copy(dnsPaddingBlockSize = it)) },
        )
        if (cotten.transportMode == "doh3") {
            ToggleRow("Allow HTTPS fallback when HTTP/3 fails", options.doh3FallbackToDoh) {
                update(options.copy(doh3FallbackToDoh = !options.doh3FallbackToDoh))
            }
        }
        Text(text = "Padding costs bandwidth. QUIC needs a path that carries 1,200-byte UDP packets.", style = MaterialTheme.typography.bodySmall)
    }
    if (!max) return
    var expanded by rememberSaveable(profileId) { mutableStateOf(false) }
    ToggleRow("Advanced CottenMax options", expanded) { expanded = !expanded }
    if (!expanded) return
    Text(text = "Defaults retain fast startup. Standbys use the same credential; existing streams reconnect after a server change.", style = MaterialTheme.typography.bodySmall)
    WhiteDnsTextField("Standby domains (up to 8 domains total)", options.standbyDomains,
        { update(options.copy(standbyDomains = it)) }, "backup.example, second.example", rawValue = true)
    CottenMaxNumberField(profileId, "Failover wait (ms)", options.failoverAfterMs, 100, 60000) { update(options.copy(failoverAfterMs = it)) }
    ToggleRow("Vary QNAME label count", options.labelCountJitter) { update(options.copy(labelCountJitter = !options.labelCountJitter)) }
    ToggleRow("Experimental QNAME loss guard", options.qnameLossGuard) { update(options.copy(qnameLossGuard = !options.qnameLossGuard)) }
    if (options.qnameLossGuard) {
        Text(text = "Recent authenticated near-cap answers can avoid false QNAME downgrades under loss. This can help lossy paths but may slow healthy ones.", style = MaterialTheme.typography.bodySmall)
    }
    ToggleRow("Authenticated resolver discovery", options.discoveryEnabled) { update(options.copy(discoveryEnabled = !options.discoveryEnabled)) }
    if (options.discoveryEnabled) {
        WhiteDnsTextField("Resolver country (optional)", options.discoveryCountry,
            { update(options.copy(discoveryCountry = it)) }, "ir", rawValue = true)
        WhiteDnsTextField("Additional resolver candidates", options.discoveryCandidates,
            { update(options.copy(discoveryCandidates = it)) }, "IP:port, IP:port", rawValue = true)
        ToggleRow("Remember verified resolver hints", options.discoveryCacheEnabled) { update(options.copy(discoveryCacheEnabled = !options.discoveryCacheEnabled)) }
        if (options.discoveryCacheEnabled) {
            WhiteDnsTextField("Network label (optional)", options.discoveryNetworkId,
                { update(options.copy(discoveryNetworkId = it)) }, "home or mobile", rawValue = true)
        }
        CottenMaxNumberField(profileId, "Address-range samples (0 disables)", options.discoverySampleRanges, 0, 4096) { update(options.copy(discoverySampleRanges = it)) }
        Text(text = "Discovery tests additional DNS endpoints. Range sampling is opt-in; cached candidates authenticate again on every connection.", style = MaterialTheme.typography.bodySmall)
        CottenMaxNumberField(profileId, "Desired verified resolvers", options.discoveryWant, 1, 4096) { update(options.copy(discoveryWant = it)) }
        CottenMaxNumberField(profileId, "Discovery concurrency", options.discoveryConcurrency, 1, 64) { update(options.copy(discoveryConcurrency = it)) }
        CottenMaxNumberField(profileId, "Discovery pace (ms)", options.discoveryPaceMs, 1, 60000) { update(options.copy(discoveryPaceMs = it)) }
    }
    CottenMaxNumberField(profileId, "Initial QNAME size", options.safeQName, 64, 253) { update(options.copy(safeQName = it)) }
    CottenMaxNumberField(profileId, "Maximum QNAME size", options.maxQName, 64, 253) { update(options.copy(maxQName = it)) }
    CottenMaxNumberField(profileId, "Maximum DNS answer size", options.maxAnswer, 512, 1232) { update(options.copy(maxAnswer = it)) }
    CottenMaxNumberField(profileId, "Per-path query window", options.maxPathWindow, 1, 1024) { update(options.copy(maxPathWindow = it)) }
    CottenMaxNumberField(profileId, "Total in-flight queries", options.maxInflight, 1, 4096) { update(options.copy(maxInflight = it)) }
    ToggleRow("Download forward error correction (FEC)", options.fecDownloadEnabled) {
        update(options.copy(fecDownloadEnabled = !options.fecDownloadEnabled))
    }
    Text(text = "FEC adds recovery data and uses extra bandwidth. Enabling it requires a server with CottenMax FEC support. Leave it off to connect to older servers.", style = MaterialTheme.typography.bodySmall)
    if (options.fecDownloadEnabled) {
        CottenMaxNumberField(profileId, "FEC data shards per block", options.fecDataShards, 2, 16) { update(options.copy(fecDataShards = it)) }
        CottenMaxNumberField(profileId, "FEC recovery shards per block", options.fecParityShards, 1, 4) { update(options.copy(fecParityShards = it)) }
    }
    CottenMaxNumberField(profileId, "Idle session retirement (ms)", options.sessionIdleMs, 1000, 90000) { update(options.copy(sessionIdleMs = it)) }
    WhiteDnsDropdownField("Held poll interval", options.holdMs,
        (0..1500 step 100).map { Choice(it, if (it == 0) "Off" else "$it ms") },
        { update(options.copy(holdMs = it)) })
}

@Composable
private fun CottenMaxNumberField(
    profileId: String,
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    var text by rememberSaveable(profileId, label) { mutableStateOf(value.toString()) }
    LaunchedEffect(value) { if (text.toIntOrNull() != value) text = value.toString() }
    WhiteDnsTextField(label, text, { next ->
        if (next.all(Char::isDigit)) {
            text = next
            next.toIntOrNull()?.takeIf { it in min..max }?.let(onValueChange)
        }
    }, "$min–$max", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), rawValue = true)
}

@Composable
private fun CottenDnsPresetSummaryPanel(settings: CottenDnsProfileSettings) {
    val summary = remember(settings) { CottenDnsSettingsRenderer.summarize(settings) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteDnsPalette.SurfaceAlt)
            .border(1.dp, WhiteDnsPalette.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CottenDnsFeatureLine(WhiteDnsL10n.profileFieldTransportMode, summary.transport)
        CottenDnsFeatureLine(WhiteDnsL10n.profileFieldDeliveryMode, summary.delivery)
        CottenDnsFeatureLine(WhiteDnsL10n.profileFieldIpMode, summary.ipMode)
        CottenDnsFeatureLine(WhiteDnsL10n.cottenSummaryMtu, summary.mtu)
        CottenDnsFeatureLine(WhiteDnsL10n.cottenSummaryHardening, WhiteDnsL10n.cottenSummaryHardeningValue)
    }
}

@Composable
private fun CottenDnsFeatureLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(74.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = WhiteDnsPalette.Muted,
                fontWeight = FontWeight.Normal,
            ),
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = WhiteDnsPalette.Description,
            ),
        )
    }
}

@Composable
private fun localizedCottenConfigPresets(): List<Choice<String>> = listOf(
    Choice("default", WhiteDnsL10n.cottenPresetDefault),
    Choice("speed", WhiteDnsL10n.cottenPresetSpeed),
    Choice("survival", WhiteDnsL10n.cottenPresetSurvival),
    Choice("tcp-survival", WhiteDnsL10n.cottenPresetTcpSurvival),
    Choice("master-storm", WhiteDnsL10n.cottenPresetMasterStorm),
)

@Composable
private fun localizedCottenTransportModes(): List<Choice<String>> = listOf(
    Choice(CottenDnsProfileSettings.ModePreset, WhiteDnsL10n.cottenFromPreset),
    Choice("auto", WhiteDnsL10n.cottenTransportAuto),
    Choice("udp", WhiteDnsL10n.cottenTransportUdp),
    Choice("tcp", WhiteDnsL10n.cottenTransportTcp),
    Choice("dot", WhiteDnsL10n.cottenTransportDot),
    Choice("doh", WhiteDnsL10n.cottenTransportDoh),
    Choice("doh3", "DoH over HTTP/3 (QUIC)"),
    Choice("doq", "DNS over QUIC"),
)

@Composable
private fun localizedCottenDeliveryModes(): List<Choice<String>> = listOf(
    Choice(CottenDnsProfileSettings.ModePreset, WhiteDnsL10n.cottenFromPreset),
    Choice("txt", WhiteDnsL10n.cottenDeliveryTxt),
    Choice("txt-cname", WhiteDnsL10n.cottenDeliveryTxtCname),
    Choice("txt-https", WhiteDnsL10n.cottenDeliveryTxtHttps),
    Choice("txt-aaaa", WhiteDnsL10n.cottenDeliveryTxtAaaa),
    Choice("all", WhiteDnsL10n.cottenDeliveryAll),
)

@Composable
private fun localizedCottenIpModes(): List<Choice<String>> = listOf(
    Choice(CottenDnsProfileSettings.ModePreset, WhiteDnsL10n.cottenFromPreset),
    Choice("auto", WhiteDnsL10n.cottenIpAuto),
    Choice("dual", WhiteDnsL10n.cottenIpDual),
    Choice("ipv4", WhiteDnsL10n.cottenIpv4Only),
    Choice("ipv6", WhiteDnsL10n.cottenIpv6Only),
)

@Composable
private fun localizedCottenQueryRateLimits(): List<Choice<String>> =
    CottenDnsProfileSettings.QueryRateLimitValues.map { value ->
        if (value == CottenDnsProfileSettings.RateOff) {
            Choice(value, WhiteDnsL10n.cottenRateOff)
        } else {
            Choice(value, WhiteDnsL10n.cottenRatePerSecond.format(value.toInt()))
        }
    }

@Composable
private fun localizedCottenQueryRateScopes(): List<Choice<String>> = listOf(
    Choice("total", WhiteDnsL10n.cottenRateScopeTotal),
    Choice("resolver", WhiteDnsL10n.cottenRateScopeResolver),
    Choice("domain", WhiteDnsL10n.cottenRateScopeDomain),
)

@Composable
private fun localizedCottenTimingMasks(): List<Choice<String>> = listOf(
    Choice("off", WhiteDnsL10n.cottenRateOff),
    Choice("light", WhiteDnsL10n.cottenMaskLight),
    Choice("strong", WhiteDnsL10n.cottenMaskStrong),
)

@Composable
private fun localizedCottenQnameModes(): List<Choice<String>> = listOf(
    Choice(CottenDnsProfileSettings.ModePreset, WhiteDnsL10n.cottenFromPreset),
    Choice("off", WhiteDnsL10n.cottenQnameOff),
    Choice("moderate", WhiteDnsL10n.cottenQnameModerate),
    Choice("aggressive", WhiteDnsL10n.cottenQnameAggressive),
)

@Composable
private fun localizedEncryptionMethods(): List<Choice<Int>> = listOf(
    Choice(0, WhiteDnsL10n.encryptionMethodNone),
    Choice(1, WhiteDnsL10n.encryptionMethodXor),
    Choice(2, WhiteDnsL10n.encryptionMethodChacha20),
    Choice(3, WhiteDnsL10n.encryptionMethodAes128),
    Choice(4, WhiteDnsL10n.encryptionMethodAes192),
    Choice(5, WhiteDnsL10n.encryptionMethodAes256),
)

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
    val shape = RoundedCornerShape(WhiteDnsRadii.control)
    val horizontalPadding = if (compact) 10.dp else 12.dp
    val borderColor by animateColorAsState(
        targetValue = if (!enabled) {
            WhiteDnsPalette.Divider
        } else if (expanded) {
            WhiteDnsPalette.Accent
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
                    .heightIn(min = WhiteDnsLayout.controlHeight)
                    .clip(shape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor, shape)
                    .semantics(mergeDescendants = true) {
                        contentDescription = label
                        stateDescription = selectedLabel
                    }
                    .clickable(enabled = enabled, role = Role.Button) {
                        haptic.performLight()
                        expanded = true
                    }
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedLabel.ifEmpty { WhiteDnsL10n.dropdownPlaceholderSelect },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = if (enabled) WhiteDnsPalette.Ink else WhiteDnsPalette.Disabled,
                        fontWeight = FontWeight.Normal,
                    ),
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
                containerColor = WhiteDnsPalette.DropdownSurface,
                tonalElevation = 0.dp,
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(WhiteDnsRadii.card))
                    .background(WhiteDnsPalette.DropdownSurface),
            ) {
                options.forEach { choice ->
                    val selected = choice.value == value
                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(WhiteDnsRadii.control))
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
                                        fontWeight = if (selected) FontWeight.Normal else FontWeight.Normal,
                                    ),
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
    allAppsLabel: String,
    noAppsLabel: String,
): String {
    if (mode == WhiteDnsOptions.SplitTunnelModeOff) {
        return allAppsLabel
    }
    if (appLabels.isEmpty()) {
        return noAppsLabel
    }
    return compactAppLabelSummary(appLabels, noAppsLabel)
}

private fun splitTunnelConnectionSummary(
    mode: String,
    packageNames: List<String>,
    labelsByPackage: Map<String, String>,
    allAppsLabel: String,
    noAppsLabel: String,
    onlyPrefix: String,
    bypassPrefix: String,
): String {
    val labels = packageNames.map { packageName ->
        labelsByPackage[packageName] ?: packageName
    }
    return when (mode) {
        WhiteDnsOptions.SplitTunnelModeInclude -> {
            if (labels.isEmpty()) allAppsLabel else "$onlyPrefix ${compactAppLabelSummary(labels, noAppsLabel)}"
        }
        WhiteDnsOptions.SplitTunnelModeExclude -> {
            if (labels.isEmpty()) allAppsLabel else "$bypassPrefix ${compactAppLabelSummary(labels, noAppsLabel)}"
        }
        else -> allAppsLabel
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

private fun compactAppLabelSummary(appLabels: List<String>, noAppsLabel: String): String {
    return when (appLabels.size) {
        0 -> noAppsLabel
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
private const val ResolverProfilesExportFileName = "client_resolvers.txt"

@Composable
private fun soarText(english: String, persian: String): String =
    if (LocalWhiteDnsStrings.current === PersianStrings) persian else english

@Composable
private fun SoarSettingsPanel(value: SoarProfileSettings, onChange: (SoarProfileSettings) -> Unit) {
    var advanced by remember { mutableStateOf(false) }
    val presets = listOf("conservative", "balanced", "fast")
    val preset = presets.firstOrNull {
        value == SoarProfileSettings.preset(it).copy(discovery = value.discovery, country = value.country)
    } ?: "custom"
    Text(soarText("Soar: TCP web traffic over DNS. VPN mode does not add UDP/QUIC support. Default server ports: 80 and 443.",
        "Soar ترافیک TCP وب را روی DNS منتقل می‌کند. حالت VPN پشتیبانی UDP/QUIC اضافه نمی‌کند. پورت‌های پیش‌فرض سرور: ۸۰ و ۴۴۳."),
        style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
    WhiteDnsDropdownField(label = soarText("Soar preset", "تنظیم آمادهٔ Soar"), value = preset,
        options = listOf(Choice("balanced", soarText("Balanced (recommended)", "متعادل (پیشنهادی)")),
            Choice("conservative", soarText("Conservative / restricted DNS", "محافظه‌کار / DNS محدود")),
            Choice("fast", soarText("Fast / reliable DNS", "سریع / DNS پایدار")), Choice("custom", soarText("Custom", "سفارشی"))),
        onValueChange = { if (it != "custom") onChange(SoarProfileSettings.preset(it).copy(discovery = value.discovery, country = value.country)) })
    WhiteDnsDropdownField(label = soarText("DNS resolver transport", "مسیر اتصال به DNS resolver"), value = value.transport,
        options = listOf(Choice("auto", soarText("Auto UDP with TCP fallback", "خودکار: UDP با جایگزین TCP")),
            Choice("on", soarText("UDP and TCP together", "UDP و TCP هم‌زمان")), Choice("only", "TCP only"), Choice("off", "UDP only")),
        onValueChange = { onChange(value.copy(transport = it)) })
    WhiteDnsDropdownField(label = soarText("Discover additional resolvers", "کشف resolverهای بیشتر"), value = value.discovery,
        options = listOf(Choice(true, soarText("Enabled", "فعال")), Choice(false, soarText("Disabled (configured list only)", "غیرفعال (فقط فهرست انتخابی)"))),
        onValueChange = { onChange(value.copy(discovery = it)) })
    if (value.discovery) {
        WhiteDnsDropdownField(label = soarText("User network country", "کشور اینترنت کاربر"), value = value.country,
            options = listOf(Choice("IR", soarText("Iran", "ایران")), Choice("", soarText("Global / other", "عمومی / سایر"))),
            onValueChange = { onChange(value.copy(country = it)) })
        Text(soarText("Uses authenticated discovery and bundled lists. Random range scanning is disabled. This is the phone network country, not the VPS location.",
            "کشف با تأیید هویت سرور و فهرست‌های آماده انجام می‌شود؛ اسکن تصادفی بازه‌های IP خاموش است. کشور اینترنت گوشی را انتخاب کنید، نه محل VPS."),
            style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
    }
    CompactActionButton(modifier = Modifier.fillMaxWidth(), label = if (advanced) soarText("Hide advanced settings", "بستن تنظیمات پیشرفته") else soarText("Advanced Soar settings", "تنظیمات پیشرفتهٔ Soar"),
        emphasized = false, enabled = true, onClick = { advanced = !advanced })
    if (advanced) {
        SoarIntField(soarText("Queries in flight (total)", "پرس‌وجوی هم‌زمان (کل)"), value.maxInflight, listOf(8, 16, 32, 64, 128, 256, 512, 1024)) { onChange(value.copy(maxInflight = it)) }
        SoarIntField(soarText("Window per resolver path", "پنجرهٔ هر مسیر resolver"), value.maxPathWindow, listOf(2, 4, 8, 16, 32, 64, 128)) { onChange(value.copy(maxPathWindow = it)) }
        SoarIntField(soarText("Maximum DNS answer (bytes)", "حداکثر اندازهٔ پاسخ DNS (بایت)"), value.maxAnswer, listOf(512, 704, 896, 1232)) { onChange(value.copy(maxAnswer = it)) }
        SoarIntField(soarText("Maximum query name length", "حداکثر طول نام پرس‌وجو"), value.maxQName, listOf(101, 128, 180, 253)) { onChange(value.copy(maxQName = it, safeQName = minOf(value.safeQName, it))) }
        SoarIntField(soarText("Initial safe query name length", "طول اولیهٔ امن نام پرس‌وجو"), value.safeQName, listOf(101, 128, 180, 253).filter { it <= value.maxQName }) { onChange(value.copy(safeQName = it)) }
        SoarIntField(soarText("Held poll budget (ms)", "مهلت نگه‌داشتن پرس‌وجو (میلی‌ثانیه)"), value.holdMs, listOf(100, 250, 500, 1000, 1500)) { onChange(value.copy(holdMs = it)) }
        SoarIntField(soarText("Maximum TCP streams", "حداکثر اتصال‌های TCP"), value.maxStreams, listOf(8, 16, 32, 64, 128, 256)) { onChange(value.copy(maxStreams = it)) }
        SoarIntField(soarText("Receive window per stream (KiB)", "پنجرهٔ دریافت هر اتصال (KiB)"), value.streamWindowKiB, listOf(64, 128, 256, 512, 1024)) { onChange(value.copy(streamWindowKiB = it)) }
        SoarIntField(soarText("Send buffer per stream (KiB)", "بافر ارسال هر اتصال (KiB)"), value.sendBufferKiB, listOf(32, 64, 128, 256, 512, 1024)) { onChange(value.copy(sendBufferKiB = it)) }
        SoarIntField(soarText("Retire idle session after (seconds)", "بازنشانی نشست بیکار پس از (ثانیه)"), value.sessionIdleSeconds, listOf(15, 30, 60, 90, 110)) { onChange(value.copy(sessionIdleSeconds = it)) }
        SoarIntField(soarText("Connection / startup timeout (seconds)", "مهلت اتصال و راه‌اندازی (ثانیه)"), value.dialTimeoutSeconds, listOf(15, 30, 45, 60, 90, 120)) { onChange(value.copy(dialTimeoutSeconds = it)) }
        if (value.discovery) {
            SoarIntField(soarText("Desired healthy resolver paths", "تعداد مسیر سالم موردنظر"), value.discoveryWant, listOf(1, 2, 4, 8, 16, 32)) { onChange(value.copy(discoveryWant = it)) }
            SoarIntField(soarText("Discovery concurrency (scan capped at 8)", "هم‌زمانی کشف (اسکن حداکثر ۸)"), value.discoveryConcurrency, listOf(1, 2, 4, 8, 16)) { onChange(value.copy(discoveryConcurrency = it)) }
            SoarIntField(soarText("Discovery spacing (ms)", "فاصلهٔ تلاش‌های کشف (میلی‌ثانیه)"), value.discoveryPaceMs, listOf(100, 250, 500, 1000, 2000, 5000)) { onChange(value.copy(discoveryPaceMs = it)) }
        }
        Text(soarText("Higher values can increase memory and DNS traffic. Start with Balanced; use Conservative when resolvers drop long names or large answers. Master/Cotten encryption, duplication, compression and MTU controls do not apply to Soar.",
            "مقادیر بیشتر می‌توانند مصرف حافظه و ترافیک DNS را بالا ببرند. از حالت متعادل شروع کنید؛ برای حذف نام‌های بلند یا پاسخ‌های بزرگ، حالت محافظه‌کار را امتحان کنید. تنظیمات رمزنگاری، تکرار، فشرده‌سازی و MTU موتورهای Master/Cotten روی Soar اعمال نمی‌شوند."),
            style = MaterialTheme.typography.bodySmall, color = WhiteDnsPalette.Muted)
    }
}

@Composable
private fun SoarIntField(label: String, value: Int, values: List<Int>, onChange: (Int) -> Unit) {
    WhiteDnsDropdownField(label = label, value = value, options = (values + value).distinct().sorted().map { Choice(it, it.toString()) }, onValueChange = onChange)
}

@Composable
private fun Modifier.dialogBodyScroll(): Modifier {
    val scrollState = rememberScrollState()
    return if (LocalCompactHomeDialog.current) this else verticalScroll(scrollState)
}

@Composable
private fun WhiteDnsDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    val homeHost = LocalHomeDialogHost.current
    val frame: @Composable () -> Unit = {
        val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .background(if (homeHost) Color.Black.copy(alpha = 0.32f) else Color.Transparent)
                .pointerInput(onDismissRequest) { detectTapGestures { onDismissRequest() } }
                .then(if (homeHost) Modifier else Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.union(WindowInsets.displayCutout).union(WindowInsets.ime)))
                .padding(horizontal = WhiteDnsLayout.screenHorizontalPadding, vertical = if (keyboardVisible) 8.dp else 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val compactHome = homeHost && maxHeight < 240.dp
            Box(Modifier.widthIn(max = WhiteDnsLayout.contentMaxWidth)
                .then(if (compactHome) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .pointerInput(Unit) { detectTapGestures { /* Keep taps inside the dialog. */ } }) {
                CompositionLocalProvider(LocalCompactHomeDialog provides compactHome) { content() }
            }
        }
    }
    if (homeHost) {
        BackHandler(onBack = onDismissRequest)
        frame()
    } else {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
            content = frame,
        )
    }
}

@Composable
private fun WhiteDnsNavigationRail(selectedTab: WhiteDnsTab, onTabSelected: (WhiteDnsTab) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxHeight().navigationBarsPadding().padding(start = 18.dp, bottom = 18.dp)) {
        val compactHeight = maxHeight < 360.dp
        if (compactHeight) {
            Column(
                Modifier.width(80.dp).clip(RoundedCornerShape(24.dp)).background(WhiteDnsPalette.Surface)
                    .verticalScroll(rememberScrollState()).selectableGroup().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WhiteDnsTab.entries.forEach { tab ->
                    val label = tabLabel(tab)
                    val selected = selectedTab == tab
                    Box(
                        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (selected) WhiteDnsPalette.AccentSurface else Color.Transparent)
                            .selectable(selected, role = Role.Tab, onClick = { onTabSelected(tab) })
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(when (tab) {
                            WhiteDnsTab.PROFILES -> R.drawable.ic_subscriptions_tab
                            WhiteDnsTab.CONNECT -> R.drawable.ic_vpn_tab
                            WhiteDnsTab.SCAN -> R.drawable.ic_connection_test
                            WhiteDnsTab.LOGS -> R.drawable.ic_logs
                        }), null, tint = if (selected) WhiteDnsPalette.Accent else WhiteDnsPalette.Muted,
                            modifier = Modifier.size(22.dp))
                    }
                }
            }
        } else {
            NavigationRail(
                modifier = Modifier.fillMaxHeight().clip(RoundedCornerShape(24.dp)),
                containerColor = WhiteDnsPalette.Surface,
            ) {
                WhiteDnsTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedTab == tab, onClick = { onTabSelected(tab) },
                        icon = {
                            val resource = when (tab) {
                                WhiteDnsTab.PROFILES -> R.drawable.ic_subscriptions_tab
                                WhiteDnsTab.CONNECT -> R.drawable.ic_vpn_tab
                                WhiteDnsTab.SCAN -> R.drawable.ic_connection_test
                                WhiteDnsTab.LOGS -> R.drawable.ic_logs
                            }
                            Icon(painterResource(resource), null, modifier = Modifier.size(22.dp))
                        },
                        label = { Text(tabLabel(tab), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = WhiteDnsPalette.Accent, selectedTextColor = WhiteDnsPalette.Accent,
                            indicatorColor = WhiteDnsPalette.AccentSurface,
                            unselectedIconColor = WhiteDnsPalette.Muted, unselectedTextColor = WhiteDnsPalette.Muted,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun profileReorderActions(
    enabled: Boolean, index: Int, count: Int, onMove: (Int) -> Unit,
): List<CustomAccessibilityAction> {
    val up = soarText("Move up", "انتقال به بالا")
    val down = soarText("Move down", "انتقال به پایین")
    return buildList {
        if (enabled && index > 0) add(CustomAccessibilityAction(up) { onMove(index - 1); true })
        if (enabled && index in 0 until count - 1) add(CustomAccessibilityAction(down) { onMove(index + 1); true })
    }
}

@Composable
private fun connectionStatusLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.DISCONNECTED -> soarText("Not connected", "متصل نیست")
    ConnectionStatus.CONNECTING -> WhiteDnsL10n.btnConnecting
    ConnectionStatus.CONNECTED -> WhiteDnsL10n.connectProgressConnected
}

@Composable
private fun HomeTrafficSummary(
    status: ConnectionStatus, stats: ConnectionStats, latestEvent: String?, onLogsClick: () -> Unit,
) {
    val requester = remember { BringIntoViewRequester() }
    var previousStatus by remember { mutableStateOf(status) }
    LaunchedEffect(status) {
        if (previousStatus != ConnectionStatus.DISCONNECTED && status == ConnectionStatus.DISCONNECTED) {
            withFrameNanos { }
            requester.bringIntoView()
        }
        previousStatus = status
    }
    Column(
        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(requester),
    ) {
        LiveSpeedStrip(if (status == ConnectionStatus.DISCONNECTED) ConnectionStats() else stats)
        if (status == ConnectionStatus.DISCONNECTED && latestEvent != null) {
            Spacer(Modifier.height(12.dp))
            Text(latestEvent.take(500), color = WhiteDnsPalette.Description,
                style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            Spacer(Modifier.height(8.dp))
            CompactActionButton(modifier = Modifier.fillMaxWidth(),
                label = soarText("View connection logs", "مشاهدهٔ گزارش اتصال"),
                emphasized = false, enabled = true, onClick = onLogsClick)
        }
    }
}

@Composable
private fun profileRevealModifier(reveal: Boolean, onRevealed: () -> Unit): Modifier {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(reveal) {
        if (reveal) {
            withFrameNanos { }
            requester.bringIntoView()
            onRevealed()
        }
    }
    return Modifier.bringIntoViewRequester(requester)
}
