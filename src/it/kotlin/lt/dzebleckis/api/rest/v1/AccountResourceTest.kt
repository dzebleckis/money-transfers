package lt.dzebleckis.api.rest.v1

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

internal class AccountResourceTest : ResourceTest() {

    @Test
    fun accountOperations() {
        val holderCreation = target("/holders")
            .request()
            .post(Entity.entity(CreateHolderData("name", "1234567891"), MediaType.APPLICATION_JSON_TYPE))

        assertEquals(Response.Status.CREATED.statusCode, holderCreation.status)
        val location = holderCreation.getHeaderString("Location")
        val holderId = location.split("/").last()

        val accountCreation = target("holders/$holderId/accounts")
            .request()
            .post(Entity.entity(OpenAccountData("EUR"), MediaType.APPLICATION_JSON_TYPE))

        assertEquals(Response.Status.CREATED.statusCode, accountCreation.status)

        val obj = getAccountInfo(holderId, "EUR")

        assertEquals(0.toBigInteger(), obj.cents)
        assertEquals("EUR", obj.currency)

        val creditOperation = target("holders/$holderId/accounts/EUR/credit")
            .request()
            .post(Entity.entity(CreditData(1001.toBigInteger()), MediaType.APPLICATION_JSON_TYPE))

        assertEquals(Response.Status.ACCEPTED.statusCode, creditOperation.status)

        val creditedAccount = getAccountInfo(holderId, "EUR")

        assertEquals(1001.toBigInteger(), creditedAccount.cents)
        assertEquals("EUR", creditedAccount.currency)

    }

    private fun getAccountInfo(holderId: String, currency: String): AccountResponse {
        val accountInfo = target("holders/$holderId/accounts/$currency").request().get(String::class.java)
        return jacksonObjectMapper().readValue(accountInfo, AccountResponse::class.java)
    }
}