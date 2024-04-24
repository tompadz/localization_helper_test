package locale

sealed class Locale(
    open val key: kotlin.String,
) {

    data class Comment(
        override val key: kotlin.String,
        val value: kotlin.String,
    ) : Locale(key)

    data class String(
        override val key: kotlin.String,
        val value: kotlin.String,
        val translatable: Boolean,
    ) : Locale(key)
}