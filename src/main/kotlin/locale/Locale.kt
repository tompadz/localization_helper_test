package locale

import xml.parser.locale.XmlToLocaleParser


data class Locale(
    val path: String,
    val language: String,
    val isDefault: Boolean,
) {

    private var _nodes = mutableListOf<LocaleNode>()
    val nodes: List<LocaleNode> get() = _nodes

    fun addNodes(nodes: List<LocaleNode>) {
        _nodes.addAll(nodes)
    }

    fun addNode(key: String, value: String) {
        _nodes.apply {
            val containsIndex = indexOfFirst { it.key == key }
            if (containsIndex != -1) {
                when (val node = get(containsIndex)) {
                    is LocaleNode.Comment -> set(containsIndex, node.copy(value = value))
                    is LocaleNode.String -> set(containsIndex, node.copy(value = value))
                }
            } else {
                if (key.contains("Comment:")) {
                    add(
                        LocaleNode.Comment(
                            key = key.substringAfter(":"),
                            value = value
                        )
                    )
                } else {
                    add(
                        LocaleNode.String(
                            key = key,
                            value = value,
                            translatable = true
                        )
                    )
                }
            }
        }
    }

    companion object {
        fun createFromLanguage(language: String): Locale {
            return Locale(
                path = "",
                language = language,
                isDefault = language == XmlToLocaleParser.LOCALE_DEFAULT
            )
        }
    }
}
