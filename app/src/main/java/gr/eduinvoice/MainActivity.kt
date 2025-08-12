package gr.eduinvoice

import android.os.Bundle
import android.os.StrictMode
// import androidx.appcompat.app.ActionBarDrawerToggle - Removed since we're not using XML toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.navigation.NavigationView
import android.view.MenuItem
import android.view.View
import gr.eduinvoice.domain.model.DomainException
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.theme.TutorBillingTheme
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.analytics.ErrorReporter
import gr.eduinvoice.utils.GlobalPdfGenerator
import gr.eduinvoice.utils.GlobalBackgroundProcessor
import gr.eduinvoice.utils.BackgroundProcessor
import gr.eduinvoice.TutorBillingApp
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
// import com.google.firebase.sessions.FirebaseSessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var navController: NavHostController? = null
    
    // Error handling components
    private lateinit var errorHandler: ErrorHandler
    private lateinit var errorReporter: ErrorReporter
    
    // Background processing
    private lateinit var backgroundProcessor: BackgroundProcessor

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure StrictMode to allow network operations on background threads
        configureStrictMode()
        
        // Initialize error handling components
        errorHandler = ErrorHandler(this)
        errorReporter = ErrorReporter(this)
        
        // Initialize background processor
        backgroundProcessor = BackgroundProcessor()
        GlobalBackgroundProcessor.initialize(backgroundProcessor)
        
        // Initialize global PDF generator
        GlobalPdfGenerator.initialize(this)
        
        // Initialize Firebase Sessions on background thread to avoid StrictMode violations
        initializeFirebaseSessions()
        
        setContentView(R.layout.activity_main)
        
        try {
            // Note: We're not using the XML toolbar anymore since we use modern Compose AppTopBar
            // The toolbar is kept in the layout for potential future use but is hidden
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            // setSupportActionBar(toolbar) - Removed since we're not using XML toolbar

            drawerLayout = findViewById(R.id.drawer_layout)

            // Note: We're not using ActionBarDrawerToggle anymore since we use Compose AppTopBar
            // The drawer is opened manually via the NavigationMenuButton in Compose
            // This provides a cleaner separation between XML and Compose UI

            findViewById<ComposeView>(R.id.compose_view).setContent {
                val controller = rememberNavController()
                navController = controller
                val viewModel: SettingsViewModel = hiltViewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                TutorBillingTheme(darkTheme = uiState.settings?.darkTheme ?: false) {
                    LaunchedEffect(controller) {
                        controller.addOnDestinationChangedListener { _, destination, _ ->
                            // Hide the XML toolbar for all screens since we're using modern Compose AppTopBar
                            toolbar.visibility = View.GONE
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ErrorBoundary(
                            onError = { error ->
                                Log.e("MainActivity", "Compose error", error)
                                errorReporter.reportError(error)
                            }
                        ) {
                            TutorBillingApp(
                                navController = controller,
                                openDrawer = { openDrawer() }
                            )
                        }
                    }
                }
            }

            findViewById<NavigationView>(R.id.navigation_view).setNavigationItemSelectedListener(this)

        } catch (e: Exception) {
            Log.e("MainActivity", "Database initialization failed", e)
            errorReporter.reportError(e)
            // Convert to domain exception
            val domainException = DomainException.ApplicationError("Database initialization failed: ${e.message}", e)
            showDatabaseErrorDialog(domainException)
        } catch (e: Exception) {
            Log.e("MainActivity", "Unexpected error during initialization", e)
            errorReporter.reportError(e)
            showFatalErrorDialog(e)
        }
    }
    
    private fun configureStrictMode() {
        if (BuildConfig.DEBUG) {
            // In debug mode, configure StrictMode to be more lenient for development
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // Still detect network violations but allow them
                    .penaltyLog() // Only log violations, don't crash
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog() // Only log violations, don't crash
                    .build()
            )
        } else {
            // In release mode, use strict policies but allow network operations on background threads
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }
    }
    
    private fun initializeFirebaseSessions() {
        // Firebase Sessions initialization temporarily disabled due to API changes
        Log.d("MainActivity", "Firebase Sessions initialization skipped")
    }

    private fun showDatabaseErrorDialog(error: DomainException) {
        // Report error to analytics
        errorReporter.reportError(error, "MainActivity_DatabaseInit")
        
        // Handle with error handler for user-friendly message
        val errorResult = errorHandler.handleError(error, "Database Initialization")
        
        // Show enhanced error dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_occurred))
            .setMessage(errorResult.userMessage)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> finish() }
            .setOnDismissListener { finish() }
            .show()
    }
    
    private fun showFatalErrorDialog(error: Exception) {
        // Report error to analytics
        errorReporter.reportError(error, "MainActivity_Unexpected")
        
        // Handle with error handler for user-friendly message
        val errorResult = errorHandler.handleError(error, "App Initialization")
        
        // Show enhanced error dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_occurred))
            .setMessage(errorResult.userMessage)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> finish() }
            .setOnDismissListener { finish() }
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val controller = navController
        if (controller == null) {
            // Navigation controller not yet initialized, ignore the navigation
            drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        
        when (item.itemId) {
            R.id.nav_home -> controller.navigate(Screen.Home.route)
            R.id.nav_students -> controller.navigate(Screen.Students.route)
            R.id.nav_lessons -> controller.navigate(Screen.Lessons.route)
            R.id.nav_groups -> controller.navigate(Screen.Groups.route)
            R.id.nav_settings -> controller.navigate(Screen.Settings.route)
            else -> return false
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
