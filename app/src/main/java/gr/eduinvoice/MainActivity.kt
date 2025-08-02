package gr.eduinvoice

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavHostController

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            findViewById<NavigationView>(R.id.navigation_view)
                .setNavigationItemSelectedListener(this)

            findViewById<ComposeView>(R.id.compose_view).setContent {
                val viewModel: SettingsViewModel = hiltViewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                TutorBillingTheme(darkTheme = uiState.settings.darkTheme) {
                    navController = rememberNavController()
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TutorBillingApp(navController, ::openDrawer)
                    }
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                toolbar.visibility =
                    if (destination.route == Screen.Welcome.route) View.GONE else View.VISIBLE
            }
        } catch (e: DatabaseInitException) {
            showDatabaseErrorDialog()
        }
    }

    private fun showDatabaseErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.db_init_error_title)
            .setMessage(R.string.db_init_error_message)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .setOnDismissListener { finish() }
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(Screen.Home.route)
            R.id.nav_students -> navController.navigate(Screen.Students.route)
            R.id.nav_lessons -> navController.navigate(Screen.Lessons.route)
            R.id.nav_groups -> navController.navigate(Screen.Groups.route)
            R.id.nav_settings -> navController.navigate(Screen.Settings.route)
            else -> return false
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
