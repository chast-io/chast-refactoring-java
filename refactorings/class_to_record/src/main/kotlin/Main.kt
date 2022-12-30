import spoon.ClassToRecord
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        1 -> ClassToRecord().convert(File(args[0]))
        2 -> ClassToRecord().convert(File(args[0]), File(args[1]))
        else -> println("Usage: java -jar class-to-record.jar <input-file> <output-file>")
    }
}
