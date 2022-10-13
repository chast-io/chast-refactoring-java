import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import java.io.File

object JDTParser {
    fun parse(file: File, javaVersion: String = JavaCore.VERSION_17): ParserContext {
        val doc: IDocument = Document(file.readText(Charsets.UTF_8))

        val parser = ASTParser.newParser(AST.getJLSLatest())
        parser.setResolveBindings(true)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        parser.setBindingsRecovery(true)
        val options: Map<String, String> = JavaCore.getOptions()
        JavaCore.setComplianceOptions(javaVersion, options)
        parser.setCompilerOptions(options)

        val unitName = file.name
        parser.setUnitName(unitName)

        val sources = arrayOf(file.parent)
        val classpath = arrayOf(File("base/src/main/resources/dummy-classpath-jar.jar").absolutePath)
        parser.setEnvironment(classpath, sources, arrayOf("UTF-8"), true)
        parser.setSource(doc.get().toCharArray())

        return ParserContext(
            doc,
            parser,
            // TODO check the use of the monitor
            parser.createAST(null) as CompilationUnit
        )
    }
}
