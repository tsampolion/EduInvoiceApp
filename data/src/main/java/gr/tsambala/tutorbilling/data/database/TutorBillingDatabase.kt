// TutorBillingDatabase.kt - Fixed database configuration
package gr.tsambala.tutorbilling.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gr.tsambala.tutorbilling.data.dao.GroupDao
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.model.GroupStudentCrossRef
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.StudentGroup

@Database(
    entities = [Student::class, Lesson::class, StudentGroup::class, GroupStudentCrossRef::class],
    version = 11, // Current version
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = AutoMigration5To6::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11)
    ]
)
abstract class TutorBillingDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: TutorBillingDatabase? = null

        fun getDatabase(context: Context): TutorBillingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TutorBillingDatabase::class.java,
                    DatabaseConstants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
