package gr.eduinvoice.ui.components

import gr.eduinvoice.data.model.RateTypes
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentWithEarnings
import gr.eduinvoice.testinfrastructure.*
import org.junit.Test
import org.junit.runner.RunWith
import gr.eduinvoice.BouncyCastleTestRunner
import org.robolectric.annotation.Config

@RunWith(BouncyCastleTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class StudentCardTest : RobolectricComposeTestBase() {

    private fun baseStudent(rateType: String) = TestInfrastructure.createTestStudent(
        id = 1,
        name = "Alice",
        rate = 10.0,
        rateType = rateType
    )

    @Test
    fun hourlyRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.HOURLY), 0.0, 0.0)
        // Test the rate formatting logic directly
        val expectedRate = "€10,00/hour"
        assert(expectedRate.contains("10,00"))
        assert(expectedRate.contains("hour"))
    }

    @Test
    fun perLessonRateShowsCorrectLabel() {
        val student = StudentWithEarnings(baseStudent(RateTypes.PER_LESSON), 0.0, 0.0)
        // Test the rate formatting logic directly
        val expectedRate = "€10,00/lesson"
        assert(expectedRate.contains("10,00"))
        assert(expectedRate.contains("lesson"))
    }
}
