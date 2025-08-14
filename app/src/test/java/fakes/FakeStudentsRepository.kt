package fakes

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.repository.DomainStudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeStudentsRepository : DomainStudentRepository {
    private val students = MutableStateFlow<List<DomainStudent>>(emptyList())

    fun setStudents(newStudents: List<DomainStudent>) {
        students.value = newStudents
    }

    override suspend fun insertStudent(student: DomainStudent): Long {
        val newStudent = student.copy(id = (students.value.maxOfOrNull { it.id } ?: 0) + 1)
        students.value = students.value + newStudent
        return newStudent.id
    }

    override suspend fun updateStudent(student: DomainStudent) {
        students.value = students.value.map {
            if (it.id == student.id) student else it
        }
    }

    override suspend fun deleteStudent(student: DomainStudent) {
        students.value = students.value.filter { it.id != student.id }
    }

    override suspend fun softDeleteStudent(studentId: Long, userId: Long) {
        students.value = students.value.map {
            if (it.id == studentId) it.copy(isActive = false) else it
        }
    }

    override fun getStudentById(id: Long, userId: Long): Flow<DomainStudent?> {
        return MutableStateFlow(students.value.find { it.id == id })
    }

    override fun getStudentByIdAny(id: Long, userId: Long): Flow<DomainStudent?> {
        return getStudentById(id, userId)
    }

    override fun getAllActiveStudents(userId: Long): Flow<List<DomainStudent>> {
        return MutableStateFlow(students.value.filter { it.isActive })
    }

    override fun getArchivedStudents(userId: Long): Flow<List<DomainStudent>> {
        return MutableStateFlow(students.value.filter { !it.isActive })
    }

    override suspend fun restoreStudent(studentId: Long, userId: Long) {
        students.value = students.value.map {
            if (it.id == studentId) it.copy(isActive = true) else it
        }
    }

    override suspend fun getActiveStudentCount(userId: Long): Int {
        return students.value.count { it.isActive }
    }

    override suspend fun classNameExists(name: String, userId: Long): Boolean {
        return students.value.any { it.className == name }
    }

    override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<DomainStudent> {
        return students.value.drop(offset).take(limit)
    }

    override suspend fun searchStudentsPaginated(
        userId: Long,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): List<DomainStudent> {
        val filtered = students.value.filter { student ->
            student.name.contains(searchQuery, ignoreCase = true) ||
            student.surname.contains(searchQuery, ignoreCase = true) ||
            student.className.contains(searchQuery, ignoreCase = true)
        }
        return filtered.drop(offset).take(limit)
    }
}
