package lt.dzebleckis.domain

abstract class Entity<T> {
    abstract val id: T

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}