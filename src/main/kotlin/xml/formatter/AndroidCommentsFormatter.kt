package xml.formatter

import utils.AndroidCommentsHelper
import java.io.File

class AndroidCommentsFormatter: XmlFileFormatter {
    override fun format(file: File): File {
        return AndroidCommentsHelper().refactorXmlCommentToAndroid(file)
    }
}