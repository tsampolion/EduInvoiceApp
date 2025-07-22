package gr.tutorbilling.domain.group

import javax.inject.Inject

data class GroupUseCases @Inject constructor(
    val insertGroup: InsertGroup,
    val updateGroup: UpdateGroup,
    val deleteGroup: DeleteGroup,
    val getAllGroups: GetAllGroups,
    val getGroupById: GetGroupById,
    val addStudentToGroup: AddStudentToGroup,
    val removeStudentFromGroup: RemoveStudentFromGroup,
    val getGroupStudents: GetGroupStudents
)
