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
class LessonsFabVisibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun add_lesson_fab_hidden_when_lessons_empty() {
        composeRule.waitForIdle()
        // With no lessons, LessonsScreen hides the Add FAB
        composeRule.onAllNodesWithContentDescription("Add Lesson").assertCountEquals(0)
    }
}


