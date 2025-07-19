package gr.tsambala.tutorbilling.utils

import gr.tsambala.tutorbilling.data.model.Student

/**
 * Returns the student's full name.
 *
 * The current [Student] model only stores a single `name` field, so this
 * simply returns that value. Having this extension keeps compatibility with
 * older code that expected a `getFullName()` helper.
 */
fun Student.getFullName(): String = this.name

