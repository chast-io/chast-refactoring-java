import CodeSyntaxVerifier.isValidCode
import FileUtils.writeCodeToFile
import analyzers.TypeAnalyzer
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.eclipse.jface.text.IDocument
import java.io.File
import java.util.*

class ClassToRecord {
    private lateinit var rewriter: ASTRewrite
    private lateinit var typeAnalyzer: TypeAnalyzer

    public fun convert(sourceFile: File, targetFile: File = sourceFile) {
        val parserContext = JDTParser.parse(sourceFile, JavaCore.VERSION_17)
        val compilationUnit = parserContext.compilationUnit
        val ast = compilationUnit.ast

        rewriter = ASTRewrite.create(ast)

        var hasChange = false

        compilationUnit.accept(object : ASTVisitor() {
            override fun visit(node: TypeDeclaration): Boolean {
                typeAnalyzer = TypeAnalyzer(node)

                if (ClassToRecordVerifier(typeAnalyzer).isConvertableClass()) {
                    val recordDeclaration = ClassToRecordConverter(node, typeAnalyzer, rewriter).convert()
                    // TODO check the use of the edit group
                    rewriter.replace(node, recordDeclaration, null)
                    hasChange = true
                }
                return true
            }
        })

        if (hasChange) {
            applyChanges(parserContext.doc)
            val formattedCode = format(parserContext.doc)
            if (isValidCode(formattedCode)) {
                writeCodeToFile(formattedCode, targetFile)
            } else {
                println("Invalid code")
            }
        }
    }

    private fun applyChanges(doc: IDocument) {
        val prefs = Properties()
        prefs.setProperty(JavaCore.COMPILER_SOURCE, CompilerOptions.VERSION_16)
        prefs.setProperty(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.VERSION_16)
        prefs.setProperty(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_16)

        rewriter.rewriteAST(doc, null).apply(doc)
    }

    private fun format(doc: IDocument): String {
        val formattedCode = Formatter.format(doc)
        // the inserted ";" seems to be a bug in the formatter (https://github.com/eclipse-jdt/eclipse.jdt.core/issues/434)
        return formattedCode.replaceFirst(Regex("^\\s*;$\n", RegexOption.MULTILINE), "")
    }

}
