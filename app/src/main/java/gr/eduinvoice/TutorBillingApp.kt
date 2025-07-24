package gr.eduinvoice

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import gr.eduinvoice.navigation.studentGraph
import gr.eduinvoice.ui.user.LoginScreen
import gr.eduinvoice.ui.user.RegisterScreen
import gr.eduinvoice.ui.user.SessionViewModel

@Composable
fun TutorBillingApp() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val loggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    val startDestination = if (loggedIn) Screen.Home.route else Screen.Welcome.route

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
                onRegister = { navController.navigate(Screen.Register.route) },
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
                onNavigateToStudent = { navController.navigate(Screen.Students.route) },
                onClassesClick = { navController.navigate(Screen.Classes.route) },
                onNavigateToLesson = { navController.navigate(Screen.Lessons.route) },
                onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                onNavigateToNewStudent = { navController.navigate(Screen.Student.createRoute(0)) },
                onNavigateToNewLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onRevenue = { navController.navigate(Screen.Revenue.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        studentGraph(navController)

        composable(Screen.Groups.route) {
            GroupsScreen(
                onBack = { navController.popBackStack() },
                onGroupClick = { id -> navController.navigate(Screen.Group.createRoute(id)) },
                onAddGroup = { navController.navigate(Screen.Group.createRoute(0)) }
            )
        }

        composable(
            route = Screen.Group.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            GroupScreen(onBack = { navController.popBackStack() })
        }

        // Classes list screen
        composable(Screen.Classes.route) {
            ClassesScreen(
                onBack = { navController.popBackStack() },
                onStudentClick = { id ->
                    navController.navigate(Screen.Student.createRoute(id))
                }
            )
        }

        // Lessons list screen
        composable(Screen.Lessons.route) {
            LessonsScreen(
                onBack = { navController.popBackStack() },
                onLessonClick = { studentId, lessonId ->
                    navController.navigate(
                        Screen.Lesson.createRoute(lessonId, studentId)
                    )
                },
                onAddLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onInvoice = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onPastInvoices = { navController.navigate(Screen.PastInvoices.route) }
            )
        }

        // Revenue screen
        composable(Screen.Revenue.route) {
            RevenueScreen(
                onBack = { navController.popBackStack() },
                onInvoice = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onPastInvoices = { navController.navigate(Screen.PastInvoices.route) }
            )
        }

        // Invoice screen
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

        // Past invoices screen
        composable(Screen.PastInvoices.route) {
            PastInvoicesScreen(onBack = { navController.popBackStack() })
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onLogout = {
                    sessionViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
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
                }
            )
        ) { backStackEntry ->
            val viewModel: LessonViewModel = hiltViewModel()

            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: 0L

            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L

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
