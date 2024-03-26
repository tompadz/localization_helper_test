package xml_helper

class XmlHelper {

    private val regex = "<string name=\"([^\"]*)\"(?: translatable=\"([^\"]*)\")?>([^<]*)<\\/string>".toRegex()

    fun getLocaleValues(xmlString: String): List<Locale> {
        val matches = regex.findAll(xmlString)
        return matches.map {
            val (name, isTranslate, value) = it.destructured
            Locale(
                key = name,
                isTranslatable = isTranslate.toBooleanStrictOrNull() ?: true,
                value = value
            )
        }.toList()
    }

    fun getLocaleValue(xmlLine: String): Locale? {
        val matches = regex.findAll(xmlLine)
        val matcher = matches.firstOrNull() ?: return null
        val (name, isTranslate, value) = matcher.destructured
        return Locale(
            key = name,
            isTranslatable = isTranslate.toBooleanStrictOrNull() ?: true,
            value = value
        )
    }

    data class Locale(
        val key: String,
        val isTranslatable: Boolean,
        val value: String,
    )
}