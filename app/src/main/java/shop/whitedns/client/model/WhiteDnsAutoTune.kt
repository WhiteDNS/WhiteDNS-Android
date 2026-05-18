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
            id = "auto-1",
            label = "WhiteDNS Config 1",
            minUploadMtu = "100",
            maxUploadMtu = "1000",
            minDownloadMtu = "200",
            maxDownloadMtu = "4000",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "15",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 3,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-2",
            label = "WhiteDNS Config 2",
            minUploadMtu = "100",
            maxUploadMtu = "500",
            minDownloadMtu = "100",
            maxDownloadMtu = "1325",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "250",
            uploadDuplication = "5",
            downloadDuplication = "15",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-3",
            label = "WhiteDNS Config 3",
            minUploadMtu = "50",
            maxUploadMtu = "1500",
            minDownloadMtu = "50",
            maxDownloadMtu = "500",
            resolverTimeoutSeconds = "0.2",
            dnsResponseFragmentStoreCapacity = "200",
            uploadDuplication = "1",
            downloadDuplication = "1",
            uploadCompression = 0,
            downloadCompression = 0,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-4",
            label = "WhiteDNS Config 4",
            minUploadMtu = "20",
            maxUploadMtu = "325",
            minDownloadMtu = "100",
            maxDownloadMtu = "1270",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "100",
            uploadDuplication = "1",
            downloadDuplication = "5",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-5",
            label = "WhiteDNS Config 5",
            minUploadMtu = "50",
            maxUploadMtu = "500",
            minDownloadMtu = "100",
            maxDownloadMtu = "9000",
            resolverTimeoutSeconds = "0.3",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "1",
            downloadDuplication = "1",
            uploadCompression = 0,
            downloadCompression = 0,
            stability = WhiteDnsAutoTunePresetStability.Aggressive,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-6",
            label = "WhiteDNS Config 6",
            minUploadMtu = "40",
            maxUploadMtu = "200",
            minDownloadMtu = "100",
            maxDownloadMtu = "1500",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "5",
            downloadDuplication = "5",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-7",
            label = "WhiteDNS Config 7",
            minUploadMtu = "64",
            maxUploadMtu = "64",
            minDownloadMtu = "756",
            maxDownloadMtu = "756",
            resolverTimeoutSeconds = "0.7",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "15",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 2,
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
