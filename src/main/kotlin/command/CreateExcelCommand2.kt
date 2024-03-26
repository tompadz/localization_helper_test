package command

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import rows.XmlLine
import xml_helper.XmlHelper
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream


class CreateExcelCommand2(
    private val inputPath: String,
    private val outputPath: String,
) : Command {

    private val helper = XmlHelper()

    companion object {
        const val SHEET_NAME = "locale"
    }

    override fun start() {
        println("parse locales xml")
        val locales = parseLocales(inputPath)
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(SHEET_NAME).apply {
            defaultColumnWidth = 50
        }
        println("create table header")
        createHeader(sheet, locales.keys.sortedByDescending { it == "original" }.toSet())
        println("create main locale rows")
        generateMainLocaleRow(sheet, locales["original"]!!)
        println("generate locales rows")
        generateLocalesRow(sheet, locales)
        println("generate file")
        createOutputFile(outputPath, workbook)
        println("finish")
    }

    private fun parseLocales(resourcePath: String): Map<String, List<XmlLine>> {
        val locales = mutableMapOf<String, List<XmlLine>>()
        val fileDir = File(resourcePath)

        require(fileDir.isDirectory) { "Resource path is not a directory" }

        fileDir.listFiles()?.forEach { resFiles ->
            if (resFiles.name.contains("values")) {
                val strings = resFiles.listFiles()?.find { it.name == "strings.xml" }
                strings?.let {
                    val localeName = if (resFiles.name == "values") {
                        "original"
                    } else {
                        resFiles.name.substringAfter("-")
                    }
                    println("try parse $localeName locales xml")
                    val xmlRows = parseXmlLines(strings.readLines())
                    locales[localeName] = xmlRows
                }
            }
        }

        return locales
    }

    private fun parseXmlLines(lines: List<String>): List<XmlLine> {
        return lines.mapNotNull { line ->
            when {
                line.contains("<!--") -> XmlLine.Comment(line.trim())
                line.contains("<string name=") -> {
                    val locale = helper.getLocaleValue(
                        line.replace(" >", ">")
                            .replace("< ", "<")
                            .replace("</ ", "</")
                            .trim()
                    )
                    locale?.let { XmlLine.Locale(it) }
                }
                else -> null
            }
        }
    }

    private fun createHeader(sheet: Sheet, locales: Set<String>) {
        sheet.createRow(0).apply {
            createCell(0).apply {
                setCellValue("keys")
            }
            locales.forEachIndexed { index, s ->
                createCell(index + 1).apply {
                    setCellValue(s)
                }
            }
        }
    }

    private fun generateMainLocaleRow(sheet: Sheet, locales: List<XmlLine>) {
        locales.forEachIndexed { index, xmlLine ->
            val row = sheet.getOrCreateRow(index + 1)
            row.createCell(0).apply {
                when (xmlLine) {
                    is XmlLine.Comment -> {
                        cellStyle = sheet.getCommentStyle()
                        setCellValue(xmlLine.value)
                    }
                    is XmlLine.Locale -> {
                        setCellValue(xmlLine.locale.key)
                    }
                }
            }
            row.createCell(1).apply {
                when (xmlLine) {
                    is XmlLine.Comment -> {
                        cellStyle = sheet.getCommentStyle()
                        setCellValue(xmlLine.value)
                    }
                    is XmlLine.Locale -> {
                        val value = xmlLine.locale.value
                        if (value.isNotBlank()) {
                            cellStyle = sheet.getDefaultStyle()
                            setCellValue(
                                sheet.findSymbolsInText(value)
                            )
                        } else {
                            cellStyle = sheet.getErrorStyle()
                            setCellValue("EMPTY")
                        }
                    }
                }
            }
        }
    }

    private fun generateLocalesRow(sheet: Sheet, locales: Map<String, List<XmlLine>>) {
        val originalLocale = locales["original"]!!
        locales.filter { it.key != "original" }.onEachIndexed { index, entry ->
            originalLocale.forEachIndexed { entryIndex, xmlLine ->
                val row = sheet.getOrCreateRow(entryIndex + 1)
                row.createCell(index + 2).apply {
                    when (xmlLine) {
                        is XmlLine.Comment -> {
                            cellStyle = sheet.getCommentStyle()
                            setCellValue(xmlLine.value)
                        }
                        is XmlLine.Locale -> {
                            if (xmlLine.locale.isTranslatable) {
                                val locale = entry.value.filterIsInstance<XmlLine.Locale>().find { it.locale.key == xmlLine.locale.key }
                                val value = locale?.locale?.value
                                if (!value.isNullOrBlank()) {
                                    cellStyle = sheet.getDefaultStyle()
                                    setCellValue(
                                        sheet.findSymbolsInText(locale.locale.value)
                                    )
                                } else {
                                    cellStyle = sheet.getErrorStyle()
                                    setCellValue("EMPTY")
                                }
                            } else {
                                cellStyle = sheet.getUntranslatableStyle()
                                setCellValue("untranslatable")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun createOutputFile(outputPath: String, workbook: Workbook) {
        val file = Path(outputPath + "locale.xlsx").createFile()
        workbook.write(file.outputStream())
        workbook.close()
    }

    fun Sheet.getErrorStyle() = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.RED.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    fun Sheet.getUntranslatableStyle() = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    fun Sheet.getDefaultStyle() = workbook.createCellStyle().apply {
        wrapText = true

    }

    fun Sheet.findSymbolsInText(text: String): RichTextString {
        val richText = workbook.creationHelper.createRichTextString(text)
        val specialFont = workbook.createFont().apply {
            color = IndexedColors.DARK_YELLOW.index
        }

        var startIndex = 0
        var index = text.indexOf("%s", startIndex)
        while (index != -1) {
            richText.applyFont(index, index + 2, specialFont)
            startIndex = index + 1
            index = text.indexOf("\n", startIndex)
        }

        return richText
    }

    fun Sheet.getCommentStyle() = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.GREY_40_PERCENT.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    fun Sheet.getOrCreateRow(index: Int): Row {
        return getRow(index) ?: createRow(index)
    }
}