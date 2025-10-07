package dev.jsinco.recipes.util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

    fun saveResourceIfExists(internalLocation: String, destinationFile: File, replace: Boolean) {
        try {
            FileUtil.javaClass.getResourceAsStream(internalLocation)
                ?.use { inputStream ->
                    makeFile(destinationFile)
                    if (!replace && destinationFile.exists()) {
                        return
                    }
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.transferTo(outputStream)
                    }
                }
        } catch (e: IOException) {
            Logger.logErr(e)
        }
    }

    @Throws(IOException::class)
    fun makeFile(file: File) {
        val dir = file.parentFile
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Can not create parent directory for: $file")
        }
        if (!file.exists() && !file.createNewFile()) {
            throw IOException("Can not create new file: $file")
        }
    }
}