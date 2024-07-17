package excel.styles

import excel.styles.api.ExcelStyler
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFColor
import java.awt.Color


class DefaultExcelStyler : ExcelStyler {

    companion object {
        private val COLOR_RED = ExcelColor(254, 194, 194)
        private val COLOR_BLUE = ExcelColor(47, 117, 181)
    }

    private fun Workbook.createBaseCellStyle(
        cellScope: (CellStyle.() -> Unit) = {}
    ): CellStyle = this.createCellStyle()
        .apply {
            wrapText = true
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
        }
        .apply(cellScope)


    override fun getHeaderCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            locked = true
        }
    }

    override fun getUntranslatableCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }

    override fun getDefaultCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle()
    }

    override fun getNotTranslatedCellStyle(sheet: Sheet): CellStyle {
        return sheet.workbook.createBaseCellStyle {
            setFillForegroundColor(getColor(COLOR_RED))
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
            color = IndexedColors.BLUE.index
        }

        val specialCharacters = listOf(
            "\\n", "%s", "%d", "%f", "&", "\\t", "<", ">"
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

    private fun getColor(color: ExcelColor): XSSFColor {
        val rgb = byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte())
        return XSSFColor(rgb, null)
    }

    private data class ExcelColor(
        val r: Int,
        val g: Int,
        val b: Int
    ) {
        fun toJavaColor() = Color(r, g, b)
    }
}