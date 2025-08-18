package gr.eduinvoice.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class DataCache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val mutex = Mutex()

    private data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttl: Duration
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl.inWholeMilliseconds
    }

    suspend fun cacheData(key: String, data: Any, ttl: Duration = 5.minutes) {
        mutex.withLock { cache[key] = CacheEntry(data, System.currentTimeMillis(), ttl) }
    }

    suspend fun getCachedData(key: String): Any? = mutex.withLock {
        val entry = cache[key] ?: return@withLock null
        if (entry.isExpired()) { cache.remove(key); null } else entry.data
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getCachedDataTyped(key: String): T? = getCachedData(key) as? T

    suspend fun hasCachedData(key: String): Boolean = mutex.withLock {
        val entry = cache[key] ?: return@withLock false
        if (entry.isExpired()) { cache.remove(key); false } else true
    }

    suspend fun removeCachedData(key: String) { mutex.withLock { cache.remove(key) } }
    suspend fun clearCache() { mutex.withLock { cache.clear() } }
}
