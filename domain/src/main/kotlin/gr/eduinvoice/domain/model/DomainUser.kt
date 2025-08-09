package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainUser(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val subjectSpecialty: String = "",
    val yearsExperience: Int = 0
)
