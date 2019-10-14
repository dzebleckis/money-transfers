package lt.dzebleckis.api.rest.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import lt.dzebleckis.api.rest.CompletionExceptionMapper
import lt.dzebleckis.api.rest.EntityNotFoundExceptionMapper
import lt.dzebleckis.api.rest.IllegalArgumentExceptionMapper
import lt.dzebleckis.application.*
import lt.dzebleckis.domain.HolderRepository
import lt.dzebleckis.infrastructure.InMemoryHolderRepository
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Application
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ContextResolver

internal class HolderResourceTest : ResourceTest() {

    @Test
    fun holder() {
        val created = target("/holders")
            .request()
            .post(Entity.entity(CreateHolderData("name11", "12345678911"), MediaType.APPLICATION_JSON_TYPE))

        assertEquals(Response.Status.CREATED.statusCode, created.status)
        val location = created.getHeaderString("Location")
        val holderId = location.split("/").last()

        val holderInfo = target("holders/$holderId").request().get(String::class.java)
        val obj = jacksonObjectMapper().readValue(holderInfo, HolderResponse::class.java)

        assertEquals(holderId, obj.id)
        assertEquals("name11", obj.name)
        assertEquals("12345678911", obj.phone)
    }
}