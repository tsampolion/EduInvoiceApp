package gr.eduinvoice

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import gr.eduinvoice.ui.components.LocalSnackbarBus
import gr.eduinvoice.ui.components.SnackbarBus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gr.eduinvoice.ui.home.HomeMenuScreen
import gr.eduinvoice.ui.classes.ClassesScreen
import gr.eduinvoice.ui.lessons.LessonsScreen
import gr.eduinvoice.ui.lesson.LessonScreen
import gr.eduinvoice.ui.lesson.LessonViewModel
import gr.eduinvoice.ui.revenue.RevenueScreen
import gr.eduinvoice.ui.invoice.InvoiceScreen
import gr.eduinvoice.ui.invoice.PastInvoicesScreen
import gr.eduinvoice.ui.invoice.InvoiceViewModel
import gr.eduinvoice.ui.groups.GroupsScreen
import gr.eduinvoice.ui.groups.GroupScreen
import gr.eduinvoice.ui.settings.SettingsScreen
import gr.eduinvoice.ui.settings.PrivacyPolicyScreen
import gr.eduinvoice.ui.welcome.WelcomeScreen
import gr.eduinvoice.ui.profile.ProfileScreen
import gr.eduinvoice.navigation.studentGraph
import gr.eduinvoice.ui.user.LoginScreen
import gr.eduinvoice.ui.user.RegisterScreen
import gr.eduinvoice.ui.user.SessionViewModel
import gr.eduinvoice.ui.user.ResetPasswordScreen
import gr.eduinvoice.ui.components.ErrorBoundary

