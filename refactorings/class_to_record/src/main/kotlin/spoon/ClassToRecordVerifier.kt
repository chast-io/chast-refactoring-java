package spoon

import kotlinx.coroutines.*
import roaster_jdt.ClassToRecordException
import spoon.reflect.declaration.CtClass
import spoon.spoonExtensions.extension

/**
 * Verifies if a class can be converted to a record.
 * According to rules specified in:
 * - https://docs.oracle.com/en/java/javase/17/language/records.html
 * - https://openjdk.org/jeps/395
 * - https://docs.oracle.com/en/java/javase/14/language/records.html
 *
 * == Restrictions on Records==
 * The following are restrictions on the use of records:
 * - Records cannot extend any class (implementing interfaces is allowed) or be extended by any class.
 * - Records cannot be abstract; they are implicitly final
 * - Records cannot declare instance fields (other than the private final fields that correspond to the components of the record component list); any other declared fields must be static
 * - Records cannot declare instance initializers
 * - The components of a record are implicitly final
 *
 * Beyond these restrictions, records behave like regular classes:
 * - You can declare them inside a class; nested records are implicitly static
 * - You can create generic records
 * - Records can implement interfaces
 * - You instantiate records with the new keyword
 * - You can declare in a record's body static methods, static fields, static initializers, constructors, instance methods, and nested types
 * - You can annotate records and a record's individual components
 */
internal class ClassToRecordVerifier(private val clazz: CtClass<*>) {
    fun isConvertableClass(): Boolean = runBlocking {
        try {
            runPreChecks()
            runMainChecks()

            return@runBlocking true
        } catch (e: ClassToRecordException) {
            println(e.message)
            return@runBlocking false
        }
    }

    private suspend fun runPreChecks(): Unit = withContext(Dispatchers.Default) {
        awaitAll(
            errorIf("is interface") { clazz.isInterface },
            errorIf("does extend other class") { clazz.extendedModifiers.isNotEmpty() },
            errorIf("is abstract") { clazz.isAbstract },
            errorIf("has no members") { clazz.fields.isEmpty() },
        )
    }

    private suspend fun runMainChecks(): Unit = withContext(Dispatchers.Default) {
        awaitAll(
            errorIf("has fields other than final and static") { !hasOnlyFinalAndStaticFields() },
            errorIf("has initialized fields") { !fieldsAreNotInitialized() },
            errorIf("has fields that do not get initialized by the constructor") { !hasConstructorThatInitializesAllFields() },
        )
    }

    private suspend fun hasOnlyFinalAndStaticFields(): Boolean = coroutineScope {
        clazz.fields.asSequence()
            .onEach { ensureActive() }
            .all { field -> field.isFinal || field.isStatic }
    }

    private suspend fun fieldsAreNotInitialized(): Boolean = coroutineScope {
        clazz.fields.asSequence()
            .onEach { ensureActive() }
            .filter { !it.isStatic }
            .none { it.defaultExpression != null }
    }

    private suspend fun hasConstructorThatInitializesAllFields(): Boolean = coroutineScope {
        clazz.constructors.asSequence()
            .onEach { ensureActive() }
            .map { it.extension() }
            .any { constructor -> constructor.initializesAllFields(clazz.extension()) }
    }

    private suspend fun isNotExtended(): Boolean = coroutineScope {
        TODO("Not yet implemented")
        // This is hard as it needs to check all other classes in the project.
        // Here we have to differentiate whether we received a single file or a complete project
    }

    private fun CoroutineScope.errorIf(
        errorReason: String = "",
        func: suspend CoroutineScope.() -> Boolean
    ): Deferred<Unit> {
        return async { if (func()) throw ClassToRecordException("Class ${errorReason.ifBlank { "does not match all conditions" }}") }
    }

}
