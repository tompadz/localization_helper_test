package locale

import org.w3c.dom.Document
import org.w3c.dom.Element

sealed class LocaleNode(
    open val key: kotlin.String,
) {

    abstract fun isBlank(): Boolean
    abstract fun toXmlElement(document: Document): Element

    data class Comment(
        override val key: kotlin.String,
        val value: kotlin.String,
    ) : LocaleNode(key) {

        override fun isBlank(): Boolean {
            return value.isBlank()
        }

        override fun toXmlElement(document: Document): Element {
            return document.createElement("comment").apply {
                setAttribute("name", key)
                appendChild(document.createTextNode(value))
            }
        }
    }

    data class String(
        override val key: kotlin.String,
        val value: kotlin.String,
        val translatable: Boolean,
    ) : LocaleNode(key) {

        override fun isBlank(): Boolean {
            return value.isBlank()
        }

        override fun toXmlElement(document: Document): Element {
            return document.createElement("string").apply {
                setAttribute("name", key)
                appendChild(document.createTextNode(value))
            }
        }
    }
}