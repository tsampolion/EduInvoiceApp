package gr.tsambala.tutorbilling.domain.group

import gr.tsambala.tutorbilling.data.repository.GroupRepository
import gr.tsambala.tutorbilling.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupStudents @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(groupId: Long): Flow<List<Student>> = repository.getStudentsForGroup(groupId)
}
