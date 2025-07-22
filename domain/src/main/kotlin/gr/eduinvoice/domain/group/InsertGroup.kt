package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.StudentGroup
import javax.inject.Inject

class InsertGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: StudentGroup): Long = repository.insertGroup(group)
}
