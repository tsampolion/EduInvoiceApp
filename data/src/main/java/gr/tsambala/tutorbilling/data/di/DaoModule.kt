package gr.tsambala.tutorbilling.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.GroupDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.database.TutorBillingDatabase

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideStudentDao(db: TutorBillingDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideLessonDao(db: TutorBillingDatabase): LessonDao = db.lessonDao()

    @Provides
    fun provideGroupDao(db: TutorBillingDatabase): GroupDao = db.groupDao()
}
