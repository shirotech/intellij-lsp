package com.github.gtache.lsp.contributors.inspection

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.contributors.fixes.{LSPCodeActionFix, LSPCommandFix}
import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.{DiagnosticRangeHighlighter, EditorEventManager}
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.codeInspection._
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import javax.swing.JComponent
import org.eclipse.lsp4j.DiagnosticSeverity
import com.github.gtache.lsp.utils.DocumentUtils._

/**
  * The inspection tool for LSP
  */
class LSPInspection extends LocalInspectionTool {

  override def checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val virtualFile = file.getVirtualFile
    if (PluginMain.isExtensionSupported(virtualFile.getExtension)) {
      val uri = FileUtils.VFSToURI(virtualFile)

      /**
        * Get all the ProblemDescriptor given an EditorEventManager
        * Look at the DiagnosticHighlights, create dummy PsiElement for each, create descriptor using it
        *
        * @param m The manager
        * @return The ProblemDescriptors
        */
      def descriptorsForManager(m: EditorEventManager): Array[ProblemDescriptor] = {
        val diagnostics = m.getDiagnostics
        diagnostics.collect { case DiagnosticRangeHighlighter(rangeHighlighter, diagnostic) =>
          val start = rangeHighlighter.getStartOffset
          val end = rangeHighlighter.getEndOffset
          if (start < end) {
            val name = m.editor.getDocument.getTextClamped(start, end)
            val severity = diagnostic.getSeverity match {
              case DiagnosticSeverity.Error => ProblemHighlightType.ERROR
              case DiagnosticSeverity.Warning => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
              case DiagnosticSeverity.Information => ProblemHighlightType.INFORMATION
              case DiagnosticSeverity.Hint => ProblemHighlightType.INFORMATION
              case _ => null
            }
            val element = LSPPsiElement(name, m.editor.getProject, start, end, file, m.editor)
            val codeActionResult = m.codeAction(element)
            val fixes = if (codeActionResult != null) {
              val (commandsE, codeActionsE) = codeActionResult.filter(e => e != null && (e.isLeft || e.isRight)).partition(e => e.isLeft)
              val commands = commandsE.map(e => e.getLeft).map(c => new LSPCommandFix(uri, c))
              val codeActions = codeActionsE.map(e => e.getRight).map(c => new LSPCodeActionFix(uri, c))
              (commands ++ codeActions).toArray
            } else null
            manager.createProblemDescriptor(element, null.asInstanceOf[TextRange], diagnostic.getMessage, severity, isOnTheFly, fixes: _*)
          } else null
        }.toArray.filter(d => d != null)
      }

      EditorEventManager.forUri(uri) match {
        case Some(m) =>
          descriptorsForManager(m)
        case None =>
          if (isOnTheFly) {
            super.checkFile(file, manager, isOnTheFly)
          } else {
            /*val descriptor = new OpenFileDescriptor(manager.getProject, virtualFile)
            ApplicationUtils.writeAction(() => FileEditorManager.getInstance(manager.getProject).openTextEditor(descriptor, false))
            EditorEventManager.forUri(uri) match {
              case Some(m) => descriptorsForManager(m)
              case None => super.checkFile(file, manager, isOnTheFly)
            }*/
            //TODO need dispatch thread
            super.checkFile(file, manager, isOnTheFly)
          }
      }
    } else super.checkFile(file, manager, isOnTheFly)
  }

  override def getDisplayName: String = getShortName

  override def createOptionsPanel(): JComponent = {
    new LSPInspectionPanel(getShortName, this)
  }

  override def getShortName: String = "LSP"

  override def getID: String = "LSP"

  override def getGroupDisplayName: String = "LSP"

  override def getStaticDescription: String = "Reports errors by the LSP server"

  override def isEnabledByDefault: Boolean = true

}
