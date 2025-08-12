package gr.eduinvoice.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * A thread-safe data cache with TTL (Time To Live) support for improving performance
 * by caching frequently accessed data in memory.
 */
class DataCache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val mutex = Mutex()

    /**
     * Cache entry with expiration time
     */
    private data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttl: Duration
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > ttl.inWholeMilliseconds
        }
    }

    /**
     * Cache data with a specific key and TTL
     * @param key The cache key
     * @param data The data to cache
     * @param ttl Time to live for the cached data (default: 5 minutes)
     */
    suspend fun cacheData(key: String, data: Any, ttl: Duration = 5.minutes) {
        mutex.withLock {
            cache[key] = CacheEntry(data, System.currentTimeMillis(), ttl)
        }
    }

    /**
     * Get cached data by key
     * @param key The cache key
     * @return The cached data or null if not found or expired
     */
    suspend fun getCachedData(key: String): Any? {
        return mutex.withLock {
            val entry = cache[key] ?: return@withLock null
            if (entry.isExpired()) {
                cache.remove(key)
                null
            } else {
                entry.data
            }
        }
    }

    /**
     * Get cached data with type safety
     * @param key The cache key
     * @return The cached data cast to the specified type or null
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getCachedDataTyped(key: String): T? {
        return getCachedData(key) as? T
    }

    /**
     * Check if data exists in cache and is not expired
     * @param key The cache key
     * @return true if data exists and is valid
     */
    suspend fun hasCachedData(key: String): Boolean {
        return mutex.withLock {
            val entry = cache[key] ?: return@withLock false
            if (entry.isExpired()) {
                cache.remove(key)
                false
            } else {
                true
            }
        }
    }

    /**
     * Remove specific data from cache
     * @param key The cache key to remove
     */
    suspend fun removeCachedData(key: String) {
        mutex.withLock {
            cache.remove(key)
        }
    }

    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        mutex.withLock {
            cache.clear()
        }
    }

    /**
     * Clear expired entries from cache
     */
    suspend fun clearExpiredEntries() {
        mutex.withLock {
            cache.entries.removeIf { it.value.isExpired() }
        }
    }

    /**
     * Get cache statistics
     * @return Cache statistics including size and expired entries count
     */
    suspend fun getCacheStats(): CacheStats {
        return mutex.withLock {
            val totalEntries = cache.size
            val expiredEntries = cache.values.count { it.isExpired() }
            CacheStats(totalEntries, expiredEntries)
        }
    }

    /**
     * Cache statistics
     */
    data class CacheStats(
        val totalEntries: Int,
        val expiredEntries: Int
    ) {
        val validEntries: Int get() = totalEntries - expiredEntries
    }
}

/**
 * Global cache instance for the application
 */
object GlobalCache {
    private val cache = DataCache()

    suspend fun cacheData(key: String, data: Any, ttl: kotlin.time.Duration = 5.minutes) {
        cache.cacheData(key, data, ttl)
    }

    suspend fun getCachedData(key: String): Any? {
        return cache.getCachedData(key)
    }

    suspend fun <T> getCachedDataTyped(key: String): T? {
        return cache.getCachedDataTyped(key)
    }

    suspend fun hasCachedData(key: String): Boolean {
        return cache.hasCachedData(key)
    }

    suspend fun removeCachedData(key: String) {
        cache.removeCachedData(key)
    }

    suspend fun clearCache() {
        cache.clearCache()
    }

    suspend fun getCacheStats(): DataCache.CacheStats {
        return cache.getCacheStats()
    }
}
