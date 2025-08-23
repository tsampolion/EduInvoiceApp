package gr.eduinvoice

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import gr.eduinvoice.analytics.ErrorReporter
import gr.eduinvoice.analytics.PerformanceMonitor
import gr.eduinvoice.analytics.StartupPerformanceMonitor
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.ui.components.ErrorBoundary
import gr.eduinvoice.ui.components.ErrorScreen
import gr.eduinvoice.ui.settings.SettingsViewModel
import gr.eduinvoice.ui.theme.EduInvoiceTheme
import gr.eduinvoice.ui.user.SessionViewModel
import gr.eduinvoice.utils.ResourceResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Log
import android.content.res.Resources

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var errorReporter: ErrorReporter
    @Inject lateinit var db: EduInvoiceDatabase
    @Inject lateinit var userUseCases: UserUseCases
    @Inject lateinit var startupPerformanceMonitor: StartupPerformanceMonitor
    @Inject lateinit var resourceResolver: ResourceResolver

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureStrictMode()

        errorReporter = ErrorReporter(this)

        try {
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
                var showLoginWarning by remember { mutableStateOf(false) }

                // Use the correct, lifecycle-aware coroutine scope for UI operations
                val scope = rememberCoroutineScope()
                // drawerState is now the single source of truth for the drawer.
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                // Close the drawer when the welcome screen is shown
                LaunchedEffect(isWelcome) {
                    if (isWelcome && drawerState.isOpen) {
                        drawerState.close()
                    }
                }
                
                LaunchedEffect(Unit) {
                    val dbInitTrace = startupPerformanceMonitor.startPhase("database_initialization")
                    
                    // Show loading state immediately
                    initState = InitState.Loading
                    
                    try {
                        // Move all database operations to background thread
                        withContext(Dispatchers.IO) {
                            // Pre-warm database connection
                            db.openHelper.writableDatabase.version
                            
                            // Create admin user if needed
                            userUseCases.createAdminUserIfNotExists()
                        }
                        
                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            initState = InitState.Success
                        }
                    } catch (t: Throwable) {
                        Log.e("MainActivity", "Initialization failed", t)
                        withContext(Dispatchers.Main) {
                            initState = InitState.Failure(t)
                        }
                    } finally {
                        startupPerformanceMonitor.endPhase("database_initialization", dbInitTrace)
                    }
                }

                EduInvoiceTheme(darkTheme = uiState.settings?.darkTheme ?: false) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        // Gestures are enabled only when not on the welcome screen
                        gesturesEnabled = !isWelcome,
                        drawerContent = {
                            ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
                                // App Header
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.tutorbilling_logo),
                                        contentDescription = stringResource(R.string.app_logo_desc),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Educational Invoicing",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Navigation Items
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text(stringResource(R.string.home)) },
                                    selected = currentRoute == Screen.Home.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Home.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    label = { Text(stringResource(R.string.students)) },
                                    selected = currentRoute == Screen.Students.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Students.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                    label = { Text(stringResource(R.string.lessons)) },
                                    selected = currentRoute == Screen.Lessons.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Lessons.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                                    label = { Text(stringResource(R.string.groups)) },
                                    selected = currentRoute == Screen.Groups.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Groups.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Class, contentDescription = null) },
                                    label = { Text(stringResource(R.string.classes)) },
                                    selected = currentRoute == Screen.Classes.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Classes.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                    label = { Text(stringResource(R.string.revenue)) },
                                    selected = currentRoute == Screen.Revenue.route,
                                    onClick = {
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.Revenue.route)
                                            scope.launch { drawerState.close() }
                                        } else {
                                            showLoginWarning = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                 // Admin-only Users management
                                if (isLoggedIn && uiState.user?.username == "admin") {
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        label = { Text("User Management") },
                                        selected = currentRoute == Screen.Users.route,
                                        onClick = {
                                            navController.navigate(Screen.Users.route)
                                            scope.launch { drawerState.close() }
                                        },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }

                                // Settings Item
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    label = { Text(stringResource(id = R.string.settings)) },
                                    selected = currentRoute == Screen.Settings.route,
                                    onClick = {
                                        navController.navigate(Screen.Settings.route)
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }

                            if (showLoginWarning) {
                                AlertDialog(
                                    onDismissRequest = { showLoginWarning = false },
                                    title = { Text("Login Required") },
                                    text = { Text("Please log in to access this feature.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showLoginWarning = false
                                            navController.navigate(Screen.Settings.route)
                                            scope.launch { drawerState.close() }
                                        }) {
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
                            topBar = { /* Top bar can be added here if needed */ }
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
                                        onRetry = { /* Re-trigger init */ },
                                        onDismiss = { /* Dismiss error */ }
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
                                                openDrawer = {
                                                    // Don't open drawer on welcome screen
                                                    if (!isWelcome) {
                                                        scope.launch { drawerState.open() }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Resources.NotFoundException) {
            val resourceName = resourceResolver.getResourceName(e.message?.substringAfterLast(" ")?.toInt(16) ?: 0)
            val errorMessage = "Critical Resource Not Found: ${e.message}. Resolved name: $resourceName"
            Log.e("MainActivity", errorMessage, e)
            errorReporter.reportError(e, errorMessage)
            // Set content to a safe error screen
            setContent {
                EduInvoiceTheme {
                    ErrorScreen(
                        errorMessage = "A critical error occurred. Please restart the application.",
                        onRetry = { /* Could try to restart the activity */ }
                    )
                }
            }
        }
    }

    private fun configureStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll() // Detect everything in debug builds
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll() // Detect everything in debug builds
                    .penaltyLog()
                    .build()
            )
        }
    }
}

// Sealed classes for initialization state
private sealed class InitState {
    data object Loading : InitState()
    data class Failure(val error: Throwable) : InitState()
    data object Success : InitState()
}

// A simple loading screen composable
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
