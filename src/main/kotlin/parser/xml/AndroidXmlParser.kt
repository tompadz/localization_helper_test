package parser.xml

import locale.Locale
import org.w3c.dom.Element
import org.w3c.dom.Node.ELEMENT_NODE
import parser.api.LocaleParser
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory


class AndroidXmlParser : LocaleParser {

    companion object {
        private const val ELEMENT_STRING = "string"
        private const val ELEMENT_COMMENT = "comment"
        private const val ATTRIBUTE_NAME = "name"
        private const val ATTRIBUTE_TRANSLATABLE = "translatable"
        private const val DIR_VALUES = "values"
        private const val DIR_STRINGS = ""
    }

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    override fun fromFile(file: File): Map<String, Locale> {
        val fileWithComments = insertCommentsToXml(file)
        val document = parseXmlDocument(fileWithComments)
        return extractLocales(document)
    }

    override fun fromProjectDir(file: File): Set<Map<String, Locale>> {
        TODO("Not yet implemented")
    }

    private fun insertCommentsToXml(xmlFile: File): File {
        val regex = Regex("<!--([\\s\\S]*?)-->")
        val document = documentBuilderFactory.newDocumentBuilder().newDocument()

        val linesWithCommentsReplaced = Files.lines(xmlFile.toPath(), StandardCharsets.UTF_8).use { lines ->
            lines.map { line ->
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
            }.collect(Collectors.toList())
        }

        Files.write(xmlFile.toPath(), linesWithCommentsReplaced, StandardCharsets.UTF_8)
        return xmlFile
    }

    private fun parseXmlDocument(file: File): org.w3c.dom.Document {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        return documentBuilder.parse(file).apply {
            documentElement.normalize()
        }
    }

    private fun extractLocales(document: org.w3c.dom.Document): Map<String, Locale> {
        val localeNodes = document.documentElement.childNodes
        val locales = mutableMapOf<String, Locale>()

        for (index in 0 until localeNodes.length) {
            val node = localeNodes.item(index)
            if (node.nodeType != ELEMENT_NODE) {
                continue
            }
            val locale = parseLocaleElement(node as Element) ?: continue
            locales[locale.key] = locale
        }

        return locales
    }

    private fun parseLocaleElement(element: Element): Locale? {
        return when (element.nodeName) {
            ELEMENT_STRING -> {
                val key = element.getAttribute(ATTRIBUTE_NAME)
                val translatable = element.getAttribute(ATTRIBUTE_TRANSLATABLE).toBooleanStrictOrNull() ?: true
                val value = element.textContent
                Locale.String(key, value, translatable)
            }
            ELEMENT_COMMENT -> {
                val key = element.getAttribute(ATTRIBUTE_NAME)
                val value = element.textContent
                Locale.Comment(key, value)
            }
            else -> null
        }
    }

    private fun findStringsXMLDirectories(sourceDirectory: String): List<File> {
        val sourceDir = File(sourceDirectory)
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            throw IllegalArgumentException("Source directory does not exist or is not a directory.")
        }

        val stringsXMLDirectories = mutableListOf<File>()

        // Сначала добавим директорию стандартной локализации (обычно это values)
        val defaultValueDir = File(sourceDir, "values")
        if (defaultValueDir.exists() && defaultValueDir.isDirectory) {
            stringsXMLDirectories.add(defaultValueDir)
        }

        // Затем добавим директории с другими локализациями
        sourceDir.listFiles()?.forEach { localeDir ->
            if (localeDir.isDirectory && localeDir.name.startsWith("values-") && localeDir != defaultValueDir) {
                stringsXMLDirectories.add(localeDir)
            }
        }

        return stringsXMLDirectories
    }

}
