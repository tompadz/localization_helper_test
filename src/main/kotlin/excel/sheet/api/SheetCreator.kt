package excel.sheet.api

import locale.Locale
import locale.LocaleSheet
import org.apache.poi.xssf.usermodel.XSSFSheet

interface SheetCreator {
    fun createSheetsFromLocales(locales: List<Locale>): List<LocaleSheet>
    fun createSheetsFromExcelSheets(sheets: List<XSSFSheet>): List<LocaleSheet>
}