package analyzers

import org.jboss.forge.roaster._shade.org.eclipse.jdt.core.dom.*


open class MethodAnalyzer(val method: MethodDeclaration) : Analyzer() {
    open var data: MethodAnalytics = MethodAnalytics()

    init {
        collectUsedFields()
    }

    override fun name() = method.name.identifier ?: "<UNDEFINED>"
    override fun fullyQualifiedName() = method.name.fullyQualifiedName ?: "<UNDEFINED>"

    private fun collectUsedFields() {
        method.accept(object : ASTVisitor() {
            override fun visit(field: SimpleName): Boolean {
                val resolvedBinding = field.resolveBinding()
                if (resolvedBinding != null) {
                    if (resolvedBinding is IVariableBinding) {
                        if (resolvedBinding.isField) {
                            data.accessedFields[resolvedBinding.name] = resolvedBinding
                        } else {
                            data.usedVariables[resolvedBinding.name] = resolvedBinding
                            if(field.parent is VariableDeclaration && field.parent.parent is MethodDeclaration) {
                                data.parameterVariables[resolvedBinding.name] = resolvedBinding
                            }
                        }
                    }
                }

                return super.visit(field)
            }
        })
        method.accept(object : ASTVisitor() {
            override fun visit(variable: VariableDeclarationFragment): Boolean {
                return super.visit(variable)
            }
        })
    }

    fun isSimple(): Boolean {
        return method.body.statements().size == 1
    }

    fun returnsField(field: VariableDeclarationFragment): Boolean {
        return data.accessedFields[field.name.fullyQualifiedName] == field.resolveBinding()
    }


    open inner class MethodAnalytics {
        val accessedFields = HashMap<String, IVariableBinding>()
        val usedVariables = HashMap<String, IVariableBinding>()
        val parameterVariables = HashMap<String, IVariableBinding>()
    }
}
