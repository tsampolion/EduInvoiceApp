package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.model.StudentGroup
import javax.inject.Inject

class DeleteGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: StudentGroup) = repository.deleteGroup(group)
}
