import spoon.reflect.declaration.*
import spoon.reflect.reference.CtTypeReference
import spoon.support.reflect.declaration.CtRecordComponentImpl
import spoonExtensions.CtClassExtension
import spoonExtensions.extension

internal class ClassToRecordConverter2(
    private val clazz: CtClass<*>,
) {
    private val record: CtRecord = createRecord()
    private val classExtension: CtClassExtension<*>
        get() = clazz.extension()

    fun convert(): CtRecord {
        copyName()
        copyModifiers()

        copyClassAttributes()

        copyRecordComponents()
        copySuperInterfaceTypes()

        copyStaticFields()
        copyConstructors()
        copyMethods()

        copyNestedStatements()

        copyUnhandledStatements()

        return record
    }

    private fun createRecord() = classExtension.factory.createRecord()


    private fun copyClassAttributes() {
        record.setComments<CtRecord>(classExtension.comments)
        if (classExtension.docComment != null && classExtension.docComment.isNotBlank()) record.setDocComment<CtRecord>(
            classExtension.docComment
        )
        record.setAnnotations<CtRecord>(classExtension.annotations)

        record.setFormalCtTypeParameters<CtRecord>(classExtension.formalCtTypeParameters)

        record.setAllMetadata<CtRecord>(classExtension.allMetadata)
        record.setLabel<CtRecord>(classExtension.label)

        record.setParent<CtRecord>(classExtension.parent)
        record.setPosition<CtRecord>(classExtension.position)

    }

    private fun copyName() {
        record.setSimpleName<CtRecord>(classExtension.simpleName)
    }

    private fun copyModifiers() {
        val modifiers = classExtension.modifiers.asSequence()
            .filter {
                it !in setOf(
                    ModifierKind.PRIVATE,
                    ModifierKind.PROTECTED,
                    ModifierKind.STATIC,
                    ModifierKind.FINAL
                )
            }
            .toSet()

        record.setModifiers<CtRecord>(modifiers)
    }

    /**
     * All fields that are not static are converted to record components.
     * The fields must be final, otherwise the class can not be converted to a record.
     */
    private fun copyRecordComponents() {
        classExtension.nonStaticFields
            .map {
                val component = CtRecordComponentImpl()
                component.setSimpleName<CtRecordComponent>(it.simpleName)
                component.setType<CtRecordComponent>(it.type as CtTypeReference<Any>)
                component
            }
            .forEach { record.addRecordComponent(it) }
    }

    private fun copySuperInterfaceTypes() {
        record.setSuperInterfaces<CtRecord>(classExtension.superInterfaces)
    }

    private fun copyStaticFields() {
        val fields = classExtension.staticFields
            .toList()

        record.setFields<CtRecord>(fields)
    }

    private fun copyConstructors() {
        classExtension.typeMembers.asSequence()
            .filterIsInstance<CtConstructor<*>>()
            .map { RecordConstructorConverter(it.extension(), classExtension).convert() }
            .filterNotNull()
            .forEach { record.addConstructor<CtRecord>(it as CtConstructor<Any>?) }
    }

    /**
     * Copies all methods that are not simple getters.
     * (Setter methods must not be present in records as all fields are read only.)
     */
    private fun copyMethods() {
        val methods = classExtension.nonSimpleGetters
            .toCollection(linkedSetOf())

        // TODO drop equals, hashCode and toString methods (maybe make it optional?)

        record.setMethods<CtRecord>(methods)
    }

    private fun copyNestedStatements() {
        record.setNestedTypes<CtRecord>(classExtension.nestedTypes)
    }

    /**
     * Copies all statements that do not need special record treatment.
     *
     * TODO: include CtAnonymousExecutable aka. static initializers => https://github.com/INRIA/spoon/issues/4961
     */
    private fun copyUnhandledStatements() {
        classExtension.typeMembers
            .filter { d ->
                d !is CtField<*>
                        && d !is CtMethod<*>
                        && d !is CtConstructor<*>
                        && d !is CtAnonymousExecutable
            }
            .forEach { record.addTypeMember<CtType<Any>>(it) }
    }
}
