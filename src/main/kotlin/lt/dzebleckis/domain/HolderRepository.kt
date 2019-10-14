package lt.dzebleckis.domain

interface HolderRepository {

    fun get(id: String): Holder

    fun save(entity: Holder)

    fun phoneUnique(phone: String): Boolean

    fun nextHolderId(): String
}