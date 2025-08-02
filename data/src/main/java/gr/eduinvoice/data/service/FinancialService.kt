package gr.eduinvoice.data.service

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.calculateFee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialService @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao
) {
    data class StudentFinancialSummary(
        val student: Student,
        val weekTotal: Double,
        val monthTotal: Double,
        val lessonCount: Int
    )

    fun getStudentFinancialSummaries(userId: Long): Flow<List<StudentFinancialSummary>> {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        return combine(
            studentDao.getAllActiveStudents(userId),
            lessonDao.getAllLessons(userId)
        ) { students, lessons ->
            students.map { student ->
                val studentLessons = lessons.filter { it.studentId == student.id }
                val studentWeekLessons = studentLessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                }
                val studentMonthLessons = studentLessons.filter { lesson ->
                    val date = LocalDate.parse(lesson.date)
                    !date.isBefore(monthStart) && !date.isAfter(monthEnd)
                }
                val weekTotal = studentWeekLessons.sumOf { it.calculateFee(student) }
                val monthTotal = studentMonthLessons.sumOf { it.calculateFee(student) }
                StudentFinancialSummary(
                    student = student,
                    weekTotal = weekTotal,
                    monthTotal = monthTotal,
                    lessonCount = studentMonthLessons.size
                )
            }
        }
    }

    fun getTotalEarningsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): Flow<Double> {
        return combine(
            lessonDao.getLessonsInDateRange(startDate.toString(), endDate.toString(), userId),
            studentDao.getAllActiveStudents(userId)
        ) { lessons, students ->
            val studentMap = students.associateBy { it.id }
            lessons.sumOf { lesson ->
                val student = studentMap[lesson.studentId] ?: return@sumOf 0.0
                lesson.calculateFee(student)
            }
        }
    }

    data class StudentDetailedReport(
        val student: Student,
        val totalLessons: Int,
        val totalHours: Double,
        val totalEarnings: Double,
        val averageLessonDuration: Double,
        val lastLessonDate: LocalDate?
    )

    suspend fun getStudentDetailedReport(
        studentId: Long,
        userId: Long
    ): StudentDetailedReport? {
        val student = studentDao.getStudentById(studentId, userId).first() ?: return null
        val lessons: List<Lesson> = lessonDao.getLessonsByStudentId(studentId, userId).first()
        val totalMinutes = lessons.sumOf { it.durationMinutes }
        val totalHours = totalMinutes / 60.0
        val totalEarnings = lessons.sumOf { it.calculateFee(student) }
        val averageDuration = if (lessons.isNotEmpty()) totalMinutes.toDouble() / lessons.size else 0.0
        val lastLessonDate = lessons.maxOfOrNull { LocalDate.parse(it.date) }
        return StudentDetailedReport(
            student = student,
            totalLessons = lessons.size,
            totalHours = totalHours,
            totalEarnings = totalEarnings,
            averageLessonDuration = averageDuration,
            lastLessonDate = lastLessonDate
        )
    }
}
