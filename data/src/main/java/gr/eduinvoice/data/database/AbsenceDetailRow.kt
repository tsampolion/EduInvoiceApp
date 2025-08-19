package gr.eduinvoice.data.database

data class AbsenceDetailRow(
    val id: Long,
    val groupLessonId: Long,
    val groupId: Long,
    val studentId: Long,
    val studentName: String?,
    val studentSurname: String?,
    val date: String,
    val startTime: String
)
