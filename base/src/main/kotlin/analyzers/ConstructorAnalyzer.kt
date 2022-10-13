package analyzers

import org.eclipse.jdt.core.dom.*
import java.util.*


class ConstructorAnalyzer(val constructor: MethodDeclaration) : MethodAnalyzer(constructor) {
    val constructorData = ConstructorAnalytics()

    init {
        collectStatements()
    }

    private fun collectStatements() {
        constructor.body.statements().forEach { statement ->
            if (statement is ExpressionStatement && statement.expression is Assignment) {
                val assignment = statement.expression as Assignment
                if (assignment.operator == Assignment.Operator.ASSIGN) {
                    val leftHandSide = assignment.leftHandSide
                    val rightHandSide = assignment.rightHandSide
                    if (leftHandSide is FieldAccess && rightHandSide is SimpleName) {
                        if (
                            data.accessedFields[leftHandSide.name.fullyQualifiedName] == leftHandSide.resolveFieldBinding() &&
                            data.usedVariables[rightHandSide.fullyQualifiedName] == rightHandSide.resolveBinding()
                        ) {
                            constructorData.nonFieldInitializingStatements.add(statement)
                            return@forEach
                        }

                    }
                }
            }
            constructorData.nonFieldInitializingStatements.add(statement as Statement)
        }
    }

    fun isSimpleConstructor(): Boolean {
        return initializesAllFields() && hasOnlyInitializingStatements()
    }

    fun canBeConvertedToSimpleConstructor(): Boolean {
        // TODO
        return true
    }

    private fun initializesAllFields(): Boolean {
        return data.accessedFields.size == constructorData.fieldInitializingStatements.size
    }

    private fun hasOnlyInitializingStatements(): Boolean {
        return constructor.body.statements().size == data.accessedFields.size
    }

    inner class ConstructorAnalytics() {
        val fieldInitializingStatements: MutableList<Statement> = LinkedList()
        val nonFieldInitializingStatements: MutableList<Statement> = LinkedList()
    }
}
