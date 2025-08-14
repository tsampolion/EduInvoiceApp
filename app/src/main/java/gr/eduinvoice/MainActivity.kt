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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private lateinit var errorReporter: ErrorReporter

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		configureStrictMode()

		errorReporter = ErrorReporter(this)

		setContent {
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
							label = { Text("Classes") },
							selected = false,
							onClick = {
								if (!isWelcome) navController.navigate(Screen.Classes.route)
								drawerOpen = false
							},
							modifier = Modifier,
							colors = NavigationDrawerItemDefaults.colors()
						)
						NavigationDrawerItem(
							label = { Text("Revenue") },
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
				) {
					Scaffold(
						topBar = {
							TopAppBar(
								title = { Text(text = stringResource(id = R.string.app_name)) },
								navigationIcon = {
									if (!isWelcome) {
										IconButton(onClick = { drawerOpen = !drawerOpen }) {
											Icon(Icons.Default.Menu, contentDescription = "Menu")
										}
									}
								}
							)
						}
					) { padding ->
						Surface(
							modifier = Modifier
								.fillMaxSize()
								.padding(padding),
							color = MaterialTheme.colorScheme.background
						) {
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
		} else {
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
