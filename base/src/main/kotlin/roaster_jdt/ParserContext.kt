package roaster_jdt

import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.dom.ASTParser
import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.dom.CompilationUnit
import org.jboss.forge.roaster._shade.org.eclipse.jface.text.IDocument

data class ParserContext(
    val doc: IDocument,
    val parser: ASTParser,
    val compilationUnit: CompilationUnit,
)
