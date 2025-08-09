package gr.eduinvoice.testcompat

import gr.eduinvoice.data.model.Student

/**
 * Compatibility extensions for data layer models
 * Provides missing functions that tests expect
 */

fun Student.getFullName(): String = "${name} ${surname}".trim()
