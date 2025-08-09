package gr.eduinvoice.testinfrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Test to verify screenshot functionality works correctly
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    @Test
    fun testScreenshotFunctionality() {
        // Test the standardized screenshot implementation
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val out = File(InstrumentationRegistry.getInstrumentation().targetContext.externalCacheDir, "shot-${System.currentTimeMillis()}.png")
        
        // This should compile and work without errors
        device.takeScreenshot(out)
        
        // Verify the file was created
        assert(out.exists()) { "Screenshot file should be created" }
    }

    @Test
    fun testScreenshotHelperFunction() {
        // Test the helper function from AndroidTestInfrastructure
        AndroidTestInfrastructure.UiTestUtils.takeScreenshot("test-screenshot")
    }
}
