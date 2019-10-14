package lt.dzebleckis.application

import lt.dzebleckis.api.rest.EntityNotFoundException
import lt.dzebleckis.api.rest.v1.AccountResponse
import lt.dzebleckis.api.rest.v1.HolderResponse
import lt.dzebleckis.domain.HolderRepository
import lt.dzebleckis.domain.MoneyDto
import java.math.BigInteger


class HolderService(private val holderRepository: HolderRepository, private val commandQueue: CommandQueue) {

    fun listAccounts(holderId: String): Collection<AccountResponse> {
        return holderRepository.get(holderId).accounts.map { AccountResponse(it.currency, it.balanceInCents) }
    }

    fun getAccount(holderId: String, currency: String): AccountResponse {
        return listAccounts(holderId).find { it.currency == currency }
            ?: throw EntityNotFoundException("No account found for currency: $currency")
    }

    fun createHolder(name: String, phone: String): AsyncResult<String?> {
        require(name.length in 4..20) { "Name length must be between 4 and 20" }
        require(phone.length in 9..15) { "Phone number length must be between 9 and 15" }
        return commandQueue.add(CreateHolder(name, phone))
    }

    fun createAccount(holderId: String, currency: String): AsyncResult<String?> {
        checkCurrency(currency)
        return commandQueue.add(OpenAccount(holderId, currency))
    }

    fun makePayment(
        holderId: String,
        receivingHolderId: String,
        cents: BigInteger,
        currency: String
    ): AsyncResult<Unit> {
        checkCurrency(currency)
        return commandQueue
            .add(MakePayment(holderId, receivingHolderId, MoneyDto(cents, currency)))
            .thenApply { Unit }
    }

    fun getHolder(holderId: String): HolderResponse {
        return holderRepository.get(holderId).let { HolderResponse(it.id, it.name, it.phone) }
    }

    private fun checkCurrency(currency: String) {
        require(currency in setOf("EUR", "USD", "GBP")) { "Not supported currency: $currency" }
    }

    fun creditAccount(holderId: String, cents: BigInteger, currency: String): AsyncResult<Unit> {
        require(cents > 0.toBigInteger()) { "Amount must be greater than zero" }
        return commandQueue.add(CreditAccount(holderId, MoneyDto(cents, currency))).thenApply { Unit }
    }
}