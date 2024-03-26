import command.CommandType
import command.CreateExcelCommand
import command.CreateExcelCommand2
import command.HelpCommand

fun main(args: Array<String>) {
    start()
}

private fun start() {
    val readResult = readLine().orEmpty().split("\\s".toRegex())
    val readCommand = readResult.getOrNull(0).orEmpty()
    val readInput = readResult.getOrNull(1).orEmpty()
    val readOutput = readResult.getOrNull(2).orEmpty()
    when (CommandType.findByValue(readCommand)) {
        CommandType.HELP -> HelpCommand()
        CommandType.CREATE_EXCEL -> CreateExcelCommand2(readInput, readOutput)
        CommandType.CREATE_LOCALE -> HelpCommand()
        CommandType.UNKNOWN -> {
            println("Unknown command, run -help")
            null
        }
    }?.start()
    start()
}
