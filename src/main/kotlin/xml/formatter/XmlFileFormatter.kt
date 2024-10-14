package xml.formatter

import java.io.File

interface XmlFileFormatter {
    fun format(file: File): File
}