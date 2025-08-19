package gr.eduinvoice

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("resetPassword")
    object Students : Screen("students")
    object Student : Screen("student/{studentId}") {
        fun createRoute(studentId: Long) = "student/$studentId"
    }
    object Lessons : Screen("lessons")
    object Lesson : Screen("lesson/{lessonId}?studentId={studentId}&groupId={groupId}&groupMasterId={groupMasterId}") {
        fun createRoute(lessonId: Long, studentId: Long = 0L, groupId: Long = 0L, groupMasterId: Long = 0L) =
            "lesson/$lessonId?studentId=$studentId&groupId=$groupId&groupMasterId=$groupMasterId"
    }
    object Classes : Screen("classes")
    object Revenue : Screen("revenue")
    object Invoice : Screen("invoice?studentId={id}") {
        fun createRoute(studentId: Long? = null) =
            studentId?.let { "invoice?studentId=$it" } ?: "invoice"
    }
    object PastInvoices : Screen("pastInvoices")
    object Settings : Screen("settings")
    object PrivacyPolicy : Screen("privacyPolicy")
    object Profile : Screen("profile")
    object ArchivedStudents : Screen("archivedStudents")
    object Groups : Screen("groups")
    object Group : Screen("group/{groupId}") {
        fun createRoute(groupId: Long) = "group/$groupId"
    }
}
