package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupStudents @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(groupId: Long): Flow<List<Student>> = repository.getStudentsForGroup(groupId)
}
