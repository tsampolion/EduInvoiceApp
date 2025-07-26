package gr.eduinvoice.data.repository

import java.security.MessageDigest
import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    private fun sha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

    fun verify(password: String, storedHash: String): Boolean {
        return if (storedHash.startsWith("$2")) {
            BCrypt.checkpw(password, storedHash)
        } else {
            sha256(password) == storedHash
        }
    }

    fun needsMigration(storedHash: String): Boolean = !storedHash.startsWith("$2")

    fun legacyHash(password: String): String = sha256(password)
}
