package gr.eduinvoice.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gr.eduinvoice.data.concurrency.ConcurrencyController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestHiltSetup {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var concurrencyController: ConcurrencyController

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testConcurrencyControllerInjection() {
        // This test verifies that Hilt injection works
        assert(concurrencyController != null)
    }
}
