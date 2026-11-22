package com.qrlifetime.qrcode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrSvgTest {
    @Test
    fun buildsSvg() {
        val svg = QrSvg.build("https://qrlifetime.com/pricing")
        assertTrue(svg.startsWith("<svg"))
        assertTrue(svg.endsWith("</svg>"))
    }

    @Test
    fun fileName() {
        assertEquals("qr-qrlifetime-com-pricing.svg", QrSvg.fileNameFor("https://qrlifetime.com/pricing"))
        assertEquals("qr-code.svg", QrSvg.fileNameFor("://"))
    }
}
