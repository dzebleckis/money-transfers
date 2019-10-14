package lt.dzebleckis.infrastructure

import lt.dzebleckis.api.rest.EntityNotFoundException
import lt.dzebleckis.domain.Holder
import lt.dzebleckis.domain.HolderRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryHolderRepository : HolderRepository {

    private val storage = ConcurrentHashMap<String, Holder>()

    override fun nextHolderId(): String {
        return "h-${storage.size}"
    }

    override fun phoneUnique(phone: String): Boolean {
        return storage.values.find { it.phone == phone } == null

    }

    override fun get(id: String): Holder {
        return storage[id] ?: throw EntityNotFoundException("No holder found with id: $id")
    }

    override fun save(entity: Holder) {
        storage[entity.id] = entity
    }
}