// AppUtils.kt - Fixed currency formatting
package gr.eduinvoice.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatAsCurrency(amount: Double, locale: Locale = Locale.getDefault()): String {
    return try {
        NumberFormat.getCurrencyInstance(locale).format(amount)
    } catch (_: Exception) {
        NumberFormat.getCurrencyInstance(Locale.US).format(amount)
    }
}

fun Double.formatAsCurrency(symbol: String = "€", decimals: Int = 2): String {
    val safeDecimals = decimals.coerceIn(0, 2)
    val symbols = DecimalFormatSymbols(Locale("el", "GR")).apply { currencySymbol = "" }
    val pattern = "#,##0.${"0".repeat(safeDecimals)}"
    val formatter = DecimalFormat(pattern, symbols).apply {
        minimumFractionDigits = safeDecimals
        maximumFractionDigits = safeDecimals
    }
    return "$symbol${formatter.format(this)}"
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

object AppUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    fun toEpochMillis(dateIso: String): Long {
        return try {
            val d = LocalDate.parse(dateIso, isoFormatter)
            d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }
    
    // Validation functions
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length <= 50 && name.matches(Regex("^[a-zA-Z\\s]+$"))
    }
    
    fun isValidClassName(className: String): Boolean {
        return className.isNotBlank() && className.length <= 30
    }
    
    fun isValidDate(date: String): Boolean {
        return try {
            LocalDate.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidTime(time: String): Boolean {
        return try {
            LocalTime.parse(time)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidDuration(duration: Int): Boolean {
        return duration > 0 && duration <= 480 // Max 8 hours
    }
    
    fun isValidNotes(notes: String): Boolean {
        return notes.length <= 500
    }
}
