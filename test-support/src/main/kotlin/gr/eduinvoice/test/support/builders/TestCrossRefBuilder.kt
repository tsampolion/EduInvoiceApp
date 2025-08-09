package gr.eduinvoice.test.support.builders

import gr.eduinvoice.data.model.GroupStudentCrossRef
import java.time.LocalDateTime

/**
 * Fluent builder for creating test GroupStudentCrossRef objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestCrossRefBuilder {
    private var groupId: Long = 1L
    private var studentId: Long = 1L
    private var ownerId: Long = 1L
    private var createdAt: LocalDateTime = LocalDateTime.now()

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

    fun withCreatedAt(createdAt: LocalDateTime): TestCrossRefBuilder {
        this.createdAt = createdAt
        return this
    }

    fun build(): GroupStudentCrossRef = GroupStudentCrossRef(
        groupId = groupId,
        studentId = studentId,
        ownerId = ownerId,
        createdAt = createdAt
    )

    companion object {
        /**
         * Creates a default test cross-reference
         */
        fun createDefault(): GroupStudentCrossRef = TestCrossRefBuilder().build()

        /**
         * Creates a test cross-reference with specific IDs
         */
        fun create(
            groupId: Long,
            studentId: Long,
            ownerId: Long = 1L
        ): GroupStudentCrossRef = TestCrossRefBuilder()
            .withGroupId(groupId)
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates multiple cross-references for a group
         */
        fun createForGroup(
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

        /**
         * Creates multiple cross-references for a student
         */
        fun createForStudent(
            studentId: Long,
            groupIds: List<Long>,
            ownerId: Long = 1L
        ): List<GroupStudentCrossRef> = groupIds.map { groupId ->
            TestCrossRefBuilder()
                .withGroupId(groupId)
                .withStudentId(studentId)
                .withOwnerId(ownerId)
                .build()
        }

        /**
         * Creates a matrix of cross-references between groups and students
         */
        fun createMatrix(
            groupIds: List<Long>,
            studentIds: List<Long>,
            ownerId: Long = 1L
        ): List<GroupStudentCrossRef> = groupIds.flatMap { groupId ->
            studentIds.map { studentId ->
                TestCrossRefBuilder()
                    .withGroupId(groupId)
                    .withStudentId(studentId)
                    .withOwnerId(ownerId)
                    .build()
            }
        }
    }
}
