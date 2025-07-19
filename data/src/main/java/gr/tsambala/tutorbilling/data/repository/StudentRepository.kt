package gr.tsambala.tutorbilling.data.repository

import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.model.Student
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
    fun getStudentById(id: Long): Flow<Student?> = getStudentByIdAny(id)
    fun getStudentByIdAny(id: Long): Flow<Student?> = studentDao.getStudentByIdAny(id)
    fun getAllActiveStudents(): Flow<List<Student>> = studentDao.getAllActiveStudents()
    fun getArchivedStudents(): Flow<List<Student>> = studentDao.getArchivedStudents()
    suspend fun restoreStudent(studentId: Long) = studentDao.restoreStudent(studentId)
    suspend fun getActiveStudentCount(): Int = studentDao.getActiveStudentCount()
    suspend fun classNameExists(name: String): Boolean = studentDao.classNameExists(name) > 0
}
