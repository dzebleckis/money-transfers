package lt.dzebleckis

import lt.dzebleckis.domain.Account
import lt.dzebleckis.domain.Holder

object Utils {
    fun holder(
        id: String = "hi",
        name: String = "n1",
        phone: String = "n1",
        accounts: Set<Account> = emptySet()
    ): Holder {
        return Holder(id, name, phone, accounts)
    }

    fun account(id: String = "a1", cents: Int = 100, currency: String = "EUR"): Account {
        return Account(id, cents.toBigInteger(), currency)
    }
}