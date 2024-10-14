package excel.parser

import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File

interface FileToExcelSheetsParser {
    fun fromFile(file: File): List<XSSFSheet>
}