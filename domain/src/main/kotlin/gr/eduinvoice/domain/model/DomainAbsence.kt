package gr.eduinvoice.domain.model

data class DomainAbsence(
    val id: Long,
    val groupLessonId: Long,
    val groupId: Long,
    val studentId: Long,
    val studentName: String? = null,
    val studentSurname: String? = null,
    val date: String,
    val startTime: String
)
