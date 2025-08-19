package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainAbsence
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAbsencesForStudent @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(studentId: Long, userId: Long): Flow<List<DomainAbsence>> =
        repository.getAbsencesForStudent(studentId, userId)
}
