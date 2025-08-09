package gr.eduinvoice.domain.repository

import gr.eduinvoice.domain.model.DomainStudent
import kotlinx.coroutines.flow.Flow

interface DomainStudentRepository {
    suspend fun insertStudent(student: DomainStudent): Long
    suspend fun updateStudent(student: DomainStudent)
    suspend fun deleteStudent(student: DomainStudent)
    suspend fun softDeleteStudent(studentId: Long, userId: Long)
    fun getStudentById(id: Long, userId: Long): Flow<DomainStudent?>
    fun getStudentByIdAny(id: Long, userId: Long): Flow<DomainStudent?>
    fun getAllActiveStudents(userId: Long): Flow<List<DomainStudent>>
    fun getArchivedStudents(userId: Long): Flow<List<DomainStudent>>
    suspend fun restoreStudent(studentId: Long, userId: Long)
    suspend fun getActiveStudentCount(userId: Long): Int
    suspend fun classNameExists(name: String, userId: Long): Boolean
    suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<DomainStudent>
    suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<DomainStudent>
}
