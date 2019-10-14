package lt.dzebleckis.domain

class HolderService(private val holderRepository: HolderRepository) {

    fun makePayment(fromHolderId: String, toHolderId: String, money: MoneyDto) {
        val sender = holderRepository.get(fromHolderId)
        val receiver = holderRepository.get(toHolderId)

        holderRepository.save(sender.debit(money))
        holderRepository.save(receiver.credit(money))
    }

    fun createHolder(name: String, phone: String): Holder {
        require(name.isNotBlank()) { "Name must be not empty" }
        require(phone.isNotBlank()) { "Phone must be not empty" }
        require(holderRepository.phoneUnique(phone)) { "Such phone number is already in use" }

        val id = holderRepository.nextHolderId()
        val holder = Holder(id, name, phone, emptySet())
        holderRepository.save(holder)
        return holder
    }

    fun openAccount(holderId: String, currency: String): Account {
        val holder = holderRepository.get(holderId)
        val updated = holder.openAccount(currency)
        holderRepository.save(updated)
        return updated.accounts.first { it.currency == currency }
    }

    fun creditAccount(holderId: String, money: MoneyDto): Holder {
        val holder = holderRepository.get(holderId)
        val updated = holder.credit(money)
        holderRepository.save(updated)
        return updated
    }

}