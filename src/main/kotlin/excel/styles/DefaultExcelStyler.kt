package excel.styles

import excel.styles.api.ExcelStyler
import org.apache.poi.ss.usermodel.*

class DefaultExcelStyler : ExcelStyler {

    private fun Workbook.createBaseCellStyle(
        cellScope: (CellStyle.() -> Unit) = {}
    ): CellStyle = this.createCellStyle()
        .apply {
            wrapText = true
        }
        .apply(cellScope)

    override fun getUntranslatableCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }

    override fun getDefaultCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle()
    }

    override fun getNotTranslatedCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle {
            fillForegroundColor = IndexedColors.RED.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }

    override fun getCommentCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle {
            fillForegroundColor = IndexedColors.GREY_40_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }

    override fun getCellTextWithStyle(sheet: Sheet, text: String): RichTextString {
        val richText = sheet.workbook.creationHelper.createRichTextString(text)
        val specialFont = sheet.workbook.createFont().apply {
            color = IndexedColors.DARK_YELLOW.index
        }

        val specialCharacters = listOf(
            "\n", "%s", "%d", "%f", "&", "\t", "<", ">", "\"", "'"
        )

        val htmlTagPattern = "<[^>]+>".toRegex()

        for (specialChar in specialCharacters) {
            var startIndex = 0
            var index = text.indexOf(specialChar, startIndex)
            while (index != -1) {
                startIndex = if (specialChar == "<") {
                    val matchResult = htmlTagPattern.find(text, startIndex)
                    if (matchResult != null) {
                        val tagStartIndex = matchResult.range.first
                        val tagEndIndex = matchResult.range.last + 1
                        richText.applyFont(tagStartIndex, tagEndIndex, specialFont)
                        tagEndIndex
                    } else {
                        index + 1
                    }
                } else {
                    richText.applyFont(index, index + specialChar.length, specialFont)
                    index + specialChar.length
                }
                index = text.indexOf(specialChar, startIndex)
            }
        }

        return richText
    }
}