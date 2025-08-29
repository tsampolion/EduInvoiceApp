package gr.eduinvoice.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val displayName: String, val permissions: Set<Permission>) {
    ADMIN(
        displayName = "Administrator",
        permissions = setOf(
            Permission.USER_MANAGEMENT,
            Permission.SYSTEM_SETTINGS,
            Permission.ALL_DATA_ACCESS,
            Permission.BACKUP_RESTORE,
            Permission.REVENUE_ANALYTICS
        )
    ),
    TEACHER(
        displayName = "Teacher",
        permissions = setOf(
            Permission.STUDENT_MANAGEMENT,
            Permission.LESSON_MANAGEMENT,
            Permission.GROUP_MANAGEMENT,
            Permission.INVOICE_MANAGEMENT,
            Permission.OWN_DATA_ACCESS
        )
    ),
    ASSISTANT(
        displayName = "Assistant",
        permissions = setOf(
            Permission.STUDENT_VIEW,
            Permission.LESSON_VIEW,
            Permission.GROUP_VIEW,
            Permission.INVOICE_VIEW,
            Permission.OWN_DATA_ACCESS
        )
    );

    fun hasPermission(permission: Permission): Boolean = permissions.contains(permission)
    
    fun isAdmin(): Boolean = this == ADMIN
    fun isTeacher(): Boolean = this == TEACHER || this == ADMIN
    fun isAssistant(): Boolean = this == ASSISTANT || this == TEACHER || this == ADMIN
}

@Serializable
enum class Permission {
    USER_MANAGEMENT,      // Create, edit, delete users
    SYSTEM_SETTINGS,      // App configuration, themes, etc.
    ALL_DATA_ACCESS,      // Access to all users' data
    BACKUP_RESTORE,       // Database backup and restore
    REVENUE_ANALYTICS,   // Financial reports and analytics
    STUDENT_MANAGEMENT,   // Full student CRUD operations
    LESSON_MANAGEMENT,    // Full lesson CRUD operations
    GROUP_MANAGEMENT,     // Full group CRUD operations
    INVOICE_MANAGEMENT,   // Full invoice CRUD operations
    STUDENT_VIEW,         // Read-only student access
    LESSON_VIEW,          // Read-only lesson access
    GROUP_VIEW,           // Read-only group access
    INVOICE_VIEW,         // Read-only invoice access
    OWN_DATA_ACCESS       // Access to own user data
}
