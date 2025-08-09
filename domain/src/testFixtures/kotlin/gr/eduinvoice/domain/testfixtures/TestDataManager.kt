package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Test data manager for managing test data across repositories
 * Provides helper methods for seeding and managing test data
 */
class TestDataManager {
    private val studentRepository = FakeStudentRepository()
    private val lessonRepository = FakeLessonRepository()
    
    // State flows for easy access to current data
    val studentFlow: StateFlow<List<Student>> = MutableStateFlow(emptyList())
    val lessonFlow: StateFlow<List<Lesson>> = MutableStateFlow(emptyList())
    val groupFlow: StateFlow<List<StudentGroup>> = MutableStateFlow(emptyList())
    val crossRefFlow: StateFlow<List<GroupStudentCrossRef>> = MutableStateFlow(emptyList())

    /**
     * Add a student to the test data
     */
    fun addStudent(student: Student) {
        studentRepository.addStudent(student)
        updateStudentFlow()
    }

    /**
     * Add a lesson to the test data
     */
    fun addLesson(lesson: Lesson) {
        lessonRepository.addLesson(lesson)
        updateLessonFlow()
    }

    /**
     * Add a group to the test data
     */
    fun addGroup(group: StudentGroup) {
        val currentGroups = groupFlow.value.toMutableList()
        currentGroups.add(group.copy(id = if (group.id == 0L) currentGroups.size.toLong() + 1 else group.id))
        (groupFlow as MutableStateFlow).value = currentGroups
    }

    /**
     * Add a cross reference to the test data
     */
    fun addCrossRef(crossRef: GroupStudentCrossRef) {
        val currentCrossRefs = crossRefFlow.value.toMutableList()
        currentCrossRefs.add(crossRef)
        (crossRefFlow as MutableStateFlow).value = currentCrossRefs
    }

    /**
     * Add a student to a group
     */
    fun addStudentToGroup(groupId: Long, studentId: Long, ownerId: Long = 1L) {
        val crossRef = createTestCrossRef(groupId, studentId, ownerId)
        addCrossRef(crossRef)
    }

    /**
     * Remove a student from a group
     */
    fun removeStudentFromGroup(groupId: Long, studentId: Long, ownerId: Long = 1L) {
        val currentCrossRefs = crossRefFlow.value.toMutableList()
        currentCrossRefs.removeAll { 
            it.groupId == groupId && it.studentId == studentId && it.ownerId == ownerId 
        }
        (crossRefFlow as MutableStateFlow).value = currentCrossRefs
    }

    /**
     * Get group-student relations
     */
    fun getGroupStudentRelations(): List<Pair<Long, Long>> {
        return crossRefFlow.value.map { it.groupId to it.studentId }
    }

    /**
     * Get students for a specific group
     */
    fun getStudentsForGroup(groupId: Long): List<Student> {
        val studentIds = crossRefFlow.value
            .filter { it.groupId == groupId }
            .map { it.studentId }
        return studentFlow.value.filter { it.id in studentIds }
    }

    /**
     * Get groups for a specific student
     */
    fun getGroupsForStudent(studentId: Long): List<StudentGroup> {
        val groupIds = crossRefFlow.value
            .filter { it.studentId == studentId }
            .map { it.groupId }
        return groupFlow.value.filter { it.id in groupIds }
    }

    /**
     * Get lessons for a specific student
     */
    fun getLessonsForStudent(studentId: Long): List<Lesson> {
        return lessonFlow.value.filter { it.studentId == studentId }
    }

    /**
     * Get lessons for a specific group
     */
    fun getLessonsForGroup(groupId: Long): List<Lesson> {
        return lessonFlow.value.filter { it.groupId == groupId }
    }

    /**
     * Clear all test data
     */
    fun clear() {
        studentRepository.clear()
        lessonRepository.clear()
        (studentFlow as MutableStateFlow).value = emptyList()
        (lessonFlow as MutableStateFlow).value = emptyList()
        (groupFlow as MutableStateFlow).value = emptyList()
        (crossRefFlow as MutableStateFlow).value = emptyList()
    }

    /**
     * Seed with default test data
     */
    fun seedDefaultData(ownerId: Long = 1L) {
        val dataset = createCompleteTestDataset(
            studentCount = 3,
            groupCount = 2,
            lessonsPerStudent = 2,
            ownerId = ownerId
        )
        
        dataset.students.forEach { addStudent(it) }
        dataset.groups.forEach { addGroup(it) }
        dataset.lessons.forEach { addLesson(it) }
        dataset.crossRefs.forEach { addCrossRef(it) }
    }

    /**
     * Get the fake repositories for use in tests
     */
    fun getStudentRepository(): FakeStudentRepository = studentRepository
    fun getLessonRepository(): FakeLessonRepository = lessonRepository

    private fun updateStudentFlow() {
        (studentFlow as MutableStateFlow).value = studentRepository.getAllStudents()
    }

    private fun updateLessonFlow() {
        (lessonFlow as MutableStateFlow).value = lessonRepository.getAllLessons()
    }
}
