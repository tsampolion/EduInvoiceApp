package gr.eduinvoice.domain.lesson

import javax.inject.Inject

data class LessonUseCases @Inject constructor(
    val getAllLessons: GetAllLessons,
    val getLessonById: GetLessonById,
    val getStudentLessons: GetStudentLessons,
    val getLessonsWithStudents: GetLessonsWithStudents,
    val getLessonsWithStudentsByStudentAndDateRange: GetLessonsWithStudentsByStudentAndDateRange,
    val addLesson: AddLesson,
    val addGroupLesson: AddGroupLesson,
    val updateLesson: UpdateLesson,
    val deleteLesson: DeleteLesson,
    val updateLessonPaidStatus: UpdateLessonPaidStatus,
    val updateLessonInvoicedStatus: UpdateLessonInvoicedStatus,
    val isLessonInvoiced: IsLessonInvoiced
)
