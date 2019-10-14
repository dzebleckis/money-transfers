package lt.dzebleckis.application

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import lt.dzebleckis.Utils
import lt.dzebleckis.api.rest.EntityNotFoundException
import lt.dzebleckis.domain.HolderRepository
import lt.dzebleckis.domain.MoneyDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
internal class HolderServiceTest {

    @RelaxedMockK
    lateinit var repository: HolderRepository

    @RelaxedMockK
    lateinit var commandQueue: CommandQueue

    lateinit var service: HolderService

    @BeforeEach
    fun setup() {
        service = HolderService(repository, commandQueue)
    }

    @Test
    fun `list accounts`() {
        val accountEntity = Utils.account()
        val holder = Utils.holder(accounts = setOf(accountEntity))
        every { repository.get(holder.id) } returns holder

        val result = service.listAccounts(holder.id)

        assertEquals(1, result.size)
        assertEquals(accountEntity.balanceInCents, result.first().cents)
        assertEquals(accountEntity.currency, result.first().currency)
    }

    @Test
    fun `get account`() {
        val accountEntity = Utils.account(cents = 121, currency = "EUR")
        val holder = Utils.holder(accounts = setOf(accountEntity))
        every { repository.get(holder.id) } returns holder

        val result = service.getAccount(holder.id, "EUR")
        assertEquals(result.cents, 121.toBigInteger())
        assertEquals(result.currency, "EUR")
    }

    @Test
    fun `get not existing account`() {
        val accountEntity = Utils.account(cents = 121, currency = "EUR")
        val holder = Utils.holder(accounts = setOf(accountEntity))
        every { repository.get(holder.id) } returns holder

        val exception = Assertions.assertThrows(EntityNotFoundException::class.java) {
            service.getAccount(holder.id, "USD")
        }
        assertEquals("No account found for currency: USD", exception.message)
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPhonesAndNumbers")
    fun `create holder invalid name or phone`(name: String, phone: String, message: String) {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.createHolder(name, phone)
        }
        assertEquals(message, exception.message)
    }

    @Test
    fun `create holder`() {
        val command = CreateHolder("name", "123456789")
        every { commandQueue.add(command) } returns CompletableFuture.completedFuture("")
        service.createHolder("name", "123456789")
        verify { commandQueue.add(command) }
        confirmVerified()
    }

    @Test
    fun `create account`() {
        val command = OpenAccount("h1", "EUR")
        every { commandQueue.add(command) } returns CompletableFuture.completedFuture("")
        service.createAccount("h1", "EUR")
        verify { commandQueue.add(command) }
        confirmVerified()
    }

    @Test
    fun `create account invalid currency`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.createAccount("h1", "AA")
        }
        assertEquals("Not supported currency: AA", exception.message)
    }

    @Test
    fun `make payment`() {
        val command = MakePayment("h1", "h2", MoneyDto(100.toBigInteger(), "EUR"))
        every { commandQueue.add(command) } returns CompletableFuture.completedFuture("")
        service.makePayment("h1", "h2", 100.toBigInteger(), "EUR")
        verify { commandQueue.add(command) }
        confirmVerified()
    }

    @Test
    fun `make payment invalid currency`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.makePayment("h1", "h2", 100.toBigInteger(), "ABC")
        }
        assertEquals("Not supported currency: ABC", exception.message)
    }

    @Test
    fun `get holder`() {
        val holder = Utils.holder()
        every { repository.get(holder.id) } returns holder

        service.getHolder(holder.id)

        verify { repository.get(holder.id) }
        confirmVerified()
    }

    @Test
    fun `credit account`() {
        val holder = Utils.holder()
        val command = CreditAccount(holder.id, MoneyDto(100.toBigInteger(), "USD"))
        every { commandQueue.add(command) } returns CompletableFuture.completedFuture("")

        service.creditAccount(holder.id, 100.toBigInteger(), "USD")

        verify { commandQueue.add(command) }
        confirmVerified()
    }

    @Test
    fun `credit account negative amount`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.creditAccount("h2", (-10).toBigInteger(), "USD")
        }
        assertEquals("Amount must be greater than zero", exception.message)
    }

    companion object {
        @JvmStatic
        fun provideInvalidPhonesAndNumbers(): Collection<Arguments> {
            return listOf(
                Arguments.of("", "12233443322", "Name length must be between 4 and 20"),
                Arguments.of("123", "12233443322", "Name length must be between 4 and 20"),
                Arguments.of("1234", "  ", "Phone number length must be between 9 and 15"),
                Arguments.of("1234", "11", "Phone number length must be between 9 and 15"),
                Arguments.of("123456789_12233443322_", "11111", "Name length must be between 4 and 20"),
                Arguments.of("123456789", "123456789_12233443322_", "Phone number length must be between 9 and 15")
            )
        }
    }
}