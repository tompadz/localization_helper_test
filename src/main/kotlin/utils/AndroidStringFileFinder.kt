package utils

import java.io.File

class AndroidStringFileFinder {

    fun find(dir: File): List<File> {
        val files = mutableListOf<File>()
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val stringFiles = find(file)
                files.addAll(stringFiles)
            } else {
                if (file.name.contains("strings.xml")) {
                    files.add(file)
                }
            }
        }
        return files
    }

}