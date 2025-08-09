package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of StudentRepository for testing
 * Uses in-memory storage with MutableStateFlow for reactive updates
 */
class FakeStudentRepository {
    private val students = MutableStateFlow<List<Student>>(emptyList())
    private var nextId = 1L

    suspend fun insertStudent(student: Student): Long {
        val newStudent = student.copy(id = nextId++)
        students.value = students.value + newStudent
        return newStudent.id
    }

    suspend fun updateStudent(student: Student) {
        students.value = students.value.map { 
            if (it.id == student.id) student else it 
        }
    }

    suspend fun deleteStudent(student: Student) {
        students.value = students.value.filter { it.id != student.id }
    }

    suspend fun softDeleteStudent(studentId: Long, userId: Long) {
        students.value = students.value.map { 
            if (it.id == studentId && it.ownerId == userId) {
                it.copy(isActive = false)
            } else it 
        }
    }

    fun getStudentById(id: Long, userId: Long): Flow<Student?> =
        students.map { studentList ->
            studentList.find { it.id == id && it.ownerId == userId && it.isActive }
        }

    fun getStudentByIdAny(id: Long, userId: Long): Flow<Student?> =
        students.map { studentList ->
            studentList.find { it.id == id && it.ownerId == userId }
        }

    fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
        students.map { studentList ->
            studentList.filter { it.ownerId == userId && it.isActive }
                .sortedBy { it.name }
        }

    fun getArchivedStudents(userId: Long): Flow<List<Student>> =
        students.map { studentList ->
            studentList.filter { it.ownerId == userId && !it.isActive }
                .sortedBy { it.name }
        }

    suspend fun restoreStudent(studentId: Long, userId: Long) {
        students.value = students.value.map { 
            if (it.id == studentId && it.ownerId == userId) {
                it.copy(isActive = true)
            } else it 
        }
    }

    suspend fun getActiveStudentCount(userId: Long): Int =
        students.value.count { it.ownerId == userId && it.isActive }

    suspend fun classNameExists(name: String, userId: Long): Boolean =
        students.value.any { 
            it.ownerId == userId && it.className.equals(name, ignoreCase = true) 
        }

    suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> {
        val activeStudents = students.value.filter { it.ownerId == userId && it.isActive }
            .sortedBy { it.name }
        return activeStudents.drop(offset).take(limit)
    }

    suspend fun searchStudentsPaginated(
        userId: Long, 
        searchQuery: String, 
        limit: Int, 
        offset: Int
    ): List<Student> {
        val activeStudents = students.value.filter { it.ownerId == userId && it.isActive }
        val filteredStudents = activeStudents.filter { student ->
            student.name.contains(searchQuery, ignoreCase = true) ||
            student.surname.contains(searchQuery, ignoreCase = true) ||
            student.className.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
        return filteredStudents.drop(offset).take(limit)
    }

    /**
     * Test helper methods
     */
    fun addStudent(student: Student) {
        students.value = students.value + student
    }

    fun clear() {
        students.value = emptyList()
        nextId = 1L
    }

    fun getAllStudents(): List<Student> = students.value
}
