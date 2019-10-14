package lt.dzebleckis.api.rest.v1

import lt.dzebleckis.api.rest.toAccepted
import lt.dzebleckis.api.rest.toCreated
import lt.dzebleckis.api.rest.urlEncode
import lt.dzebleckis.application.HolderService
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType

@Path("holders/{holderId}/accounts")
@Produces(MediaType.APPLICATION_JSON)
class AccountResource(private val holderService: HolderService) {

    @GET
    fun listAccounts(@PathParam("holderId") holderId: String): Collection<AccountResponse> {
        return holderService.listAccounts(holderId.urlEncode())
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun openAccount(
        @Suspended response: AsyncResponse,
        @PathParam("holderId") holderId: String,
        data: OpenAccountData
    ) {
        holderService
            .createAccount(holderId.urlEncode(), data.currency)
            .toCreated(response) { currency -> "/v1/holders/$holderId/accounts/$currency" }
    }

    @GET
    @Path("/{currency}")
    fun getAccount(
        @PathParam("holderId") holderId: String,
        @PathParam("currency") currency: String
    ): AccountResponse {
        return holderService.getAccount(holderId.urlEncode(), currency)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{currency}/credit")
    fun creditAccount(
        @Suspended response: AsyncResponse,
        @PathParam("holderId") holderId: String,
        @PathParam("currency") currency: String,
        data: CreditData
    ) {
        holderService
            .creditAccount(holderId.urlEncode(), data.cents, currency)
            .toAccepted(response)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{currency}/payment")
    fun createPayment(
        @Suspended response: AsyncResponse,
        @PathParam("holderId") holderId: String,
        @PathParam("currency") currency: String,
        data: PaymentData
    ) {
        holderService
            .makePayment(holderId.urlEncode(), data.recipientId, data.cents, currency)
            .toAccepted(response)
    }
}