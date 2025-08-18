package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import javax.inject.Inject

class ArchiveGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(groupId: Long, userId: Long = 0) =
        repository.archiveGroup(groupId, userId)
}
