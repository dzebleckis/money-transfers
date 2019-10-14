package lt.dzebleckis.application

import lt.dzebleckis.domain.HolderService
import lt.dzebleckis.domain.MoneyDto

sealed class Command {
    private val promise = AsyncResult<String?>()
    fun result(): AsyncResult<String?> = promise
}

data class MakePayment(
    val fromHolderId: String,
    val toHolderId: String,
    val money: MoneyDto
) : Command()

data class OpenAccount(val holderId: String, val currency: String) : Command()
data class CreditAccount(val holderId: String, val money: MoneyDto) : Command()
data class CreateHolder(val name: String, val phone: String) : Command()
object PoisonPill : Command()

class CommandProcessor(
    commandQueue: CommandQueue,
    private val holderService: HolderService
) {

    init {
        do {
            val command = commandQueue.get()
            try {
                val result = handle(command)
                command.result().complete(result)
            } catch (e: Exception) {
                command.result().completeExceptionally(e)
            }
        } while (command != PoisonPill)
    }

    private fun handle(command: Command): String? {
        return when (command) {
            is MakePayment -> {
                holderService.makePayment(command.fromHolderId, command.toHolderId, command.money)
                null
            }
            is CreateHolder -> {
                holderService.createHolder(command.name, command.phone).id
            }
            is OpenAccount -> {
                holderService.openAccount(command.holderId, command.currency).currency
            }
            is CreditAccount -> {
                holderService.creditAccount(command.holderId, command.money)
                null
            }
            PoisonPill -> null
        }
    }
}