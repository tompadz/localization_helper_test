package parser.api

import locale.Locale
import java.io.File

interface LocaleParser {
    fun fromFile(file: File): Map<String, Locale>
    fun fromProjectDir(file: File): Set<Map<String, Locale>>
}