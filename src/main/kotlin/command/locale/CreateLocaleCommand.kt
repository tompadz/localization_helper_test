package command.locale

import command.api.Command
import excel.sheet.api.SheetCreator
import excel.sheet.impl.DefaultSheetCreator
import excel.parser.FileToExcelSheetsParser
import excel.parser.DefaultExcelParser
import utils.AndroidStringFileFinder
import xml.parser.locale.XmlToLocaleParser.Companion.LOCALE_DEFAULT
import xml.parser.xml.AndroidLocaleToXmlParser
import java.io.File

class CreateLocaleCommand(
    private val excelPath: String,
    private val androidPath: String
) : Command {

    override fun start() {
        val excelFile = File(excelPath)
        val androidFile = File(androidPath)
        val parser = getExcelParser()
        val sheetCreator = getSheetCreator()

        val excelSheets = parser.fromFile(excelFile)
        val locales = sheetCreator.createSheetsFromExcelSheets(excelSheets)
        val files = AndroidStringFileFinder().find(androidFile)

        val testLocales = locales.first().locales

        files.forEach { file ->
            val language = file.path.split("\\").run {
                val unsafeLocale = get(size - 2)
                    .replace("values", "")
                    .replace("-", "")
                unsafeLocale.ifBlank { LOCALE_DEFAULT }
            }
            println("test file language: $language")
            val locale = testLocales.find { it.language == language }
            if (locale != null) {
                println("test locale: ${locale.language}")
                AndroidLocaleToXmlParser().updateXml(locale, file)
            }
        }

        println("excel to xml success update")
    }

    private fun getExcelParser(): FileToExcelSheetsParser {
        return DefaultExcelParser()
    }

    private fun getSheetCreator(): SheetCreator {
        return DefaultSheetCreator()
    }
}