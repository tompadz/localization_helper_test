package command.utils

enum class CommandType(val value: String) {
    HELP("-help"),
    CREATE_EXCEL("excel"),
    CREATE_LOCALE("locale"),
    UNKNOWN("");

    companion object {
        fun findByValue(value: String) = values().find { it.value == value } ?: UNKNOWN
    }
}