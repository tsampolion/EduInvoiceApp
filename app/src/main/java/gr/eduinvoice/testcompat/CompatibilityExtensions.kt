@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package gr.eduinvoice.testcompat

import gr.eduinvoice.domain.model.DomainStudent

/**
 * Legacy location kept for binary/source compatibility with older tests.
 * Delegates to the canonical DomainStudent.getFullName() implementation to avoid duplication.
 */
@Deprecated("Use DomainStudent.getFullName() directly")
fun DomainStudent.getFullName(): String = "${this.name} ${this.surname}".trim()
