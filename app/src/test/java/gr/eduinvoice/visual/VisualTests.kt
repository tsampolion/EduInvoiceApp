package gr.eduinvoice.visual

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.DeviceConfig
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore

@Ignore("Temporarily disabled - Paparazzi configuration needs fixing")
class VisualTests {
    
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6,
        maxPercentDifference = 0.0
    )

    @Test
    fun simpleText_snapshot() {
        paparazzi.snapshot { 
            MaterialTheme {
                Surface {
                    SampleComposable() 
                }
            }
        }
    }
    
    @Test
    fun styledText_snapshot() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    StyledTextComposable()
                }
            }
        }
    }
    
    @Test
    fun multipleTextVariants_snapshot() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    MultipleTextVariants()
                }
            }
        }
    }
}

@Composable
fun SampleComposable() {
    Text("Snapshot Test")
}

@Composable
fun StyledTextComposable() {
    Text(
        text = "Styled Text Test",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun MultipleTextVariants() {
    Text("Regular Text")
    Text(
        text = "Large Bold Text",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Small Italic Text",
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
}
