package gr.eduinvoice.data.database

import android.content.Context
import android.util.Log
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
    version = 1,
    exportSchema = true
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
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .setQueryExecutor(queryExecutor)
                    .setTransactionExecutor(transactionExecutor)
                    .fallbackToDestructiveMigration(gr.eduinvoice.data.BuildConfig.DEBUG)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
