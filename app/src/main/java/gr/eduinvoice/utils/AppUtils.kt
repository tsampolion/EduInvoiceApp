// AppUtils.kt - Fixed currency formatting
package gr.eduinvoice.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.time.LocalDate
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
}
