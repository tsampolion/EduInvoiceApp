package gr.eduinvoice.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import gr.eduinvoice.Screen
import gr.eduinvoice.ui.student.StudentScreen
import gr.eduinvoice.ui.student.StudentViewModel
import gr.eduinvoice.ui.students.StudentsScreen
import gr.eduinvoice.ui.students.ArchivedStudentsScreen

fun NavGraphBuilder.studentGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Students.route, route = "student_graph") {
        composable(Screen.Students.route) {
            StudentsScreen(
                onNavigateToStudent = { id ->
                    navController.navigate(Screen.Student.createRoute(id))
                },
                onAddStudent = {
                    navController.navigate(Screen.Student.createRoute(0L))
                },
                onBack = { navController.popBackStack() },
                onViewArchived = { navController.navigate(Screen.ArchivedStudents.route) }
            )
        }

        composable(
            route = Screen.Student.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val viewModel: StudentViewModel = hiltViewModel()

            val studentIdArg = backStackEntry.arguments?.getLong("studentId") ?: 0L

            LaunchedEffect(Unit) {
                viewModel.setNavigationCallback {
                    navController.popBackStack()
                }
            }

            val studentId = studentIdArg.toString()
            StudentScreen(
                studentId = studentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLesson = { lessonId ->
                    navController.navigate(
                        Screen.Lesson.createRoute(lessonId, studentIdArg)
                    )
                },
                onAddLesson = {
                    navController.navigate(Screen.Lesson.createRoute(0L, studentIdArg))
                },
                viewModel = viewModel
            )
        }

        composable(Screen.ArchivedStudents.route) {
            ArchivedStudentsScreen(
                onBack = { navController.popBackStack() },
                onStudentClick = { navController.navigate(Screen.Student.createRoute(it)) }
            )
        }
    }
}
