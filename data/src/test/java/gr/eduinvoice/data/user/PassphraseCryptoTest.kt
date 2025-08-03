package gr.eduinvoice.data.user

import android.content.Context
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.data.BouncyCastleTestRunner

@RunWith(BouncyCastleTestRunner::class)
class PassphraseCryptoTest : TestBase() {
    private lateinit var crypto: PassphraseCrypto

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        crypto = PassphraseCrypto(context)
    }

    @Test
    fun generatePassphraseProduces32Bytes() {
        val passphrase = crypto.generatePassphrase()
        val bytes = Base64.decode(passphrase, Base64.NO_WRAP)
        assertEquals(32, bytes.size)
    }

    @Test
    fun encryptDecryptRoundTrip() {
        val passphrase = crypto.generatePassphrase()
        val encrypted = crypto.encrypt(passphrase)
        val decrypted = crypto.decrypt(encrypted)
        assertEquals(passphrase, decrypted)
    }

    @Test
    fun isEncryptedDetectsPrefix() {
        val passphrase = crypto.generatePassphrase()
        val encrypted = crypto.encrypt(passphrase)
        assertTrue(crypto.isEncrypted(encrypted))
        assertFalse(crypto.isEncrypted(passphrase))
    }
}
