package command.commands
import command.common.CommandArgsException
import command.api.Command
import excel.creator.api.ExcelCreator
import excel.creator.impl.DefaultExcelCreator
import excel.sheet.api.SheetCreator
import excel.sheet.impl.DefaultSheetCreator
import excel.styles.DefaultExcelStyler
import excel.styles.api.ExcelStyler
import xml.parser.locale.AndroidXmlToLocaleParser
import xml.parser.locale.XmlToLocaleParser
import java.io.File

class ExcelFromAndroidProjectCommand: Command {

    companion object {
        const val DEFAULT_EXCEL_FILE_NAME = "androidExcelLocale"
    }

    override fun start(args: List<String>) {
        val androidProjectDir: String = args.getOrNull(0) ?: throw CommandArgsException(this)
        val excelOutputDir: String = args.getOrNull(1) ?: throw CommandArgsException(this)
        val excelFileName: String = args.getOrNull(2) ?: DEFAULT_EXCEL_FILE_NAME

        val parser = getLocaleParser()
        val excelStyler = getExcelStyler()
        val sheetCreator = getSheetCreator()
        val excelCreator = getExcelCreator()

        val androidProjectDirFile = File(androidProjectDir)

        val locales = parser.fromProjectDir(androidProjectDirFile)
        val sheets = sheetCreator.createSheetsFromLocales(locales)
        val excelFile = excelCreator.createLocaleExcelFile(
            outputDir = excelOutputDir,
            excelFileName = excelFileName,
            sheets = sheets,
            styler = excelStyler
        )

        println("locale excel file path: ${excelFile.path}")
    }

    override fun getInfo(): String {
        return StringBuilder().apply {
            appendLine("Создаёт файл Excel, используя локализацию из проекта Android-приложения. " +
                    "В качестве аргументов необходимо указать директорию Android-приложения и директорию, " +
                    "в которую будет сохранён файл Excel. В качестве третьего необязательного параметра можно " +
                    "указать имя файла.")
            appendLine("Например: ${getKey()} \".\\..\\projects\\myAndroidApp\" \".\\..\\desktop\\locales\\myAndroidAppLocales\" \"appLocales\"")
        }.toString()
    }

    override fun getKey(): String = "-excelFromAndroidDir"

    private fun getLocaleParser(): XmlToLocaleParser {
        return AndroidXmlToLocaleParser()
    }

    private fun getSheetCreator(): SheetCreator {
        return DefaultSheetCreator()
    }

    private fun getExcelCreator(): ExcelCreator {
        return DefaultExcelCreator()
    }

    private fun getExcelStyler(): ExcelStyler {
        return DefaultExcelStyler()
    }
}

