package xml.formatter

import utils.AndroidCommentsHelper
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class AndroidSpacerFormatter: XmlFileFormatter {

    override fun format(file: File): File {
        val commentsHelper = AndroidCommentsHelper()
        val lines = file.readLines()
        val newLines = mutableListOf<String>()
        for (i in lines.indices) {
            val line = lines[i]
            val isComment = commentsHelper.isAndroidComment(line)
            if (isComment) {
                newLines.add("")
            }
            newLines.add(line)
        }
        Files.write(file.toPath(), newLines, StandardCharsets.UTF_8)
        return file
    }
}