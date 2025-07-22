package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroups @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(): Flow<List<StudentGroup>> = repository.getAllGroups()
}
