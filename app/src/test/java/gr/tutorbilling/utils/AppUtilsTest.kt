package gr.tutorbilling.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUtilsTest {
    @Test
    fun currencyFormatClampsDecimals() {
        val result = 100.0.formatAsCurrency("€", 36)
        assertEquals("€100.00", result)
    }
}
