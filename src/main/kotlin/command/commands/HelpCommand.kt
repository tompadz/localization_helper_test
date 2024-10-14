package command.commands

import command.provider.CommandsProvider
import command.api.Command
import kotlin.text.StringBuilder

class HelpCommand: Command {

    override fun getInfo(): String {
        return StringBuilder().apply {
            appendLine("Предоставляет сведения о доступных командах")
        }.toString()
    }

    override fun getKey(): String = "-help"

    override fun start(args: List<String>) {
        val message = StringBuilder().apply {
            CommandsProvider.getAllowedCommands().forEach {
                appendLine("${it.getKey()}:")
                appendLine(it.getInfo())
            }
        }.toString()
        println(message)
    }
}