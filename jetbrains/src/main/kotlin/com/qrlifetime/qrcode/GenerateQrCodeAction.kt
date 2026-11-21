package com.qrlifetime.qrcode

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

class GenerateQrCodeAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        val text = readInput(project, editor) ?: return

        val svg = try {
            QrSvg.build(text)
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Could not generate the QR code: ${ex.message}", TITLE)
            return
        }

        val options = if (editor != null && editor.document.isWritable) {
            arrayOf(INSERT, SAVE, Messages.getCancelButton())
        } else {
            arrayOf(SAVE, Messages.getCancelButton())
        }
        val index = Messages.showDialog(project, "What should be done with the QR code?", TITLE, options, 0, null)
        when (options.getOrNull(index)) {
            INSERT -> insert(project, editor!!, svg)
            SAVE -> save(project, text, svg)
        }
    }

    private fun readInput(project: Project, editor: Editor?): String? {
        val selected = editor?.selectionModel?.selectedText?.trim()
        if (!selected.isNullOrEmpty()) return selected
        return Messages.showInputDialog(
            project,
            "URL or text to encode in the QR code",
            TITLE,
            null,
            "https://",
            object : InputValidator {
                override fun checkInput(input: String?) = !input.isNullOrBlank() && input.length <= QrSvg.MAX_LENGTH
                override fun canClose(input: String?) = checkInput(input)
            },
        )?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun insert(project: Project, editor: Editor, svg: String) {
        WriteCommandAction.runWriteCommandAction(project, TITLE, null, {
            val selection = editor.selectionModel
            val start = if (selection.hasSelection()) selection.selectionStart else editor.caretModel.offset
            val end = if (selection.hasSelection()) selection.selectionEnd else start
            editor.document.replaceString(start, end, svg)
            selection.removeSelection()
            editor.caretModel.moveToOffset(start + svg.length)
        })
    }

    private fun save(project: Project, text: String, svg: String) {
        val root = project.guessProjectDir()
        if (root == null) {
            Messages.showErrorDialog(project, "No project folder was found to save the QR code.", TITLE)
            return
        }
        var file: VirtualFile? = null
        WriteCommandAction.runWriteCommandAction(project, TITLE, null, {
            val folder = VfsUtil.createDirectoryIfMissing(root, OUTPUT_FOLDER)
            val name = QrSvg.fileNameFor(text)
            val target = folder.findChild(name) ?: folder.createChildData(this, name)
            VfsUtil.saveText(target, svg + "\n")
            file = target
        })
        val saved = file ?: return
        FileEditorManager.getInstance(project).openFile(saved, true)
        NotificationGroupManager.getInstance()
            .getNotificationGroup("QRLifetime")
            .createNotification("QR code saved to $OUTPUT_FOLDER/${saved.name}", NotificationType.INFORMATION)
            .notify(project)
    }

    companion object {
        const val TITLE = "QR Lifetime: Generate QR code"
        const val INSERT = "Insert SVG in the current file"
        const val SAVE = "Save as an SVG file in the project"
        const val OUTPUT_FOLDER = "qr-codes"
    }
}
