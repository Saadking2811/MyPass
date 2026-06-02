package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import com.example.myapplication.model.BoardingPass

/** Persist + share intent for a generated boarding-pass PDF. */
object BoardingPassSharer {

    /** Save the PDF and open the system Share sheet (Wallet / Drive / Files / email…). */
    fun share(context: Context, boardingPass: BoardingPass) {
        val (_, uri) = BoardingPassPdfGenerator.save(context, boardingPass)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MyPass boarding pass · ${boardingPass.flightNumber}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Boarding pass for ${boardingPass.passengerName}: " +
                "${boardingPass.flightNumber} (${boardingPass.origin}→${boardingPass.destination}), " +
                "Seat ${boardingPass.seat}, Gate ${boardingPass.gate}."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Add boarding pass to…")
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    }

    /** Save then open the PDF directly in the default viewer (no share sheet). */
    fun openInViewer(context: Context, boardingPass: BoardingPass) {
        val (_, uri) = BoardingPassPdfGenerator.save(context, boardingPass)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(view) }
    }
}