@Composable
fun EduInvoiceApp(
    navController: NavHostController = rememberNavController(),
    openDrawer: () -> Unit = {},
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val loggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(loggedIn) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (!loggedIn && currentRoute != Screen.Welcome.route) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0)
            }
        }
    }

    val startDestination = if (loggedIn) Screen.Home.route else Screen.Welcome.route

    // Wrap navigation with ErrorBoundary for session management errors
    ErrorBoundary(
        onError = { error ->
            // Session errors are handled gracefully - redirect to welcome
            if (loggedIn) {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(0)
                }
            }
        }
    ) {
        val snackbarHostState = SnackbarHostState()
        val bus = SnackbarBus()
        LaunchedEffect(bus.message) {
            bus.message?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                bus.consume()
            }
        }
        CompositionLocalProvider(LocalSnackbarBus provides bus) {
        Scaffold(bottomBar = { SnackbarHost(hostState = snackbarHostState) }) { paddingIgnored ->
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
        // Authentication
        composable(Screen.Welcome.route) {
            gr.eduinvoice.ui.welcome.WelcomeScreen(
                onSignIn = { navController.navigate(Screen.Login.route) },
                onSignUp = { navController.navigate(Screen.Register.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onResetPassword = { navController.navigate(Screen.ResetPassword.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeMenuScreen(
                onOpenDrawer = openDrawer,
                onNavigateToStudent = { navController.navigate(Screen.Students.route) },
                onClassesClick = { navController.navigate(Screen.Classes.route) },
                onNavigateToLesson = { navController.navigate(Screen.Lessons.route) },
                onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                onNavigateToNewStudent = { navController.navigate(Screen.Student.createRoute(0)) },
                onNavigateToNewLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onRevenue = { navController.navigate(Screen.Revenue.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onUsers = { navController.navigate(Screen.Users.route) }
            )
        }

        studentGraph(navController, openDrawer)

        composable(Screen.Groups.route) {
            GroupsScreen(
                openDrawer = openDrawer,
                onGroupClick = { id -> navController.navigate(Screen.Group.createRoute(id)) },
                onAddGroup = { navController.navigate(Screen.Group.createRoute(0)) }
            )
        }

        composable(
            route = Screen.Group.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            GroupScreen(
                onBack = { navController.popBackStack() },
                onAddGroupLesson = { groupId ->
                    navController.navigate(Screen.Lesson.createRoute(0, 0, groupId))
                },
                onMemberClick = { studentId ->
                    navController.navigate(Screen.Student.createRoute(studentId))
                },
                onEditGroupMaster = { groupId, masterId ->
                    navController.navigate(Screen.Lesson.createRoute(0, 0, groupId, masterId))
                }
            )
        }

        // Classes list screen
        composable(Screen.Classes.route) {
            ClassesScreen(
                openDrawer = openDrawer,
                onStudentClick = { id ->
                    navController.navigate(Screen.Student.createRoute(id))
                }
            )
        }

        // Lessons list screen (new standardized route retained)
        composable(
            route = Screen.Lessons.route,
            arguments = listOf(
                navArgument("batchStudentId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("openPay") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val batchStudentId = backStackEntry.arguments?.getLong("batchStudentId")?.takeIf { it != 0L }
            val openPay = backStackEntry.arguments?.getBoolean("openPay") ?: false
            LessonsScreen(
                openDrawer = openDrawer,
                onLessonClick = { studentId, lessonId, groupId ->
                    navController.navigate(
                        Screen.Lesson.createRoute(lessonId, studentId, groupId)
                    )
                },
                onAddLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onInvoice = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onPastInvoices = { navController.navigate(Screen.PastInvoices.route) },
                onReschedules = { navController.navigate(Screen.Reschedules.route) },
                batchStudentId = batchStudentId,
                openPayOnStart = openPay
            )
        }

        // Alias: legacy lessons route without query params
        composable("lessons") {
            // Log: legacy route hit
            androidx.compose.runtime.LaunchedEffect(Unit) {
                android.util.Log.w("DeepLinks", "Legacy route 'lessons' used")
            }
            LessonsScreen(
                openDrawer = openDrawer,
                onLessonClick = { studentId, lessonId, groupId ->
                    navController.navigate(
                        Screen.Lesson.createRoute(lessonId, studentId, groupId)
                    )
                },
                onAddLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onInvoice = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onPastInvoices = { navController.navigate(Screen.PastInvoices.route) },
                onReschedules = { navController.navigate(Screen.Reschedules.route) }
            )
        }

        // Revenue screen
        composable(Screen.Revenue.route) {
            RevenueScreen(
                openDrawer = openDrawer,
                onInvoice = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onPastInvoices = { navController.navigate(Screen.PastInvoices.route) }
            )
        }

        // Invoice screen (standardized)
        composable(
            route = Screen.Invoice.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val viewModel: InvoiceViewModel = hiltViewModel()
            val studentIdArg = backStackEntry.arguments?.getLong("id") ?: -1L
            val id = studentIdArg.takeIf { it != -1L }
            InvoiceScreen(
                onBack = { navController.popBackStack() },
                defaultStudentId = id,
                viewModel = viewModel
            )
        }

        // Alias: legacy invoice route 'invoice/{id}'
        composable(
            route = "invoice/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val viewModel: InvoiceViewModel = hiltViewModel()
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it > 0 }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                android.util.Log.w("DeepLinks", "Legacy route 'invoice/{id}' used")
            }
            InvoiceScreen(
                onBack = { navController.popBackStack() },
                defaultStudentId = id,
                viewModel = viewModel
            )
        }

        // Past invoices screen
        composable(Screen.PastInvoices.route) {
            PastInvoicesScreen(onBack = { navController.popBackStack() })
        }

        // Edit Invoice Master
        composable(
            route = Screen.EditInvoiceMaster.route,
            arguments = listOf(navArgument("masterId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("masterId") ?: 0L
            gr.eduinvoice.ui.invoice.EditInvoiceMasterScreen(
                masterId = id,
                onBack = { navController.popBackStack() }
            )
        }

        // Reschedules screen
        composable(Screen.Reschedules.route) {
            gr.eduinvoice.ui.lessons.ReschedulesScreen(onBack = { navController.popBackStack() })
        }

        // Users screen (Admin only)
        composable(Screen.Users.route) {
            gr.eduinvoice.ui.user.UsersScreen(
                openDrawer = openDrawer,
                onUserClick = { userId ->
                    // For now, just navigate to profile. Could be enhanced later
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                openDrawer = openDrawer,
                onPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onLogin = { navController.navigate(Screen.Login.route) },
                onRegister = { navController.navigate(Screen.Register.route) },
                onLogout = {
                    sessionViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onSwitchAccount = {
                    sessionViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Profile.route) {
            gr.eduinvoice.ui.profile.ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        // Lesson Detail/Edit Screen
        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("lessonId") {
                    type = NavType.LongType
                },
                navArgument("studentId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("groupId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("groupMasterId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val viewModel: LessonViewModel = hiltViewModel()

            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: 0L

            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
            val groupMasterId = backStackEntry.arguments?.getLong("groupMasterId") ?: 0L

            LaunchedEffect(Unit) {
                viewModel.setNavigationCallback {
                    navController.popBackStack()
                }
            }

            LessonScreen(
                studentId = studentId.takeIf { it != 0L },
                lessonId = lessonId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        }
        }
        }
    }
}
