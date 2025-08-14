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
class StudentsFabVisibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun add_fab_hidden_when_students_empty() {
        composeRule.waitForIdle()
        // With no students, StudentsScreen hides the Add FAB
        composeRule.onAllNodesWithContentDescription("Add Student").assertCountEquals(0)
    }
}


