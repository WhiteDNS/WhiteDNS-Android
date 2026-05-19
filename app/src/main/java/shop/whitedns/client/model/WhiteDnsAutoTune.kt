package shop.whitedns.client.model

data class WhiteDnsAutoTunePreset(
    val id: String,
    val label: String,
    val minUploadMtu: String,
    val maxUploadMtu: String,
    val minDownloadMtu: String,
    val maxDownloadMtu: String,
    val resolverTimeoutSeconds: String,
    val dnsResponseFragmentStoreCapacity: String,
    val uploadDuplication: String,
    val downloadDuplication: String,
    val uploadCompression: Int,
    val downloadCompression: Int,
    val stability: WhiteDnsAutoTunePresetStability = WhiteDnsAutoTunePresetStability.Stable,
)

enum class WhiteDnsAutoTunePresetStability {
    Stable,
    Aggressive,
}

object WhiteDnsAutoTunePresets {
    val all: List<WhiteDnsAutoTunePreset> = listOf(
        WhiteDnsAutoTunePreset(
            id = "iran-average",
            label = "Iran Default",
            minUploadMtu = "40",
            maxUploadMtu = "140",
            minDownloadMtu = "300",
            maxDownloadMtu = "3000",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "3",
            downloadDuplication = "7",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-low-mtu-scan",
            label = "Iran Low MTU Scan",
            minUploadMtu = "20",
            maxUploadMtu = "120",
            minDownloadMtu = "160",
            maxDownloadMtu = "768",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "3",
            downloadDuplication = "7",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-fast-low-mtu",
            label = "Iran Fast Low MTU",
            minUploadMtu = "20",
            maxUploadMtu = "325",
            minDownloadMtu = "100",
            maxDownloadMtu = "1270",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "100",
            uploadDuplication = "5",
            downloadDuplication = "10",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-compact-fixed",
            label = "Iran Compact Fixed",
            minUploadMtu = "62",
            maxUploadMtu = "62",
            minDownloadMtu = "414",
            maxDownloadMtu = "414",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "384",
            uploadDuplication = "6",
            downloadDuplication = "8",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-fixed-64-balanced",
            label = "Iran Fixed 64 Balanced",
            minUploadMtu = "64",
            maxUploadMtu = "64",
            minDownloadMtu = "756",
            maxDownloadMtu = "756",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "8",
            downloadDuplication = "8",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-mid-reliable",
            label = "Iran Mid Reliable",
            minUploadMtu = "120",
            maxUploadMtu = "160",
            minDownloadMtu = "652",
            maxDownloadMtu = "1110",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "5",
            downloadDuplication = "11",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-download-heavy",
            label = "Iran Download Heavy",
            minUploadMtu = "104",
            maxUploadMtu = "139",
            minDownloadMtu = "394",
            maxDownloadMtu = "1000",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "8",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-fixed-64-aggressive",
            label = "Iran Fixed 64 Wide",
            minUploadMtu = "64",
            maxUploadMtu = "64",
            minDownloadMtu = "756",
            maxDownloadMtu = "1317",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "230",
            uploadDuplication = "14",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 2,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-large-download-aggressive",
            label = "Iran No Compression Max",
            minUploadMtu = "100",
            maxUploadMtu = "600",
            minDownloadMtu = "800",
            maxDownloadMtu = "6500",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "640",
            uploadDuplication = "23",
            downloadDuplication = "30",
            uploadCompression = 0,
            downloadCompression = 0,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
        WhiteDnsAutoTunePreset(
            id = "iran-wide-range-aggressive",
            label = "Iran Wide Range Max",
            minUploadMtu = "100",
            maxUploadMtu = "1000",
            minDownloadMtu = "200",
            maxDownloadMtu = "2667",
            resolverTimeoutSeconds = "2.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "15",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 2,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
    )
}

object WhiteDnsParallelTest {
    const val EnabledByDefault = false
    const val MaxSelectedConfigs = 10
    private const val WhiteDnsConfigPrefix = "whitedns:"
    private const val SettingConfigPrefix = "setting:"

    val defaultConfigIds: List<String>
        get() = stableConfigIds

    val stableConfigIds: List<String>
        get() = WhiteDnsAutoTunePresets.all
            .filter { it.stability == WhiteDnsAutoTunePresetStability.Stable }
            .map { whiteDnsConfigId(it.id) }

    val aggressiveConfigIds: List<String>
        get() = WhiteDnsAutoTunePresets.all
            .filter { it.stability == WhiteDnsAutoTunePresetStability.Aggressive }
            .map { whiteDnsConfigId(it.id) }

    val allConfigIds: List<String>
        get() = WhiteDnsAutoTunePresets.all.map { whiteDnsConfigId(it.id) }

    fun whiteDnsConfigIds(includeAggressive: Boolean): List<String> {
        return if (includeAggressive) {
            allConfigIds
        } else {
            stableConfigIds
        }
    }

    fun whiteDnsConfigId(presetId: String): String = "$WhiteDnsConfigPrefix$presetId"

    fun settingConfigId(profileId: String): String = "$SettingConfigPrefix$profileId"

    fun presetIdFromConfigId(configId: String): String? {
        val normalized = normalizeLegacyConfigId(configId)
        return normalized.removePrefix(WhiteDnsConfigPrefix)
            .takeIf { normalized.startsWith(WhiteDnsConfigPrefix) && it.isNotBlank() }
    }

    fun settingProfileIdFromConfigId(configId: String): String? {
        return configId.removePrefix(SettingConfigPrefix)
            .takeIf { configId.startsWith(SettingConfigPrefix) && it.isNotBlank() }
    }

    fun normalizeConfigIds(
        configIds: List<String>,
        advancedProfiles: List<AdvancedSettingsProfile>,
        defaultIfEmpty: Boolean = true,
        includeAggressive: Boolean = false,
    ): List<String> {
        val whiteDnsIds = whiteDnsConfigIds(includeAggressive)
        val settingIds = advancedProfiles
            .filter { it.id.isNotBlank() && it.id != AdvancedSettingsProfile.DefaultId }
            .map { settingConfigId(it.id) }
        val availableIds = (whiteDnsIds + settingIds).toSet()
        val requestedIds = if (configIds.isEmpty() && defaultIfEmpty) {
            whiteDnsIds
        } else {
            configIds
        }
        return requestedIds
            .map(::normalizeLegacyConfigId)
            .distinct()
            .filter { it in availableIds }
            .take(MaxSelectedConfigs)
            .ifEmpty {
                if (defaultIfEmpty) {
                    whiteDnsIds.take(MaxSelectedConfigs)
                } else {
                    emptyList()
                }
            }
    }

    private fun normalizeLegacyConfigId(configId: String): String {
        return if (WhiteDnsAutoTunePresets.all.any { it.id == configId }) {
            whiteDnsConfigId(configId)
        } else {
            configId
        }
    }
}

fun WhiteDnsSettings.applyAutoTunePreset(preset: WhiteDnsAutoTunePreset): WhiteDnsSettings {
    return copy(
        minUploadMtu = preset.minUploadMtu,
        maxUploadMtu = preset.maxUploadMtu,
        minDownloadMtu = preset.minDownloadMtu,
        maxDownloadMtu = preset.maxDownloadMtu,
        mtuTestTimeoutResolvers = preset.resolverTimeoutSeconds,
        mtuTestTimeoutLogs = preset.resolverTimeoutSeconds,
        dnsResponseFragmentStoreCapacity = preset.dnsResponseFragmentStoreCapacity,
        uploadDuplication = preset.uploadDuplication,
        downloadDuplication = preset.downloadDuplication,
        uploadCompression = preset.uploadCompression,
        downloadCompression = preset.downloadCompression,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.recoverPersistedParallelTestPreset(): WhiteDnsSettings {
    val settings = syncSelectedConnectionProfileFields()
    if (!settings.matchesAutoTunePresetFields()) {
        return settings
    }

    val selectedProfile = settings.selectedAdvancedProfile()
    return when {
        selectedProfile.id != AdvancedSettingsProfile.DefaultId &&
            settings.matchesAdvancedProfile(selectedProfile) -> settings

        selectedProfile.id != AdvancedSettingsProfile.DefaultId -> settings
            .applyAdvancedProfile(selectedProfile)
            .copy(
                autoTuneEnabled = settings.autoTuneEnabled,
                parallelTestSelectedConfigIds = settings.parallelTestSelectedConfigIds,
            )
            .syncSelectedConnectionProfileFields()

        else -> settings
            .copyAutoTunePresetFieldsFrom(WhiteDnsSettings())
            .syncSelectedConnectionProfileFields()
    }
}

private fun WhiteDnsSettings.matchesAutoTunePresetFields(): Boolean {
    return WhiteDnsAutoTunePresets.all.any { preset ->
        minUploadMtu == preset.minUploadMtu &&
            maxUploadMtu == preset.maxUploadMtu &&
            minDownloadMtu == preset.minDownloadMtu &&
            maxDownloadMtu == preset.maxDownloadMtu &&
            mtuTestTimeoutResolvers == preset.resolverTimeoutSeconds &&
            mtuTestTimeoutLogs == preset.resolverTimeoutSeconds &&
            dnsResponseFragmentStoreCapacity == preset.dnsResponseFragmentStoreCapacity &&
            uploadDuplication == preset.uploadDuplication &&
            downloadDuplication == preset.downloadDuplication &&
            uploadCompression == preset.uploadCompression &&
            downloadCompression == preset.downloadCompression
    }
}

private fun WhiteDnsSettings.copyAutoTunePresetFieldsFrom(source: WhiteDnsSettings): WhiteDnsSettings {
    return copy(
        minUploadMtu = source.minUploadMtu,
        maxUploadMtu = source.maxUploadMtu,
        minDownloadMtu = source.minDownloadMtu,
        maxDownloadMtu = source.maxDownloadMtu,
        mtuTestTimeoutResolvers = source.mtuTestTimeoutResolvers,
        mtuTestTimeoutLogs = source.mtuTestTimeoutLogs,
        dnsResponseFragmentStoreCapacity = source.dnsResponseFragmentStoreCapacity,
        uploadDuplication = source.uploadDuplication,
        downloadDuplication = source.downloadDuplication,
        uploadCompression = source.uploadCompression,
        downloadCompression = source.downloadCompression,
    )
}
