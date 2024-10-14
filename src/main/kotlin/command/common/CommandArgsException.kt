package command.common

import command.api.Command

class CommandArgsException(
    private val command: Command
): Exception() {

    override val message: String
        get() = "Аргументы команды $command были переданы некорректно. " +
                "Для получения дополнительной информации воспользуйтесь командой \"-help\"."
}