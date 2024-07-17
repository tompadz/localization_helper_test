package parser.impl

import locale.Locale
import locale.LocaleNode
import org.w3c.dom.Element
import org.w3c.dom.Node.ELEMENT_NODE
import parser.api.LocaleParser
import parser.api.LocaleParser.Companion.LOCALE_DEFAULT
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory


class AndroidXmlParser : LocaleParser {

    companion object {
        private const val ELEMENT_STRING = "string"
        private const val ELEMENT_COMMENT = "comment"
        private const val ATTRIBUTE_NAME = "name"
        private const val ATTRIBUTE_TRANSLATABLE = "translatable"
    }

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    override fun fromFile(file: File): Locale {
        val fileWithComments = insertCommentsToXml(file)
        val document = parseXmlDocument(fileWithComments)
        val nodes = extractLocales(document)
        val locale = file.path.split("\\").run {
            val unsafeLocale = get(size - 2)
                .replace("values", "")
                .replace("-", "")
            unsafeLocale.ifBlank { LOCALE_DEFAULT }
        }
        return Locale(
            localeNodes = nodes,
            path = file.path,
            locale = locale,
            isDefault = locale == LOCALE_DEFAULT
        )
    }

    override fun fromProjectDir(file: File): List<Locale> {
        if (!file.exists() || !file.isDirectory) {
            throw IllegalArgumentException("Source directory does not exist or is not a directory.")
        }
        val stringFiles = getStringFilesFromDir(file)
        return stringFiles.map { fromFile(it) }.sortedByDescending { it.locale == LOCALE_DEFAULT }
    }

    private fun insertCommentsToXml(xmlFile: File): File {
        val regex = Regex("<!--([\\s\\S]*?)-->")
        val document = documentBuilderFactory.newDocumentBuilder().newDocument()
        val tempFileName = "locale+${xmlFile.path}+${Date().time}"
        val tempFile = File.createTempFile(tempFileName, ".xml")
        Files.copy(xmlFile.toPath(), FileOutputStream(tempFile))
        val linesWithCommentsReplaced = tempFile.readLines(StandardCharsets.UTF_8).map { line ->
            val commentMatcher = regex.find(line)
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

    private fun parseXmlDocument(file: File): org.w3c.dom.Document {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        return documentBuilder.parse(file).apply {
            documentElement.normalize()
        }
    }

    private fun extractLocales(document: org.w3c.dom.Document): List<LocaleNode> {
        val localeNodes = document.documentElement.childNodes
        val locales = mutableListOf<LocaleNode>()
        for (index in 0 until localeNodes.length) {
            val node = localeNodes.item(index)
            if (node.nodeType != ELEMENT_NODE) {
                continue
            }
            val locale = parseLocaleElement(node as Element) ?: continue
            locales.add(locale)
        }
        return locales
    }

    private fun parseLocaleElement(element: Element): LocaleNode? {
        return when (element.nodeName) {
            ELEMENT_STRING -> {
                val key = element.getAttribute(ATTRIBUTE_NAME)
                val translatable = element.getAttribute(ATTRIBUTE_TRANSLATABLE).toBooleanStrictOrNull() ?: true
                //todo remove if need show untranslatable items
                if (!translatable) return null
                val value = element.textContent
                LocaleNode.String(key, value, translatable)
            }
            ELEMENT_COMMENT -> {
                val key = element.getAttribute(ATTRIBUTE_NAME)
                val value = element.textContent
                LocaleNode.Comment(key, value)
            }
            else -> null
        }
    }

    private fun getStringFilesFromDir(dir: File): List<File> {
        val files = mutableListOf<File>()
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val stringFiles = getStringFilesFromDir(file)
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
