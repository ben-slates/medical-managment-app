package com.medical.management.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.medical.management.data.model.Bill
import com.medical.management.data.model.Treatment
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun bill(context: Context, bill: Bill): File {
        val lines = listOf(
            "Medical Management System",
            "Bill",
            "Patient: ${bill.patientName}",
            "Doctor: ${bill.doctorName}",
            "Date: ${bill.generatedDate}",
            "Services: ${bill.services}",
            "Description: ${bill.description}",
            "Consultation: ${bill.consultationFee}",
            "Medicine: ${bill.medicineFee}",
            "Tests: ${bill.testsFee}",
            "Total: ${bill.amount}"
        )
        return write(context, "bill_${bill.billId.ifBlank { System.currentTimeMillis().toString() }}.pdf", lines)
    }

    fun prescription(context: Context, treatment: Treatment): File {
        val lines = listOf(
            "Medical Management System",
            "Prescription",
            "Patient: ${treatment.patientName}",
            "Doctor: ${treatment.doctorName}",
            "Date: ${treatment.treatmentDate}",
            "Disease: ${treatment.disease}",
            "Diagnosis: ${treatment.diagnosis}",
            "Prescription: ${treatment.prescription}",
            "Progress: ${treatment.progress}",
            "Follow-up: ${treatment.followUpDate}"
        )
        return write(context, "prescription_${treatment.treatmentId.ifBlank { System.currentTimeMillis().toString() }}.pdf", lines)
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    private fun write(context: Context, fileName: String, lines: List<String>): File {
        val outputDir = File(context.getExternalFilesDir(null), "pdf").apply { mkdirs() }
        val file = File(outputDir, fileName)
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 18f }
        var y = 72f
        lines.forEachIndexed { index, line ->
            paint.textSize = if (index == 0) 24f else 16f
            paint.isFakeBoldText = index < 2
            page.canvas.drawText(line.take(85), 48f, y, paint)
            y += if (index == 0) 38f else 28f
        }
        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
