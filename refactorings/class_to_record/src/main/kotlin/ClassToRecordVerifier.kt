import analyzers.TypeAnalyzer
import kotlinx.coroutines.*
import org.eclipse.jdt.core.dom.Modifier

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
internal class ClassToRecordVerifier(private val typeAnalyzer: TypeAnalyzer) {
    fun isConvertableClass(): Boolean = runBlocking {
        try {
            runPreChecks()
            analyzeFullType()
            runMainChecks()

            return@runBlocking true
        } catch (e: ClassToRecordException) {
            println(e.message)
            return@runBlocking false
        }
    }

    private suspend fun runPreChecks(): Unit = withContext(Dispatchers.Default) {
        awaitAll(
            verificationCheck("is interface") { !typeAnalyzer.isInterface() },
            verificationCheck("does extend other class") { !typeAnalyzer.extendsClass() },
            verificationCheck("is abstract") { !typeAnalyzer.isAbstractClass() },
        )
    }

    private suspend fun analyzeFullType(): Unit = withContext(Dispatchers.Default) {
        awaitAll(
            async { typeAnalyzer.analyzeConstructors() },
            async { typeAnalyzer.analyzeMethods() }
        )
    }

    private suspend fun runMainChecks(): Unit = withContext(Dispatchers.Default) {
        awaitAll(
            verificationCheck("has fields other than final and static") { hasOnlyFinalAndStaticFields() },
            verificationCheck("has fields with no getter") { hasEveryFieldASimpleGetter() },
            verificationCheck("has initialized fields") { fieldsAreNotInitialized() },
//            verificationCheck("has fields that do not get initialized by the constructor") { hasConstructorThatInitializesAllFields() },
        )
    }

    private suspend fun hasOnlyFinalAndStaticFields(): Boolean = coroutineScope {
        typeAnalyzer.data.fields.values.asSequence()
            .onEach { ensureActive() }
            .map { field -> field.resolveBinding() }
            .all { field -> Modifier.isFinal(field.modifiers) || Modifier.isStatic(field.modifiers) }
    }

    // TODO check if this is optional
    private suspend fun hasEveryFieldASimpleGetter(): Boolean = coroutineScope {
        val fields = typeAnalyzer.data.fields.values.asSequence()
            .onEach { ensureActive() }
            .filter { field -> !Modifier.isStatic(field.resolveBinding().modifiers) }
            .map { field -> field.name.fullyQualifiedName }
            .toSet()

        typeAnalyzer.getSimpleGetters().size == fields.size
    }

    private suspend fun fieldsAreNotInitialized(): Boolean = coroutineScope {
        typeAnalyzer.data.fields.values.asSequence()
            .onEach { ensureActive() }
            .filter { field -> !Modifier.isStatic(field.resolveBinding().modifiers) }
            .none { field -> field.initializer != null }
    }

    private suspend fun hasConstructorThatInitializesAllFields(): Boolean = coroutineScope {
        TODO("Not yet implemented")
    }

    private suspend fun isNotExtended(): Boolean = coroutineScope {
        TODO("Not yet implemented")
        // This is hard as it needs to check all other classes in the project.
        // Here we have to differentiate whether we received a single file or a complete project
    }

    private fun CoroutineScope.verificationCheck(
        errorReason: String = "",
        func: suspend CoroutineScope.() -> Boolean
    ): Deferred<Unit> {
        return async { if (!func()) throw ClassToRecordException("Class ${errorReason.ifBlank { "does not match all conditions" }}") }
    }

}

private class ClassToRecordException(s: String) : Throwable(s)
