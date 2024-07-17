package command.excel.create

import command.api.Command
import excel.creator.api.ExcelCreator
import excel.creator.impl.DefaultExcelCreator
import excel.sheet.api.SheetCreator
import excel.sheet.impl.AndroidSheetCreator
import excel.styles.DefaultExcelStyler
import excel.styles.api.ExcelStyler
import parser.api.LocaleParser
import parser.impl.AndroidXmlParser
import java.io.File

class CreateExcelCommand(
    private val inputPath: String,
    private val outputPath: String,
) : Command {

    override fun start() {
        val file = File(inputPath)
        val parser = getLocaleParser()
        val excelStyler = getExcelStyler()
        val sheetCreator = getSheetCreator()
        val excelCreator = getExcelCreator()

        val locales = parser.fromProjectDir(file)

        val sheets = sheetCreator.createSheets(locales)
        val excelFile = excelCreator.createLocaleExcelFile(
            outputDir = outputPath,
            sheets = sheets,
            styler = excelStyler
        )

        println("locale excel file path: ${excelFile.path}")
    }

    private fun getLocaleParser(): LocaleParser {
        return AndroidXmlParser()
    }

    private fun getSheetCreator(): SheetCreator {
        return AndroidSheetCreator()
    }

    private fun getExcelCreator(): ExcelCreator {
        return DefaultExcelCreator()
    }

    private fun getExcelStyler(): ExcelStyler {
        return DefaultExcelStyler()
    }
}