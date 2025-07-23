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
    suspend fun softDeleteStudent(studentId: Long) = studentDao.softDeleteStudent(studentId)
    fun getStudentById(id: Long, userId: Long = 0): Flow<Student?> = getStudentByIdAny(id, userId)
    fun getStudentByIdAny(id: Long, userId: Long = 0): Flow<Student?> = studentDao.getStudentByIdAny(id, userId)
    fun getAllActiveStudents(userId: Long = 0): Flow<List<Student>> = studentDao.getAllActiveStudents(userId)
    fun getArchivedStudents(userId: Long = 0): Flow<List<Student>> = studentDao.getArchivedStudents(userId)
    suspend fun restoreStudent(studentId: Long) = studentDao.restoreStudent(studentId)
    suspend fun getActiveStudentCount(userId: Long = 0): Int = studentDao.getActiveStudentCount(userId)
    suspend fun classNameExists(name: String, userId: Long = 0): Boolean = studentDao.classNameExists(name, userId) > 0
}
