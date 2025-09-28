package gr.eduinvoice.analytics

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.database.EarningsByClassRow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ReportingRepository @Inject constructor(
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao
) {
    fun earningsByClass(startDate: String, endDate: String, userId: Long): Flow<List<EarningsByClassRow>> =
        lessonDao.getEarningsByClass(startDate, endDate, userId)

    fun unpaidLessonsCount(startDate: String, endDate: String, userId: Long): Flow<Int> =
        lessonDao.getUnpaidLessonsInDateRange(startDate, endDate, userId).mapToCount()

    fun unpaidLessonsCountForStudent(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<Int> =
        lessonDao.getUnpaidLessonsByStudentAndDateRange(studentId, startDate, endDate, userId).mapToCount()
}

private fun <T> Flow<List<T>>.mapToCount(): Flow<Int> = kotlinx.coroutines.flow.map { it.size }