package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.StudentGroup

/**
 * Fluent builder for creating test StudentGroup objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestGroupBuilder {
    private var id: Long = 0L
    private var ownerId: Long = 1L
    private var name: String = "Test Group"

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

    fun build(): StudentGroup = StudentGroup(
        id = id,
        ownerId = ownerId,
        name = name
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
    }
}
