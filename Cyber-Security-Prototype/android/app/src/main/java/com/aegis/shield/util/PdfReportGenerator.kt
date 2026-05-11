package com.aegis.shield.util

import android.content.ContentResolver
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.aegis.shield.data.ThreatEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
    fun generate(
        contentResolver: ContentResolver,
        uri: Uri,
        threats: List<ThreatEntity>,
        totalBlocked: Int,
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val sectionPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = 11f }

        var y = 48f
        canvas.drawText("Aegis Threat Analytics Report", 32f, y, headerPaint)
        y += 22f
        val generatedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        canvas.drawText("Generated: $generatedAt", 32f, y, bodyPaint)

        y += 34f
        canvas.drawText("Summary", 32f, y, sectionPaint)
        y += 20f
        canvas.drawText("Total threats blocked: $totalBlocked", 32f, y, bodyPaint)
        canvas.drawText("Total records: ${threats.size}", 280f, y, bodyPaint)

        y += 34f
        canvas.drawText("Details", 32f, y, sectionPaint)
        y += 20f
        canvas.drawText("Type", 32f, y, bodyPaint.apply { isFakeBoldText = true })
        canvas.drawText("Risk Score", 170f, y, bodyPaint)
        canvas.drawText("Date", 280f, y, bodyPaint)
        bodyPaint.isFakeBoldText = false

        y += 14f
        threats.forEach { threat ->
            if (y > 810f) return@forEach
            val date = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(threat.timestamp))
            canvas.drawText(threat.type, 32f, y, bodyPaint)
            canvas.drawText("${threat.score}", 170f, y, bodyPaint)
            canvas.drawText(date, 280f, y, bodyPaint)
            y += 16f
        }

        document.finishPage(page)
        contentResolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        }
        document.close()
    }

    fun generateCertInThreatSubmission(
        contentResolver: ContentResolver,
        uri: Uri,
        url: String,
        threatIntelSources: List<String>,
        detectedElements: List<String>,
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val sectionPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = 11f }

        var y = 48f
        canvas.drawText("CERT-In Threat Submission", 32f, y, headerPaint)
        y += 22f
        val generatedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        canvas.drawText("Generated: $generatedAt", 32f, y, bodyPaint)

        y += 34f
        canvas.drawText("Submission", 32f, y, sectionPaint)
        y += 20f
        canvas.drawText(
            "CERT-In Threat Submission. The following URL has been identified as a phishing/scam vector:",
            32f,
            y,
            bodyPaint,
        )
        y += 18f
        canvas.drawText(url, 32f, y, bodyPaint.apply { isFakeBoldText = true })
        bodyPaint.isFakeBoldText = false

        y += 34f
        canvas.drawText("Threat Intelligence (Mock)", 32f, y, sectionPaint)
        y += 20f
        if (threatIntelSources.isEmpty()) {
            canvas.drawText("No sources provided.", 32f, y, bodyPaint)
            y += 16f
        } else {
            threatIntelSources.forEach { src ->
                if (y > 810f) return@forEach
                canvas.drawText("• Flagged by $src", 32f, y, bodyPaint)
                y += 16f
            }
        }

        y += 18f
        canvas.drawText("Detected Elements (Mock)", 32f, y, sectionPaint)
        y += 20f
        if (detectedElements.isEmpty()) {
            canvas.drawText("No elements detected.", 32f, y, bodyPaint)
            y += 16f
        } else {
            detectedElements.forEach { el ->
                if (y > 810f) return@forEach
                canvas.drawText("• $el", 32f, y, bodyPaint)
                y += 16f
            }
        }

        document.finishPage(page)
        contentResolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        }
        document.close()
    }
}
