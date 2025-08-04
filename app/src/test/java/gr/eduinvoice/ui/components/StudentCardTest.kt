package gr.eduinvoice.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import gr.eduinvoice.data.model.RateTypes
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentWithEarnings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import org.robolectric.annotation.Config

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34])
class StudentCardTest : gr.eduinvoice.TestBase() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @get:Rule
    val composeRule = createComposeRule()

    private fun baseStudent(rateType: String) = Student(
        id = 1,
        ownerId = 1L,
        name = "Alice",
        surname = "",
        parentMobile = "",
        className = "",
        rate = 10.0,
        rateType = rateType
    )

    @Test
    fun hourlyRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.HOURLY), 0.0, 0.0)
        composeRule.setContent {
            StudentCard(student, onStudentClick = {}, onDeleteClick = {})
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("€10,00/hour").assertExists()
    }

    @Test
    fun perLessonRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.PER_LESSON), 0.0, 0.0)
        composeRule.setContent {
            StudentCard(student, onStudentClick = {}, onDeleteClick = {})
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("€10,00/lesson").assertExists()
    }
}
