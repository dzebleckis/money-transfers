package lt.dzebleckis.domain

import java.math.BigInteger

class Holder(override val id: String, val name: String, val phone: String, val accounts: Set<Account>) :
    Entity<String>() {

    init {
        require(accounts.groupBy { it.currency }.size == accounts.size) { "Accounts with same currency are not allowed" }
    }

    fun debit(money: MoneyDto): Holder {
        require(money.cents > 0.toBigInteger()) { "Amount should be greater than zero" }

        val account = accounts.find { it.currency == money.currency }
            ?: throw IllegalArgumentException("Holder has no account for currency ${money.currency}")

        require(account.balanceInCents >= money.cents) { "Not enough funds in account" }

        return updateBalance(account.id, account.balanceInCents.minus(money.cents))
    }

    fun credit(money: MoneyDto): Holder {
        require(money.cents > 0.toBigInteger()) { "Amount should be greater than zero" }

        val account = accountForCurrency(money.currency)
        return if (account != null) {
            updateBalance(account.id, account.balanceInCents.plus(money.cents))
        } else {
            openAccount(money.currency).credit(money)
        }
    }

    private fun updateBalance(accountId: String, balanceInCents: BigInteger): Holder {
        require(accounts.find { it.id == accountId } != null) { "Can not update non existing account" }
        return Holder(
            id = this.id,
            name = this.name,
            phone = this.phone,
            accounts = accounts.map {
                if (it.id == accountId) {
                    Account(it.id, balanceInCents, it.currency)
                } else {
                    it
                }
            }.toSet()
        )
    }

    fun openAccount(currency: String): Holder {
        val existing = accounts.find { it.currency == currency }
        return if (existing != null) {
            this
        } else {
            Holder(
                id = id,
                name = name,
                phone = phone,
                accounts = accounts + Account(nextAccountId(), 0.toBigInteger(), currency)
            )
        }
    }

    private fun accountForCurrency(currency: String): Account? {
        return accounts.find { it.currency == currency }
    }

    private fun nextAccountId(): String {
        return "a-${accounts.size}"
    }
}