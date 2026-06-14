package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Logbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportLogbooksToPdf(context: Context, studentName: String?, logbooks: List<Logbook>) {
        if (logbooks.isEmpty()) {
            Toast.makeText(context, "Tidak ada data logbook untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val writer = PageWriter(pdfDocument, 595, 842)
        
        val titleText = studentName ?: "Semua Siswa"
        writer.drawPageHeader(titleText, studentName)

        // Set up paints
        val itemTitlePaint = Paint().apply {
            color = Color.rgb(44, 62, 80) // Dark Blue-Gray
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(44, 62, 80) // Dark Slate
            textSize = 8.5f
            isAntiAlias = true
        }

        val badgeBgPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(230, 233, 237)
            strokeWidth = 0.8f
        }

        for (log in logbooks) {
            // Calculate height of this log item block
            val activityLines = wrapText("Aktivitas: ${log.activity}", 400, bodyPaint)
            val toolsText = "Tools: ${log.toolsUsed.ifBlank { "Tidak dicantumkan" }}"
            val toolsLines = wrapText(toolsText, 400, bodyPaint)
            
            val hasObstacle = log.obstacle.isNotBlank()
            val obstacleLines = if (hasObstacle) wrapText("Kendala: ${log.obstacle}", 400, bodyPaint) else emptyList()
            val solutionLines = if (hasObstacle) wrapText("Solusi: ${log.solution}", 400, bodyPaint) else emptyList()
            
            val linkText = "Link Karya: ${log.projectLink.ifBlank { "Tidak disematkan" }}"
            val linkLines = wrapText(linkText, 400, bodyPaint)

            val totalLinesCount = activityLines.size + toolsLines.size + obstacleLines.size + solutionLines.size + linkLines.size
            val cardHeight = 35f + (totalLinesCount * 12f) + 15f

            // Ensure we have space on the current page for at least 1 row header + a few lines
            writer.checkSpace(Math.min(cardHeight, 120f), titleText, studentName)

            // Draw Log Title (Date and Student Name)
            val leftSideText = "[${log.date}] - ${log.studentName} (${log.hoursPerformed} Jam)"
            writer.canvas.drawText(leftSideText, 40f, writer.y, itemTitlePaint)

            // Draw status tags (DUDI & Guru Approved) on the right
            var badgeX = 555f
            
            // 1. Teacher Approved Badge
            val teacherText = if (log.approvedByTeacher) "SEKOLAH: OK" else "SEKOLAH: -"
            val teacherColor = if (log.approvedByTeacher) Color.rgb(46, 204, 113) else Color.rgb(231, 76, 60)
            val teacherWidth = badgeTextPaint.measureText(teacherText) + 10f
            badgeX -= teacherWidth
            badgeBgPaint.color = teacherColor
            
            val teacherRect = RectF(badgeX, writer.y - 8f, badgeX + teacherWidth, writer.y + 3f)
            writer.canvas.drawRoundRect(teacherRect, 4f, 4f, badgeBgPaint)
            writer.canvas.drawText(teacherText, badgeX + 5f, writer.y - 0.5f, badgeTextPaint)

            // 2. DUDI Approved Badge
            badgeX -= 6f // space between badges
            val dudiText = if (log.approvedByDudi) "DUNIA INDUSTRI: OK" else "DUNIA INDUSTRI: -"
            val dudiColor = if (log.approvedByDudi) Color.rgb(46, 204, 113) else Color.rgb(231, 76, 60)
            val dudiWidth = badgeTextPaint.measureText(dudiText) + 10f
            badgeX -= dudiWidth
            badgeBgPaint.color = dudiColor

            val dudiRect = RectF(badgeX, writer.y - 8f, badgeX + dudiWidth, writer.y + 3f)
            writer.canvas.drawRoundRect(dudiRect, 4f, 4f, badgeBgPaint)
            writer.canvas.drawText(dudiText, badgeX + 5f, writer.y - 0.5f, badgeTextPaint)

            writer.y += 16f

            // Draw Activity Lines
            for (line in activityLines) {
                writer.checkSpace(12f, titleText, studentName)
                writer.canvas.drawText(line, 50f, writer.y, bodyPaint)
                writer.y += 11f
            }

            // Draw Tools Lines
            for (line in toolsLines) {
                writer.checkSpace(12f, titleText, studentName)
                writer.canvas.drawText(line, 50f, writer.y, bodyPaint)
                writer.y += 11f
            }

            // Draw Obstacles & Solutions if any
            if (hasObstacle) {
                for (line in obstacleLines) {
                    writer.checkSpace(12f, titleText, studentName)
                    writer.canvas.drawText(line, 50f, writer.y, bodyPaint)
                    writer.y += 11f
                }
                for (line in solutionLines) {
                    writer.checkSpace(12f, titleText, studentName)
                    writer.canvas.drawText(line, 50f, writer.y, bodyPaint)
                    writer.y += 11f
                }
            }

            // Draw Link Lines
            for (line in linkLines) {
                writer.checkSpace(12f, titleText, studentName)
                writer.canvas.drawText(line, 50f, writer.y, bodyPaint)
                writer.y += 11f
            }

            // Draw divider between items
            writer.y += 4f
            writer.checkSpace(10f, titleText, studentName)
            writer.canvas.drawLine(40f, writer.y, 555f, writer.y, dividerPaint)
            writer.y += 14f
        }

        // Add footer details on the last page: Summary or Signature Box
        writer.checkSpace(70f, titleText, studentName)
        writer.y += 10f
        
        val footerLabelPaint = Paint().apply {
            color = Color.rgb(100, 110, 120)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val stampText = "Dicetak otomatis oleh SimPKL SMKN 14 Kabupaten Tangerang pada ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID")).format(Date())}"
        writer.canvas.drawText(stampText, 40f, writer.y, footerLabelPaint)
        
        writer.y += 12f
        val totalHours = logbooks.sumOf { it.hoursPerformed }
        val approvedLogs = logbooks.count { it.approvedByDudi && it.approvedByTeacher }
        val statisticsText = "Total Entri Jurnal: ${logbooks.size} | Akumulasi Kerja: $totalHours Jam | Disetujui Penuh: $approvedLogs jurnal"
        writer.canvas.drawText(statisticsText, 40f, writer.y, footerLabelPaint)

        writer.finish()

        try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileSuffix = studentName?.replace(" ", "_") ?: "Semua_Siswa"
            val fileName = "Jurnal_SimPKL_DKV_${fileSuffix}_$dateStr.pdf"
            
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            Toast.makeText(context, "PDF Berhasil Dibuat: $fileName", Toast.LENGTH_SHORT).show()
            triggerPdfOpenIntent(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan berkas PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun wrapText(text: String, width: Int, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)
            if (textWidth > width) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    private fun triggerPdfOpenIntent(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Lampiran Jurnal Logbook PKL SMKN 14")
                putExtra(Intent.EXTRA_TEXT, "Berikut dilampirkan ringkasan jurnal harian logbook PKL DKV SMKN 14.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Simpan / Kirim Laporan PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private class PageWriter(
        val pdfDocument: PdfDocument,
        val pageWidth: Int = 595,
        val pageHeight: Int = 842
    ) {
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var y = 45f

        fun checkSpace(neededHeight: Float, titleText: String, studentName: String?) {
            if (y + neededHeight > 790f) {
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 45f
                drawPageHeader(titleText, studentName)
            }
        }

        fun drawPageHeader(titleText: String, studentName: String?) {
            val titlePaint = Paint().apply {
                color = Color.rgb(26, 82, 118) // Slate Indigo/Navy
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(127, 140, 141) // Gray italic
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            canvas.drawText("SIMPKL DKV SMKN 14 KABUPATEN TANGERANG", 40f, y, titlePaint)
            
            val pageNumText = "Halaman $pageNumber"
            val pagePaint = Paint().apply {
                color = Color.rgb(127, 140, 141)
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText(pageNumText, 555f - pagePaint.measureText(pageNumText), y, pagePaint)
            
            y += 12f
            canvas.drawText("Laporan Ringkasan Jurnal Logbook Harian: $titleText (Status Cetak Luring)", 40f, y, subtitlePaint)
            y += 7f

            val headerDividerPaint = Paint().apply {
                color = Color.rgb(200, 206, 214)
                strokeWidth = 1f
            }
            canvas.drawLine(40f, y, 555f, y, headerDividerPaint)
            y += 18f
        }

        fun finish() {
            pdfDocument.finishPage(currentPage)
        }
    }
}
