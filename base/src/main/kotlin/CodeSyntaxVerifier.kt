import java.net.URI
import javax.tools.*

object CodeSyntaxVerifier {
    fun isValidCode(code: String): Pair<Boolean, DiagnosticCollector<JavaFileObject>> {
        print("Verifying code... ")
        val success = compile(code)
        if (success.first) {
            println("Code can be compiled - Verification passed!")
        } else {
            println("Code cannot be compiled - Verification failed!")
        }
        return success
    }

    private fun compile(code: String): Pair<Boolean, DiagnosticCollector<JavaFileObject>> {
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        val compileOptions = arrayOf("-d", "bin").toList()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val compilationUnits: Iterable<JavaFileObject> = listOf(JavaSourceFromString("HelloWorld", code))
        val task: JavaCompiler.CompilationTask = compiler.getTask(
            null,
            null,
            diagnostics,
            compileOptions,
            null,
            compilationUnits
        )
        val success: Boolean = task.call()

        return success to diagnostics
    }

    private class JavaSourceFromString(name: String, val code: String) :
        SimpleJavaFileObject(
            URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
            JavaFileObject.Kind.SOURCE
        ) {
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
            return code
        }
    }
}
