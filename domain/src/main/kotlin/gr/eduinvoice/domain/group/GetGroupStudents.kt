package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupStudents @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(groupId: Long): Flow<List<Student>> = repository.getStudentsForGroup(groupId)
}
