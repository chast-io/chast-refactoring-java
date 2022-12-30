package spoon.spoonExtensions

import spoon.reflect.code.CtFieldRead
import spoon.reflect.code.CtReturn
import spoon.reflect.declaration.CtMethod

class CtMethodExtension<T>(private val method: CtMethod<T>) : CtMethod<T> by method {
    val isSimpleGetter: Boolean
        get() {
            if (method.isStatic) return false
            if (method.parameters.size > 0) return false
            if (method.body.statements.size != 1) return false
            val statement = method.body.statements[0]
            if (statement !is CtReturn<*>) return false
            val returnedExpr = statement.returnedExpression
            if (returnedExpr !is CtFieldRead) return false

            return when (returnedExpr.variable.simpleName){
                method.simpleName,
                method.simpleName.replace("get", "").lowercase()
                -> true
                else -> false
            }
        }
}

fun <T> CtMethod<T>.extension() = CtMethodExtension(this)
