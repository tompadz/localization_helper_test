package xml.creator

import locale.Locale
import org.apache.commons.io.FileUtils
import utils.writeToFile
import xml.formatter.XmlFileFormatter
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class AndroidXmlCreator private constructor(
    private val formatters: List<XmlFileFormatter>
): XmlCreator {

    private companion object {
        const val XML_VERSION = "1.0"
        const val XML_ENCODING = "UTF-8"
        const val XML_STANDALONE = "no"
    }

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    override fun createXmlFileFromLocale(locale: Locale): File {
        val tempFileName = locale.language
        val file = utils.createTempFileInAppDir(tempFileName)
        appendDefaultXmlBody(file)
        appendLocaleToXmlFile(file, locale)
        formatters.forEach { it.format(file) }
        return file
    }

    private fun appendLocaleToXmlFile(file: File, locale: Locale): File {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val xmlDocument = documentBuilder.parse(file)
        val notBlankNodes = locale.nodes.filter { !it.isBlank() }
        notBlankNodes.forEach { node ->
            val nodeElement = node.toXmlElement(xmlDocument)
            xmlDocument.documentElement.appendChild(nodeElement)
        }
        return xmlDocument.writeToFile(file)
    }

    private fun appendDefaultXmlBody(file: File): File {
        val information = StringBuilder().apply {
            appendLine(getXmlHeader())
            appendLine("<resources>")
            appendLine("</resources>")
        }.toString()
        file.writeText(information)
        return file
    }

    private fun getXmlHeader(): String {
        return "<?xml version=\"$XML_VERSION\" encoding=\"$XML_ENCODING\" standalone=\"$XML_STANDALONE\"?>"
    }

    class Builder {

        private val formatters = mutableListOf<XmlFileFormatter>()

        fun addFormatter(formatter: XmlFileFormatter): Builder {
            formatters.add(formatter)
            return this
        }

        fun build(): XmlCreator {
            return AndroidXmlCreator(formatters)
        }
    }
}