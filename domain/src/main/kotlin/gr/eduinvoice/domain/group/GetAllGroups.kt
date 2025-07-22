package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroups @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(): Flow<List<StudentGroup>> = repository.getAllGroups()
}
