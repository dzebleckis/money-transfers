package lt.dzebleckis.api.rest.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import lt.dzebleckis.api.rest.CompletionExceptionMapper
import lt.dzebleckis.api.rest.EntityNotFoundExceptionMapper
import lt.dzebleckis.api.rest.IllegalArgumentExceptionMapper
import lt.dzebleckis.application.BlockingCommandQueue
import lt.dzebleckis.application.CommandProcessor
import lt.dzebleckis.application.CommandQueue
import lt.dzebleckis.application.PoisonPill
import lt.dzebleckis.domain.HolderRepository
import lt.dzebleckis.domain.HolderService
import lt.dzebleckis.infrastructure.InMemoryHolderRepository
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.ws.rs.core.Application
import javax.ws.rs.ext.ContextResolver

abstract class ResourceTest : JerseyTest() {

    private lateinit var executor: ExecutorService
    private lateinit var queue: CommandQueue
    private lateinit var repository: HolderRepository

    override fun configure(): Application {
        repository = InMemoryHolderRepository()
        queue = BlockingCommandQueue()
        executor = Executors.newFixedThreadPool(1)
        executor.submit {
            CommandProcessor(queue, HolderService(repository))
        }
        val service = lt.dzebleckis.application.HolderService(repository, queue)

        return ResourceConfig()
            .register(HolderResource(service))
            .register(AccountResource(service))
            .register(EntityNotFoundExceptionMapper::class.java)
            .register(IllegalArgumentExceptionMapper::class.java)
            .register(CompletionExceptionMapper::class.java)
            .register(JacksonFeature::class.java)
            .register(ContextResolver<ObjectMapper> { jacksonObjectMapper() })
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        queue.add(PoisonPill)
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)
    }
}