import spoon.Launcher
import spoon.compiler.Environment
import spoon.refactoring.Refactoring
import spoon.reflect.declaration.CtCompilationUnit
import spoon.reflect.declaration.CtRecord
import spoon.support.reflect.declaration.CtClassImpl
import spoon.support.sniper.SniperJavaPrettyPrinter
import java.io.File
import kotlin.streams.asStream

class ClassToRecord2 {

    public fun convert(sourceFile: File, targetFile: File = sourceFile) {
        val launcher = Launcher()
        launcher.setSourceOutputDirectory(targetFile.parentFile)

        // path can be a folder or a file
        // addInputResource can be called several times
        launcher.addInputResource(sourceFile.absolutePath)
        launcher.environment.complianceLevel = 17
        launcher.environment.isAutoImports = true
        launcher.environment.isIgnoreDuplicateDeclarations = true
        launcher.environment.setCommentEnabled(true)
        launcher.environment.isPreserveLineNumbers = false
//        launcher.environment.ignoreSyntaxErrors = true
        launcher.environment.setPrettyPrinterCreator { SniperJavaPrettyPrinter(launcher.environment) }

        println("Start building model... ")
        launcher.buildModel()
        println("Model built.")

        val model = launcher.model

        model.allTypes.asSequence()
            .filterIsInstance<CtClassImpl<*>>()
            .asStream().parallel()
            .filter { ClassToRecordVerifier2(it).isConvertableClass() }
            .map {
                val record: CtRecord = ClassToRecordConverter2(it).convert()
                it.replace(record)
                it.position.compilationUnit
            }
            .forEach { println(getTypeSource(it, launcher.environment)) }
    }

    private fun getTypeSource(cu: CtCompilationUnit, env: Environment): String {
        return SniperJavaPrettyPrinter(env).printCompilationUnit(cu).trim()
    }
}

fun main() {


}
