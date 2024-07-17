package locale

sealed class LocaleNode(
    open val key: kotlin.String,
) {

    data class Comment(
        override val key: kotlin.String,
        val value: kotlin.String,
    ) : LocaleNode(key)

    data class String(
        override val key: kotlin.String,
        val value: kotlin.String,
        val translatable: Boolean,
    ) : LocaleNode(key)
}