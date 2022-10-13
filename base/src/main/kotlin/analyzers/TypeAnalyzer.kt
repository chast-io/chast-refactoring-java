package analyzers

import org.eclipse.jdt.core.dom.*
import java.util.*


class TypeAnalyzer(val type: TypeDeclaration) : Analyzer() {
    val data = ClassOrInterfaceAnalytics()

    init {
        collectFields()
        collectConstructors()
        collectMethods()
    }

    override fun name() = type.name.identifier ?: "<UNDEFINED>"
    override fun fullyQualifiedName() = type.name.fullyQualifiedName ?: "<UNDEFINED>"

    fun analyzeMethods(): HashMap<String, MethodAnalyzer> {
        if (data.analyzedMethods == null) {
            val map = HashMap<String, MethodAnalyzer>()
            data.methods.values
                .forEach() { method ->
                    map[getMethodSignature(method)] = MethodAnalyzer(method)
                }
            data.analyzedMethods = map
        }
        return data.analyzedMethods!!
    }

    fun analyzeConstructors(): List<ConstructorAnalyzer> {
        if (data.analyzedConstructors == null) {
            data.analyzedConstructors = data.constructors.parallelStream()
                .map() { constructor -> ConstructorAnalyzer(constructor) }
                .toList()
        }
        return data.analyzedConstructors!!
    }

    fun getNonSimpleGetterMethods(): LinkedList<MethodAnalyzer> {
        return splitMethodsIntoSimpleGettersAndOthers().second
    }

    fun getSimpleGetters(): LinkedList<MethodAnalyzer> {
        return splitMethodsIntoSimpleGettersAndOthers().first
    }

    private fun splitMethodsIntoSimpleGettersAndOthers(): Pair<LinkedList<MethodAnalyzer>, LinkedList<MethodAnalyzer>> {
        val getterMethods = LinkedList<MethodAnalyzer>()
        val otherMethods = LinkedList<MethodAnalyzer>()
        for ((name, methodAnalyzer) in analyzeMethods()) {
            val methodName = name.substring(0, name.length - 2)
            if (data.fields.contains(methodName)) { // true if end only contains () and is therefore a getter with no parameters
                if ((methodAnalyzer.isSimple() && methodAnalyzer.returnsField(data.fields[methodName]!!)))
                    getterMethods.add(methodAnalyzer)
            } else otherMethods.add(methodAnalyzer)

        }
        return getterMethods to otherMethods
    }

    private fun collectFields() {
        type.accept(object : ASTVisitor() {
            override fun visit(field: FieldDeclaration): Boolean {
                @Suppress("UNCHECKED_CAST")
                val variableDeclarationFragments = field.fragments() as List<VariableDeclarationFragment>
                for (fragment in variableDeclarationFragments) {
                    data.fields[fragment.name.fullyQualifiedName] = fragment
                }
                return super.visit(field)
            }
        })
    }

    private fun collectMethods() {
        type.accept(object : ASTVisitor() {
            override fun visit(method: MethodDeclaration): Boolean {
                if (method.isConstructor) return super.visit(method)
                data.methods[getMethodSignature(method)] = method
                return super.visit(method)
            }
        })
    }

    private fun getMethodSignature(method: MethodDeclaration): String {
        val parameters = method.parameters()
            .joinToString(",") { param -> (param as SingleVariableDeclaration).type.toString() }
        return "${method.name.fullyQualifiedName}(${parameters})"
    }

    private fun collectConstructors() {
        type.accept(object : ASTVisitor() {
            override fun visit(constructor: MethodDeclaration): Boolean {
                if (!constructor.isConstructor) return super.visit(constructor)
                data.constructors.add(constructor)
                return super.visit(constructor)
            }
        })
    }

    inner class ClassOrInterfaceAnalytics {
        val fields = HashMap<String, VariableDeclarationFragment>()
        val constructors: MutableList<MethodDeclaration> = ArrayList()
        val methods = HashMap<String, MethodDeclaration>()

        var analyzedMethods: HashMap<String, MethodAnalyzer>? = null
        var analyzedConstructors: MutableList<ConstructorAnalyzer>? = null
    }

}
