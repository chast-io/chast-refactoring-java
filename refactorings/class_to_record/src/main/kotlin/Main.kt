import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        1 -> ClassToRecord2().convert(File(args[0]))
        2 -> ClassToRecord2().convert(File(args[0]), File(args[1]))
        else -> println("Usage: java -jar class-to-record.jar <input-file> <output-file>")
    }
}
