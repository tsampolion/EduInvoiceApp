package gr.tsambala.tutorbilling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import gr.tsambala.tutorbilling.ui.home.HomeMenuScreen
import gr.tsambala.tutorbilling.ui.classes.ClassesScreen
import gr.tsambala.tutorbilling.ui.lessons.LessonsScreen
import gr.tsambala.tutorbilling.ui.lesson.LessonScreen
import gr.tsambala.tutorbilling.ui.lesson.LessonViewModel
import gr.tsambala.tutorbilling.ui.revenue.RevenueScreen
import gr.tsambala.tutorbilling.ui.invoice.InvoiceScreen
import gr.tsambala.tutorbilling.ui.invoice.PastInvoicesScreen
import gr.tsambala.tutorbilling.ui.invoice.InvoiceViewModel
import gr.tsambala.tutorbilling.ui.settings.SettingsScreen
import gr.tsambala.tutorbilling.ui.settings.PrivacyPolicyScreen
import gr.tsambala.tutorbilling.navigation.studentGraph

@Composable
fun TutorBillingApp(signInClient: GoogleSignInClient) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeMenuScreen(
                onNavigateToStudent = { navController.navigate(Screen.Students.route) },
                onClassesClick = { navController.navigate(Screen.Classes.route) },
                onNavigateToLesson = { navController.navigate(Screen.Lessons.route) },
                onNavigateToNewStudent = { navController.navigate(Screen.Student.createRoute(0)) },
                onNavigateToNewLesson = { navController.navigate(Screen.Lesson.createRoute(0)) },
                onRevenue = { navController.navigate(Screen.Revenue.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        studentGraph(navController)

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
                onAddLesson = {
                    navController.navigate(Screen.Lesson.createRoute(0))
                }
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
                signInClient = signInClient,
                onPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
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
