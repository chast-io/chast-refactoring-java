import RefactorUtils.copyNode
import analyzers.TypeAnalyzer
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

internal class ClassToRecordConverter(
    private val baseClass: TypeDeclaration,
    private val typeAnalyzer: TypeAnalyzer,
    private val rewriter: ASTRewrite
) {
    private val record: RecordDeclaration = createRecord()
    private val baseClassAst: AST
        get() = baseClass.ast

    fun convert(): RecordDeclaration {
        copyJavadoc()
        copyName()
        copyModifiers()
        copyTypeParameters()
        copyRecordComponents()
        copySuperInterfaceTypes()
        copyStaticFields()
        copyConstructors()
        copyMethods()

        return record
    }

    private fun createRecord() = baseClassAst.newRecordDeclaration()

    private fun copyJavadoc() {
        if (baseClass.javadoc != null) record.javadoc = copyNode(baseClass.javadoc, rewriter)
    }

    private fun copyName() {
        record.name = baseClassAst.newSimpleName(baseClass.name.identifier)
    }

    private fun copyModifiers() {
        record.modifiers().addAll(baseClass.modifiers().map { m -> copyNode(m as ASTNode) })
    }

    private fun copyTypeParameters() {
        record.typeParameters().addAll(baseClass.typeParameters().map { m -> copyNode(m as ASTNode) })
    }

    /**
     * All fields that are not static are converted to record components.
     * The fields must be final, otherwise the class can not be converted to a record.
     */
    private fun copyRecordComponents() {
        val recordComponents = typeAnalyzer.data.fields
            .filter { field -> !Modifier.isStatic(field.value.resolveBinding().modifiers) }
            .map {
                val field = baseClassAst.newSingleVariableDeclaration()
                field.name = baseClassAst.newSimpleName(it.key)
                val type = (it.value.parent as FieldDeclaration).type
                field.type = copyNode(type, false)
                field
            }
        record.recordComponents().addAll(recordComponents)
    }

    private fun copySuperInterfaceTypes() {
        record.superInterfaceTypes().addAll(baseClass.superInterfaceTypes().map { m -> copyNode(m as ASTNode) })
    }

    private fun copyStaticFields() {
        val staticFields = typeAnalyzer.data.fields
            .filter { field -> Modifier.isStatic(field.value.resolveBinding().modifiers) }
            .map { field -> copyNode(field.value.parent as FieldDeclaration, true) }

        record.bodyDeclarations().addAll(staticFields)
    }

    private fun copyConstructors() {
        // TODO implement
        println("Copying constructors is not implemented yet. Records have a different kind of constructors and therefore need to be handled with care.")
    }

    /**
     * Copies all methods that are not simple getters.
     * (Setter methods must not be present in records as all fields are read only.)
     */
    private fun copyMethods() {
        val methods = typeAnalyzer.getNonSimpleGetterMethods()
            .map { methodAnalyzer ->
                copyNode(methodAnalyzer.method, true)
            }

        // TODO drop equals, hashCode and toString methods (maybe make it optional?)

        record.bodyDeclarations().addAll(methods)
    }

    /**
     * Copies all statements that do not need special record treatment.
     */
    private fun copyNotSpecialStatements(){
        val remainingStatements = baseClass.bodyDeclarations()
            .filter { d -> d !is MethodDeclaration && d !is FieldDeclaration }
            .map { m -> copyNode(m as ASTNode, true) }
        record.bodyDeclarations().addAll(remainingStatements)
    }


    // Utils
    private inline fun <reified T : ASTNode> copyNode(node: T, includeComments: Boolean = true): T {
        return copyNode(node, rewriter, includeComments)
    }


}
