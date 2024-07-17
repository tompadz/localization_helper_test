package excel.styles.api

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.ss.usermodel.Sheet

interface ExcelStyler {
    fun getHeaderCellStyle(sheet: Sheet): CellStyle
    fun getUntranslatableCellStyle(sheet: Sheet): CellStyle
    fun getDefaultCellStyle(sheet: Sheet): CellStyle
    fun getNotTranslatedCellStyle(sheet: Sheet): CellStyle
    fun getCommentCellStyle(sheet: Sheet): CellStyle
    fun getCellTextWithStyle(sheet: Sheet, text: String): RichTextString
}