package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.domain.model.DomainLesson

fun Lesson.toDomain(): DomainLesson = DomainLesson(
    id = id,
    ownerId = ownerId,
    studentId = studentId,
    groupId = groupId,
    date = date,
    startTime = startTime,
    durationMinutes = durationMinutes,
    notes = notes,
    isPaid = isPaid,
    isInvoiced = isInvoiced,
    lastModified = lastModified
)

fun DomainLesson.toData(): Lesson = Lesson(
    id = id,
    ownerId = ownerId,
    studentId = studentId,
    groupId = groupId,
    date = date,
    startTime = startTime,
    durationMinutes = durationMinutes,
    notes = notes,
    isPaid = isPaid,
    isInvoiced = isInvoiced,
    lastModified = lastModified
)
