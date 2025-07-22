package gr.tutorbilling.domain.student

import gr.tutorbilling.data.repository.StudentRepository
import gr.tutorbilling.data.model.Student
import javax.inject.Inject

class InsertStudent @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(student: Student): Long = repository.insertStudent(student)
}
