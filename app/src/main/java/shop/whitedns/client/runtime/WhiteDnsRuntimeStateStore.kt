package shop.whitedns.client.runtime

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.json.JSONObject
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.selectedConnectionProfile

data class WhiteDnsRuntimeState(
    val sessionId: String,
    val mode: String,
    val status: String,
    val connectionProfileId: String,
    val listenIp: String,
    val listenPort: Int,
    val updatedAtMillis: Long,
    val message: String = "",
)

object WhiteDnsRuntimeStateStore {
    const val ModeProxy = "proxy"
    const val ModeVpn = "vpn"
    const val StatusStarting = "starting"
    const val StatusReady = "ready"
    const val StatusStopped = "stopped"
    const val StatusFailed = "failed"

    fun markStarting(context: Context, settings: WhiteDnsSettings, sessionId: String, message: String = "") {
        writeSettingsState(context, settings, sessionId, StatusStarting, message)
    }

    fun markReady(context: Context, settings: WhiteDnsSettings, sessionId: String, message: String = "") {
        writeSettingsState(context, settings, sessionId, StatusReady, message)
    }

    fun markStopped(context: Context, mode: String, sessionId: String = "", message: String = "") {
        writeModeState(context, mode, sessionId, StatusStopped, message)
    }

    fun markFailed(context: Context, mode: String, message: String, sessionId: String = "") {
        writeModeState(context, mode, sessionId, StatusFailed, message)
    }

    fun read(context: Context, mode: String): WhiteDnsRuntimeState? {
        return runCatching {
            val file = stateFile(context, mode)
            if (!file.exists()) {
                return null
            }
            val raw = AtomicFile(file).openRead().use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            }
            decode(JSONObject(raw))
        }.getOrNull()
    }

    fun readAll(context: Context): List<WhiteDnsRuntimeState> {
        return listOf(ModeProxy, ModeVpn).mapNotNull { mode ->
            read(context, mode)
        }
    }

    private fun writeSettingsState(
        context: Context,
        settings: WhiteDnsSettings,
        sessionId: String,
        status: String,
        message: String,
    ) {
        val resolvedSettings = settings.resolve()
        val connectionProfile = settings.selectedConnectionProfile()
        writeState(
            context = context,
            state = WhiteDnsRuntimeState(
                sessionId = sessionId,
                mode = resolvedSettings.connectionMode,
                status = status,
                connectionProfileId = connectionProfile.id,
                listenIp = resolvedSettings.listenIp,
                listenPort = resolvedSettings.listenPort,
                updatedAtMillis = System.currentTimeMillis(),
                message = message,
            ),
        )
    }

    private fun writeModeState(
        context: Context,
        mode: String,
        sessionId: String,
        status: String,
        message: String,
    ) {
        val previous = read(context, mode)
        writeState(
            context = context,
            state = WhiteDnsRuntimeState(
                sessionId = sessionId.ifBlank { previous?.sessionId.orEmpty() },
                mode = mode,
                status = status,
                connectionProfileId = previous?.connectionProfileId.orEmpty(),
                listenIp = previous?.listenIp.orEmpty(),
                listenPort = previous?.listenPort ?: 0,
                updatedAtMillis = System.currentTimeMillis(),
                message = message,
            ),
        )
    }

    private fun writeState(context: Context, state: WhiteDnsRuntimeState) {
        runCatching {
            val target = stateFile(context, state.mode)
            target.parentFile?.mkdirs()
            val atomicFile = AtomicFile(target)
            var stream: FileOutputStream? = null
            try {
                stream = atomicFile.startWrite()
                stream.write(encode(state).toString().toByteArray(Charsets.UTF_8))
                atomicFile.finishWrite(stream)
            } catch (error: IOException) {
                stream?.let(atomicFile::failWrite)
                throw error
            }
        }
    }

    private fun stateFile(context: Context, mode: String): File {
        return File(File(context.noBackupFilesDir, RuntimeStateDirectory), "$mode.json")
    }

    private fun encode(state: WhiteDnsRuntimeState): JSONObject {
        return JSONObject()
            .put("sessionId", state.sessionId)
            .put("mode", state.mode)
            .put("status", state.status)
            .put("connectionProfileId", state.connectionProfileId)
            .put("listenIp", state.listenIp)
            .put("listenPort", state.listenPort)
            .put("updatedAtMillis", state.updatedAtMillis)
            .put("message", state.message)
    }

    private fun decode(json: JSONObject): WhiteDnsRuntimeState {
        return WhiteDnsRuntimeState(
            sessionId = json.optString("sessionId"),
            mode = json.optString("mode"),
            status = json.optString("status"),
            connectionProfileId = json.optString("connectionProfileId"),
            listenIp = json.optString("listenIp"),
            listenPort = json.optInt("listenPort"),
            updatedAtMillis = json.optLong("updatedAtMillis"),
            message = json.optString("message"),
        )
    }

    private const val RuntimeStateDirectory = "runtime-state"
}
