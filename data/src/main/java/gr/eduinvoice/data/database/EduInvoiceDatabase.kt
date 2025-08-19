package gr.eduinvoice.data.database

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.database.MIGRATION_12_13
import gr.eduinvoice.data.database.MIGRATION_13_15
import gr.eduinvoice.data.database.MIGRATION_14_15
import gr.eduinvoice.data.database.MIGRATION_15_16
import gr.eduinvoice.data.database.MIGRATION_18_19
import gr.eduinvoice.data.database.MIGRATION_19_20
import gr.eduinvoice.data.database.MIGRATION_20_21
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.model.GroupLessonMaster
import gr.eduinvoice.data.model.GroupLessonAbsence
import gr.eduinvoice.data.model.InvoiceMaster
import gr.eduinvoice.data.model.PaymentBatchMaster
import gr.eduinvoice.data.model.RescheduleMaster
import gr.eduinvoice.data.model.RescheduleMasterLessonLink
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Student::class, Lesson::class, StudentGroup::class, GroupStudentCrossRef::class, User::class, GroupLessonMaster::class, GroupLessonAbsence::class, InvoiceMaster::class, PaymentBatchMaster::class, RescheduleMaster::class, RescheduleMasterLessonLink::class],
    version = 21,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = AutoMigration5To6::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15)
    ]
)
abstract class EduInvoiceDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun groupDao(): GroupDao
    abstract fun userDao(): UserDao

    companion object {
        private const val TAG = "EduInvoiceDatabase"

        @Volatile
        private var INSTANCE: EduInvoiceDatabase? = null

        fun getDatabase(
            context: Context,
            passphrase: ByteArray
        ): EduInvoiceDatabase {
            if (passphrase.isEmpty() || passphrase.all { it == 0.toByte() }) {
                Log.e(TAG, "Invalid database passphrase")
                throw IllegalArgumentException("Invalid database passphrase")
            }
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduInvoiceDatabase::class.java,
                    DatabaseConstants.DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration(false)
                    .addMigrations(
                        MIGRATION_12_13,
                        MIGRATION_13_15,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
