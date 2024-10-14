import command.excel.create.CreateExcelCommand
import command.help.HelpCommand
import command.locale.CreateLocaleCommand
import command.utils.CommandType

// locale C:\Users\User\Desktop\testlocale\locale_unistream.xlsx C:\Users\User\Desktop\testlocale\unistream_bank\app\src\unistream
// excel C:\Users\User\Desktop\testlocale\unistream_bank\app\src\main C:\Users\User\Desktop\testlocale

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
        CommandType.CREATE_EXCEL -> CreateExcelCommand(readInput, readOutput)
        CommandType.CREATE_LOCALE -> CreateLocaleCommand(readInput, readOutput)
        CommandType.UNKNOWN -> {
            println("Unknown command, run -help")
            null
        }
    }?.start()
    start()
}
