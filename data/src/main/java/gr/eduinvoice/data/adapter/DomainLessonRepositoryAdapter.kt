package gr.eduinvoice.data.adapter

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainLessonRepositoryAdapter @Inject constructor(
    private val tutorBillingRepository: TutorBillingRepository,
    private val lessonDao: LessonDao
) : DomainLessonRepository {
    
    override suspend fun addLesson(lesson: DomainLesson, userId: Long): Long =
        tutorBillingRepository.addLesson(lesson.toDataModel(), userId)
    
    override suspend fun addGroupLesson(lesson: DomainLesson, userId: Long): Long =
        tutorBillingRepository.addGroupLesson(lesson.toDataModel(), userId)
    
    override suspend fun updateLesson(lesson: DomainLesson, userId: Long) =
        tutorBillingRepository.updateLesson(lesson.toDataModel(), userId)
    
    override suspend fun deleteLesson(lessonId: Long, userId: Long) =
        lessonDao.deleteLesson(lessonId, userId)
    
    override suspend fun updateLessonPaidStatus(lessonId: Long, isPaid: Boolean, userId: Long) =
        lessonDao.updateLessonPaidStatus(lessonId, isPaid, userId)
    
    override suspend fun updateLessonInvoicedStatus(lessonId: Long, isInvoiced: Boolean, userId: Long) =
        lessonDao.updateLessonInvoicedStatus(lessonId, isInvoiced, userId)
    
    override suspend fun isLessonInvoiced(lessonId: Long, userId: Long): Boolean =
        lessonDao.isLessonInvoiced(lessonId, userId)
    
    override fun getAllLessons(userId: Long): Flow<List<DomainLesson>> =
        lessonDao.getAllLessons(userId).map { lessons ->
            lessons.map { it.toDomainModel() }
        }
    
    override fun getLessonById(lessonId: Long, userId: Long): Flow<DomainLesson?> =
        lessonDao.getLessonById(lessonId, userId).map { it?.toDomainModel() }
    
    override fun getStudentLessons(studentId: Long, userId: Long): Flow<List<DomainLesson>> =
        tutorBillingRepository.getStudentLessons(studentId, userId).map { lessons ->
            lessons.map { it.toDomainModel() }
        }
    
    override fun getLessonsWithStudents(userId: Long): Flow<List<DomainLesson>> =
        lessonDao.getLessonsWithStudents(userId).map { lessons ->
            lessons.map { it.lesson.toDomainModel() }
        }
    
    override fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String,
        userId: Long
    ): Flow<List<DomainLesson>> =
        lessonDao.getLessonsWithStudentsByStudentAndDateRange(studentId, startDate, endDate, userId)
            .map { lessons ->
                lessons.map { it.lesson.toDomainModel() }
            }
    
    override suspend fun getLessonsWithStudentsPaginated(
        userId: Long,
        limit: Int,
        offset: Int
    ): List<DomainLesson> =
        lessonDao.getLessonsWithStudentsPaginated(userId, limit, offset)
            .map { it.lesson.toDomainModel() }
    
    private fun DomainLesson.toDataModel(): Lesson = Lesson(
        id = id,
        ownerId = ownerId,
        studentId = studentId,
        groupId = groupId,
        date = date,
        startTime = startTime,
        durationMinutes = durationMinutes,
        notes = notes,
        isPaid = isPaid,
        isInvoiced = isInvoiced,
        lastModified = lastModified
    )
    
    private fun Lesson.toDomainModel(): DomainLesson = DomainLesson(
        id = id,
        ownerId = ownerId,
        studentId = studentId,
        groupId = groupId,
        date = date,
        startTime = startTime,
        durationMinutes = durationMinutes,
        notes = notes,
        isPaid = isPaid,
        isInvoiced = isInvoiced,
        lastModified = lastModified
    )
}
