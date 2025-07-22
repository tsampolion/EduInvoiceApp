package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.model.StudentGroup
import javax.inject.Inject

class InsertGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: StudentGroup): Long = repository.insertGroup(group)
}
