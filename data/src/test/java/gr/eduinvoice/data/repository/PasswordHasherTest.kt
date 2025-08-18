package gr.eduinvoice.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `should hash password with BCrypt`() {
        // Given
        val password = "testPassword123"

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `should hash password with BCrypt and verify correctly`() {
        // Given
        val password = "securePassword456"
        val wrongPassword = "wrongPassword789"

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(PasswordHasher.verify(password, hash))
        assertFalse(PasswordHasher.verify(wrongPassword, hash))
    }

    @Test
    fun `should hash empty password with BCrypt`() {
        // Given
        val password = ""

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `should hash special characters password with BCrypt`() {
        // Given
        val password = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `should hash unicode password with BCrypt`() {
        // Given
        val password = "pässwörd🚀🎉"

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `should hash very long password with BCrypt`() {
        // Given
        val password = "a".repeat(1000)

        // When
        val hash = PasswordHasher.hash(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `should verify legacy SHA-256 hash correctly`() {
        // Given
        val password = "legacyPassword"
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val result = PasswordHasher.verify(password, legacyHash)

        // Then
        assertTrue(result)
        assertFalse(legacyHash.startsWith("$2"))
    }

    @Test
    fun `should verify legacy SHA-256 hash with wrong password`() {
        // Given
        val password = "correctPassword"
        val wrongPassword = "wrongPassword"
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val result = PasswordHasher.verify(wrongPassword, legacyHash)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should verify legacy SHA-256 hash with empty password`() {
        // Given
        val password = ""
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val result = PasswordHasher.verify(password, legacyHash)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should verify legacy SHA-256 hash with special characters`() {
        // Given
        val password = "!@#$%^&*()"
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val result = PasswordHasher.verify(password, legacyHash)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should verify legacy SHA-256 hash with unicode characters`() {
        // Given
        val password = "pässwörd🚀"
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val result = PasswordHasher.verify(password, legacyHash)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should detect BCrypt hash does not need migration`() {
        // Given
        val bcryptHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        // When
        val needsMigration = PasswordHasher.needsMigration(bcryptHash)

        // Then
        assertFalse(needsMigration)
    }

    @Test
    fun `should detect legacy SHA-256 hash needs migration`() {
        // Given
        val legacyHash = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        // When
        val needsMigration = PasswordHasher.needsMigration(legacyHash)

        // Then
        assertTrue(needsMigration)
    }

    @Test
    fun `should detect empty hash needs migration`() {
        // Given
        val emptyHash = ""

        // When
        val needsMigration = PasswordHasher.needsMigration(emptyHash)

        // Then
        assertTrue(needsMigration)
    }

    @Test
    fun `should detect hash starting with $2a needs no migration`() {
        // Given
        val bcryptHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        // When
        val needsMigration = PasswordHasher.needsMigration(bcryptHash)

        // Then
        assertFalse(needsMigration)
    }

    @Test
    fun `should detect hash starting with $2b needs no migration`() {
        // Given
        val bcryptHash = "\$2b\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        // When
        val needsMigration = PasswordHasher.needsMigration(bcryptHash)

        // Then
        assertFalse(needsMigration)
    }

    @Test
    fun `should detect hash starting with $2y needs no migration`() {
        // Given
        val bcryptHash = "\$2y\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        // When
        val needsMigration = PasswordHasher.needsMigration(bcryptHash)

        // Then
        assertFalse(needsMigration)
    }

    @Test
    fun `should generate consistent legacy SHA-256 hash`() {
        // Given
        val password = "consistentPassword"

        // When
        val hash1 = PasswordHasher.legacyHash(password)
        val hash2 = PasswordHasher.legacyHash(password)

        // Then
        assertEquals(hash1, hash2)
    }

    @Test
    fun `should generate different legacy SHA-256 hashes for different passwords`() {
        // Given
        val password1 = "password1"
        val password2 = "password2"

        // When
        val hash1 = PasswordHasher.legacyHash(password1)
        val hash2 = PasswordHasher.legacyHash(password2)

        // Then
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `should generate different BCrypt hashes for same password`() {
        // Given
        val password = "samePassword"

        // When
        val hash1 = PasswordHasher.hash(password)
        val hash2 = PasswordHasher.hash(password)

        // Then
        assertNotEquals(hash1, hash2)
        assertTrue(PasswordHasher.verify(password, hash1))
        assertTrue(PasswordHasher.verify(password, hash2))
    }

    @Test
    fun `should handle mixed hash verification scenarios`() {
        // Given
        val password = "mixedPassword"
        val bcryptHash = PasswordHasher.hash(password)
        val legacyHash = PasswordHasher.legacyHash(password)

        // When
        val bcryptResult = PasswordHasher.verify(password, bcryptHash)
        val legacyResult = PasswordHasher.verify(password, legacyHash)

        // Then
        assertTrue(bcryptResult)
        assertTrue(legacyResult)
        assertFalse(PasswordHasher.needsMigration(bcryptHash))
        assertTrue(PasswordHasher.needsMigration(legacyHash))
    }

    @Test
    fun `should handle edge case with very short password`() {
        // Given
        val password = "a"

        // When
        val hash = PasswordHasher.hash(password)
        val legacyHash = PasswordHasher.legacyHash(password)

        // Then
        assertTrue(PasswordHasher.verify(password, hash))
        assertTrue(PasswordHasher.verify(password, legacyHash))
    }

    @Test
    fun `should handle edge case with password containing only numbers`() {
        // Given
        val password = "1234567890"

        // When
        val hash = PasswordHasher.hash(password)
        val legacyHash = PasswordHasher.legacyHash(password)

        // Then
        assertTrue(PasswordHasher.verify(password, hash))
        assertTrue(PasswordHasher.verify(password, legacyHash))
    }

    @Test
    fun `should handle edge case with password containing only special characters`() {
        // Given
        val password = "!@#$%^&*()"

        // When
        val hash = PasswordHasher.hash(password)
        val legacyHash = PasswordHasher.legacyHash(password)

        // Then
        assertTrue(PasswordHasher.verify(password, hash))
        assertTrue(PasswordHasher.verify(password, legacyHash))
    }
}
