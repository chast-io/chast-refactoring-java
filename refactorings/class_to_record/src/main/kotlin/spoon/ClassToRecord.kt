package spoon

import CodeSyntaxVerifier.isValidCode
import FileUtils.writeCodeToFile
import spoon.compiler.Environment
import spoon.reflect.declaration.CtCompilationUnit
import spoon.reflect.declaration.CtRecord
import spoon.support.reflect.declaration.CtClassImpl
import spoon.support.sniper.SniperJavaPrettyPrinter
import java.io.File
import kotlin.streams.asStream

class ClassToRecord {

    fun convert(sourceFile: File, targetFile: File = sourceFile) {
        preConvertCheck(sourceFile)

        val launcher = Launcher()
        launcher.setSourceOutputDirectory(targetFile.parentFile)

        // path can be a folder or a file
        // addInputResource can be called several times
        launcher.addInputResource(sourceFile.absolutePath)
        launcher.environment.complianceLevel = 17
        launcher.environment.isAutoImports = true
        launcher.environment.prettyPrintingMode = Environment.PRETTY_PRINTING_MODE.AUTOIMPORT
        launcher.environment.isIgnoreDuplicateDeclarations = true
        launcher.environment.setCommentEnabled(true)
        launcher.environment.isPreserveLineNumbers = false

        // SniperJavaPrettyPrinter currently not used because it has troubles with newlines.
//        launcher.environment.setPrettyPrinterCreator { SniperJavaPrettyPrinter(launcher.environment) }

        println("Start building model... ")
        launcher.buildModel()
        println("Model built.")

        val model = launcher.model

        model.allTypes.asSequence()
            .filterIsInstance<CtClassImpl<*>>()
            .asStream().parallel()
            .filter { ClassToRecordVerifier(it).isConvertableClass() }
            .map {
                val record: CtRecord = ClassToRecordConverter(it).convert()
                it.replace(record)
                it.position.compilationUnit
            }
            .forEach {
//                val formattedCode = getTypeSource(it, launcher.environment)
                val formattedCode = it.prettyprint()
                val (validCode, diagnosticCollector) = isValidCode(formattedCode)
                if (validCode) {
                    writeCodeToFile(formattedCode, targetFile)
                } else {
                    println("Invalid code: ")

                    for (error in diagnosticCollector.diagnostics) {
                        println(error)
                    }
                }
            }
    }

    private fun preConvertCheck(sourceFile: File) {
        if (!sourceFile.exists()) throw IllegalArgumentException("File does not exist: ${sourceFile.absolutePath}")
        if (!sourceFile.isFile) throw IllegalArgumentException("File is not a file: ${sourceFile.absolutePath}")
        if (!sourceFile.canRead()) throw IllegalArgumentException("File is not readable: ${sourceFile.absolutePath}")
        val (validCode, diagnosticCollector) = isValidCode(sourceFile.readText())
        if (!validCode) {
            for (diagnostic in diagnosticCollector.diagnostics) {
                println(diagnostic)
            }
            throw IllegalArgumentException("File is not valid code: ${sourceFile.absolutePath}")
        }
    }

    // SniperJavaPrettyPrinter currently not used because it has troubles with newlines.
    private fun getTypeSource(cu: CtCompilationUnit, env: Environment): String {
        return SniperJavaPrettyPrinter(env).printCompilationUnit(cu).trim()
    }
}
