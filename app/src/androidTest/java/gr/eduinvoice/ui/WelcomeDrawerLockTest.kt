package gr.eduinvoice.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeDrawerLockTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun drawer_menu_icon_hidden_on_welcome() {
        // On first launch (no logged-in user), app should show Welcome screen
        // and the global navigation menu button should be hidden.
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("Menu").assertCountEquals(0)
    }
}