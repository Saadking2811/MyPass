package com.example.myapplication.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.myapplication.model.BoardingPass
import java.io.IOException

/**
 * Renders a [BoardingPass] to a single-page A4 PDF and writes it to the
 * MediaStore Downloads collection. The design mirrors the in-app pass card
 * (emerald header, large IATA codes, 3×3 info grid, QR section, footer strip).
 */
object BoardingPassPdfGenerator {

    /**
     * Build and persist the PDF.
     * @return `Pair(fileName, contentUri)` so the caller can immediately share or view it.
     */
    fun save(context: Context, boardingPass: BoardingPass): Pair<String, Uri> {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val emerald     = Color.parseColor("#004D2F")
        val emeraldDark = Color.parseColor("#003520")
        val white       = Color.WHITE
        val charcoal    = Color.parseColor("#0F172A")
        val gray        = Color.parseColor("#475569")
        val grayMuted   = Color.parseColor("#94A3B8")
        val divider     = Color.parseColor("#E2E8F0")
        val cream       = Color.parseColor("#F8F8F6")

        canvas.drawColor(white)

        // Header
        val headerH = 110f
        canvas.drawRect(0f, 0f, 595f, headerH, Paint().apply { color = emerald })

        // Brand mark
        val markX = 32f; val markY = 32f; val markSize = 46f
        canvas.drawRoundRect(
            RectF(markX, markY, markX + markSize, markY + markSize),
            9f, 9f,
            Paint().apply { color = white; isAntiAlias = true }
        )
        val planePaint = Paint().apply { color = emerald; isAntiAlias = true }
        val planePath = Path()
        val pcx = markX + markSize / 2
        val pcy = markY + markSize / 2
        val matrix = Matrix().apply { setRotate(-30f, pcx, pcy) }
        planePath.addRect(pcx - 1.5f, pcy - 11f, pcx + 1.5f, pcy + 11f, Path.Direction.CW)
        planePath.moveTo(pcx - 2f, pcy - 2f); planePath.lineTo(pcx - 13f, pcy + 5f)
        planePath.lineTo(pcx - 13f, pcy + 7f); planePath.lineTo(pcx + 13f, pcy + 7f)
        planePath.lineTo(pcx + 13f, pcy + 5f); planePath.lineTo(pcx + 2f, pcy - 2f)
        planePath.close()
        planePath.moveTo(pcx - 5f, pcy + 7f); planePath.lineTo(pcx - 5f, pcy + 9f)
        planePath.lineTo(pcx + 5f, pcy + 9f); planePath.lineTo(pcx + 5f, pcy + 7f); planePath.close()
        planePath.transform(matrix)
        canvas.drawPath(planePath, planePaint)

        canvas.drawText(
            "MyPass",
            markX + markSize + 14f, markY + 24f,
            Paint().apply { color = white; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        )
        canvas.drawText(
            "BOARDING PASS",
            markX + markSize + 14f, markY + 42f,
            Paint().apply {
                color = Color.argb(220, 255, 255, 255)
                textSize = 9f; isAntiAlias = true; letterSpacing = 0.20f; isFakeBoldText = true
            }
        )

        val rightPaint = Paint().apply {
            color = white; textSize = 11f; isAntiAlias = true; letterSpacing = 0.18f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("FLIGHT", 565f, markY + 8f, rightPaint)
        canvas.drawText(
            boardingPass.flightNumber, 565f, markY + 36f,
            Paint().apply {
                color = white; textSize = 26f; isFakeBoldText = true; isAntiAlias = true
                textAlign = Paint.Align.RIGHT
            }
        )

        // Route
        val labelPaint = Paint().apply {
            color = gray; textSize = 9f; isAntiAlias = true; letterSpacing = 0.18f; isFakeBoldText = true
        }
        val cityPaint = Paint().apply {
            color = gray; textSize = 11f; isAntiAlias = true; letterSpacing = 0.10f
        }
        val timePaint = Paint().apply {
            color = charcoal; textSize = 16f; isFakeBoldText = true; isAntiAlias = true
        }
        val codePaint = Paint().apply {
            color = emerald; textSize = 56f; isFakeBoldText = true; isAntiAlias = true
        }

        var y = headerH + 50f
        canvas.drawText("FROM", 32f, y, labelPaint)
        canvas.drawText("TO", 470f, y, labelPaint)
        y += 50f
        canvas.drawText(boardingPass.origin, 32f, y, codePaint)
        canvas.drawText(boardingPass.destination, 470f, y, codePaint)
        y += 18f
        canvas.drawText(boardingPass.originCity.uppercase(), 32f, y, cityPaint)
        canvas.drawText(boardingPass.destinationCity.uppercase(), 470f, y, cityPaint)
        y += 20f
        canvas.drawText(boardingPass.departureTime.takeLast(5), 32f, y, timePaint)
        canvas.drawText(boardingPass.arrivalTime.takeLast(5), 470f, y, timePaint)

        // Center plane icon + DIRECT line
        val centerX = 297.5f
        val arrowY = headerH + 100f
        canvas.drawText(
            "✈", centerX - 6f, arrowY - 4f,
            Paint().apply {
                color = emerald; textSize = 22f; isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
        )
        val dashPaint = Paint().apply {
            color = divider
            strokeWidth = 1f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(3.5f, 3.5f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(180f, arrowY + 8f, 270f, arrowY + 8f, dashPaint)
        canvas.drawLine(324f, arrowY + 8f, 414f, arrowY + 8f, dashPaint)
        canvas.drawText(
            "DIRECT", centerX, arrowY + 22f,
            Paint().apply {
                color = gray; textSize = 9f; isFakeBoldText = true; isAntiAlias = true
                letterSpacing = 0.20f
                textAlign = Paint.Align.CENTER
            }
        )

        y += 36f
        canvas.drawLine(32f, y, 563f, y, Paint().apply { color = divider; strokeWidth = 0.6f })

        // Passenger
        y += 36f
        canvas.drawText("PASSENGER", 32f, y, labelPaint)
        y += 22f
        canvas.drawText(
            boardingPass.passengerName.uppercase(),
            32f, y,
            Paint().apply { color = emerald; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        )

        // 3×3 info grid
        y += 40f
        val colWidth = (563f - 32f) / 3f
        val gridValuePaint = Paint().apply {
            color = emerald; textSize = 22f; isFakeBoldText = true; isAntiAlias = true
        }
        val rows = listOf(
            listOf("SEAT" to boardingPass.seat,
                   "GATE" to boardingPass.gate,
                   "GROUP" to boardingPass.boardingGroup),
            listOf("DEPART" to boardingPass.departureTime.takeLast(5),
                   "ARRIVAL" to boardingPass.arrivalTime.takeLast(5),
                   "TERMINAL" to boardingPass.terminal.ifBlank { "—" }),
            listOf("CLASS" to boardingPass.seatClass,
                   "SEQUENCE" to boardingPass.sequence,
                   "BAGGAGE" to boardingPass.baggageInfo.take(12).ifBlank { "—" })
        )
        rows.forEach { row ->
            row.forEachIndexed { idx, (label, value) ->
                val cx = 32f + idx * colWidth
                canvas.drawText(label, cx, y, labelPaint)
                canvas.drawText(value, cx, y + 26f, gridValuePaint)
            }
            canvas.drawLine(32f, y + 38f, 563f, y + 38f, Paint().apply { color = divider; strokeWidth = 0.4f })
            y += 56f
        }

        // Perforated divider
        y += 4f
        canvas.drawLine(32f, y, 563f, y, dashPaint)

        // QR section
        y += 24f
        val qrSize = 170f
        val qrX = (595f - qrSize) / 2f
        canvas.drawRoundRect(
            RectF(qrX - 18f, y - 18f, qrX + qrSize + 18f, y + qrSize + 42f),
            10f, 10f,
            Paint().apply { color = cream }
        )
        val qrBitmap = QrGenerator.generate(boardingPass.qrPayload, qrSize.toInt())
        canvas.drawBitmap(qrBitmap, qrX, y, null)
        canvas.drawText(
            "SCAN AT GATE",
            centerX, y + qrSize + 26f,
            Paint().apply {
                color = gray; textSize = 10f; isFakeBoldText = true; isAntiAlias = true
                letterSpacing = 0.30f
                textAlign = Paint.Align.CENTER
            }
        )

        // Footer
        val footerY = 800f
        canvas.drawLine(32f, footerY, 563f, footerY, Paint().apply { color = divider; strokeWidth = 0.5f })
        val statusPaint = Paint().apply {
            color = emerald; textSize = 10f; isFakeBoldText = true; isAntiAlias = true
            letterSpacing = 0.20f
        }
        canvas.drawText("STATUS · ${boardingPass.status.uppercase()}", 32f, footerY + 22f, statusPaint)
        canvas.drawText(
            "BOOKING · ${boardingPass.bookingReference}",
            563f, footerY + 22f,
            Paint().apply {
                color = grayMuted; textSize = 10f; isAntiAlias = true; letterSpacing = 0.20f
                textAlign = Paint.Align.RIGHT
            }
        )
        canvas.drawText(
            "MyPass · ${boardingPass.airlineName} · Issued ${boardingPass.issuedAt.take(10)}",
            centerX, footerY + 22f,
            Paint().apply {
                color = grayMuted; textSize = 9f; isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
        )

        canvas.drawRect(0f, 838f, 595f, 842f, Paint().apply { color = emeraldDark })

        pdf.finishPage(page)

        val fileName = "MyPass-${boardingPass.bookingReference}-${boardingPass.flightNumber.replace(" ", "")}.pdf"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create PDF file")
        resolver.openOutputStream(uri)?.use { output -> pdf.writeTo(output) }
            ?: throw IOException("Unable to open output stream")
        pdf.close()
        return fileName to uri
    }
}
