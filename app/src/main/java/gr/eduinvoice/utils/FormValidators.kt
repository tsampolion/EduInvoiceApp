package gr.eduinvoice.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object FormValidators {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val isoDateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun nonBlank(value: String): Boolean = value.isNotBlank()

    fun positiveRate(value: String): Boolean = value.toDoubleOrNull()?.let { it > 0.0 } ?: false

    fun emailOrBlank(value: String): Boolean = value.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()

    fun validClassSelection(selectedClass: String, customClass: String): Boolean =
        selectedClass.isNotBlank() && (selectedClass != "Custom" || customClass.isNotBlank())

    fun validDateDdMMyyyy(value: String): Boolean = runCatching { LocalDate.parse(value, dateFormatter) }.isSuccess

    fun validIsoDate(value: String): Boolean = runCatching { LocalDate.parse(value, isoDateFormatter) }.isSuccess

    fun validTime(value: String): Boolean = runCatching { LocalTime.parse(value, timeFormatter) }.isSuccess

    fun validDurationMinutes(value: String, min: Int, max: Int): Boolean =
        value.toIntOrNull()?.let { it in min..max } ?: false
}


