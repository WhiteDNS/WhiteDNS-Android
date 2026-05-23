package shop.whitedns.client.scan

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.json.JSONObject

data class WhiteDnsScanLaunchRequest(
    val id: String,
    val sourceName: String,
    val resolverFilePath: String,
    val workerCount: Int,
    val initialValidResolvers: List<String> = emptyList(),
    val initialRejectedResolvers: List<String> = emptyList(),
    val initialCompletedResolvers: Int = 0,
    val totalResolvers: Int = 0,
)

object WhiteDnsScanRequestStore {
    private const val DirectoryName = "scan-launch"
    private const val Extension = ".json"
    private val SafeIdRegex = Regex("[A-Za-z0-9._-]+")

    fun save(context: Context, request: WhiteDnsScanLaunchRequest) {
        require(request.id.isSafeRequestId()) { "Invalid scan launch request ID" }
        requestDirectory(context).mkdirs()
        writeAtomicText(requestFile(context, request.id), encode(request).toString())
    }

    fun load(context: Context, requestId: String): WhiteDnsScanLaunchRequest? {
        if (!requestId.isSafeRequestId()) {
            return null
        }
        val file = requestFile(context, requestId)
        if (!file.isFile) {
            return null
        }
        return runCatching {
            decode(JSONObject(file.readText(Charsets.UTF_8)))
        }.getOrNull()
    }

    fun delete(context: Context, requestId: String) {
        if (requestId.isSafeRequestId()) {
            requestFile(context, requestId).delete()
        }
    }

    private fun requestDirectory(context: Context): File {
        return File(context.noBackupFilesDir, DirectoryName)
    }

    private fun requestFile(context: Context, requestId: String): File {
        return File(requestDirectory(context), "$requestId$Extension")
    }

    private fun encode(request: WhiteDnsScanLaunchRequest): JSONObject {
        return JSONObject()
            .put("id", request.id)
            .put("sourceName", request.sourceName)
            .put("resolverFilePath", request.resolverFilePath)
            .put("workerCount", request.workerCount)
            .put("initialValidResolvers", org.json.JSONArray().also { array ->
                request.initialValidResolvers.forEach(array::put)
            })
            .put("initialRejectedResolvers", org.json.JSONArray().also { array ->
                request.initialRejectedResolvers.forEach(array::put)
            })
            .put("initialCompletedResolvers", request.initialCompletedResolvers)
            .put("totalResolvers", request.totalResolvers)
    }

    private fun decode(json: JSONObject): WhiteDnsScanLaunchRequest {
        return WhiteDnsScanLaunchRequest(
            id = json.optString("id"),
            sourceName = json.optString("sourceName"),
            resolverFilePath = json.optString("resolverFilePath"),
            workerCount = json.optInt("workerCount", 1).coerceAtLeast(1),
            initialValidResolvers = json.optJSONArray("initialValidResolvers").toStringList(),
            initialRejectedResolvers = json.optJSONArray("initialRejectedResolvers").toStringList(),
            initialCompletedResolvers = json.optInt("initialCompletedResolvers").coerceAtLeast(0),
            totalResolvers = json.optInt("totalResolvers").coerceAtLeast(0),
        )
    }

    private fun org.json.JSONArray?.toStringList(): List<String> {
        if (this == null) {
            return emptyList()
        }
        return List(length()) { index -> optString(index) }
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
    }

    private fun String.isSafeRequestId(): Boolean {
        return isNotBlank() && SafeIdRegex.matches(this)
    }

    private fun writeAtomicText(file: File, text: String) {
        val atomicFile = AtomicFile(file)
        var stream: FileOutputStream? = null
        try {
            stream = atomicFile.startWrite()
            stream.write(text.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
        } catch (error: IOException) {
            stream?.let(atomicFile::failWrite)
            throw error
        }
    }
}
