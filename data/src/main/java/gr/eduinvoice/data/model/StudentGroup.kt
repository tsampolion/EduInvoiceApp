package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import gr.eduinvoice.data.database.DatabaseConstants

@Serializable
@Entity(tableName = DatabaseConstants.GROUPS_TABLE)
data class StudentGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val ownerId: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "''")
    val className: String = "",
    @ColumnInfo(defaultValue = "0.0")
    val rate: Double = 0.0,
    @ColumnInfo(defaultValue = "'hourly'")
    val rateType: String = "hourly",
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = System.currentTimeMillis()
)
