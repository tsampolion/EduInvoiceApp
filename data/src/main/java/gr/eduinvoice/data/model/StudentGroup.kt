package gr.eduinvoice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import gr.eduinvoice.data.database.DatabaseConstants

@Serializable
@Entity(tableName = DatabaseConstants.GROUPS_TABLE)
data class StudentGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long = 0,
    val name: String,
    val className: String = "",
    val rate: Double = 0.0,
    val rateType: String = RateTypes.HOURLY,
    val isActive: Boolean = true
)
