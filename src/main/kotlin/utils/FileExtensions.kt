package utils

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.*


val File.fileName get() = this.name
    .substringAfterLast("\\")
    .substringBeforeLast(".")

val File.suffix get() = this.name
    .substringAfterLast(".")

fun createTempFileInAppDir(
    name: String = "tempLHFile+${Date().time}",
    suffix: String = ".xml"
): File {
    val dirName = "localization_helper"
    File(FileUtils.getTempDirectory().absolutePath + "\\$dirName").apply {
        mkdirs()
    }
    return createTempFile(dirName + "\\" + name , suffix)
}

fun createTempFile(prefix: String, suffix: String): File {
    val parent = File(System.getProperty("java.io.tmpdir"))
    val temp = File(parent, prefix + suffix)
    if (temp.exists()) {
        temp.delete()
    }
    try {
        temp.createNewFile()
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return temp
}


fun createTempDirectory(fileName: String?): File {
    val parent = File(System.getProperty("java.io.tmpdir"))
    val temp = File(parent, fileName)
    if (temp.exists()) {
        temp.delete()
    }
    temp.mkdir()
    return temp
}