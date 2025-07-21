package gr.tsambala.tutorbilling.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import gr.tsambala.tutorbilling.data.database.DatabaseConstants

@Entity(tableName = DatabaseConstants.GROUPS_TABLE)
data class StudentGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
