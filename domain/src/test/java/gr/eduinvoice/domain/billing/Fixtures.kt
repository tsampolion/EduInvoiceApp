package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent

@Suppress("UNUSED_PARAMETER")
fun sampleDomainStudent(
    id: Long = 1L,
    name: String = "Alex",
    hourlyRate: Double? = 20.0,
    isArchived: Boolean = false
): DomainStudent = TODO("Return DomainStudent(id=..., name=..., hourlyRate=..., isArchived=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDomainLesson(
    id: Long = 1L,
    studentId: Long = 1L,
    durationMinutes: Int = 60,
    defaultRate: Double? = 18.0
): DomainLesson = TODO("Return DomainLesson(id=..., studentId=..., durationMinutes=..., defaultRate=..., ...)")
