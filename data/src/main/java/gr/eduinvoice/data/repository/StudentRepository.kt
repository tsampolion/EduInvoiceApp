package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao
) {
    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) { studentDao.insert(student) }
    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) { studentDao.update(student) }
    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) { studentDao.delete(student) }
    suspend fun softDeleteStudent(studentId: Long, userId: Long) = withContext(Dispatchers.IO) { studentDao.softDeleteStudent(studentId, userId) }
    fun getStudentById(id: Long, userId: Long): Flow<Student?> = getStudentByIdAny(id, userId)
    fun getStudentByIdAny(id: Long, userId: Long): Flow<Student?> =
        studentDao.getStudentByIdAny(id, userId)
    fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
        studentDao.getAllActiveStudents(userId)
    fun getArchivedStudents(userId: Long): Flow<List<Student>> =
        studentDao.getArchivedStudents(userId)
    suspend fun restoreStudent(studentId: Long, userId: Long) = withContext(Dispatchers.IO) {
        studentDao.restoreStudent(studentId, userId)
    }
    suspend fun getActiveStudentCount(userId: Long): Int = withContext(Dispatchers.IO) {
        studentDao.getActiveStudentCount(userId)
    }
    suspend fun classNameExists(name: String, userId: Long): Boolean = withContext(Dispatchers.IO) {
        studentDao.classNameExists(name, userId) > 0
    }

    suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> = withContext(Dispatchers.IO) {
        studentDao.getStudentsPaginated(userId, limit, offset)
    }

    suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> = withContext(Dispatchers.IO) {
        studentDao.searchStudentsPaginated(userId, searchQuery, limit, offset)
    }
}
