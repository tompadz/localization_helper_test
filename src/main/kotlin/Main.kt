
import command.provider.CommandsProvider
import command.common.ConditionalCommand

fun main(args: Array<String>) {
    start()
}

private fun start() {
    val readResult = readlnOrNull().orEmpty()
    val conditionalCommand = ConditionalCommand.fromString(readResult)
    val command = CommandsProvider.findCommandOrHelp(conditionalCommand.key)
    command.start(conditionalCommand.args)
    start()
}
