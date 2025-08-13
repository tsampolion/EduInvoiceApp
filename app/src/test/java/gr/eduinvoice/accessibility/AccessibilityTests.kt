package gr.eduinvoice.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AccessibilityTests {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun text_hasReadableContent() {
        compose.setContent { Text("Hello") }
        compose.onNodeWithText("Hello").assertExists()
    }
}


