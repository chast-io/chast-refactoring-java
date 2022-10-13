import analyzers.TypeAnalyzer
import org.eclipse.jdt.core.dom.Modifier

internal class ClassToRecordVerifier(private val typeAnalyzer: TypeAnalyzer) {
    fun isConvertableClass(): Boolean {
        typeAnalyzer.analyzeConstructors()
        typeAnalyzer.analyzeMethods()

        return hasOnlyFinalAndStaticFields() &&
                hasEveryFieldASimpleGetter()
    }

    private fun hasOnlyFinalAndStaticFields(): Boolean {
        return typeAnalyzer.data.fields.values
            .map { field -> field.resolveBinding() }
            .all { field -> Modifier.isFinal(field.modifiers) || Modifier.isStatic(field.modifiers) }
    }

    private fun hasEveryFieldASimpleGetter(): Boolean {
        val fields = typeAnalyzer.data.fields.values
            .filter { field -> !Modifier.isStatic(field.resolveBinding().modifiers) }
            .map { field -> field.name.fullyQualifiedName }
            .toSet()

        return typeAnalyzer.getSimpleGetters().size == fields.size
    }
}
