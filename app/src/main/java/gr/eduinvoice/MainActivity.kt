package gr.eduinvoice

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dagger.hilt.android.AndroidEntryPoint
import gr.eduinvoice.analytics.ErrorReporter
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.user.SessionViewModel
import gr.eduinvoice.ui.theme.EduInvoiceTheme
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.analytics.PerformanceMonitor
import gr.eduinvoice.ui.components.ErrorScreen
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var errorReporter: ErrorReporter
    @Inject lateinit var db: EduInvoiceDatabase
    @Inject lateinit var userUseCases: gr.eduinvoice.domain.user.UserUseCases

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureStrictMode()

        errorReporter = ErrorReporter(this)

        setContent {
            var initState by remember { mutableStateOf<InitState>(InitState.Loading) }
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val isWelcome = currentRoute == Screen.Welcome.route
            
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value
            val isLoggedIn = sessionViewModel.isLoggedIn.collectAsStateWithLifecycle().value
            var drawerOpen by remember { mutableStateOf(false) }
            var showLoginWarning by remember { mutableStateOf(false) }
            var isDrawerOperationInProgress by remember { mutableStateOf(false) }
            
            // Helper function to handle navigation with login validation
            fun handleNavigation(route: String) {
                if (isWelcome) return
                if (isLoggedIn) {
                    navController.navigate(route)
                    drawerOpen = false
                } else {
                    showLoginWarning = true
                }
            }
            
            // Helper function to safely open drawer with debouncing
            fun safeOpenDrawer() {
                if (!isWelcome && !isDrawerOperationInProgress) {
                    isDrawerOperationInProgress = true
                    drawerOpen = true
                    // Reset operation flag after a delay
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500) // 500ms debounce
                        isDrawerOperationInProgress = false
                    }
                }
            }
            
            // Helper function to safely close drawer with debouncing
            fun safeCloseDrawer() {
                if (!isDrawerOperationInProgress) {
                    isDrawerOperationInProgress = true
                    drawerOpen = false
                    // Reset operation flag after a delay
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500) // 500ms debounce
                        isDrawerOperationInProgress = false
                    }
                }
            }
            
            LaunchedEffect(Unit) {
                val trace = PerformanceMonitor(this@MainActivity).startTrace("db_init")
                trace.start()
                initState = try {
                    withContext(Dispatchers.IO) {
                        // Access database to ensure open happens off the main thread
                        db.openHelper.writableDatabase.version
                        // Create admin user if it doesn't exist
                        userUseCases.createAdminUserIfNotExists()
                        InitState.Success
                    }
                } catch (t: Throwable) {
                    Log.e("MainActivity", "Initialization failed", t)
                    InitState.Failure(t)
                } finally {
                    trace.stop()
                }
            }
            
            EduInvoiceTheme(darkTheme = uiState.settings?.darkTheme ?: false) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                
                // Synchronize drawer state with local state
                LaunchedEffect(drawerState.currentValue) {
                    drawerOpen = drawerState.currentValue == DrawerValue.Open
                }
                
                // Handle drawer open/close operations with error handling
                LaunchedEffect(drawerOpen) {
                    try {
                        when {
                            drawerOpen && drawerState.currentValue == DrawerValue.Closed -> {
                                drawerState.open()
                            }
                            !drawerOpen && drawerState.currentValue == DrawerValue.Open -> {
                                drawerState.close()
                            }
                        }
                    } catch (e: Exception) {
                        // Reset state on error to prevent getting stuck
                        Log.e("MainActivity", "Drawer operation failed", e)
                        drawerOpen = false
                        if (drawerState.currentValue != DrawerValue.Closed) {
                            try {
                                drawerState.close()
                            } catch (closeError: Exception) {
                                Log.e("MainActivity", "Failed to close drawer after error", closeError)
                            }
                        }
                    }
                }
                
                // State validation to prevent stuck drawer
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000) // Check every second
                        try {
                            // If local state and drawer state are out of sync, fix it
                            val isDrawerOpen = drawerState.currentValue == DrawerValue.Open
                            if (drawerOpen != isDrawerOpen) {
                                Log.w("MainActivity", "Drawer state mismatch detected. Local: $drawerOpen, Drawer: $isDrawerOpen")
                                drawerOpen = isDrawerOpen
                            }
                            
                            // Log drawer state for debugging
                            if (BuildConfig.DEBUG) {
                                Log.d("MainActivity", "Drawer State - Local: $drawerOpen, Drawer: ${drawerState.currentValue}, Operation: $isDrawerOperationInProgress")
                            }
                            
                            // Emergency reset if operation is stuck for too long
                            if (isDrawerOperationInProgress) {
                                // Check if operation has been in progress for more than 3 seconds
                                delay(2000) // Wait 2 more seconds
                                if (isDrawerOperationInProgress) {
                                    Log.w("MainActivity", "Drawer operation stuck for too long, triggering emergency reset")
                                    // Force close the drawer state
                                    try {
                                        if (drawerState.currentValue != DrawerValue.Closed) {
                                            drawerState.close()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Emergency reset failed", e)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "State validation failed", e)
                        }
                    }
                }
                
                LaunchedEffect(isWelcome) {
                    if (isWelcome) {
                        drawerOpen = false
                        if (drawerState.currentValue != DrawerValue.Closed) {
                            try {
                                drawerState.close()
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to close drawer on welcome", e)
                            }
                        }
                    }
                }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(320.dp)
                        ) {
                            // App Header with Logo and Name
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.tutorbilling_logo),
                                    contentDescription = stringResource(R.string.app_logo_desc),
                                    modifier = Modifier.size(64.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Educational Invoicing",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                            
                            // Navigation Items with Icons
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.home)) },
                                selected = currentRoute == Screen.Home.route,
                                onClick = { 
                                    handleNavigation(Screen.Home.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.students)) },
                                selected = currentRoute == Screen.Students.route,
                                onClick = { 
                                    handleNavigation(Screen.Students.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Schedule, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.lessons)) },
                                selected = currentRoute == Screen.Lessons.route,
                                onClick = { 
                                    handleNavigation(Screen.Lessons.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Group, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.groups)) },
                                selected = currentRoute == Screen.Groups.route,
                                onClick = { 
                                    handleNavigation(Screen.Groups.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Class, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.classes)) },
                                selected = currentRoute == Screen.Classes.route,
                                onClick = { 
                                    handleNavigation(Screen.Classes.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.BarChart, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.revenue)) },
                                selected = currentRoute == Screen.Revenue.route,
                                onClick = { 
                                    handleNavigation(Screen.Revenue.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                            
                            // Admin-only Users management
                            if (isLoggedIn && uiState.user?.username == "admin") {
                                NavigationDrawerItem(
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Group, contentDescription = null) },
                                    label = { Text("User Management") },
                                    selected = currentRoute == Screen.Users.route,
                                    onClick = { 
                                        handleNavigation(Screen.Users.route)
                                        safeCloseDrawer()
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = NavigationDrawerItemDefaults.colors()
                                )
                            }
                            
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = null) },
                                label = { Text(stringResource(id = R.string.settings)) },
                                selected = currentRoute == Screen.Settings.route,
                                onClick = { 
                                    handleNavigation(Screen.Settings.route)
                                    safeCloseDrawer()
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                        }
                        
                        // Login Warning Dialog
                        if (showLoginWarning) {
                            AlertDialog(
                                onDismissRequest = { showLoginWarning = false },
                                title = { Text("Login Required") },
                                text = { Text("Please log in to access this feature.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = { 
                                            showLoginWarning = false
                                            navController.navigate(Screen.Settings.route)
                                            drawerOpen = false
                                        }
                                    ) {
                                        Text("Go to Login")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showLoginWarning = false }) {
                                        Text("Cancel")
                                    }
                                }
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
                                            openDrawer = { safeOpenDrawer() }
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
