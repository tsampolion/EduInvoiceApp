package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.GroupStudentCrossRef
import javax.inject.Inject

class AddStudentToGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long, userId: Long = 0) =
        repository.insertCrossRef(GroupStudentCrossRef(groupId, studentId, userId))
}
