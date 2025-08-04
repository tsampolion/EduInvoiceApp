package gr.eduinvoice

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.BeforeClass
import java.security.Security

abstract class TestBase {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupTestEnvironment() {
            // Ensure BouncyCastle provider is available
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }
} 