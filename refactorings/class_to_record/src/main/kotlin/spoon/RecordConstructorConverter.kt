package spoon

import spoon.reflect.code.CtAssignment
import spoon.reflect.code.CtStatementList
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtConstructor
import spoon.spoonExtensions.CtClassExtension
import spoon.spoonExtensions.CtConstructorExtension
import spoon.spoonExtensions.extension

class RecordConstructorConverter<T>(
    private val constructor: CtConstructor<T>,
    private val clazz: CtClass<*>
) {
    private val constructorExtension: CtConstructorExtension<T>
        get() = constructor.extension()
    private val classExtension: CtClassExtension<*>
        get() = clazz.extension()


    fun convert(): CtConstructor<T>? {
        return when {
            constructorExtension.isSimpleConstructor(classExtension) -> null
            canBeConvertedToCompactConstructor() -> toCompactConstructor()
            else -> constructorExtension
        }
    }

    private fun toCompactConstructor(): CtConstructor<T> {
        val compactConstructor = constructorExtension.clone().extension()
        compactConstructor.parameters.clear()
        compactConstructor.isCompactConstructor = true
        val statements = constructorExtension.explicitStatements
            .filter { it !is CtAssignment<*, *> }
            .map { it.clone() }
            .toList()
        compactConstructor.body.setStatements<CtStatementList>(statements)
        return compactConstructor
    }

    private fun canBeConvertedToCompactConstructor(): Boolean {
        if (constructorExtension.hasInpureInitializingAssignment()) return false
        if (!constructorExtension.allParametersInitializeAllFields(classExtension)) return false
        return true
    }
}
