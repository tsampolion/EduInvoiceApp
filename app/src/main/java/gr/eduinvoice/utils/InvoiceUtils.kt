package gr.eduinvoice.utils

import java.io.File

fun deleteInvoice(file: File) {
    if (file.exists()) {
        file.delete()
    }
}

fun archiveInvoice(file: File) {
    if (!file.exists()) return
    val archiveDir = File(file.parentFile, "archive")
    if (!archiveDir.exists()) archiveDir.mkdirs()
    file.copyTo(File(archiveDir, file.name), overwrite = true)
    file.delete()
}

