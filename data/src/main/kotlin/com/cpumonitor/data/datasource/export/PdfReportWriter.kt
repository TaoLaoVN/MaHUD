package com.cpumonitor.data.datasource.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

internal object PdfReportWriter {

    fun write(file: File, lines: List<String>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        var y = 40f
        val lineHeight = 18f
        lines.forEach { line ->
            if (y > pageInfo.pageHeight - 40f) return@forEach
            canvas.drawText(line, 40f, y, paint)
            y += lineHeight
        }

        document.finishPage(page)
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()
    }
}
