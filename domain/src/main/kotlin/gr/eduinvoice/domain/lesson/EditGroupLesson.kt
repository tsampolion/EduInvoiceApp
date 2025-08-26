package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class EditGroupLesson @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(
        masterId: Long,
        groupId: Long,
        originalDate: String,
        originalStartTime: String,
        originalDuration: Int,
        newDate: String,
        newStartTime: String,
        newDuration: Int,
        newNotes: String?,
        newAbsentStudentIds: List<Long>,
        userId: Long = 0
    ) = repository.editGroupLesson(
        masterId,
        groupId,
        originalDate,
        originalStartTime,
        originalDuration,
        newDate,
        newStartTime,
        newDuration,
        newNotes,
        newAbsentStudentIds,
        userId
    )
}
