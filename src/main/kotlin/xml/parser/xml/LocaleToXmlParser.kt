package xml.parser.xml

import locale.Locale
import java.io.File

interface LocaleToXmlParser {
    fun updateXml(locale: Locale, file: File): File
}