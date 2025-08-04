package gr.eduinvoice.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import gr.eduinvoice.data.model.RateTypes
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentWithEarnings
import gr.eduinvoice.testinfrastructure.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import gr.eduinvoice.MainDispatcherRule
import org.robolectric.annotation.Config

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class StudentCardTest : ComposeTestBase() {

    private fun baseStudent(rateType: String) = TestInfrastructure.createTestStudent(
        id = 1,
        name = "Alice",
        rate = 10.0,
        rateType = rateType
    )

    @Test
    fun hourlyRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.HOURLY), 0.0, 0.0)
        setComposeContent {
            StudentCard(student, onStudentClick = {}, onDeleteClick = {})
        }
        composeTestRule.onNodeWithText("€10,00/hour").assertExists()
    }

    @Test
    fun perLessonRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.PER_LESSON), 0.0, 0.0)
        setComposeContent {
            StudentCard(student, onStudentClick = {}, onDeleteClick = {})
        }
        composeTestRule.onNodeWithText("€10,00/lesson").assertExists()
    }
}
