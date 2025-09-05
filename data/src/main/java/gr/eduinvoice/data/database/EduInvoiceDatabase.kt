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
import java.util.concurrent.Executors

@Database(
    entities = [Student::class, Lesson::class, StudentGroup::class, GroupStudentCrossRef::class, User::class, GroupLessonMaster::class, GroupLessonAbsence::class, InvoiceMaster::class, PaymentBatchMaster::class, RescheduleMaster::class, RescheduleMasterLessonLink::class],
    version = 22,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = AutoMigration5To6::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13, spec = AutoMigration12To13::class),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16, spec = AutoMigration15To16::class),
        AutoMigration(from = 16, to = 17, spec = AutoMigration16To17::class),
        AutoMigration(from = 17, to = 18, spec = AutoMigration17To18::class),
        AutoMigration(from = 18, to = 19, spec = AutoMigration18To19::class),
        AutoMigration(from = 19, to = 20, spec = AutoMigration19To20::class),
        AutoMigration(from = 20, to = 21, spec = AutoMigration20To21::class),
        AutoMigration(from = 21, to = 22, spec = AutoMigration21To22::class)
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
                val queryExecutor = Executors.newFixedThreadPool(4)
                val transactionExecutor = Executors.newSingleThreadExecutor()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduInvoiceDatabase::class.java,
                    DatabaseConstants.DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .setQueryExecutor(queryExecutor)
                    .setTransactionExecutor(transactionExecutor)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
