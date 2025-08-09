package gr.eduinvoice.data.testfixtures

import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.domain.testfixtures.createTestStudent
import gr.eduinvoice.domain.testfixtures.createTestLesson
import gr.eduinvoice.domain.testfixtures.createTestGroup
import gr.eduinvoice.domain.testfixtures.createTestCrossRef

/**
 * Helper functions for database testing
 */
object DatabaseTestHelpers {

    /**
     * Gets all DAOs from a database
     */
    fun getDaos(db: EduInvoiceDatabase): Triple<StudentDao, LessonDao, GroupDao> {
        return Triple(db.studentDao(), db.lessonDao(), db.groupDao())
    }

    /**
     * Clears all data from the database
     */
    suspend fun clearDatabase(db: EduInvoiceDatabase) {
        val (studentDao, lessonDao, groupDao) = getDaos(db)
        
        // Clear in reverse dependency order
        lessonDao.getAllLessons(0L).collect { lessons ->
            lessons.forEach { lessonDao.delete(it) }
        }
        groupDao.getAllGroups(0L).collect { groups ->
            groups.forEach { groupDao.deleteGroup(it) }
        }
        studentDao.getAllActiveStudents(0L).collect { students ->
            students.forEach { studentDao.delete(it) }
        }
    }

    /**
     * Seeds the database with test data
     */
    suspend fun seedTestData(
        db: EduInvoiceDatabase,
        studentCount: Int = 3,
        groupCount: Int = 2,
        lessonsPerStudent: Int = 2,
        ownerId: Long = 1L
    ) {
        val (studentDao, lessonDao, groupDao) = getDaos(db)

        // Create students
        val students = mutableListOf<Student>()
        repeat(studentCount) { index ->
            val student = createTestStudent(
                id = (index + 1).toLong(),
                name = "Student ${index + 1}",
                ownerId = ownerId
            )
            val studentId = studentDao.insert(student)
            students.add(student.copy(id = studentId))
        }

        // Create groups
        val groups = mutableListOf<StudentGroup>()
        repeat(groupCount) { index ->
            val group = createTestGroup(
                id = (index + 1).toLong(),
                name = "Group ${index + 1}",
                ownerId = ownerId
            )
            val groupId = groupDao.insertGroup(group)
            groups.add(group.copy(id = groupId))
        }

        // Create lessons for each student
        students.forEach { student ->
            repeat(lessonsPerStudent) { lessonIndex ->
                val lesson = createTestLesson(
                    studentId = student.id,
                    ownerId = ownerId
                )
                lessonDao.insert(lesson)
            }
        }

        // Add some students to groups
        students.take(groupCount).forEachIndexed { index, student ->
            val crossRef = createTestCrossRef(
                groupId = groups[index].id,
                studentId = student.id,
                ownerId = ownerId
            )
            groupDao.insertCrossRef(crossRef)
        }
    }

    /**
     * Seeds the database with a single student
     */
    suspend fun seedSingleStudent(
        db: EduInvoiceDatabase,
        student: Student
    ): Long {
        val (studentDao, _, _) = getDaos(db)
        return studentDao.insert(student)
    }

    /**
     * Seeds the database with a single lesson
     */
    suspend fun seedSingleLesson(
        db: EduInvoiceDatabase,
        lesson: Lesson
    ): Long {
        val (_, lessonDao, _) = getDaos(db)
        return lessonDao.insert(lesson)
    }

    /**
     * Seeds the database with a single group
     */
    suspend fun seedSingleGroup(
        db: EduInvoiceDatabase,
        group: StudentGroup
    ): Long {
        val (_, _, groupDao) = getDaos(db)
        return groupDao.insertGroup(group)
    }

    /**
     * Seeds the database with a cross reference
     */
    suspend fun seedCrossRef(
        db: EduInvoiceDatabase,
        crossRef: GroupStudentCrossRef
    ) {
        val (_, _, groupDao) = getDaos(db)
        groupDao.insertCrossRef(crossRef)
    }

    /**
     * Creates a test database with seeded data
     */
    suspend fun createSeededDatabase(
        context: android.content.Context,
        studentCount: Int = 3,
        groupCount: Int = 2,
        lessonsPerStudent: Int = 2,
        ownerId: Long = 1L
    ): EduInvoiceDatabase {
        val db = TestDbFactory.createInMemory(context)
        seedTestData(db, studentCount, groupCount, lessonsPerStudent, ownerId)
        return db
    }
}
