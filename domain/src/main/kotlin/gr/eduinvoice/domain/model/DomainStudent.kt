package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainStudent(
    val id: Long = 0,
    val ownerId: Long = 0,
    val name: String,
    val surname: String = "",
    val parentMobile: String = "",
    val parentEmail: String? = null,
    val className: String = "",
    val rate: Double,
    val hourlyRate: Double? = null, // Specific hourly rate for billing calculations
    val rateType: String = "hourly",
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun getFullName(): String = "${name} ${surname}".trim()
    
    // Helper to get the effective hourly rate
    fun getEffectiveHourlyRate(): Double = hourlyRate ?: rate
}
