package command

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import rows.XmlLine
import xml_helper.XmlHelper
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream


class CreateExcelCommand(
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
        val sheet = workbook.createSheet(SHEET_NAME)
        println("generate tables")
        createHeader(sheet, locales.keys)
        generateRows(sheet, locales)
        println("generate file")
        createOutputFile(outputPath, workbook)
        println("finish")
    }

    private fun parseLocales(resourcePath: String): Map<String, List<XmlLine>> {

        val locales = mutableMapOf<String, List<XmlLine>>()
        val fileDir = File(resourcePath)

        if (!fileDir.isDirectory) {
            throw Exception("resource path not dir")
        }

        fileDir.listFiles().orEmpty()
            .filter { resFiles ->
                resFiles.name.contains("values")
            }.forEach { resFiles ->

                val strings = resFiles.listFiles()?.find { it.name == "strings.xml" }
                if (strings != null) {

                    val localeName = resFiles.name
                    val xmlRows = strings.readLines().mapNotNull {
                        when {
                            it.contains("<!--") -> XmlLine.Comment(it.trim())
                            it.contains("<string name=") -> {
                                val locale = helper.getLocaleValue(
                                    it.replace(" >", ">")
                                        .replace("< ", "<")
                                        .replace("</ ", "</")
                                        .trim()
                                )
                                if (locale == null) {
                                    null
                                } else {
                                    XmlLine.Locale(locale)
                                }
                            }
                            else -> null
                        }
                    }
                    locales[localeName] = xmlRows
                }
            }
        return locales
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

    private fun generateRows(sheet: Sheet, locales: Map<String, List<XmlLine>>) {
        locales.values.first().forEachIndexed { index, xmlRow ->
            val realIndex = index + 1
            val row = sheet.getRow(realIndex) ?: sheet.createRow(realIndex)
            row.createCell(0).apply {
                if (xmlRow is XmlLine.Locale) {
                    setCellValue(xmlRow.locale.key)
                }
            }
        }
        locales.onEachIndexed { mapIndex, entry ->
            entry.value.forEachIndexed { index, xmlRow ->
                val realIndex = index + 1
                val row = sheet.getRow(realIndex) ?: sheet.createRow(realIndex)
                row.createCell(mapIndex + 1).apply {
                    when (xmlRow) {
                        is XmlLine.Comment -> createComment(sheet, row, this, locales.keys.size, xmlRow)
                        is XmlLine.Locale -> createLocale(row, this, xmlRow)
                    }
                }
            }
        }
    }

    private fun createComment(
        sheet: Sheet,
        row: Row,
        cell: Cell,
        cellCount: Int,
        comment: XmlLine.Comment,
    ) {
        cell.setCellValue(comment.value)
//        sheet.addMergedRegion(CellRangeAddress(row.rowNum, row.rowNum, 0, cellCount - 1))
    }

    private fun createLocale(row: Row, cell: Cell, comment: XmlLine.Locale) {
        cell.setCellValue(comment.locale.value)
    }

    private fun createOutputFile(outputPath: String, workbook: Workbook) {
        val file = Path(outputPath + "locale.xlsx").createFile()
        workbook.write(file.outputStream())
        workbook.close()
    }

}