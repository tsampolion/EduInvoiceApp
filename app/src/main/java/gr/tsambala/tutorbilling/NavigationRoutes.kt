package gr.tsambala.tutorbilling

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Students : Screen("students")
    object Student : Screen("student/{studentId}") {
        fun createRoute(studentId: Long) = "student/$studentId"
    }
    object Lessons : Screen("lessons")
    object Lesson : Screen("lesson/{lessonId}?studentId={studentId}") {
        fun createRoute(lessonId: Long, studentId: Long = 0L) =
            "lesson/$lessonId?studentId=$studentId"
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
    object ArchivedStudents : Screen("archivedStudents")
    object Groups : Screen("groups")
    object Group : Screen("group/{groupId}") {
        fun createRoute(groupId: Long) = "group/$groupId"
    }
}
