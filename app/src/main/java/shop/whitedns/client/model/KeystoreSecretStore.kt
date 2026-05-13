package shop.whitedns.client.model

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreSecretStore(
    @Suppress("UNUSED_PARAMETER")
    context: Context,
) {
    fun encryptToString(plaintext: String): String {
        if (plaintext.isEmpty()) {
            return ""
        }
        val cipher = Cipher.getInstance(CipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(cipher.iv.size + ciphertext.size)
        System.arraycopy(cipher.iv, 0, payload, 0, cipher.iv.size)
        System.arraycopy(ciphertext, 0, payload, cipher.iv.size, ciphertext.size)
        return PayloadPrefix + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decryptFromString(payload: String?): String? {
        if (payload.isNullOrEmpty()) {
            return payload
        }
        if (!payload.startsWith(PayloadPrefix)) {
            return null
        }
        return runCatching {
            val bytes = Base64.decode(payload.removePrefix(PayloadPrefix), Base64.NO_WRAP)
            if (bytes.size <= GcmIvBytes) {
                return@runCatching null
            }
            val iv = bytes.copyOfRange(0, GcmIvBytes)
            val ciphertext = bytes.copyOfRange(GcmIvBytes, bytes.size)
            val cipher = Cipher.getInstance(CipherTransformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GcmTagBits, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
        (keyStore.getEntry(KeyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
        val spec = KeyGenParameterSpec.Builder(
            KeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val AndroidKeyStore = "AndroidKeyStore"
        const val KeyAlias = "whitedns_settings_secret_v1"
        const val CipherTransformation = "AES/GCM/NoPadding"
        const val PayloadPrefix = "wdenc1:"
        const val GcmIvBytes = 12
        const val GcmTagBits = 128
    }
}
