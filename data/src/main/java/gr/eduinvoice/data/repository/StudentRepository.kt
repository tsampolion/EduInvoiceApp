package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao
) {
    suspend fun insertStudent(student: Student): Long = studentDao.insert(student)
    suspend fun updateStudent(student: Student) = studentDao.update(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)
    suspend fun softDeleteStudent(studentId: Long, userId: Long) = studentDao.softDeleteStudent(studentId, userId)
    fun getStudentById(id: Long, userId: Long): Flow<Student?> = getStudentByIdAny(id, userId)
    fun getStudentByIdAny(id: Long, userId: Long): Flow<Student?> =
        studentDao.getStudentByIdAny(id, userId)
    fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
        studentDao.getAllActiveStudents(userId)
    fun getArchivedStudents(userId: Long): Flow<List<Student>> =
        studentDao.getArchivedStudents(userId)
    suspend fun restoreStudent(studentId: Long, userId: Long) =
        studentDao.restoreStudent(studentId, userId)
    suspend fun getActiveStudentCount(userId: Long): Int =
        studentDao.getActiveStudentCount(userId)
    suspend fun classNameExists(name: String, userId: Long): Boolean =
        studentDao.classNameExists(name, userId) > 0
    
    suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> =
        studentDao.getStudentsPaginated(userId, limit, offset)
    
    suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> =
        studentDao.searchStudentsPaginated(userId, searchQuery, limit, offset)
}
