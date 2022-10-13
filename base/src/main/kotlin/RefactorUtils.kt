import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

object RefactorUtils {
    inline fun <reified T : ASTNode> copyNode(node: T, rewriter: ASTRewrite, includeComments: Boolean = true): T {
        return if (includeComments) rewriter.createCopyTarget(node) as T else ASTNode.copySubtree(node.ast, node) as T
    }
}
