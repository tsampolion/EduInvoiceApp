package gr.eduinvoice.test.support.builders

import gr.eduinvoice.data.model.Student
import java.time.LocalDateTime

/**
 * Fluent builder for creating test Student objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestStudentBuilder {
    private var id: Long = 1L
    private var ownerId: Long = 1L
    private var name: String = "Test Student"
    private var surname: String = "Test Surname"
    private var parentMobile: String = "+30123456789"
    private var parentEmail: String = "test@example.com"
    private var className: String = "Test Class"
    private var rate: Double = 25.0
    private var isActive: Boolean = true
    private var createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    fun withId(id: Long): TestStudentBuilder {
        this.id = id
        return this
    }

    fun withOwnerId(ownerId: Long): TestStudentBuilder {
        this.ownerId = ownerId
        return this
    }

    fun withName(name: String): TestStudentBuilder {
        this.name = name
        return this
    }

    fun withSurname(surname: String): TestStudentBuilder {
        this.surname = surname
        return this
    }

    fun withParentMobile(parentMobile: String): TestStudentBuilder {
        this.parentMobile = parentMobile
        return this
    }

    fun withParentEmail(parentEmail: String): TestStudentBuilder {
        this.parentEmail = parentEmail
        return this
    }

    fun withClassName(className: String): TestStudentBuilder {
        this.className = className
        return this
    }

    fun withRate(rate: Double): TestStudentBuilder {
        this.rate = rate
        return this
    }

    fun withIsActive(isActive: Boolean): TestStudentBuilder {
        this.isActive = isActive
        return this
    }

    fun withCreatedAt(createdAt: LocalDateTime): TestStudentBuilder {
        this.createdAt = createdAt
        return this
    }

    fun withUpdatedAt(updatedAt: LocalDateTime): TestStudentBuilder {
        this.updatedAt = updatedAt
        return this
    }

    fun build(): Student = Student(
        id = id,
        ownerId = ownerId,
        name = name,
        surname = surname,
        parentMobile = parentMobile,
        parentEmail = parentEmail,
        className = className,
        rate = rate,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Creates a default test student
         */
        fun createDefault(): Student = TestStudentBuilder().build()

        /**
         * Creates a test student with minimal required fields
         */
        fun createMinimal(
            name: String = "Minimal Student",
            ownerId: Long = 1L
        ): Student = TestStudentBuilder()
            .withName(name)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates an archived (inactive) test student
         */
        fun createArchived(
            name: String = "Archived Student",
            ownerId: Long = 1L
        ): Student = TestStudentBuilder()
            .withName(name)
            .withOwnerId(ownerId)
            .withIsActive(false)
            .build()

        /**
         * Creates a test student with high rate
         */
        fun createHighRate(
            name: String = "High Rate Student",
            rate: Double = 50.0,
            ownerId: Long = 1L
        ): Student = TestStudentBuilder()
            .withName(name)
            .withRate(rate)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates multiple test students with sequential IDs
         */
        fun createMultiple(count: Int, ownerId: Long = 1L): List<Student> =
            (1..count).map { index ->
                TestStudentBuilder()
                    .withId(index.toLong())
                    .withName("Student $index")
                    .withOwnerId(ownerId)
                    .build()
            }
    }
}
