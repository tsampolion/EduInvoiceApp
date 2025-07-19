package gr.tsambala.tutorbilling.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.database.TutorBillingDatabase
import gr.tsambala.tutorbilling.data.repository.StudentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTutorBillingDatabase(
        @ApplicationContext context: Context
    ): TutorBillingDatabase {
        return TutorBillingDatabase.getDatabase(context)
    }

    @Provides
    fun provideStudentDao(database: TutorBillingDatabase): StudentDao {
        return database.studentDao()
    }

    @Provides
    fun provideLessonDao(database: TutorBillingDatabase): LessonDao {
        return database.lessonDao()
    }

    @Provides
    @Singleton
    fun provideStudentRepository(studentDao: StudentDao): StudentRepository {
        return StudentRepository(studentDao)
    }
}
