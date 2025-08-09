package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainGroupStudentCrossRef(
    val groupId: Long,
    val studentId: Long,
    val ownerId: Long = 0
)
