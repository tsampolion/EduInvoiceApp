package gr.eduinvoice.data.repository

import android.database.Cursor
import androidx.room.withTransaction
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val db: EduInvoiceDatabase
) {
    @Serializable
    data class BackupDump(
        val students: List<Student>,
        val lessons: List<Lesson>,
        val groups: List<StudentGroup>,
        val crossRefs: List<GroupStudentCrossRef>,
        val users: List<User>
    )

    suspend fun exportJson(): String {
        val readable = db.openHelper.readableDatabase
        val students = readable.query("SELECT * FROM ${DatabaseConstants.STUDENTS_TABLE}").use { cursor ->
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .map { Student(
                    id = it.getLong(0),
                    ownerId = it.getLong(1),
                    name = it.getString(2),
                    surname = it.getString(3),
                    parentMobile = it.getString(4),
                    parentEmail = it.getString(5),
                    className = it.getString(6),
                    rate = it.getDouble(7),
                    rateType = it.getString(8),
                    isActive = it.getInt(9) == 1
                ) }.toList()
        }
        val lessons = readable.query("SELECT * FROM ${DatabaseConstants.LESSONS_TABLE}").use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { Lesson(
                    id = it.getLong(0),
                    ownerId = it.getLong(1),
                    studentId = it.getLong(2),
                    groupId = if (it.isNull(3)) null else it.getLong(3),
                    date = it.getString(4),
                    startTime = it.getString(5),
                    durationMinutes = it.getInt(6),
                    notes = it.getString(7),
                    isPaid = it.getInt(8) == 1,
                    isInvoiced = it.getInt(9) == 1
                ) }.toList()
        }
        val groups = readable.query("SELECT * FROM ${DatabaseConstants.GROUPS_TABLE}").use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { StudentGroup(
                    id = it.getLong(0),
                    ownerId = it.getLong(1),
                    name = it.getString(2)
                ) }.toList()
        }
        val crossRefs = readable.query("SELECT * FROM ${DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}").use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { GroupStudentCrossRef(
                    groupId = it.getLong(0),
                    studentId = it.getLong(1),
                    ownerId = it.getLong(2)
                ) }.toList()
        }
        val users = readable.query("SELECT * FROM ${DatabaseConstants.USERS_TABLE}").use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { User(
                    id = it.getLong(0),
                    username = it.getString(1),
                    passwordHash = it.getString(2),
                    fullName = it.getString(3),
                    subjectSpecialty = it.getString(4),
                    yearsExperience = it.getInt(5)
                ) }.toList()
        }
        val dump = BackupDump(students, lessons, groups, crossRefs, users)
        return Json.encodeToString(BackupDump.serializer(), dump)
    }

    suspend fun restoreFromJson(json: String): Result<Unit> {
        return try {
            val element = Json.parseToJsonElement(json).jsonObject
            val required = listOf("students", "lessons", "groups", "crossRefs", "users")
            if (!required.all { element.containsKey(it) }) {
                return Result.failure(IllegalArgumentException("Backup JSON missing required fields"))
            }
            val dump = Json.decodeFromString(BackupDump.serializer(), json)
            db.withTransaction {
                db.clearAllTables()
                val studentDao = db.studentDao()
                val lessonDao = db.lessonDao()
                val groupDao = db.groupDao()
                val userDao = db.userDao()
                dump.users.forEach { userDao.insert(it) }
                dump.students.forEach { studentDao.insert(it) }
                dump.groups.forEach { groupDao.insertGroup(it) }
                dump.crossRefs.forEach { groupDao.insertCrossRef(it) }
                dump.lessons.forEach { lessonDao.insert(it) }
            }
            Result.success(Unit)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.failure(e)
        }
    }
}
