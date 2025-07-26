package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupById @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<StudentGroup?> =
        repository.getGroupById(id, userId)
}
