import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.JavaCore
import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.ToolFactory
import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.formatter.CodeFormatter
import org.jboss.forge.roaster._shade.org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.jboss.forge.roaster._shade.org.eclipse.jface.text.BadLocationException
import org.jboss.forge.roaster._shade.org.eclipse.jface.text.IDocument
import org.jboss.forge.roaster._shade.org.eclipse.text.edits.TextEdit
import java.util.*

object Formatter {
    fun format(doc: IDocument): String {
        val prefs = Properties()
        prefs.setProperty(JavaCore.COMPILER_SOURCE, CompilerOptions.VERSION_16)
        prefs.setProperty(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.VERSION_16)
        prefs.setProperty(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_16)

        val codeFormatter: CodeFormatter = ToolFactory.createCodeFormatter(prefs)
        val code = doc.get()

        return try {
            val edit: TextEdit = codeFormatter.format(
                CodeFormatter.K_COMPILATION_UNIT or CodeFormatter.F_INCLUDE_COMMENTS,
                code, 0, code.length, 0, null
            )
            edit.apply(doc)
            doc.get()
        } catch (e: BadLocationException) {
            // TODO log error
            code
        }
    }
}
