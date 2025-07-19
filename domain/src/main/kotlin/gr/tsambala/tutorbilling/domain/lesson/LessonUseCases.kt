package gr.tsambala.tutorbilling.domain.lesson

import javax.inject.Inject

data class LessonUseCases @Inject constructor(
    val getAllLessons: GetAllLessons,
    val getLessonById: GetLessonById,
    val getLessonsByStudentId: GetLessonsByStudentId,
    val getLessonsWithStudents: GetLessonsWithStudents,
    val getLessonsWithStudentsByStudentAndDateRange: GetLessonsWithStudentsByStudentAndDateRange,
    val insertLesson: InsertLesson,
    val updateLesson: UpdateLesson,
    val deleteLesson: DeleteLesson,
    val updateLessonPaidStatus: UpdateLessonPaidStatus
)
