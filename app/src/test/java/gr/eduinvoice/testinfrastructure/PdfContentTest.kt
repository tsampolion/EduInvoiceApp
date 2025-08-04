package gr.eduinvoice.testinfrastructure

import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.RateTypes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * PDF content testing framework that tests business logic without Android dependencies
 */
class PdfContentTest {
    
    companion object {
        /**
         * Test data for invoice content generation
         */
        fun createTestInvoiceData(): List<LessonWithStudent> {
            val student1 = TestInfrastructure.createTestStudent(
                id = 1L,
                name = "Alice Johnson",
                rate = 25.0,
                rateType = RateTypes.HOURLY
            )
            
            val student2 = TestInfrastructure.createTestStudent(
                id = 2L,
                name = "Bob Smith",
                rate = 30.0,
                rateType = RateTypes.PER_LESSON
            )
            
            val lesson1 = TestInfrastructure.createTestLesson(
                id = 1L,
                studentId = 1L,
                date = "2024-01-15",
                startTime = "10:00",
                durationMinutes = 60
            )
            
            val lesson2 = TestInfrastructure.createTestLesson(
                id = 2L,
                studentId = 2L,
                date = "2024-01-15",
                startTime = "14:00",
                durationMinutes = 90
            )
            
            return listOf(
                LessonWithStudent(lesson1, student1),
                LessonWithStudent(lesson2, student2)
            )
        }
        
        /**
         * Calculate total invoice amount
         */
        fun calculateInvoiceTotal(lessons: List<LessonWithStudent>): Double {
            return lessons.sumOf { lessonWithStudent ->
                calculateLessonFee(lessonWithStudent.lesson, lessonWithStudent.student)
            }
        }
        
        /**
         * Calculate individual lesson fee
         */
        fun calculateLessonFee(lesson: Lesson, student: Student): Double {
            return when (student.rateType) {
                RateTypes.PER_LESSON -> student.rate
                RateTypes.HOURLY -> {
                    val hours = lesson.durationMinutes / 60.0
                    student.rate * hours
                }
                else -> student.rate
            }
        }
        
        /**
         * Generate invoice content as text (for testing without PDF generation)
         */
        fun generateInvoiceContent(
            lessons: List<LessonWithStudent>,
            invoiceNumber: String
        ): String {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val total = calculateInvoiceTotal(lessons)
            
            val content = StringBuilder()
            content.appendLine("Invoice $invoiceNumber")
            content.appendLine("Date: $currentDate")
            content.appendLine()
            content.appendLine("Lessons:")
            
            lessons.forEach { lessonWithStudent ->
                val lesson = lessonWithStudent.lesson
                val student = lessonWithStudent.student
                val fee = calculateLessonFee(lesson, student)
                
                content.appendLine("  ${lesson.date} - ${student.name} - ${lesson.startTime} (${lesson.durationMinutes} min)")
                content.appendLine("    Fee: €${String.format("%.2f", fee)}")
                content.appendLine()
            }
            
            content.appendLine("Total: €${String.format("%.2f", total)}")
            
            return content.toString()
        }
        
        /**
         * Validate invoice content structure
         */
        fun validateInvoiceContent(content: String): Boolean {
            val lines = content.lines()
            
            // Check for required sections
            val hasInvoiceHeader = lines.any { it.startsWith("Invoice ") }
            val hasDate = lines.any { it.startsWith("Date: ") }
            val hasLessons = lines.any { it == "Lessons:" }
            val hasTotal = lines.any { it.startsWith("Total: €") }
            
            return hasInvoiceHeader && hasDate && hasLessons && hasTotal
        }
        
        /**
         * Extract lesson count from invoice content
         */
        fun extractLessonCount(content: String): Int {
            return content.lines()
                .count { it.trim().startsWith("Fee: €") }
        }
        
        /**
         * Extract total amount from invoice content
         */
        fun extractTotalAmount(content: String): Double? {
            val totalLine = content.lines()
                .find { it.startsWith("Total: €") }
            
            return totalLine?.let { line ->
                val amountStr = line.substringAfter("Total: €")
                amountStr.toDoubleOrNull()
            }
        }
    }
    
