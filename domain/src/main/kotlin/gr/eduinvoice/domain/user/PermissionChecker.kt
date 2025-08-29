package gr.eduinvoice.domain.user

import gr.eduinvoice.domain.model.Permission
import gr.eduinvoice.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionChecker @Inject constructor() {
    
    fun hasPermission(userRole: UserRole?, permission: Permission): Boolean {
        return userRole?.hasPermission(permission) ?: false
    }
    
    fun canAccessUserManagement(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.USER_MANAGEMENT)
    }
    
    fun canAccessSystemSettings(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.SYSTEM_SETTINGS)
    }
    
    fun canAccessAllData(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.ALL_DATA_ACCESS)
    }
    
    fun canAccessBackupRestore(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.BACKUP_RESTORE)
    }
    
    fun canAccessRevenueAnalytics(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.REVENUE_ANALYTICS)
    }
    
    fun canManageStudents(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.STUDENT_MANAGEMENT)
    }
    
    fun canViewStudents(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.STUDENT_VIEW) || 
               hasPermission(userRole, Permission.STUDENT_MANAGEMENT)
    }
    
    fun canManageLessons(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.LESSON_MANAGEMENT)
    }
    
    fun canViewLessons(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.LESSON_VIEW) || 
               hasPermission(userRole, Permission.LESSON_MANAGEMENT)
    }
    
    fun canManageGroups(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.GROUP_MANAGEMENT)
    }
    
    fun canViewGroups(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.GROUP_VIEW) || 
               hasPermission(userRole, Permission.GROUP_MANAGEMENT)
    }
    
    fun canManageInvoices(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.INVOICE_MANAGEMENT)
    }
    
    fun canViewInvoices(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.INVOICE_VIEW) || 
               hasPermission(userRole, Permission.INVOICE_MANAGEMENT)
    }
    
    fun canAccessOwnData(userRole: UserRole?): Boolean {
        return hasPermission(userRole, Permission.OWN_DATA_ACCESS)
    }
    
    fun isAdmin(userRole: UserRole?): Boolean {
        return userRole == UserRole.ADMIN
    }
    
    fun isTeacher(userRole: UserRole?): Boolean {
        return userRole == UserRole.TEACHER || userRole == UserRole.ADMIN
    }
    
    fun isAssistant(userRole: UserRole?): Boolean {
        return userRole == UserRole.ASSISTANT || userRole == UserRole.TEACHER || userRole == UserRole.ADMIN
    }
}
