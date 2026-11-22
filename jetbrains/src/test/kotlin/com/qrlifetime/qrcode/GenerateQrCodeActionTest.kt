package com.qrlifetime.qrcode

import com.intellij.openapi.ui.TestDialog
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class GenerateQrCodeActionTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            TestDialogManager.setTestDialog(TestDialog.DEFAULT)
        } finally {
            super.tearDown()
        }
    }

    fun testInsertReplacesSelection() {
        myFixture.configureByText("page.html", "<p><selection>https://qrlifetime.com/pricing</selection></p>")
        TestDialogManager.setTestDialog { 0 } // "Insert SVG in the current file"
        myFixture.testAction(GenerateQrCodeAction())
        val text = myFixture.editor.document.text
        assertTrue(text.startsWith("<p><svg"))
        assertTrue(text.endsWith("</svg></p>"))
        File("build/test-insert.svg").writeText(text.removePrefix("<p>").removeSuffix("</p>"))
    }

    fun testSaveWritesFileInProject() {
        myFixture.configureByText("notes.md", "Link: <selection>https://example.com/saved</selection>")
        TestDialogManager.setTestDialog { 1 } // "Save as an SVG file in the project"
        myFixture.testAction(GenerateQrCodeAction())
        val saved = myFixture.findFileInTempDir("qr-codes/qr-example-com-saved.svg")
        assertNotNull(saved)
        val content = VfsUtil.loadText(saved!!)
        assertTrue(content.startsWith("<svg"))
        File("build/test-save.svg").writeText(content)
        assertEquals("Link: https://example.com/saved", myFixture.editor.document.text)
    }
}
