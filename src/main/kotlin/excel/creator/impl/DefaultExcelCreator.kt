package excel.creator.impl

import excel.creator.api.ExcelCreator
import excel.styles.api.ExcelStyler
import locale.Locale
import locale.LocaleNode
import locale.LocaleSheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

class DefaultExcelCreator: ExcelCreator {

    private lateinit var styler: ExcelStyler

    override fun createLocaleExcelFile(
        outputDir: String,
        sheets: List<LocaleSheet>,
        styler: ExcelStyler
    ): File {
        this.styler = styler
        val workbook = XSSFWorkbook()
        val excelSheets = createSheets(workbook, sheets)
        excelSheets.forEach { sheet ->
            val sheetLocales = sheets.find { it.name == sheet.sheetName }?.locales ?: throw Exception("unknown sheet")
            fillExcelSheet(sheet, sheetLocales)
        }
        val file = Path("$outputDir\\locale.xlsx").createFile()
        workbook.write(file.outputStream())
        workbook.close()
        return file.toFile()
    }

    private fun createSheets(
        workbook: Workbook,
        localeSheets: List<LocaleSheet>
    ): List<Sheet> {
        return localeSheets.map {
            workbook.createSheet(it.name).apply {
                defaultColumnWidth = 50
            }
        }
    }

    private fun fillExcelSheet(sheet: Sheet, locales: List<Locale>) {
        val defaultLocale = locales.first { it.isDefault }
        createSheetHeader(sheet, locales)
        fillKeysCell(sheet, defaultLocale)
        fillLocaleValues(sheet, locales, defaultLocale)
    }

    private fun createSheetHeader(sheet: Sheet, locales: List<Locale>) {
        sheet.createRow(0).apply {
            heightInPoints = 50f
            createCell(0).apply {
                cellStyle = styler.getHeaderCellStyle(sheet)
            }
            locales.forEachIndexed { index, locale ->
                createCell(index + 1).apply {  //+1 because cell 0 will be created before
                    setCellValue(locale.language)
                    cellStyle = styler.getHeaderCellStyle(sheet)
                }
            }
        }
    }

    private fun fillKeysCell(sheet: Sheet, defaultLocale: Locale) {
        val rowCount = defaultLocale.nodes.size
        for (i in 0 until rowCount) {
            val node = defaultLocale.nodes[i]
            sheet.createRow(i + 1).apply {
                createCell(0).apply {
                    when (node) {
                        is LocaleNode.Comment -> {
                            setCellValue("Comment:${node.value}")
                            cellStyle = styler.getCommentCellStyle(sheet)
                        }
                        is LocaleNode.String -> {
                            setCellValue(node.key)
                            cellStyle = if (node.translatable) {
                                styler.getDefaultCellStyle(sheet)
                            } else {
                                styler.getUntranslatableCellStyle(sheet)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fillLocaleValues(sheet: Sheet, locales: List<Locale>, defaultLocale: Locale) {
        defaultLocale.nodes.forEachIndexed { i, defaultNode ->
            val row = sheet.getOrCreateRow(i + 1)
            locales.forEachIndexed { j, locale ->
                val node = locale.nodes.find { it.key == defaultNode.key }
                row.createCell(j + 1).apply {
                    styleLocaleNode(defaultNode, node, this)
                }
            }
        }
    }

    private fun styleLocaleNode(
        defaultNode: LocaleNode,
        node: LocaleNode?,
        cell: Cell
    ) {
        when (node) {
            is LocaleNode.Comment -> {
                cell.setCellValue(node.value)
                cell.cellStyle = styler.getCommentCellStyle(cell.sheet)
            }
            is LocaleNode.String -> {
                val text = styler.getCellTextWithStyle(cell.sheet, node.value)
                cell.setCellValue(text)
                cell.cellStyle = if (node.translatable) {
                    styler.getDefaultCellStyle(cell.sheet)
                } else {
                    styler.getUntranslatableCellStyle(cell.sheet)
                }
            }
            null -> {
                when {
                    defaultNode is LocaleNode.Comment -> {
                        cell.setCellValue(defaultNode.value)
                        cell.cellStyle = styler.getCommentCellStyle(cell.sheet)
                    }
                    defaultNode is LocaleNode.String && !defaultNode.translatable -> {
                        cell.cellStyle = styler.getUntranslatableCellStyle(cell.sheet)
                    }
                    else -> {
                        cell.cellStyle = styler.getNotTranslatedCellStyle(cell.sheet)
                    }
                }
            }
        }
    }

    private fun Sheet.getOrCreateRow(index: Int): Row {
        return getRow(index) ?: createRow(index)
    }

}