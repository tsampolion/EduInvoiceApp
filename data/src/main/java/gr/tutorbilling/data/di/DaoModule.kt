package gr.tutorbilling.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.dao.GroupDao
import gr.tutorbilling.data.dao.StudentDao
import gr.tutorbilling.data.database.EduInvoiceDatabase

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideStudentDao(db: EduInvoiceDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideLessonDao(db: EduInvoiceDatabase): LessonDao = db.lessonDao()

    @Provides
    fun provideGroupDao(db: EduInvoiceDatabase): GroupDao = db.groupDao()
}
