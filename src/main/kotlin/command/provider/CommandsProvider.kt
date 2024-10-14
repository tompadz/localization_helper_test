package command.provider

import command.api.Command
import command.commands.AndroidXmlFromExcelCommand
import command.commands.ExcelFromAndroidProjectCommand
import command.commands.HelpCommand

object CommandsProvider {

    fun getAllowedCommands(): List<Command> {
        return buildList {
            add(HelpCommand())
            add(ExcelFromAndroidProjectCommand())
            add(AndroidXmlFromExcelCommand())
        }
    }

    fun findAllowedCommand(commandKey: String): Command? {
        return getAllowedCommands().find { it.getKey() == commandKey }
    }

    fun findCommandOrHelp(line: String): Command {
        return findAllowedCommand(line) ?: HelpCommand()
    }

}