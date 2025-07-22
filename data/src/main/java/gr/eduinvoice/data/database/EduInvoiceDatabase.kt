// EduInvoiceDatabase.kt - Fixed database configuration
package gr.eduinvoice.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup

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
abstract class EduInvoiceDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: EduInvoiceDatabase? = null

        fun getDatabase(context: Context): EduInvoiceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduInvoiceDatabase::class.java,
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
