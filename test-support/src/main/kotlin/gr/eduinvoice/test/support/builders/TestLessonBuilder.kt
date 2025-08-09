package gr.eduinvoice.test.support.builders

import gr.eduinvoice.data.model.Lesson
import java.time.LocalDate
import java.time.LocalTime

/**
 * Fluent builder for creating test Lesson objects
 * Provides sensible defaults and allows easy customization for different test scenarios
 */
class TestLessonBuilder {
    private var id: Long = 1L
    private var studentId: Long = 1L
    private var ownerId: Long = 1L
    private var date: LocalDate = LocalDate.now()
    private var startTime: LocalTime = LocalTime.of(10, 0)
    private var durationMinutes: Int = 60
    private var notes: String = "Test lesson"
    private var isPaid: Boolean = false
    private var isInvoiced: Boolean = false
    private var createdAt: Long = System.currentTimeMillis()
    private var updatedAt: Long = System.currentTimeMillis()

    fun withId(id: Long): TestLessonBuilder {
        this.id = id
        return this
    }

    fun withStudentId(studentId: Long): TestLessonBuilder {
        this.studentId = studentId
        return this
    }

    fun withOwnerId(ownerId: Long): TestLessonBuilder {
        this.ownerId = ownerId
        return this
    }

    fun withDate(date: LocalDate): TestLessonBuilder {
        this.date = date
        return this
    }

    fun withDate(date: String): TestLessonBuilder {
        this.date = LocalDate.parse(date)
        return this
    }

    fun withStartTime(startTime: LocalTime): TestLessonBuilder {
        this.startTime = startTime
        return this
    }

    fun withStartTime(hour: Int, minute: Int): TestLessonBuilder {
        this.startTime = LocalTime.of(hour, minute)
        return this
    }

    fun withDurationMinutes(durationMinutes: Int): TestLessonBuilder {
        this.durationMinutes = durationMinutes
        return this
    }

    fun withNotes(notes: String): TestLessonBuilder {
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

    fun withCreatedAt(createdAt: Long): TestLessonBuilder {
        this.createdAt = createdAt
        return this
    }

    fun withUpdatedAt(updatedAt: Long): TestLessonBuilder {
        this.updatedAt = updatedAt
        return this
    }

    fun build(): Lesson = Lesson.create(
        studentId = studentId,
        date = date,
        startTime = startTime,
        durationMinutes = durationMinutes,
        notes = notes,
        ownerId = ownerId
    ).copy(
        id = id,
        isPaid = isPaid,
        isInvoiced = isInvoiced,
        createdAt = createdAt,
        updatedAt = updatedAt
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
         * Creates a test lesson for a specific date
         */
        fun createForDate(
            date: LocalDate,
            studentId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withDate(date)
            .withStudentId(studentId)
            .withOwnerId(ownerId)
            .build()

        /**
         * Creates a long duration test lesson
         */
        fun createLongDuration(
            durationMinutes: Int = 120,
            studentId: Long = 1L,
            ownerId: Long = 1L
        ): Lesson = TestLessonBuilder()
            .withDurationMinutes(durationMinutes)
            .withStudentId(studentId)
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
                .withStartTime(9 + (index % 8), 0)
                .build()
        }
    }
}
