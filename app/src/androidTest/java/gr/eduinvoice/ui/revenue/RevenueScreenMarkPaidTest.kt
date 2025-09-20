package gr.eduinvoice.ui.revenue

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import gr.eduinvoice.MainActivity
import org.junit.Rule
import org.junit.Test

class RevenueScreenMarkPaidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun markPaidButton_clicks() {
        // This is a smoke test to ensure the button exists and is clickable.
        // Full verification would use an IdlingResource or fake ViewModel.
        composeTestRule.onNodeWithText("Mark Paid").performClick()
    }
}