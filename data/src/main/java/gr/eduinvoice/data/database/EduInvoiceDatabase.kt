package gr.eduinvoice.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.database.MIGRATION_12_13

@Database(
    entities = [Student::class, Lesson::class, StudentGroup::class, GroupStudentCrossRef::class, User::class],
    version = 14,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = AutoMigration5To6::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 13, to = 14)
    ]
)
abstract class EduInvoiceDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun groupDao(): GroupDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: EduInvoiceDatabase? = null

        fun getDatabase(
            context: Context,
            passphrase: ByteArray = SQLiteDatabase.getBytes("eduinvoice".toCharArray())
        ): EduInvoiceDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduInvoiceDatabase::class.java,
                    DatabaseConstants.DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration(false)
                    .addMigrations(MIGRATION_12_13)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}