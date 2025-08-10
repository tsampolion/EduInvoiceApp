package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainStudentGroup
import gr.eduinvoice.domain.model.DomainUser

@Suppress("UNUSED_PARAMETER")
fun sampleDataStudent(
    id: Long,
    name: String? = "Name",
    groupId: Long? = null,
    hourlyRate: Double? = null,
    isArchived: Boolean = false
): Student = TODO("Return Student(id=..., name=..., groupId=..., hourlyRate=..., isArchived=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDataLesson(
    id: Long,
    studentId: Long,
    durationMinutes: Int = 60,
    defaultRate: Double? = null
): Lesson = TODO("Return Lesson(id=..., studentId=..., durationMinutes=..., defaultRate=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDataUser(
    id: Long,
    name: String = "User"
): User = TODO("Return User(id=..., name=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDataGroup(
    id: Long,
    name: String = "Group"
): StudentGroup = TODO("Return StudentGroup(id=..., name=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDomainStudentForData(
    id: Long,
    name: String,
    groupId: Long? = null,
    hourlyRate: Double? = null,
    isArchived: Boolean = false
): DomainStudent = TODO("Return DomainStudent(id=..., name=..., groupId=..., hourlyRate=..., isArchived=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDomainLessonForData(
    id: Long,
    studentId: Long,
    durationMinutes: Int = 60,
    defaultRate: Double? = null
): DomainLesson = TODO("Return DomainLesson(id=..., studentId=..., durationMinutes=..., defaultRate=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDomainUserForData(
    id: Long,
    name: String
): DomainUser = TODO("Return DomainUser(id=..., name=..., ...)")

@Suppress("UNUSED_PARAMETER")
fun sampleDomainGroupForData(
    id: Long,
    name: String
): DomainStudentGroup = TODO("Return DomainStudentGroup(id=..., name=..., ...)")
