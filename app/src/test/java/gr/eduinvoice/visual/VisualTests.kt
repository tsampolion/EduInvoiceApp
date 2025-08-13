package gr.eduinvoice.visual

import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.junit.Rule
import org.junit.Test

class VisualTests {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun simpleText_snapshot() {
        paparazzi.snapshot { SampleComposable() }
    }
}

@Composable
fun SampleComposable() {
    Text("Snapshot Test")
}