    /**
     * Test invoice content generation
     */
    fun testInvoiceContentGeneration(): String {
        val lessons = createTestInvoiceData()
        val invoiceNumber = "INV-TEST-001"
        return generateInvoiceContent(lessons, invoiceNumber)
    }
    
    /**
     * Test fee calculation logic
     */
    fun testFeeCalculation(): Map<String, Double> {
        val student1 = TestInfrastructure.createTestStudent(
            name = "Hourly Student",
            rate = 25.0,
            rateType = RateTypes.HOURLY
        )
        
        val student2 = TestInfrastructure.createTestStudent(
            name = "Per Lesson Student",
            rate = 30.0,
            rateType = RateTypes.PER_LESSON
        )
        
        val lesson1 = TestInfrastructure.createTestLesson(
            durationMinutes = 60
        )
        
        val lesson2 = TestInfrastructure.createTestLesson(
            durationMinutes = 90
        )
        
        return mapOf(
            "hourly_60min" to calculateLessonFee(lesson1, student1),
            "hourly_90min" to calculateLessonFee(lesson2, student1),
            "per_lesson_60min" to calculateLessonFee(lesson1, student2),
            "per_lesson_90min" to calculateLessonFee(lesson2, student2)
        )
    }
    
    /**
     * Test invoice validation
     */
    fun testInvoiceValidation(): Boolean {
        val content = testInvoiceContentGeneration()
        return validateInvoiceContent(content)
    }
}

/**
 * Invoice content validator for testing
 */
class InvoiceContentValidator {
    
    /**
     * Validate that invoice contains all required fields
     */
    fun validateInvoiceStructure(content: String): ValidationResult {
        val lines = content.lines()
        val errors = mutableListOf<String>()
        
        // Check required sections
        if (!lines.any { it.startsWith("Invoice ") }) {
            errors.add("Missing invoice header")
        }
        
        if (!lines.any { it.startsWith("Date: ") }) {
            errors.add("Missing date")
        }
        
        if (!lines.any { it == "Lessons:" }) {
            errors.add("Missing lessons section")
        }
        
        if (!lines.any { it.startsWith("Total: €") }) {
            errors.add("Missing total amount")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate that lesson fees are calculated correctly
     */
    fun validateLessonFees(content: String, expectedLessons: List<LessonWithStudent>): ValidationResult {
        val errors = mutableListOf<String>()
        val lines = content.lines()
        
        val feeLines = lines.filter { it.trim().startsWith("Fee: €") }
        
        if (feeLines.size != expectedLessons.size) {
            errors.add("Expected ${expectedLessons.size} lessons, found ${feeLines.size}")
        }
        
        // Validate each lesson fee
        expectedLessons.forEachIndexed { index, lessonWithStudent ->
            if (index < feeLines.size) {
                val feeLine = feeLines[index]
                val expectedFee = PdfContentTest.calculateLessonFee(
                    lessonWithStudent.lesson,
                    lessonWithStudent.student
                )
                val actualFee = feeLine.substringAfter("Fee: €").toDoubleOrNull()
                
                if (actualFee == null) {
                    errors.add("Invalid fee format: $feeLine")
                } else if (kotlin.math.abs(actualFee - expectedFee) > 0.01) {
                    errors.add("Fee mismatch for lesson ${index + 1}: expected €$expectedFee, got €$actualFee")
                }
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate total amount calculation
     */
    fun validateTotalAmount(content: String, expectedLessons: List<LessonWithStudent>): ValidationResult {
        val errors = mutableListOf<String>()
        
        val expectedTotal = PdfContentTest.calculateInvoiceTotal(expectedLessons)
        val actualTotal = PdfContentTest.extractTotalAmount(content)
        
        if (actualTotal == null) {
            errors.add("Could not extract total amount from content")
        } else if (kotlin.math.abs(actualTotal - expectedTotal) > 0.01) {
            errors.add("Total amount mismatch: expected €$expectedTotal, got €$actualTotal")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) 