package gr.eduinvoice

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ExperimentalMaterial3Api
import dagger.hilt.android.AndroidEntryPoint
import gr.eduinvoice.analytics.ErrorReporter
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.theme.EduInvoiceTheme
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import gr.eduinvoice.ui.components.ErrorScreen
import gr.eduinvoice.data.database.EduInvoiceDatabase
import javax.inject.Inject
import gr.eduinvoice.analytics.PerformanceMonitor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var errorReporter: ErrorReporter
    @Inject lateinit var db: EduInvoiceDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureStrictMode()

        errorReporter = ErrorReporter(this)

        setContent {
            var initState by remember { mutableStateOf<InitState>(InitState.Loading) }
            LaunchedEffect(Unit) {
                val trace = PerformanceMonitor(this@MainActivity).startTrace("db_init")
                trace.start()
                initState = try {
                    withContext(Dispatchers.IO) {
                        // Access database to ensure open happens off the main thread
                        db.openHelper.writableDatabase.version
                        InitState.Success
                    }
                } catch (t: Throwable) {
                    Log.e("MainActivity", "Initialization failed", t)
                    InitState.Failure(t)
                } finally {
                    trace.stop()
                }
            }

            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val isWelcome = currentRoute == Screen.Welcome.route
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value
            var drawerOpen by remember { mutableStateOf(false) }

            EduInvoiceTheme(darkTheme = uiState.settings?.darkTheme ?: false) {
                val drawerState = rememberDrawerState(initialValue = if (drawerOpen) DrawerValue.Open else DrawerValue.Closed)
                LaunchedEffect(drawerOpen) {
                    if (drawerOpen) drawerState.open() else drawerState.close()
                }
                LaunchedEffect(isWelcome) {
                    if (isWelcome) {
                        drawerOpen = false
                    }
                }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.home)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Home.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.students)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Students.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.lessons)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Lessons.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.groups)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Groups.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.classes)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Classes.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.revenue)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Revenue.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                label = { Text(stringResource(id = R.string.settings)) },
                                selected = false,
                                onClick = {
                                    if (!isWelcome) navController.navigate(Screen.Settings.route)
                                    drawerOpen = false
                                },
                                modifier = Modifier,
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = { }
                    ) { padding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (val state = initState) {
                                is InitState.Loading -> LoadingScreen()
                                is InitState.Failure -> ErrorScreen(
                                    error = state.error,
                                    onRetry = { initState = InitState.Loading; },
                                    onDismiss = { }
                                )
                                is InitState.Success -> {
                                    ErrorBoundary(
                                        onError = { error ->
                                            Log.e("MainActivity", "Compose error", error)
                                            errorReporter.reportError(error)
                                        }
                                    ) {
                                        EduInvoiceApp(
                                            navController = navController,
                                            openDrawer = { if (!isWelcome) drawerOpen = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configureStrictMode() {
        if (BuildConfig.DEBUG) {
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
}

private sealed class InitState {
    data object Loading : InitState()
    data class Failure(val error: Throwable) : InitState()
    data object Success : InitState()
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}
