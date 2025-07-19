package gr.tsambala.tutorbilling.data.model

/**
 * Extension function to calculate lesson fee based on student's rate
 */
fun Lesson.calculateFee(student: Student): Double {
    return (durationMinutes / 60.0) * student.rate
}
