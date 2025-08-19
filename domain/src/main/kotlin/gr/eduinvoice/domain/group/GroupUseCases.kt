package gr.eduinvoice.domain.group

import javax.inject.Inject

data class GroupUseCases @Inject constructor(
    val insertGroup: InsertGroup,
    val updateGroup: UpdateGroup,
    val deleteGroup: DeleteGroup,
    val archiveGroup: ArchiveGroup,
    val getAllGroups: GetAllGroups,
    val getGroupById: GetGroupById,
    val getStudentGroups: GetStudentGroups,
    val addStudentToGroup: AddStudentToGroup,
    val removeStudentFromGroup: RemoveStudentFromGroup,
    val getGroupStudents: GetGroupStudents
)
