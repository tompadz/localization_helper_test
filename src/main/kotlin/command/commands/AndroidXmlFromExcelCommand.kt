package command.commands

import command.common.CommandArgsException
import command.api.Command
import excel.parser.DefaultExcelParser
import excel.parser.FileToExcelSheetsParser
import excel.sheet.api.SheetCreator
import excel.sheet.impl.DefaultSheetCreator
import utils.AndroidStringFileFinder
import utils.fileName
import xml.creator.AndroidXmlCreator
import xml.creator.XmlCreator
import xml.formatter.AndroidCommentsFormatter
import xml.formatter.AndroidSpacerFormatter
import xml.formatter.EscapingCharactersFormatter
import xml.formatter.UTF8Formatter
import xml.parser.locale.XmlToLocaleParser.Companion.LOCALE_DEFAULT
import xml.parser.xml.AndroidLocaleToXmlParser
import java.io.File

class AndroidXmlFromExcelCommand: Command {

    override fun start(args: List<String>) {
        val excelFileDir: String = args.getOrNull(0) ?: throw CommandArgsException(this)
        val androidProjectDir: String = args.getOrNull(1) ?: throw CommandArgsException(this)

        val parser = getExcelParser()
        val sheetCreator = getSheetCreator()
        val xmlCreator = getXmlCreator()

        val excelSheets = parser.fromFile(File(excelFileDir))
        val localeSheets = sheetCreator.createSheetsFromExcelSheets(excelSheets)
        val androidStringFiles = AndroidStringFileFinder().find(File(androidProjectDir))

        //test only for unistream
        val unistreamLocales = localeSheets.first().locales
        val newXmlFiles = unistreamLocales.map { xmlCreator.createXmlFileFromLocale(it) }

        androidStringFiles.forEach { file ->
            val language = file.path.split("\\").run {
                val unsafeLocale = get(size - 2)
                    .replace("values", "")
                    .replace("-", "")
                unsafeLocale.ifBlank { LOCALE_DEFAULT }
            }
            newXmlFiles.find { it.fileName == language }?.copyTo(file, overwrite = true)
        }
    }

    override fun getInfo(): String {
        return StringBuilder().apply {
            appendLine("Осуществляет генерацию XML-файла локализации для Android-приложения на основе данных из " +
                    "таблицы локализации в Excel. В качестве аргументов требуется указать путь к файлу Excel и путь" +
                    " к директории проекта Android, в которую необходимо сохранить новую локализацию.")
            appendLine("Например: ${getKey()} \".\\..\\locales\\androidAppLocale.xlsx\" \".\\..\\.projects\\myAndroidApp\"")
        }.toString()
    }

    override fun getKey(): String = "-androidXmlFromExcel"

    private fun getExcelParser(): FileToExcelSheetsParser {
        return DefaultExcelParser()
    }

    private fun getSheetCreator(): SheetCreator {
        return DefaultSheetCreator()
    }

    private fun getXmlCreator(): XmlCreator {
        return AndroidXmlCreator.Builder().apply {
            addFormatter(AndroidCommentsFormatter())
            addFormatter(EscapingCharactersFormatter())
            addFormatter(AndroidSpacerFormatter())
            addFormatter(UTF8Formatter())
        }.build()
    }
}