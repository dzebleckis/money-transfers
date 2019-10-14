package lt.dzebleckis.domain

interface AccountRepository {

    fun get(id: String): Account?

    fun save(entity: Account)
}