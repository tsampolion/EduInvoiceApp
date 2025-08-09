package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainStudentGroup(
    val id: Long = 0,
    val ownerId: Long = 0,
    val name: String
)
