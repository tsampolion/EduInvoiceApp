package gr.eduinvoice.ui.mappers

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.ui.model.UiLessonWithStudent
import gr.eduinvoice.ui.model.UiStudentWithEarnings

fun DomainLesson.with(student: DomainStudent): UiLessonWithStudent =
    UiLessonWithStudent(this, student)

fun DomainStudent.withEarnings(weekEarnings: Double, monthEarnings: Double): UiStudentWithEarnings =
    UiStudentWithEarnings(this, weekEarnings, monthEarnings)
