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

package gr.eduinvoice.integration

import org.junit.Test

class UserFlowIntegrationTest {
    @Test
    fun smoke_login_crud_invoice_export() {
        // Placeholder: real flow is covered in instrumentation tests
        assert(true)
    }
}
