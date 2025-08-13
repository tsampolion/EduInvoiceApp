package fakes

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLessonsRepository : DomainLessonRepository {
    private val lessons = MutableStateFlow<List<DomainLesson>>(emptyList())

    fun setLessons(newLessons: List<DomainLesson>) {
        lessons.value = newLessons
    }

    override suspend fun addLesson(lesson: DomainLesson, userId: Long): Long {
        val newLesson = lesson.copy(id = (lessons.value.maxOfOrNull { it.id } ?: 0) + 1)
        lessons.value = lessons.value + newLesson
        return newLesson.id
    }

    override suspend fun addGroupLesson(lesson: DomainLesson, userId: Long): Long {
        return addLesson(lesson, userId)
    }

    override suspend fun updateLesson(lesson: DomainLesson, userId: Long) {
        lessons.value = lessons.value.map { 
            if (it.id == lesson.id) lesson else it 
        }
    }

    override suspend fun deleteLesson(lessonId: Long, userId: Long) {
        lessons.value = lessons.value.filter { it.id != lessonId }
    }

    override suspend fun updateLessonPaidStatus(lessonId: Long, isPaid: Boolean, userId: Long) {
        lessons.value = lessons.value.map { 
            if (it.id == lessonId) it.copy(isPaid = isPaid) else it 
        }
    }

    override suspend fun updateLessonInvoicedStatus(lessonId: Long, isInvoiced: Boolean, userId: Long) {
        lessons.value = lessons.value.map { 
            if (it.id == lessonId) it.copy(isInvoiced = isInvoiced) else it 
        }
    }

    override suspend fun isLessonInvoiced(lessonId: Long, userId: Long): Boolean {
        return lessons.value.find { it.id == lessonId }?.isInvoiced ?: false
    }

    override fun getAllLessons(userId: Long): Flow<List<DomainLesson>> = lessons

    override fun getLessonById(lessonId: Long, userId: Long): Flow<DomainLesson?> {
        return MutableStateFlow(lessons.value.find { it.id == lessonId })
    }

    override fun getStudentLessons(studentId: Long, userId: Long): Flow<List<DomainLesson>> {
        return MutableStateFlow(lessons.value.filter { it.studentId == studentId })
    }

    override fun getLessonsWithStudents(userId: Long): Flow<List<DomainLesson>> = lessons

    override fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String,
        userId: Long
    ): Flow<List<DomainLesson>> {
        return MutableStateFlow(
            lessons.value.filter { 
                it.studentId == studentId && 
                it.date >= startDate && 
                it.date <= endDate 
            }
        )
    }

    override suspend fun getLessonsWithStudentsPaginated(
        userId: Long,
        limit: Int,
        offset: Int
    ): List<DomainLesson> {
        return lessons.value.drop(offset).take(limit)
    }
}
