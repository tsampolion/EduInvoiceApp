package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.GroupStudentCrossRef

/**
 * Fluent builder for creating test GroupStudentCrossRef objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestCrossRefBuilder {
    private var groupId: Long = 1L
    private var studentId: Long = 1L
    private var ownerId: Long = 1L

    fun withGroupId(groupId: Long): TestCrossRefBuilder {
        this.groupId = groupId
        return this
    }

    fun withStudentId(studentId: Long): TestCrossRefBuilder {
        this.studentId = studentId
        return this
    }

    fun withOwnerId(ownerId: Long): TestCrossRefBuilder {
        this.ownerId = ownerId
        return this
    }

    fun build(): GroupStudentCrossRef = GroupStudentCrossRef(
        groupId = groupId,
        studentId = studentId,
        ownerId = ownerId
    )

    companion object {
        /**
         * Creates a default test cross reference
         */
        fun createDefault(): GroupStudentCrossRef = TestCrossRefBuilder().build()

        /**
         * Creates a test cross reference with specific IDs
         */
        fun createWithIds(
            groupId: Long,
            studentId: Long,
            ownerId: Long = 1L
        ): GroupStudentCrossRef = TestCrossRefBuilder()
            .withGroupId(groupId)
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates multiple cross references for a group
         */
        fun createMultipleForGroup(
            groupId: Long,
            studentIds: List<Long>,
            ownerId: Long = 1L
        ): List<GroupStudentCrossRef> = studentIds.map { studentId ->
            TestCrossRefBuilder()
                .withGroupId(groupId)
                .withStudentId(studentId)
                .withOwnerId(ownerId)
                .build()
        }
    }
}
