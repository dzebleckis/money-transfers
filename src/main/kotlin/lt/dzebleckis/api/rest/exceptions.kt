package lt.dzebleckis.api.rest

import java.util.concurrent.CompletionException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

class EntityNotFoundException(override val message: String) : WebApplicationException(message)

class EntityNotFoundExceptionMapper : ExceptionMapper<EntityNotFoundException> {

    override fun toResponse(exception: EntityNotFoundException): Response {
        return Response
            .status(404)
            .entity(mapOf("message" to exception.message))
            .type(MediaType.APPLICATION_JSON).build()
    }
}

class IllegalArgumentExceptionMapper : ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException): Response {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(mapOf("message" to exception.message))
            .type(MediaType.APPLICATION_JSON).build()
    }
}

class CompletionExceptionMapper : ExceptionMapper<CompletionException> {
    override fun toResponse(exception: CompletionException): Response {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(mapOf("message" to exception.cause?.message))
            .type(MediaType.APPLICATION_JSON).build()
    }
}
