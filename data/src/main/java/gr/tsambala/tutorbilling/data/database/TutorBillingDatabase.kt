// TutorBillingDatabase.kt - Fixed database configuration
package gr.tsambala.tutorbilling.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gr.tsambala.tutorbilling.BuildConfig
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student

@Database(
    entities = [Student::class, Lesson::class],
    version = 9, // Current version
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = AutoMigration5To6::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9)
    ]
)
abstract class TutorBillingDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao

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
                    // Fallback to destructive migration only in debug builds
                    .apply {
                        if (BuildConfig.DEBUG) {
                            fallbackToDestructiveMigration(false)
                        }
                    }
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
