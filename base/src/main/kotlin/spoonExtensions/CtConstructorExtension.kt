package spoonExtensions

import spoon.reflect.code.CtAssignment
import spoon.reflect.code.CtFieldWrite
import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtVariableRead
import spoon.reflect.declaration.CtConstructor
import spoon.reflect.reference.CtFieldReference

class CtConstructorExtension<T>(private val constructor: CtConstructor<T>) : CtConstructor<T> by constructor {
    fun isSimpleConstructor(clazz: CtClassExtension<*>): Boolean {
        if (hasNonAssignmentStatements()) return false
        if (hasNonInitializingParameters(clazz)) return false
        if (hasNonFieldInitializingStatements()) return false
        if (hasInpureInitializingAssignment()) return false
        return true
    }

    fun hasInpureInitializingAssignment(): Boolean {
        val assignments = explicitStatements
            .filterIsInstance<CtAssignment<*, *>>()
            .map { it.assignment }
        return assignments
            .filterIsInstance<CtVariableRead<*>>()
            .count() != assignments.count()
    }

    fun hasNonInitializingParameters(clazz: CtClassExtension<*>): Boolean {
        return !allParametersInitializeAllFields(clazz)
    }

    fun allParametersInitializeAllFields(clazz: CtClassExtension<*>): Boolean {
        if (!hasAllAndOnlyClassFieldsAsParameters(clazz)) return false
        if (!initializesAllFields(clazz)) return false
        return true
    }

    fun initializesAllFields(clazz: CtClassExtension<*>): Boolean {
        return explicitStatements
            .filterIsInstance<CtAssignment<*, *>>()
            .map { it.assigned }
            .filterIsInstance<CtFieldWrite<*>>()
            .map { it.variable }
            .filterIsInstance<CtFieldReference<*>>()
            .all { clazz.fields.contains(it.fieldDeclaration) }
    }

    fun hasOnlyFieldInitializingStatements(): Boolean {
        return explicitStatements
            .filterIsInstance<CtAssignment<*, *>>()
            .map { it.assigned }
            .filterIsInstance<CtFieldWrite<*>>()
            .map { it.variable }
            .filterIsInstance<CtFieldReference<*>>()
            .count() == explicitStatementsCount
    }

    fun hasNonFieldInitializingStatements(): Boolean {
        return !hasOnlyFieldInitializingStatements()
    }

    val explicitStatements: Sequence<CtStatement>
        get() {
            return body.statements.asSequence().filter { !it.isImplicit }
        }

    val explicitStatementsCount: Int by lazy {
        body.statements.asSequence().filter { !it.isImplicit }.count()
    }

    fun hasNonAssignmentStatements(): Boolean {
        return explicitStatements.filter { it !is CtAssignment<*, *> }.any()
    }

    fun hasAllClassFieldsAsParameters(clazz: CtClassExtension<*>): Boolean {
        val fields = clazz.nonStaticFields.map { it.simpleName }.toSet()
        if(fields.size > parameters.size) return false
        return parameters.asSequence()
            .count { fields.contains(it.simpleName) } == fields.size
    }

    fun hasAllAndOnlyClassFieldsAsParameters(clazz: CtClassExtension<*>): Boolean {
        val fields = clazz.nonStaticFields.map { it.simpleName }.toSet()
        if(fields.size != parameters.size) return false
        return parameters.asSequence()
            .all { fields.contains(it.simpleName) }
    }

}

fun <T> CtConstructor<T>.extension() = CtConstructorExtension(this)
