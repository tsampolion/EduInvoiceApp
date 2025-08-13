package gr.eduinvoice.testcompat

import gr.eduinvoice.domain.model.DomainStudent

/**
 * Compatibility extensions for data layer models
 * Provides missing functions that tests expect
 */

fun DomainStudent.getFullName(): String = "${name} ${surname}".trim()
