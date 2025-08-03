package gr.eduinvoice.domain

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.runners.model.InitializationError
import org.robolectric.RobolectricTestRunner
import java.security.Security

class BouncyCastleTestRunner(testClass: Class<*>) : RobolectricTestRunner(testClass) {
    init {
        // Add BouncyCastle provider if not already present
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
} 