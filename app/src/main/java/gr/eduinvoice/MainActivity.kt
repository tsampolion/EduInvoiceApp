package gr.eduinvoice

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
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
import gr.eduinvoice.data.database.DatabaseInitException
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.theme.TutorBillingTheme
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.utils.ErrorHandler
import gr.eduinvoice.analytics.ErrorReporter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var navController: NavHostController? = null
    
    // Error handling components
    private lateinit var errorHandler: ErrorHandler
    private lateinit var errorReporter: ErrorReporter

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize error handling components
        errorHandler = ErrorHandler(this)
        errorReporter = ErrorReporter(this)
        
        setContentView(R.layout.activity_main)
        try {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)

            drawerLayout = findViewById(R.id.drawer_layout)

            val toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            findViewById<ComposeView>(R.id.compose_view).setContent {
                val controller = rememberNavController()
                navController = controller
                val viewModel: SettingsViewModel = hiltViewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                TutorBillingTheme(darkTheme = uiState.settings.darkTheme) {
                    LaunchedEffect(controller) {
                        controller.addOnDestinationChangedListener { _, destination, _ ->
                            toolbar.visibility =
                                if (destination.route == Screen.Welcome.route) {
                                    View.GONE
                                } else {
                                    View.VISIBLE
                                }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Wrap the main app with ErrorBoundary
                        ErrorBoundary(
                            onError = { error ->
                                errorReporter.reportError(error, "MainActivity")
                            }
                        ) {
                            TutorBillingApp(controller, ::openDrawer)
                        }
                    }
                }
            }

            findViewById<NavigationView>(R.id.navigation_view)
                .setNavigationItemSelectedListener(this)
        } catch (e: DatabaseInitException) {
            handleDatabaseInitError(e)
        } catch (e: Exception) {
            handleUnexpectedError(e)
        }
    }

    private fun handleDatabaseInitError(error: DatabaseInitException) {
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
    
    private fun handleUnexpectedError(error: Exception) {
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
