package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.StudentGroup
import javax.inject.Inject

class DeleteGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: StudentGroup) = repository.deleteGroup(group)
}
