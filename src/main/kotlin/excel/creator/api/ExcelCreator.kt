package excel.creator.api

import excel.styles.api.ExcelStyler
import java.io.File

interface ExcelCreator {

    fun createLocaleExcelFile(
        styler: ExcelStyler,

    ): File

}