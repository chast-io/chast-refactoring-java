import java.io.File

object FileUtils {
    fun writeCodeToFile(code: String, targetFile: File) {
        targetFile.parentFile.mkdirs()
        targetFile.createNewFile()
        targetFile.writeText(code)
    }
}
