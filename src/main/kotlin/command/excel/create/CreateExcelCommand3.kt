package command.excel.create

import command.api.Command
import parser.api.LocaleParser
import parser.xml.AndroidXmlParser
import java.io.File

class CreateExcelCommand3(
    private val inputPath: String,
    private val outputPath: String,
) : Command {

    private val parser: LocaleParser = AndroidXmlParser()

    companion object {
        const val SHEET_NAME = "locale"
    }

    override fun start() {
        val file = File(inputPath)
        val locales = parser.fromFile(file)
    }


}