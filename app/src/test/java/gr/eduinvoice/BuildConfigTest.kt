package gr.eduinvoice

import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConfigTest {
    @Test
    fun firebaseApiKey_notEmpty() {
        assertTrue(BuildConfig.FIREBASE_API_KEY.isNotBlank())
    }
}
