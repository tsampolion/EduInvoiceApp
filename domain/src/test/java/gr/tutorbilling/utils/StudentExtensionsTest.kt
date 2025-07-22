package gr.tutorbilling.utils

import gr.tutorbilling.data.model.Student
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentExtensionsTest {
    @Test
    fun getFullNameConcatenatesNameAndSurname() {
        val student = Student(id = 1, name = "Alice", surname = "Smith", parentMobile = "", className = "", rate = 10.0)
        assertEquals("Alice Smith", student.getFullName())
    }

    @Test
    fun getFullNameTrimsWhenSurnameMissing() {
        val student = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        assertEquals("Alice", student.getFullName())
    }
}
