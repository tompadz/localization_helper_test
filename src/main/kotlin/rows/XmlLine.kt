package rows

import xml_helper.XmlHelper

sealed class XmlLine {
    data class Comment(val value: String): XmlLine()
    data class Locale(val locale: XmlHelper.Locale): XmlLine()
}
