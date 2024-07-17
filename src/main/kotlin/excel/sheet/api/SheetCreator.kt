package excel.sheet.api

import locale.Locale
import locale.LocaleSheet

interface SheetCreator {
    fun createSheets(locales: List<Locale>): List<LocaleSheet>
}