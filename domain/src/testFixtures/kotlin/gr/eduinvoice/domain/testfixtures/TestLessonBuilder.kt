package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.Lesson
import java.time.LocalDate
import java.time.LocalTime

/**
 * Fluent builder for creating test Lesson objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestLessonBuilder {
    private var id: Long = 0L
    private var ownerId: Long = 1L
    private var studentId: Long = 1L
    private var groupId: Long? = null
    private var date: String = LocalDate.now().toString()
    private var startTime: String = LocalTime.of(14, 0).toString()
    private var durationMinutes: Int = 60
    private var notes: String? = null
    private var isPaid: Boolean = false
    private var isInvoiced: Boolean = false
    private var lastModified: Long = System.currentTimeMillis()

    fun withId(id: Long): TestLessonBuilder {
        this.id = id
        return this
    }

    fun withOwnerId(ownerId: Long): TestLessonBuilder {
        this.ownerId = ownerId
        return this
    }

    fun withStudentId(studentId: Long): TestLessonBuilder {
        this.studentId = studentId
        return this
    }

    fun withGroupId(groupId: Long?): TestLessonBuilder {
        this.groupId = groupId
        return this
    }

    fun withDate(date: String): TestLessonBuilder {
        this.date = date
        return this
    }

    fun withDate(date: LocalDate): TestLessonBuilder {
        this.date = date.toString()
        return this
    }

    fun withStartTime(startTime: String): TestLessonBuilder {
        this.startTime = startTime
        return this
    }

    fun withStartTime(startTime: LocalTime): TestLessonBuilder {
        this.startTime = startTime.toString()
        return this
    }

    fun withDurationMinutes(durationMinutes: Int): TestLessonBuilder {
        this.durationMinutes = durationMinutes
        return this
    }

    fun withNotes(notes: String?): TestLessonBuilder {
        this.notes = notes
        return this
    }

    fun withIsPaid(isPaid: Boolean): TestLessonBuilder {
        this.isPaid = isPaid
        return this
    }

    fun withIsInvoiced(isInvoiced: Boolean): TestLessonBuilder {
        this.isInvoiced = isInvoiced
        return this
    }

    fun withLastModified(lastModified: Long): TestLessonBuilder {
        this.lastModified = lastModified
        return this
    }

    fun build(): Lesson = Lesson(
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

    companion object {
        /**
         * Creates a default test lesson
         */
        fun createDefault(): Lesson = TestLessonBuilder().build()

        /**
         * Creates a test lesson with minimal required fields
         */
        fun createMinimal(
            studentId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates a paid test lesson
         */
        fun createPaid(
            studentId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .withIsPaid(true)
            .build()

        /**
         * Creates an invoiced test lesson
         */
        fun createInvoiced(
            studentId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .withIsInvoiced(true)
            .build()

        /**
         * Creates a group lesson
         */
        fun createGroupLesson(
            groupId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withGroupId(groupId)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates multiple test lessons for a student
         */
        fun createMultipleForStudent(
            studentId: Long,
            count: Int,
            ownerId: Long = 1L
        ): List<Lesson> = (1..count).map { index ->
            TestLessonBuilder()
                .withId(index.toLong())
                .withStudentId(studentId)
                .withOwnerId(ownerId)
                .withDate(LocalDate.now().plusDays(index.toLong()))
                .build()
        }

        /**
         * Creates a test lesson with specific date and time
         */
        fun createWithDateTime(
            studentId: Long,
            date: LocalDate,
            startTime: LocalTime,
            durationMinutes: Int = 60,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withStudentId(studentId)
            .withDate(date)
            .withStartTime(startTime)
            .withDurationMinutes(durationMinutes)
            .withOwnerId(ownerId)
            .build()
    }
}
