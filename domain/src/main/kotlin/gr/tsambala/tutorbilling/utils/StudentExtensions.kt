package gr.tsambala.tutorbilling.utils

import gr.tsambala.tutorbilling.data.model.Student

/**
 * Returns the student's full name by combining the [name] and [surname] fields.
 *
 * The resulting string is trimmed so that missing surnames don't leave a trailing
 * space. Keeping this extension maintains compatibility with older code that
 * expected a `getFullName()` helper.
 */
fun Student.getFullName(): String = "${name} ${surname}".trim()

