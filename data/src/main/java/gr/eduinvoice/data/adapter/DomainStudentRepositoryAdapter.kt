package gr.eduinvoice.data.adapter

import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainStudentRepositoryAdapter @Inject constructor(
    private val studentRepository: StudentRepository
) : DomainStudentRepository {

    override suspend fun insertStudent(student: DomainStudent): Long =
        studentRepository.insertStudent(student.toDataModel())

    override suspend fun updateStudent(student: DomainStudent) =
        studentRepository.updateStudent(student.toDataModel())

    override suspend fun deleteStudent(student: DomainStudent) =
        studentRepository.deleteStudent(student.toDataModel())

    override suspend fun softDeleteStudent(studentId: Long, userId: Long) =
        studentRepository.softDeleteStudent(studentId, userId)

    override fun getStudentById(id: Long, userId: Long): Flow<DomainStudent?> =
        studentRepository.getStudentById(id, userId).map { it?.toDomainModel() }

    override fun getStudentByIdAny(id: Long, userId: Long): Flow<DomainStudent?> =
        studentRepository.getStudentByIdAny(id, userId).map { it?.toDomainModel() }

    override fun getAllActiveStudents(userId: Long): Flow<List<DomainStudent>> =
        studentRepository.getAllActiveStudents(userId).map { students ->
            students.map { it.toDomainModel() }
        }

    override fun getArchivedStudents(userId: Long): Flow<List<DomainStudent>> =
        studentRepository.getArchivedStudents(userId).map { students ->
            students.map { it.toDomainModel() }
        }

    override suspend fun restoreStudent(studentId: Long, userId: Long) =
        studentRepository.restoreStudent(studentId, userId)

    override suspend fun getActiveStudentCount(userId: Long): Int =
        studentRepository.getActiveStudentCount(userId)

    override suspend fun classNameExists(name: String, userId: Long): Boolean =
        studentRepository.classNameExists(name, userId)

    override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<DomainStudent> =
        studentRepository.getStudentsPaginated(userId, limit, offset).map { it.toDomainModel() }

    override suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<DomainStudent> =
        studentRepository.searchStudentsPaginated(userId, searchQuery, limit, offset).map { it.toDomainModel() }

    private fun DomainStudent.toDataModel(): Student = Student(
        id = id,
        ownerId = ownerId,
        name = name,
        surname = surname,
        parentMobile = parentMobile,
        parentEmail = parentEmail,
        className = className,
        rate = rate,
        rateType = rateType,
        isActive = isActive,
        lastModified = lastModified
    )

    private fun Student.toDomainModel(): DomainStudent = DomainStudent(
        id = id,
        ownerId = ownerId,
        name = name,
        surname = surname,
        parentMobile = parentMobile,
        parentEmail = parentEmail,
        className = className,
        rate = rate,
        rateType = rateType,
        isActive = isActive,
        lastModified = lastModified
    )
}
