package gr.eduinvoice.test.support.builders

import gr.eduinvoice.data.model.StudentGroup
import java.time.LocalDateTime

/**
 * Fluent builder for creating test StudentGroup objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestGroupBuilder {
    private var id: Long = 1L
    private var ownerId: Long = 1L
    private var name: String = "Test Group"
    private var description: String? = null
    private var createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    fun withId(id: Long): TestGroupBuilder {
        this.id = id
        return this
    }

    fun withOwnerId(ownerId: Long): TestGroupBuilder {
        this.ownerId = ownerId
        return this
    }

    fun withName(name: String): TestGroupBuilder {
        this.name = name
        return this
    }

    fun withDescription(description: String?): TestGroupBuilder {
        this.description = description
        return this
    }

    fun withCreatedAt(createdAt: LocalDateTime): TestGroupBuilder {
        this.createdAt = createdAt
        return this
    }

    fun withUpdatedAt(updatedAt: LocalDateTime): TestGroupBuilder {
        this.updatedAt = updatedAt
        return this
    }

    fun build(): StudentGroup = StudentGroup(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Creates a default test group
         */
        fun createDefault(): StudentGroup = TestGroupBuilder().build()

        /**
         * Creates a test group with minimal required fields
         */
        fun createMinimal(
            name: String = "Minimal Group",
            ownerId: Long = 1L
        ): StudentGroup = TestGroupBuilder()
            .withName(name)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates a test group with description
         */
        fun createWithDescription(
            name: String = "Group with Description",
            description: String = "This is a test group description",
            ownerId: Long = 1L
        ): StudentGroup = TestGroupBuilder()
            .withName(name)
            .withDescription(description)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates multiple test groups with sequential IDs
         */
        fun createMultiple(count: Int, ownerId: Long = 1L): List<StudentGroup> =
            (1..count).map { index ->
                TestGroupBuilder()
                    .withId(index.toLong())
                    .withName("Group $index")
                    .withOwnerId(ownerId)
                    .build()
            }

        /**
         * Creates test groups with different naming patterns
         */
        fun createWithPattern(pattern: String, count: Int, ownerId: Long = 1L): List<StudentGroup> =
            (1..count).map { index ->
                TestGroupBuilder()
                    .withId(index.toLong())
                    .withName("$pattern $index")
                    .withOwnerId(ownerId)
                    .build()
            }
    }
}
