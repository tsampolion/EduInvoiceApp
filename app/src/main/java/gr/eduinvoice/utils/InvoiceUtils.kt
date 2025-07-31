package gr.eduinvoice.utils

import java.io.File

fun deleteInvoice(file: File): Boolean {
    if (!file.exists()) return true
    val deleted = file.delete()
    if (!deleted) {
        android.util.Log.e("InvoiceUtils", "Failed to delete invoice ${file.absolutePath}")
    }
    return deleted
}

fun archiveInvoice(file: File): Boolean {
    if (!file.exists()) return false
    val archiveDir = File(file.parentFile, "archive")
    if (!archiveDir.exists() && !archiveDir.mkdirs()) {
        android.util.Log.e("InvoiceUtils", "Failed to create archive dir ${archiveDir.absolutePath}")
        return false
    }
    return try {
        file.copyTo(File(archiveDir, file.name), overwrite = true)
        val deleted = file.delete()
        if (!deleted) {
            android.util.Log.e("InvoiceUtils", "Failed to delete original invoice ${file.absolutePath}")
        }
        deleted
    } catch (e: Exception) {
        android.util.Log.e("InvoiceUtils", "Failed to archive invoice ${file.absolutePath}", e)
        false
    }
}

