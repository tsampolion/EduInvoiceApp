package gr.eduinvoice.data.user

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

private const val KEY_ALIAS = "db_passphrase_key"
private const val ENC_PREFIX = "enc:"
private const val IV_SIZE = 12

internal class PassphraseCrypto(context: Context) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun getSecretKey(): SecretKey? {
        return try {
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (t: Throwable) {
            Log.e("PassphraseCrypto", "Failed to get secret key", t)
            null
        }
    }

    fun generatePassphrase(): String {
        val bytes = ByteArray(32)
        Random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun encrypt(plain: String): String? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = getSecretKey() ?: return null
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            ENC_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (t: Throwable) {
            Log.e("PassphraseCrypto", "Failed to encrypt data", t)
            null
        }
    }

    fun decrypt(data: String): String? {
        return try {
            val raw = if (data.startsWith(ENC_PREFIX)) data.substring(ENC_PREFIX.length) else data
            val bytes = Base64.decode(raw, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, IV_SIZE)
            val cipherText = bytes.copyOfRange(IV_SIZE, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            val key = getSecretKey() ?: return null
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (t: Throwable) {
            Log.e("PassphraseCrypto", "Failed to decrypt data", t)
            null
        }
    }

    fun isEncrypted(data: String?): Boolean = data?.startsWith(ENC_PREFIX) == true
}

internal const val ENCRYPTED_PREFIX = ENC_PREFIX
