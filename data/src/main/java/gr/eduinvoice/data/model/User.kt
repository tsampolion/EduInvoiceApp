package gr.eduinvoice.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import gr.eduinvoice.data.database.DatabaseConstants

@Entity(
    tableName = DatabaseConstants.USERS_TABLE,
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String
)
