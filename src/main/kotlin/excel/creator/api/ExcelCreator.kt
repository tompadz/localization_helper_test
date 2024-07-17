package excel.creator.api

import excel.styles.api.ExcelStyler
import locale.LocaleSheet
import java.io.File
import java.nio.file.Path

interface ExcelCreator {

    fun createLocaleExcelFile(
        outputDir: String,
        sheets: List<LocaleSheet>,
        styler: ExcelStyler
    ): File

}