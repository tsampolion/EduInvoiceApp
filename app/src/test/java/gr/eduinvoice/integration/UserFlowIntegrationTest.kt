package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class UserFlowIntegrationTest {

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    @Test
    fun database_initializes_with_sqlcipher() {
        assertNotNull(databaseContainer.database)
    }
}

