package gr.eduinvoice.utils

import gr.eduinvoice.ui.components.FilterOptions
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainLesson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModernFilterManager @Inject constructor() {
    fun applyStudentFilters(
        students: List<DomainStudent>,
        filters: FilterOptions
    ): List<DomainStudent> {
        var result = students

        filters.status.takeIf { it.isNotEmpty() }?.let { statuses ->
            result = result.filter { student ->
                val activeStatus = if (student.isActive) "active" else "inactive"
                statuses.contains(activeStatus)
            }
        }

        // Date range not applied at student-level; handled when mapping with lessons
        return result
    }

    fun applyLessonDateRange(
        lessons: List<DomainLesson>,
        dateRange: Pair<Long?, Long?>
    ): List<DomainLesson> {
        val (start, end) = dateRange
        if (start == null && end == null) return lessons
        return lessons.filter { lesson ->
            val epoch = gr.eduinvoice.utils.AppUtils.toEpochMillis(lesson.date)
            val afterStart = start?.let { epoch >= it } ?: true
            val beforeEnd = end?.let { epoch <= it } ?: true
            afterStart && beforeEnd
        }
    }
}


