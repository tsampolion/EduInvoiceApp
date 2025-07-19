// Student.kt - Fixed data model
package gr.tsambala.tutorbilling.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "''")
    val surname: String,
    @ColumnInfo(defaultValue = "''")
    val parentMobile: String,
    @ColumnInfo(defaultValue = "NULL")
    val parentEmail: String? = null,
    @ColumnInfo(defaultValue = "''")
    val className: String,
    val rate: Double,
    @ColumnInfo(defaultValue = "'hourly'")
    val rateType: String = RateTypes.HOURLY,
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true // Default to active
)
