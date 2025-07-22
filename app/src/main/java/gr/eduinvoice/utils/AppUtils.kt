// AppUtils.kt - Fixed currency formatting
package gr.eduinvoice.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

fun formatAsCurrency(amount: Double): String {
    return try {
        // Use NumberFormat for proper currency formatting
        val format = NumberFormat.getCurrencyInstance(Locale("el", "GR"))
        format.currency = java.util.Currency.getInstance("EUR")
        format.format(amount)
    } catch (e: Exception) {
        // Fallback to simple formatting if NumberFormat fails
        "€ %.2f".format(Locale.US, amount)
    }
}

fun Double.formatAsCurrency(symbol: String = "€", decimals: Int = 2): String {
    val safeDecimals = decimals.coerceIn(0, 2)
    val symbols = DecimalFormatSymbols().apply { currencySymbol = symbol }
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
