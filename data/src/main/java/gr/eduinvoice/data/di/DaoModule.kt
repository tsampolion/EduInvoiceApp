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

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideStudentDao(db: EduInvoiceDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideLessonDao(db: EduInvoiceDatabase): LessonDao = db.lessonDao()

    @Provides
    fun provideGroupDao(db: EduInvoiceDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideUserDao(db: EduInvoiceDatabase): UserDao = db.userDao()
}
