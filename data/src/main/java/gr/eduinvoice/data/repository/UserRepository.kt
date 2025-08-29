package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.UserDao
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.PasswordHasher
import gr.eduinvoice.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    suspend fun createUser(user: User): Long = withContext(Dispatchers.IO) {
        val hashed = user.copy(passwordHash = PasswordHasher.hash(user.passwordHash))
        dao.insert(hashed)
    }
    
    fun getUserById(id: Long): Flow<User?> = dao.getUserById(id)
    
    suspend fun getByUsername(username: String): User? = withContext(Dispatchers.IO) { 
        dao.getByUsername(username) 
    }
    
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) { 
        dao.getAllUsers() 
    }
    
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        // Prevent changing admin role to non-admin
        val existingUser = dao.getByUsername(user.username)
        if (existingUser?.role == UserRole.ADMIN.name && user.role != UserRole.ADMIN.name) {
            throw IllegalStateException("Cannot downgrade admin user role. This is a system-critical account.")
        }
        
        // Prevent changing username of admin user
        if (existingUser?.role == UserRole.ADMIN.name && existingUser.username != user.username) {
            throw IllegalStateException("Cannot change admin username. This is a system-critical account.")
        }
        
        dao.update(user)
    }
    
    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) { 
        // Prevent deletion of admin user
        if (user.role == UserRole.ADMIN.name) {
            throw IllegalStateException("Cannot delete admin user. This is a system-critical account.")
        }
        dao.delete(user) 
    }
    
    suspend fun deleteUserById(userId: Long) = withContext(Dispatchers.IO) { 
        val user = dao.getUserById(userId).first()
        user?.let { 
            if (it.role == UserRole.ADMIN.name) {
                throw IllegalStateException("Cannot delete admin user. This is a system-critical account.")
            }
            dao.deleteById(userId)
        }
    }

    suspend fun createAdminUserIfNotExists() {
        withContext(Dispatchers.IO) {
            val existingAdmin = dao.getByUsername("admin")
            if (existingAdmin == null) {
                val adminUser = User(
                    username = "admin",
                    passwordHash = PasswordHasher.hash("admin1!"),
                    fullName = "Administrator",
                    subjectSpecialty = "System Administration",
                    yearsExperience = 0,
                    role = UserRole.ADMIN.name
                )
                dao.insert(adminUser)
            } else if (existingAdmin.role != UserRole.ADMIN.name) {
                // Update existing admin user to have ADMIN role if missing
                dao.update(existingAdmin.copy(role = UserRole.ADMIN.name))
            }
        }
    }
    
    suspend fun getUserRole(userId: Long): UserRole? = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId).first()
        user?.role?.let { UserRole.valueOf(it) }
    }
    
    suspend fun hasPermission(userId: Long, permission: gr.eduinvoice.domain.model.Permission): Boolean = withContext(Dispatchers.IO) {
        val role = getUserRole(userId)
        role?.hasPermission(permission) ?: false
    }
    
    suspend fun authenticate(username: String, password: String): User? {
        val user = withContext(Dispatchers.IO) { dao.getByUsername(username) } ?: return null
        val valid = PasswordHasher.verify(password, user.passwordHash)
        if (!valid) return null
        if (PasswordHasher.needsMigration(user.passwordHash)) {
            val updated = user.copy(passwordHash = PasswordHasher.hash(password))
            withContext(Dispatchers.IO) { dao.update(updated) }
            return updated
        }
        return user
    }

    suspend fun resetPassword(
        username: String,
        newPassword: String
    ): Boolean {
        val user = withContext(Dispatchers.IO) { dao.getByUsername(username) } ?: return false
        val updated = user.copy(passwordHash = PasswordHasher.hash(newPassword))
        withContext(Dispatchers.IO) { dao.update(updated) }
        return true
    }
}


