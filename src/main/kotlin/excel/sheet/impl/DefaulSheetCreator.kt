package excel.sheet.impl

import excel.sheet.api.SheetCreator
import locale.Locale
import locale.LocaleSheet
import org.apache.poi.xssf.usermodel.XSSFSheet

class DefaultSheetCreator: SheetCreator {

    override fun createSheetsFromLocales(locales: List<Locale>): List<LocaleSheet> {
        val sheetsMap = mutableMapOf<String, List<Locale>>()
        locales.forEach { locale ->
            val pathSplit = locale.path.split("\\")
            val srcIndex = pathSplit.indexOfLast { it == "src" }
            if (srcIndex == -1) {
                throw Exception("src index in file path not find")
            }
            val name = pathSplit.getOrNull(srcIndex - 1) ?: throw Exception("sheet name in file path not find")
            val tempList = sheetsMap[name] ?: emptyList()
            val newList = tempList.toMutableList().apply {
                add(locale)
            }
            sheetsMap[name] = newList
        }
        return sheetsMap.map {
            LocaleSheet(
                name = it.key,
                locales = it.value
            )
        }
    }

    override fun createSheetsFromExcelSheets(sheets: List<XSSFSheet>): List<LocaleSheet> {
        val sheetsMap = mutableMapOf<String, List<Locale>>()
        sheets.forEach { sheet ->
            val name = sheet.sheetName
            val locales = mutableListOf<Locale>()
            val languageRow = sheet.getRow(0)
            languageRow.forEachIndexed { index, cell ->
                if (index != 0) {
                    locales.add(Locale.createFromLanguage(cell.stringCellValue))
                }
            }
            for (row in sheet) {
                if (row == languageRow) continue
                val keyColumn = row.getCell(0)
                for (column in row) {
                    if (column == keyColumn) continue
                    val columnIndex = column.columnIndex
                    val language = languageRow.getCell(columnIndex).stringCellValue
                    val value = column.stringCellValue
                    val key = keyColumn.stringCellValue
                    val locale = locales.find { it.language == language }
                    locale?.addNode(key, value)
                }
            }
            sheetsMap[name] = locales
        }
        return sheetsMap.map {
            LocaleSheet(
                name = it.key,
                locales = it.value
            )
        }
    }

}