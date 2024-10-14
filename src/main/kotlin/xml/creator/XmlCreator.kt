package xml.creator

import locale.Locale
import java.io.File

interface XmlCreator {
    fun createXmlFileFromLocale(locale: Locale): File
}