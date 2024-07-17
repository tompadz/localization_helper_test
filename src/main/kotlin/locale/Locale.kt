package locale


data class Locale(
    val path: String,
    val locale: String,
    val isDefault: Boolean,
    val localeNodes: List<LocaleNode>
) {
    val pathHash = path.hashCode()
}
