package lt.dzebleckis.domain

import lt.dzebleckis.Utils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class HolderTest {

    @Nested
    inner class DebitTests {

        @Test
        fun `debit non existing account`() {
            val holder = Utils.holder()
            val exception = assertThrows(IllegalArgumentException::class.java) {
                holder.debit(MoneyDto(100.toBigInteger(), "EUR"))
            }
            assertEquals("Holder has no account for currency EUR", exception.message)
        }

        @Test
        fun `debit with negative amount`() {
            val holder = Utils.holder(accounts = setOf(Utils.account()))
            val exception = assertThrows(IllegalArgumentException::class.java) {
                holder.debit(MoneyDto(0.toBigInteger(), "EUR"))
            }
            assertEquals("Amount should be greater than zero", exception.message)
        }


        @Test
        fun `debit with exceeding amount`() {
            val holder = Utils.holder(accounts = setOf(Utils.account(cents = 20)))
            val exception = assertThrows(IllegalArgumentException::class.java) {
                holder.debit(MoneyDto(21.toBigInteger(), "EUR"))
            }
            assertEquals("Not enough funds in account", exception.message)
        }
    }

    @Nested
    inner class CreditTests {

        @Test
        fun `credit with negative amount`() {
            val holder = Utils.holder(accounts = setOf(Utils.account()))
            val exception = assertThrows(IllegalArgumentException::class.java) {
                holder.credit(MoneyDto(0.toBigInteger(), "EUR"))
            }
            assertEquals("Amount should be greater than zero", exception.message)
        }

        @Test
        fun `credit non existing account`() {
            val holder = Utils.holder(accounts = emptySet())
            val credited = holder.credit(MoneyDto(10.toBigInteger(), "EUR"))

            assertEquals(1, credited.accounts.size)
            val account = credited.accounts.first()
            assertEquals("a-0", account.id)
            assertEquals(10.toBigInteger(), account.balanceInCents)
            assertEquals("EUR", account.currency)
        }

        @Test
        fun `credit existing account`() {
            val holder = Utils.holder(accounts = setOf(Utils.account("a-11", 10, "USD")))
            val credited = holder.credit(MoneyDto(11.toBigInteger(), "USD"))

            assertEquals(1, credited.accounts.size)
            val account = credited.accounts.first()
            assertEquals("a-11", account.id)
            assertEquals(21.toBigInteger(), account.balanceInCents)
            assertEquals("USD", account.currency)
        }
    }

    @Test
    fun `accounts with same currency`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Utils.holder(
                accounts = setOf(
                    Utils.account(id = "a1", currency = "EUR"),
                    Utils.account(id = "a2", currency = "EUR")
                )
            )
        }
        assertEquals("Accounts with same currency are not allowed", exception.message)
    }

    @Test
    fun `open new account`() {
        val holder = Utils.holder(accounts = emptySet()).openAccount("GBP")
        assertEquals(1, holder.accounts.size)
        val account = holder.accounts.first()
        assertEquals("a-0", account.id)
        assertEquals(0.toBigInteger(), account.balanceInCents)
        assertEquals("GBP", account.currency)
    }

    @Test
    fun `open existing account`() {
        val holder = Utils.holder(
            accounts = setOf(Utils.account(id = "a-11", cents = 42, currency = "GBP"))
        ).openAccount("GBP")

        assertEquals(1, holder.accounts.size)
        val account = holder.accounts.first()
        assertEquals("a-11", account.id)
        assertEquals(42.toBigInteger(), account.balanceInCents)
        assertEquals("GBP", account.currency)
    }
}