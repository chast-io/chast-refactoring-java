package spoonExtensions

import spoon.reflect.declaration.CtClass

class CtClassExtension<T>(private val clazz: CtClass<T>) : CtClass<T> by clazz {
    val staticFields
        get() = clazz.fields.asSequence()
            .filter { it.isStatic }

    val nonStaticFields
        get() = clazz.fields.asSequence()
            .filter { !it.isStatic }

    val simpleGetters
        get() = clazz.methods.asSequence()
            .filter { it.extension().isSimpleGetter }

    val nonSimpleGetters
        get() = clazz.methods.asSequence()
            .filter { !it.extension().isSimpleGetter }
}

fun <T> CtClass<T>.extension() = CtClassExtension(this)
