package gr.eduinvoice.data

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.BeforeClass
import org.robolectric.RobolectricTestRunner
import java.security.Security

class BouncyCastleTestRunner(testClass: Class<*>) : RobolectricTestRunner(testClass) {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupProvider() {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }
}
