package lt.dzebleckis.api.rest.v1

import java.math.BigInteger
import java.time.LocalDateTime

data class PaymentData(val recipientId: String, val cents: BigInteger)
data class CreditData(val cents: BigInteger)
data class CreateHolderData(val name: String, val phone: String)
data class OpenAccountData(val currency: String)

data class HolderResponse(val id: String, val name: String, val phone: String)
data class AccountResponse(val currency: String, val cents: BigInteger)
data class TransactionResponse(
    val date: LocalDateTime,
    val paidOut: BigInteger,
    val paidIn: BigInteger,
    val currency: String
)