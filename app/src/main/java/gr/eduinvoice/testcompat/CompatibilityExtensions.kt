package gr.eduinvoice.testcompat

import gr.eduinvoice.domain.model.DomainStudent

/**
 * Legacy location kept for binary/source compatibility with older tests.
 * Delegates to the canonical DomainStudent.getFullName() implementation to avoid duplication.
 */

fun DomainStudent.getFullName(): String = this.getFullName()
