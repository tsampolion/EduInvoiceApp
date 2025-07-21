package gr.tsambala.tutorbilling.domain.group

import gr.tsambala.tutorbilling.data.repository.GroupRepository
import gr.tsambala.tutorbilling.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroups @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(): Flow<List<StudentGroup>> = repository.getAllGroups()
}
