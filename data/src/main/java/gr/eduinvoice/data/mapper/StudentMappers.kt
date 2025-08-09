package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.domain.model.DomainStudent

fun Student.toDomain(): DomainStudent = DomainStudent(
    id = id,
    ownerId = ownerId,
    name = name,
    surname = surname,
    parentMobile = parentMobile,
    parentEmail = parentEmail,
    className = className,
    rate = rate,
    rateType = rateType,
    isActive = isActive,
    lastModified = lastModified
)

fun DomainStudent.toData(): Student = Student(
    id = id,
    ownerId = ownerId,
    name = name,
    surname = surname,
    parentMobile = parentMobile,
    parentEmail = parentEmail,
    className = className,
    rate = rate,
    rateType = rateType,
    isActive = isActive,
    lastModified = lastModified
)
