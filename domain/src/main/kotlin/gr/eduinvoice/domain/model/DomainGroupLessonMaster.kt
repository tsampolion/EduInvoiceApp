package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainGroupLessonMaster(
    val id: Long,
    val ownerId: Long = 0,
    val groupId: Long,
    val date: String,
    val startTime: String,
    val durationMinutes: Int,
    val notes: String? = null
)
