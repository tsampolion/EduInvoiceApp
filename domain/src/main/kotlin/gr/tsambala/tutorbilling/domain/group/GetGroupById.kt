package gr.tsambala.tutorbilling.domain.group

import gr.tsambala.tutorbilling.data.repository.GroupRepository
import gr.tsambala.tutorbilling.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupById @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(id: Long): Flow<StudentGroup?> = repository.getGroupById(id)
}
