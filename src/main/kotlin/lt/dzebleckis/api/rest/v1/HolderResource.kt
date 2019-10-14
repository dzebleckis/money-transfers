package lt.dzebleckis.api.rest.v1

import lt.dzebleckis.api.rest.toCreated
import lt.dzebleckis.api.rest.urlEncode
import lt.dzebleckis.application.HolderService
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType

@Path("holders")
@Produces(MediaType.APPLICATION_JSON)
class HolderResource(private val holderService: HolderService) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun createHolder(
        @Suspended response: AsyncResponse,
        data: CreateHolderData
    ) {

        holderService
            .createHolder(data.name, data.phone)
            .toCreated(response) { id -> "/v1/holders/$id" }
    }

    @GET
    @Path("/{holderId}")
    fun getHolder(@PathParam("holderId") holderId: String): HolderResponse {
        return holderService.getHolder(holderId.urlEncode())
    }
}