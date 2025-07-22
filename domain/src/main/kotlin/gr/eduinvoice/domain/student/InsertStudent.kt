package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.model.Student
import javax.inject.Inject

class InsertStudent @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(student: Student): Long = repository.insertStudent(student)
}
