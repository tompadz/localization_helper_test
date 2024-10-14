package xml.parser.xml

import locale.Locale
import locale.LocaleNode
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import utils.AndroidCommentsHelper
import utils.find
import java.io.File
import java.io.FileWriter
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class AndroidLocaleToXmlParser: LocaleToXmlParser {

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    override fun updateXml(locale: Locale, file: File): File {
        val fileSuffix = file.name.substringAfterLast(".")
        if (file.isDirectory || fileSuffix != "xml") {
            throw Exception("The updated file must be in .xml format")
        }
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val xmlDocument = documentBuilder.parse(file)
        val nodes = xmlDocument.documentElement.childNodes
        locale.nodes.filter {
            when (it) {
                is LocaleNode.Comment -> it.value.isNotBlank()
                is LocaleNode.String -> it.value.isNotBlank()
            }
        }.forEach { localeNode ->
            when (localeNode) {
                is LocaleNode.Comment -> {
                    val element = xmlDocument.createElement("comment").apply {
                        setAttribute("name", localeNode.key)
                    }
                    element.appendChild(xmlDocument.createTextNode(localeNode.value))
                    xmlDocument.documentElement.appendChild(element)
                }
                is LocaleNode.String -> {
                    val nodeByKey = nodes.find { it.nodeName == localeNode.key }
                    if (nodeByKey != null) {
                        nodeByKey.textContent = localeNode.value
                    } else {
                        val element = xmlDocument.createElement("string").apply {
                            setAttribute("name", localeNode.key)
                        }
                        element.appendChild(xmlDocument.createTextNode(localeNode.value))
                        xmlDocument.documentElement.appendChild(element)
                    }
                }
            }
        }
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        val source = DOMSource(xmlDocument)
        val writer = FileWriter(file)
        val result = StreamResult(writer)
        transformer.transform(source, result)
        AndroidCommentsHelper().refactorXmlCommentToAndroid(file)
        return file
    }
}