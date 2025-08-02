package gr.eduinvoice.utils

import android.content.Context
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintDocumentAdapter
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
class PdfFilePrintAdapterTest {
    @Test
    fun onWriteCancelledWhenSignalPreCancelled() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = createTempFileWithData(1)
        val dest = File.createTempFile("dest", ".pdf")
        ParcelFileDescriptor.open(dest, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
            val adapter = PdfFilePrintAdapter(context, source)
            val signal = CancellationSignal().apply { cancel() }
            val callback = RecordingCallback()

            adapter.onWrite(arrayOf(PageRange.ALL_PAGES), pfd, signal, callback)

            assertTrue(callback.cancelled)
            assertFalse(callback.finished)
        }
    }

    @Test
    fun onWriteCancelledDuringCopy() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = createTempFileWithData(10)
        val dest = File.createTempFile("dest", ".pdf")
        ParcelFileDescriptor.open(dest, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
            val adapter = PdfFilePrintAdapter(context, source)
            val signal = CancellationSignal()
            val callback = RecordingCallback()
            Thread {
                Thread.sleep(10)
                signal.cancel()
            }.start()

            adapter.onWrite(arrayOf(PageRange.ALL_PAGES), pfd, signal, callback)

            assertTrue(callback.cancelled)
            assertFalse(callback.finished)
        }
    }

    private class RecordingCallback : PrintDocumentAdapter.WriteResultCallback() {
        var cancelled = false
        var finished = false

        override fun onWriteCancelled() {
            cancelled = true
        }

        override fun onWriteFinished(pages: Array<out PageRange>?) {
            finished = true
        }
    }

    private fun createTempFileWithData(sizeMb: Int): File {
        val file = File.createTempFile("src", ".pdf")
        FileOutputStream(file).use { output ->
            val buffer = ByteArray(1024 * 1024)
            repeat(sizeMb) { output.write(buffer) }
        }
        return file
    }
}

