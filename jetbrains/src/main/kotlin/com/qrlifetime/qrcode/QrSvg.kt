package com.qrlifetime.qrcode

import io.nayuki.qrcodegen.QrCode

/** Builds QR codes as SVG strings, entirely offline. */
object QrSvg {
    const val MAX_LENGTH = 2000

    fun build(text: String, ecc: QrCode.Ecc = QrCode.Ecc.MEDIUM, margin: Int = 4): String {
        val qr = QrCode.encodeText(text, ecc)
        val size = qr.size + margin * 2
        val path = StringBuilder()
        for (y in 0 until qr.size) {
            for (x in 0 until qr.size) {
                if (qr.getModule(x, y)) {
                    if (path.isNotEmpty()) path.append(' ')
                    path.append("M${x + margin},${y + margin}h1v1h-1z")
                }
            }
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 $size $size\" shape-rendering=\"crispEdges\">" +
            "<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>" +
            "<path d=\"$path\" fill=\"#000000\"/></svg>"
    }

    /** Turns a URL or text into a short, safe file name. */
    fun fileNameFor(text: String): String {
        val base = text
            .replace(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://"), "")
            .replace(Regex("[^a-zA-Z0-9]+"), "-")
            .trim('-')
            .lowercase()
            .take(60)
        return "qr-${base.ifEmpty { "code" }}.svg"
    }
}
