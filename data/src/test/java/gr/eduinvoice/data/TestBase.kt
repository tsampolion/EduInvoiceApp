package gr.eduinvoice.data

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.BeforeClass
import java.security.Security

abstract class TestBase {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupBouncyCastle() {
            // Add BouncyCastle provider if not already present
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }
} 