package utils

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class AndroidCommentsHelper {

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    companion object {
        private const val ELEMENT_COMMENT = "comment"
        private const val ATTRIBUTE_NAME = "name"
    }

    fun refactorAndroidCommentToXml(xmlFile: File): File {
        val document = documentBuilderFactory.newDocumentBuilder().newDocument()
        val tempFileName = "locale+${xmlFile.path}+${Date().time}"
        val tempFile = File.createTempFile(tempFileName, ".xml")
        Files.copy(xmlFile.toPath(), FileOutputStream(tempFile))
        val linesWithCommentsReplaced = tempFile.readLines(StandardCharsets.UTF_8).map { line ->
            val commentMatcher = findAndroidComment(line)
            if (commentMatcher != null) {
                val commentElement = document.createElement(ELEMENT_COMMENT).apply {
                    setAttribute(ATTRIBUTE_NAME, UUID.randomUUID().toString())
                    textContent = commentMatcher.groupValues[1]
                }
                "<$ELEMENT_COMMENT $ATTRIBUTE_NAME=\"${commentElement.getAttribute(ATTRIBUTE_NAME)}\">${commentElement.textContent}</$ELEMENT_COMMENT>"
            } else {
                line
            }
        }
        Files.write(tempFile.toPath(), linesWithCommentsReplaced, StandardCharsets.UTF_8)
        return tempFile
    }

    fun findAndroidComment(string: String): MatchResult? {
        val regex = Regex("<!--([\\s\\S]*?)-->")
        return regex.find(string)
    }

    fun isAndroidComment(string: String): Boolean {
        return findAndroidComment(string) != null
    }

    fun refactorXmlCommentToAndroid(xmlFile: File): File {
        val tempFileName = "locale+${xmlFile.path}+${Date().time}"
        val tempFile = File.createTempFile(tempFileName, ".xml")
        Files.copy(xmlFile.toPath(), FileOutputStream(tempFile))
        val linesWithCommentsReplaced = tempFile.readLines(StandardCharsets.UTF_8).map { line ->
            val regex = """<comment name="([^"]+)">([^<]+)</comment>""".toRegex()
            regex.replace(line) { matchResult ->
                "<!-- ${matchResult.groups[2]?.value?.trim()} -->"
            }
        }
        Files.write(tempFile.toPath(), linesWithCommentsReplaced, StandardCharsets.UTF_8)
        Files.copy(tempFile.toPath(), FileOutputStream(xmlFile))
        return xmlFile
    }

}