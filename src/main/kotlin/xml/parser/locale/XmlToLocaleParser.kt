package xml.parser.locale

import locale.Locale
import java.io.File

interface XmlToLocaleParser {

    companion object {
        const val LOCALE_DEFAULT = "Default"
    }

    fun fromFile(file: File): Locale
    fun fromProjectDir(file: File): List<Locale>

}