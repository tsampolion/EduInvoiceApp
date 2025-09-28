package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "reschedule_master",
    indices = [
        Index(value = ["ownerId", "newDate", "newStartTime"], name = "idx_reschedule_owner_date_time")
    ]
)
data class RescheduleMaster(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val ownerId: Long = 0,
    val title: String? = null,
    val newDate: String, // Store as ISO date string (yyyy-MM-dd)
    val newStartTime: String, // Store as time string (HH:mm)
    val newDurationMinutes: Int,
    val notes: String? = null,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = System.currentTimeMillis()
)
