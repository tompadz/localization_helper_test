package command.help

import command.api.Command
import java.lang.StringBuilder

class HelpCommand: Command {
    override fun start() {
        val message = StringBuilder().apply {
            appendLine("locale *input* *output* - generate localization xml files from excel table")
            appendLine("excel *input* *output* - generate excel file from resource dir")
        }.toString()
        println(message)
    }
}