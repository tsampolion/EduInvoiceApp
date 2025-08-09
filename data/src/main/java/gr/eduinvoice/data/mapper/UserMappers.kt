package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.User
import gr.eduinvoice.domain.model.DomainUser

fun User.toDomain(): DomainUser = DomainUser(
    id = id,
    username = username,
    passwordHash = passwordHash,
    fullName = fullName,
    subjectSpecialty = subjectSpecialty,
    yearsExperience = yearsExperience
)

fun DomainUser.toData(): User = User(
    id = id,
    username = username,
    passwordHash = passwordHash,
    fullName = fullName,
    subjectSpecialty = subjectSpecialty,
    yearsExperience = yearsExperience
)
