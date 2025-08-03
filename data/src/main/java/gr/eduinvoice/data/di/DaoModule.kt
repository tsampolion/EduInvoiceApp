package gr.eduinvoice.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.database.EduInvoiceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    @Singleton
    fun provideStudentDao(db: EduInvoiceDatabase): StudentDao = db.studentDao()

    @Provides
    @Singleton
    fun provideLessonDao(db: EduInvoiceDatabase): LessonDao = db.lessonDao()

    @Provides
    @Singleton
    fun provideGroupDao(db: EduInvoiceDatabase): GroupDao = db.groupDao()

    @Provides
    @Singleton
    fun provideUserDao(db: EduInvoiceDatabase): UserDao = db.userDao()
}
