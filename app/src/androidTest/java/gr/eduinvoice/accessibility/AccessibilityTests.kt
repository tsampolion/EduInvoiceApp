package gr.eduinvoice.accessibility

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityTests {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun text_hasReadableContent() {
        compose.setContent { Text("Hello") }
        compose.onNodeWithText("Hello").assertExists()
    }
}
