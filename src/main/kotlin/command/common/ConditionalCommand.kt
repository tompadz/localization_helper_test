package command.common

data class ConditionalCommand(
    val key: String,
    val args: List<String>
) {
    companion object {
        fun fromString(value: String): ConditionalCommand {
            val valueArray = value.split("\\s".toRegex())
            val argsRegex = "\"([^\"]*)\"".toRegex()
            return ConditionalCommand(
                key = valueArray.firstOrNull().orEmpty(),
                args = runCatching {
                    argsRegex.findAll(value).mapNotNull { it.groups[1]?.value }.toList()
                }.getOrDefault(emptyList())
            )
        }
    }
}