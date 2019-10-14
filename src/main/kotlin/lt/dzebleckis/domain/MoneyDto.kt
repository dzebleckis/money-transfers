package lt.dzebleckis.domain

import java.math.BigInteger

data class MoneyDto(val cents: BigInteger, val currency: String)