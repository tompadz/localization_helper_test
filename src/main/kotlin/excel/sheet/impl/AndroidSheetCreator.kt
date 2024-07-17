package excel.sheet.impl

import excel.sheet.api.SheetCreator
import locale.Locale
import locale.LocaleSheet

class AndroidSheetCreator: SheetCreator {

    override fun createSheets(locales: List<Locale>): List<LocaleSheet> {
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
}