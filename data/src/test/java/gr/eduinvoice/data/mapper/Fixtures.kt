package gr.eduinvoice.data.mapper

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainUser
import gr.eduinvoice.domain.model.DomainStudentGroup

object Fixtures {
    // Data Student factories
    fun sampleDataStudent(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "John",
        surname: String = "Doe",
        parentMobile: String = "+306912345678",
        parentEmail: String? = "parent@example.com",
        className: String = "10th Grade",
        rate: Double = 25.0,
        rateType: String = "hourly",
        isActive: Boolean = true,
        lastModified: Long = System.currentTimeMillis()
    ): Student {
        return Student(
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
    }

    fun sampleDomainStudentForData(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "John",
        surname: String = "Doe",
        parentMobile: String = "+306912345678",
        parentEmail: String? = "parent@example.com",
        className: String = "10th Grade",
        rate: Double = 25.0,
        hourlyRate: Double? = 30.0,
        rateType: String = "hourly",
        isActive: Boolean = true,
        lastModified: Long = System.currentTimeMillis()
    ): DomainStudent {
        return DomainStudent(
            id = id,
            ownerId = ownerId,
            name = name,
            surname = surname,
            parentMobile = parentMobile,
            parentEmail = parentEmail,
            className = className,
            rate = rate,
            hourlyRate = hourlyRate,
            rateType = rateType,
            isActive = isActive,
            lastModified = lastModified
        )
    }

    // Data Lesson factories
    fun sampleDataLesson(
        id: Long = 1L,
        ownerId: Long = 1L,
        studentId: Long = 1L,
        groupId: Long? = null,
        date: String = "2024-01-15",
        startTime: String = "14:00",
        durationMinutes: Int = 60,
        notes: String? = "Math tutoring session",
        isPaid: Boolean = false,
        isInvoiced: Boolean = false,
        lastModified: Long = System.currentTimeMillis()
    ): Lesson {
        return Lesson(
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
    }

    fun sampleDomainLessonForData(
        id: Long = 1L,
        ownerId: Long = 1L,
        studentId: Long = 1L,
        groupId: Long? = null,
        date: String = "2024-01-15",
        startTime: String = "14:00",
        durationMinutes: Int = 60,
        notes: String? = "Math tutoring session",
        defaultRate: Double? = 25.0,
        isPaid: Boolean = false,
        isInvoiced: Boolean = false,
        lastModified: Long = System.currentTimeMillis()
    ): DomainLesson {
        return DomainLesson(
            id = id,
            ownerId = ownerId,
            studentId = studentId,
            groupId = groupId,
            date = date,
            startTime = startTime,
            durationMinutes = durationMinutes,
            notes = notes,
            defaultRate = defaultRate,
            isPaid = isPaid,
            isInvoiced = isInvoiced,
            lastModified = lastModified
        )
    }

    // Data User factories
    fun sampleDataUser(
        id: Long = 1L,
        username: String = "tutor123",
        passwordHash: String = "hashed_password_123",
        fullName: String = "John Smith",
        subjectSpecialty: String = "Mathematics",
        yearsExperience: Int = 5
    ): User {
        return User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            fullName = fullName,
            subjectSpecialty = subjectSpecialty,
            yearsExperience = yearsExperience
        )
    }

    fun sampleDomainUserForData(
        id: Long = 1L,
        username: String = "tutor123",
        passwordHash: String = "hashed_password_123",
        fullName: String = "John Smith",
        subjectSpecialty: String = "Mathematics",
        yearsExperience: Int = 5
    ): DomainUser {
        return DomainUser(
            id = id,
            username = username,
            passwordHash = passwordHash,
            fullName = fullName,
            subjectSpecialty = subjectSpecialty,
            yearsExperience = yearsExperience
        )
    }

    // Data Group factories
    fun sampleDataGroup(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "Advanced Math Group"
    ): StudentGroup {
        return StudentGroup(
            id = id,
            ownerId = ownerId,
            name = name
        )
    }

    fun sampleDomainGroupForData(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "Advanced Math Group"
    ): DomainStudentGroup {
        return DomainStudentGroup(
            id = id,
            ownerId = ownerId,
            name = name
        )
    }
}
