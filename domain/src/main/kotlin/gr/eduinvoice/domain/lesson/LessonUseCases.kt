package gr.eduinvoice.domain.lesson

import javax.inject.Inject

data class LessonUseCases @Inject constructor(
    val getAllLessons: GetAllLessons,
    val getLessonById: GetLessonById,
    val getStudentLessons: GetStudentLessons,
    val getLessonsWithStudents: GetLessonsWithStudents,
    val getLessonsWithStudentsByStudentAndDateRange: GetLessonsWithStudentsByStudentAndDateRange,
    val addLesson: AddLesson,
    val addGroupLesson: AddGroupLesson,
    val addGroupLessonWithAbsences: AddGroupLessonWithAbsences,
    val editGroupLesson: EditGroupLesson,
    val updateLesson: UpdateLesson,
    val deleteLesson: DeleteLesson,
    val updateLessonPaidStatus: UpdateLessonPaidStatus,
    val updateLessonInvoicedStatus: UpdateLessonInvoicedStatus,
    val isLessonInvoiced: IsLessonInvoiced,
    val getLessonsWithStudentsPaginated: GetLessonsWithStudentsPaginated,
    val getAbsencesForStudent: GetAbsencesForStudent,
    val getGroupLessonMasters: GetGroupLessonMasters,
    val getGroupLessonMasterById: GetGroupLessonMasterById,
    val getAbsentStudentIdsForMaster: GetAbsentStudentIdsForMaster,
    val deleteGroupLesson: DeleteGroupLesson,
    val hasInvoicedOrPaidLessonsForMaster: HasInvoicedOrPaidLessonsForMaster,
    val createInvoiceMasterAndMarkLessons: CreateInvoiceMasterAndMarkLessons,
    val archiveInvoiceMaster: ArchiveInvoiceMaster,
    val deleteInvoiceMaster: DeleteInvoiceMaster
    ,
    val getInvoiceMastersByStudent: GetInvoiceMastersByStudent,
    val getInvoiceMasterById: GetInvoiceMasterById,
    val updateInvoiceMaster: UpdateInvoiceMaster
    ,
    val createPaymentBatchAndMarkLessons: CreatePaymentBatchAndMarkLessons,
    val createRescheduleMasterAndApply: CreateRescheduleMasterAndApply,
    val getRescheduleMasters: GetRescheduleMasters,
    val getEarningsByClass: GetEarningsByClass
)
