package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.domain.model.DomainStudentGroup

fun StudentGroup.toDomain(): DomainStudentGroup = DomainStudentGroup(
    id = id,
    ownerId = ownerId,
    name = name
)

fun DomainStudentGroup.toData(): StudentGroup = StudentGroup(
    id = id,
    ownerId = ownerId,
    name = name
)
