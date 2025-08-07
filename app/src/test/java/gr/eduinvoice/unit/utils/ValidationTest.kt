package gr.eduinvoice.unit.utils

import gr.eduinvoice.testinfrastructure.BaseTest
import gr.eduinvoice.utils.AppUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for validation utilities and input validation logic
 */
class ValidationTest : BaseTest() {

    @Test
    fun `email validation accepts valid emails`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "test123@test-domain.com",
            "user@subdomain.example.com"
        )

        validEmails.forEach { email ->
            assertTrue("Email $email should be valid", isValidEmail(email))
        }
    }

    @Test
    fun `email validation rejects invalid emails`() {
        val invalidEmails = listOf(
            "invalid-email",
            "@example.com",
            "user@",
            "user@.com",
            "user..name@example.com",
            "user@example..com",
            "user name@example.com",
            "user@example com",
            "",
            "   ",
            "user@example",
            "user.example.com"
        )

        invalidEmails.forEach { email ->
            assertFalse("Email $email should be invalid", isValidEmail(email))
        }
    }

    @Test
    fun `phone number validation accepts valid phone numbers`() {
        val validPhoneNumbers = listOf(
            "+30123456789",
            "+44123456789",
            "+1234567890",
            "30123456789",
            "44123456789",
            "1234567890"
        )

        validPhoneNumbers.forEach { phone ->
            assertTrue("Phone number $phone should be valid", isValidPhoneNumber(phone))
        }
    }

    @Test
    fun `phone number validation rejects invalid phone numbers`() {
        val invalidPhoneNumbers = listOf(
            "123",
            "12345678901234567890", // Too long
            "+",
            "+abc123456",
            "abc123456",
            "123-456-7890",
            "(123) 456-7890",
            "",
            "   ",
            "123.456.7890"
        )

        invalidPhoneNumbers.forEach { phone ->
            assertFalse("Phone number $phone should be invalid", isValidPhoneNumber(phone))
        }
    }

    @Test
    fun `rate validation accepts valid rates`() {
        val validRates = listOf(
            1.0,
            25.5,
            100.0,
            999.99,
            0.01
        )

        validRates.forEach { rate ->
            assertTrue("Rate $rate should be valid", isValidRate(rate))
        }
    }

    @Test
    fun `rate validation rejects invalid rates`() {
        val invalidRates = listOf(
            -1.0,
            -10.5,
            0.0,
            1001.0,
            9999.99,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )

        invalidRates.forEach { rate ->
            assertFalse("Rate $rate should be invalid", isValidRate(rate))
        }
    }

    @Test
    fun `name validation accepts valid names`() {
        val validNames = listOf(
            "John",
            "Mary Jane",
            "José",
            "O'Connor",
            "van der Berg",
            "Jean-Pierre",
            "A", // Minimum length
            "A".repeat(50) // Reasonable maximum length
        )

        validNames.forEach { name ->
            assertTrue("Name '$name' should be valid", AppUtils.isValidName(name))
        }
    }

    @Test
    fun `name validation rejects invalid names`() {
        val invalidNames = listOf(
            "",
            "   ",
            "A".repeat(1000), // Too long
            "123",
            "John@Doe",
            "John#Doe",
            "John$Doe"
        )

        invalidNames.forEach { name ->
            assertFalse("Name '$name' should be invalid", AppUtils.isValidName(name))
        }
    }

    @Test
    fun `class name validation accepts valid class names`() {
        val validClassNames = listOf(
            "Math 101",
            "Advanced Physics",
            "English Literature",
            "Computer Science",
            "A", // Minimum length
            "A".repeat(100) // Reasonable maximum length
        )

        validClassNames.forEach { className ->
            assertTrue("Class name '$className' should be valid", AppUtils.isValidClassName(className))
        }
    }

    @Test
    fun `class name validation rejects invalid class names`() {
        val invalidClassNames = listOf(
            "",
            "   ",
            "A".repeat(1000), // Too long
            "Class@Name",
            "Class#Name",
            "Class$Name"
        )

        invalidClassNames.forEach { className ->
            assertFalse("Class name '$className' should be invalid", AppUtils.isValidClassName(className))
        }
    }

    @Test
    fun `date validation accepts valid dates`() {
        val validDates = listOf(
            "2024-01-01",
            "2024-12-31",
            "2023-02-29", // Leap year
            "2024-06-15"
        )

        validDates.forEach { date ->
            assertTrue("Date $date should be valid", AppUtils.isValidDate(date))
        }
    }

    @Test
    fun `date validation rejects invalid dates`() {
        val invalidDates = listOf(
            "",
            "   ",
            "2024-13-01", // Invalid month
            "2024-12-32", // Invalid day
            "2024-02-30", // February doesn't have 30 days
            "2023-02-29", // Not a leap year
            "2024/01/01", // Wrong format
            "01-01-2024", // Wrong format
            "2024-1-1", // Missing leading zeros
            "abc-def-ghi"
        )

        invalidDates.forEach { date ->
            assertFalse("Date $date should be invalid", AppUtils.isValidDate(date))
        }
    }

    @Test
    fun `time validation accepts valid times`() {
        val validTimes = listOf(
            "09:00",
            "12:30",
            "23:59",
            "00:00",
            "14:15"
        )

        validTimes.forEach { time ->
            assertTrue("Time $time should be valid", AppUtils.isValidTime(time))
        }
    }

    @Test
    fun `time validation rejects invalid times`() {
        val invalidTimes = listOf(
            "",
            "   ",
            "24:00", // Invalid hour
            "12:60", // Invalid minute
            "25:30", // Invalid hour
            "12:99", // Invalid minute
            "9:00", // Missing leading zero
            "12:5", // Missing leading zero
            "abc:def",
            "12-30", // Wrong format
            "12.30" // Wrong format
        )

        invalidTimes.forEach { time ->
            assertFalse("Time $time should be invalid", AppUtils.isValidTime(time))
        }
    }

    @Test
    fun `duration validation accepts valid durations`() {
        val validDurations = listOf(
            15, // 15 minutes
            30, // 30 minutes
            60, // 1 hour
            90, // 1.5 hours
            120, // 2 hours
            480 // 8 hours
        )

        validDurations.forEach { duration ->
            assertTrue("Duration $duration should be valid", AppUtils.isValidDuration(duration))
        }
    }

    @Test
    fun `duration validation rejects invalid durations`() {
        val invalidDurations = listOf(
            -1,
            -10,
            0,
            481, // More than 8 hours
            1000,
            Int.MAX_VALUE
        )

        invalidDurations.forEach { duration ->
            assertFalse("Duration $duration should be invalid", AppUtils.isValidDuration(duration))
        }
    }

    @Test
    fun `notes validation accepts valid notes`() {
        val validNotes = listOf(
            "Regular lesson",
            "Student was late by 10 minutes",
            "Focused on algebra today",
            "Homework assigned: Chapter 5, problems 1-10",
            "", // Empty notes are valid
            "A".repeat(1000) // Reasonable maximum length
        )

        validNotes.forEach { note ->
            assertTrue("Note '$note' should be valid", AppUtils.isValidNotes(note))
        }
    }

    @Test
    fun `notes validation rejects invalid notes`() {
        val invalidNotes = listOf(
            "A".repeat(10001), // Too long
            null
        )

        invalidNotes.forEach { note ->
            assertFalse("Note '$note' should be invalid", AppUtils.isValidNotes(note))
        }
    }
}
