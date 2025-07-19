package gr.tsambala.tutorbilling.domain.student

import javax.inject.Inject

data class StudentUseCases @Inject constructor(
    val getActiveStudents: GetActiveStudents,
    val getArchivedStudents: GetArchivedStudents,
    val getStudentById: GetStudentById,
    val insertStudent: InsertStudent,
    val updateStudent: UpdateStudent,
    val softDeleteStudent: SoftDeleteStudent,
    val restoreStudent: RestoreStudent,
    val getActiveStudentCount: GetActiveStudentCount,
    val classNameExists: ClassNameExists
)
