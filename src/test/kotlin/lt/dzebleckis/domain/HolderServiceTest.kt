package lt.dzebleckis.domain

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import lt.dzebleckis.Utils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
internal class HolderServiceTest {

    @RelaxedMockK
    lateinit var repository: HolderRepository

    @Test
    fun `create holder with empty name`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            HolderService(repository).createHolder("  ", "1232132")
        }
        assertEquals("Name must be not empty", exception.message)
    }

    @Test
    fun `create holder with empty phone`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            HolderService(repository).createHolder("name", "  ")
        }
        assertEquals("Phone must be not empty", exception.message)
    }

    @Test
    fun `create holder with non unique phone`() {
        every { repository.phoneUnique("1232132") } returns false

        val exception = assertThrows(IllegalArgumentException::class.java) {
            HolderService(repository).createHolder("name", "1232132")
        }
        assertEquals("Such phone number is already in use", exception.message)
    }

    @Test
    fun `create holder`() {
        val service = HolderService(repository)
        val slot = slot<Holder>()

        every { repository.phoneUnique("1232132") } returns true
        every { repository.nextHolderId() } returns "h-11"
        every { repository.save(capture(slot)) } returns Unit

        service.createHolder("name", "1232132")

        val holder = slot.captured
        assertEquals("h-11", holder.id)
        assertEquals("name", holder.name)
        assertEquals("1232132", holder.phone)
        assertTrue(holder.accounts.isEmpty())
    }


    @Test
    fun makePayment() {
        val fromHolder = Utils.holder(id = "h-1", accounts = setOf(Utils.account(cents = 1000, currency = "USD")))
        val toHolder = Utils.holder(id = "h-2", accounts = setOf(Utils.account(cents = 200, currency = "USD")))

        val holders = mutableListOf<Holder>()

        every { repository.get(fromHolder.id) } returns fromHolder
        every { repository.get(toHolder.id) } returns toHolder
        every { repository.save(capture(holders)) } returns Unit

        val service = HolderService(repository)

        service.makePayment(fromHolder.id, toHolder.id, MoneyDto(133.toBigInteger(), "USD"))

        assertEquals(2, holders.size)
        val debited = holders.first()
        val credited = holders.last()

        assertEquals(balance(fromHolder) - 133.toBigInteger(), balance(debited))
        assertEquals(balance(toHolder) + 133.toBigInteger(), balance(credited))
    }

    @Test
    fun openAccount() {
        val holder = Utils.holder(accounts = emptySet())
        val updated = slot<Holder>()

        every { repository.get(holder.id) } returns holder
        every { repository.save(capture(updated)) } returns Unit

        val service = HolderService(repository)

        service.openAccount(holder.id, "EUR")

        assertEquals(1, updated.captured.accounts.size)
        val account = updated.captured.accounts.first()

        assertEquals(0.toBigInteger(), account.balanceInCents)
        assertEquals("EUR", account.currency)
    }

    @Test
    fun creditAccount() {
        val holder = Utils.holder(accounts = emptySet())
        val updated = slot<Holder>()

        every { repository.get(holder.id) } returns holder
        every { repository.save(capture(updated)) } returns Unit

        val service = HolderService(repository)

        service.creditAccount(holder.id, MoneyDto(111.toBigInteger(), "EUR"))

        assertEquals(1, updated.captured.accounts.size)
        val account = updated.captured.accounts.first()

        assertEquals(111.toBigInteger(), account.balanceInCents)
        assertEquals("EUR", account.currency)
    }

    private fun balance(holder: Holder, currency: String = "USD"): BigInteger {
        return holder.accounts.first { it.currency == currency }.balanceInCents
    }
}