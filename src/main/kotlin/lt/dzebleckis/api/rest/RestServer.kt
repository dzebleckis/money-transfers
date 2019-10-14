package lt.dzebleckis.api.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import lt.dzebleckis.api.rest.v1.OpenApiServlet
import lt.dzebleckis.api.rest.v1.AccountResource
import lt.dzebleckis.api.rest.v1.HolderResource
import lt.dzebleckis.application.HolderService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import javax.ws.rs.ext.ContextResolver

class RestServer(port: Int, holderService: HolderService) {

    private val server: Server = Server()

    init {
        val connector = ServerConnector(server)
        connector.port = port
        server.connectors = arrayOf<Connector>(connector)

        val context = ServletContextHandler(
            ServletContextHandler.NO_SECURITY or ServletContextHandler.NO_SESSIONS
        )
        context.contextPath = "/"
        server.handler = context
        server.stopAtShutdown = true
        server.stopTimeout = 10_000

        val resourceConfig = ResourceConfig()
            .register(HolderResource(holderService))
            .register(AccountResource(holderService))
            .register(EntityNotFoundExceptionMapper::class.java)
            .register(IllegalArgumentExceptionMapper::class.java)
            .register(CompletionExceptionMapper::class.java)
            .register(JacksonFeature::class.java)
            .register(ContextResolver<ObjectMapper> { jacksonObjectMapper() })

        context.addServlet(DefaultServlet::class.java, "/")
        context.addServlet(OpenApiServlet::class.java, "/v1/swagger.yml")
        context.addServlet(ServletHolder(ServletContainer(resourceConfig)), "/v1/*")
    }

    fun start() {
        server.start()
    }

    fun join() {
        server.join()
    }

    fun stop() {
        server.stop()
    }
}