import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jface.text.IDocument

data class ParserContext(
    val doc: IDocument,
    val parser: ASTParser,
    val compilationUnit: CompilationUnit,
)
