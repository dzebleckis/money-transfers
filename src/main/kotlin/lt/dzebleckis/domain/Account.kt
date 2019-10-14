package lt.dzebleckis.domain

import java.math.BigInteger

data class Account(override val id: String, val balanceInCents: BigInteger, val currency: String) :
    Entity<String>()
