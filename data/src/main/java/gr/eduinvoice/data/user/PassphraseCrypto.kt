package gr.eduinvoice.data.user

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_ALIAS = "db_passphrase_key"
private const val ENC_PREFIX = "enc:"
private const val IV_SIZE = 12

internal class PassphraseCrypto(context: Context) {
    private val keyStore: KeyStore = try {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    } catch (e: Exception) {
        // Fallback for test environments where AndroidKeyStore is not available
        KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null) }
    }

private fun getSecretKey(): SecretKey {
    return try {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        
        // Try to create a new key with more robust error handling
        try {
            // First try with AndroidKeyStore if available
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false) // Don't require biometric auth
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Log.e("PassphraseCrypto", "Failed to create key with AndroidKeyStore, trying alternative approach", e)
            // Fallback: try without specifying the provider
            try {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
                keyGenerator.init(256) // Use 256-bit key
                keyGenerator.generateKey()
            } catch (e2: Exception) {
                Log.e("PassphraseCrypto", "Failed to create key without provider, trying BouncyCastle", e2)
                // Final fallback: try with BouncyCastle provider
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "BC")
                keyGenerator.init(256)
                keyGenerator.generateKey()
            }
        }
    } catch (e: GeneralSecurityException) {
        Log.e("PassphraseCrypto", "Failed to get secret key", e)
        throw e
    } catch (e: IOException) {
        Log.e("PassphraseCrypto", "Failed to get secret key", e)
        throw e
    }
}

    fun generatePassphrase(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun encrypt(plain: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            ENC_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: GeneralSecurityException) {
            Log.e("PassphraseCrypto", "Failed to encrypt data", e)
            throw e
        } catch (e: IOException) {
            Log.e("PassphraseCrypto", "Failed to encrypt data", e)
            throw e
        }
    }

    fun decrypt(data: String): String {
        return try {
            val raw = if (data.startsWith(ENC_PREFIX)) data.substring(ENC_PREFIX.length) else data
            val bytes = Base64.decode(raw, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, IV_SIZE)
            val cipherText = bytes.copyOfRange(IV_SIZE, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            val key = getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            Log.e("PassphraseCrypto", "Failed to decrypt data", e)
            throw e
        } catch (e: IOException) {
            Log.e("PassphraseCrypto", "Failed to decrypt data", e)
            throw e
        }
    }

    fun isEncrypted(data: String?): Boolean = data?.startsWith(ENC_PREFIX) == true
}

internal const val ENCRYPTED_PREFIX = ENC_PREFIX
