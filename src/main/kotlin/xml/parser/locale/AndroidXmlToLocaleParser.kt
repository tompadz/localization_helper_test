package xml.parser.locale

import locale.Locale
import locale.LocaleNode
import org.w3c.dom.Element
import org.w3c.dom.Node.ELEMENT_NODE
import utils.AndroidCommentsHelper
import utils.AndroidStringFileFinder
import utils.asString
import xml.parser.locale.XmlToLocaleParser.Companion.LOCALE_DEFAULT
import java.io.File
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory



class AndroidXmlToLocaleParser : XmlToLocaleParser {

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
        val fileWithComments = AndroidCommentsHelper().refactorAndroidCommentToXml(file)
        val document = parseXmlDocument(fileWithComments)
        val nodes = extractLocales(document)
        val locale = file.path.split("\\").run {
            val unsafeLocale = get(size - 2)
                .replace("values", "")
                .replace("-", "")
            unsafeLocale.ifBlank { LOCALE_DEFAULT }
        }
        return Locale(
            path = file.path,
            language = locale,
            isDefault = locale == LOCALE_DEFAULT
        ).apply {
            addNodes(nodes)
        }
    }

    override fun fromProjectDir(file: File): List<Locale> {
        if (!file.exists() || !file.isDirectory) {
            throw IllegalArgumentException("Source directory does not exist or is not a directory.")
        }
        val stringFiles = AndroidStringFileFinder().find(file)
        return stringFiles.map { fromFile(it) }.sortedByDescending { it.language == LOCALE_DEFAULT }
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
                val value = element.asString()
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
}
