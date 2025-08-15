package gr.eduinvoice.data.dao

import androidx.room.*
import gr.eduinvoice.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.USERS_TABLE} WHERE id = :id")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.USERS_TABLE} WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): User?

    @Query("DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.USERS_TABLE} WHERE id = :id")
    suspend fun deleteById(id: Long)
}
