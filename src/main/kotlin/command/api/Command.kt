package command.api

interface Command {
    fun getInfo(): String
    fun getKey(): String
    fun start(args: List<String>)
}