package excel.parser

import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

class DefaultExcelParser : FileToExcelSheetsParser {
    override fun fromFile(file: File): List<XSSFSheet> {
        val fis = FileInputStream(file)
        val workbook = XSSFWorkbook(fis)
        val sheets = mutableListOf<XSSFSheet>()
        for (index in 0 until workbook.numberOfSheets) {
            sheets.add(workbook.getSheetAt(index))
        }
        return sheets
    }
}