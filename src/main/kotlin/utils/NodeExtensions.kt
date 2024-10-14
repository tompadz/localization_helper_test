package utils

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Node.asString(onlyContent: Boolean = true): String {
    val writer = StringWriter()
    try {
        val trans = TransformerFactory.newInstance().newTransformer()
        trans.setOutputProperty(OutputKeys.INDENT, "yes")
        trans.setOutputProperty(OutputKeys.VERSION, "1.0")
        if (this !is Document) {
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
        trans.transform(DOMSource(this), StreamResult(writer))
    } catch (ex: TransformerConfigurationException) {
        throw IllegalStateException(ex)
    } catch (ex: TransformerException) {
        throw java.lang.IllegalArgumentException(ex)
    }
    val xmlNodeString = writer.toString()
    return if (onlyContent) {
        xmlNodeString.substringAfter("\">").substringBeforeLast("</")
    } else {
        xmlNodeString
    }
}

fun Document.writeToFile(file: File): File {
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
    }
    val source = DOMSource(this)
    val writer = FileWriter(file)
    val result = StreamResult(writer)
    transformer.transform(source, result)
    return file
}

fun NodeList.find(result: (Node) -> Boolean): Node? {
    var node: Node? = null
    for (index in 0 until length) {
        val item = item(index)
        if (result.invoke(item)){
            node = item
            break
        }
    }
    return node
}

fun NodeList.contains(result: (Node) -> Boolean): Boolean {
    var contains = false
    for (index in 0 until length) {
        val node = item(index)
        contains = result.invoke(node)
        if (contains) break
    }
    return contains
}