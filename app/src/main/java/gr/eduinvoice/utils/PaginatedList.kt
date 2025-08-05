package gr.eduinvoice.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Represents a paginated list of items with metadata about pagination state.
 * Provides efficient memory management for large datasets.
 */
data class PaginatedList<T>(
    val items: List<T>,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val currentPage: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
) {
    
    companion object {
        /**
         * Create an empty paginated list
         */
        fun <T> empty(pageSize: Int = 20): PaginatedList<T> {
            return PaginatedList(
                items = emptyList(),
                hasNextPage = false,
                hasPreviousPage = false,
                currentPage = 0,
                pageSize = pageSize,
                totalCount = 0,
                totalPages = 0
            )
        }
        
        /**
         * Create a paginated list from a full list
         */
        fun <T> fromList(
            fullList: List<T>,
            page: Int,
            pageSize: Int
        ): PaginatedList<T> {
            val totalCount = fullList.size
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, totalCount)
            
            val items = if (startIndex < totalCount) {
                fullList.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            return PaginatedList(
                items = items,
                hasNextPage = page < totalPages - 1,
                hasPreviousPage = page > 0,
                currentPage = page,
                pageSize = pageSize,
                totalCount = totalCount,
                totalPages = totalPages
            )
        }
    }
    
    /**
     * Check if the list is empty
     */
    val isEmpty: Boolean
        get() = items.isEmpty()
    
    /**
     * Check if the list is not empty
     */
    val isNotEmpty: Boolean
        get() = items.isNotEmpty()
    
    /**
     * Get the number of items in the current page
     */
    val size: Int
        get() = items.size
    
    /**
     * Get the first item in the current page
     */
    fun firstOrNull(): T? = items.firstOrNull()
    
    /**
     * Get the last item in the current page
     */
    fun lastOrNull(): T? = items.lastOrNull()
    
    /**
     * Get an item at the specified index
     */
    fun get(index: Int): T = items[index]
    
    /**
     * Get an item at the specified index or null if out of bounds
     */
    fun getOrNull(index: Int): T? = items.getOrNull(index)
    
    /**
     * Map the items to a new type
     */
    fun <R> map(transform: (T) -> R): PaginatedList<R> {
        return PaginatedList(
            items = items.map(transform),
            hasNextPage = hasNextPage,
            hasPreviousPage = hasPreviousPage,
            currentPage = currentPage,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }
    
    /**
     * Filter the items
     */
    fun filter(predicate: (T) -> Boolean): PaginatedList<T> {
        return PaginatedList(
            items = items.filter(predicate),
            hasNextPage = hasNextPage,
            hasPreviousPage = hasPreviousPage,
            currentPage = currentPage,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }
}

/**
 * Pagination configuration
 */
data class PaginationConfig(
    val pageSize: Int = 20,
    val maxPagesInMemory: Int = 3,
    val enableCaching: Boolean = true,
    val preloadNextPage: Boolean = true
)

/**
 * Pagination state for managing paginated data
 */
data class PaginationState<T>(
    val currentPage: PaginatedList<T>? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasReachedEnd: Boolean = false
)

/**
 * Pagination manager for handling paginated data operations
 */
class PaginationManager<T>(
    private val config: PaginationConfig = PaginationConfig()
) {
    
    private val cachedPages = mutableMapOf<Int, PaginatedList<T>>()
    private var currentState = PaginationState<T>()
    
    /**
     * Get the current pagination state
     */
    fun getCurrentState(): PaginationState<T> = currentState
    
    /**
     * Load a specific page
     */
    suspend fun loadPage(
        page: Int,
        loader: suspend (Int, Int) -> PaginatedList<T>
    ): PaginationState<T> {
        currentState = currentState.copy(isLoading = true, error = null)
        
        return try {
            // Check cache first
            val cachedPage = if (config.enableCaching) {
                cachedPages[page]
            } else null
            
            val pageData = cachedPage ?: loader(page, config.pageSize)
            
            // Cache the page if caching is enabled
            if (config.enableCaching) {
                cachePage(page, pageData)
            }
            
            currentState = currentState.copy(
                currentPage = pageData,
                isLoading = false,
                hasReachedEnd = !pageData.hasNextPage
            )
            
            currentState
            
        } catch (e: Exception) {
            currentState = currentState.copy(
                isLoading = false,
                error = e.message ?: "Failed to load page"
            )
            currentState
        }
    }
    
    /**
     * Load the next page
     */
    suspend fun loadNextPage(
        loader: suspend (Int, Int) -> PaginatedList<T>
    ): PaginationState<T> {
        val currentPage = currentState.currentPage
        if (currentPage == null || !currentPage.hasNextPage || currentState.hasReachedEnd) {
            return currentState
        }
        
        return loadPage(currentPage.currentPage + 1, loader)
    }
    
    /**
     * Load the previous page
     */
    suspend fun loadPreviousPage(
        loader: suspend (Int, Int) -> PaginatedList<T>
    ): PaginationState<T> {
        val currentPage = currentState.currentPage
        if (currentPage == null || !currentPage.hasPreviousPage) {
            return currentState
        }
        
        return loadPage(currentPage.currentPage - 1, loader)
    }
    
    /**
     * Refresh the current page
     */
    suspend fun refresh(
        loader: suspend (Int, Int) -> PaginatedList<T>
    ): PaginationState<T> {
        val currentPage = currentState.currentPage
        if (currentPage == null) {
            return currentState
        }
        
        // Clear cache for current page
        if (config.enableCaching) {
            cachedPages.remove(currentPage.currentPage)
        }
        
        return loadPage(currentPage.currentPage, loader)
    }
    
    /**
     * Clear all cached pages
     */
    fun clearCache() {
        cachedPages.clear()
    }
    
    /**
     * Get cached page
     */
    private fun cachePage(page: Int, pageData: PaginatedList<T>) {
        cachedPages[page] = pageData
        
        // Remove oldest pages if we exceed the limit
        if (cachedPages.size > config.maxPagesInMemory) {
            val oldestPage = cachedPages.keys.minOrNull()
            if (oldestPage != null) {
                cachedPages.remove(oldestPage)
            }
        }
    }
    
    /**
     * Reset the pagination state
     */
    fun reset() {
        currentState = PaginationState()
        clearCache()
    }
}

/**
 * Create a flow of paginated data
 */
fun <T> paginatedFlow(
    config: PaginationConfig = PaginationConfig(),
    loader: suspend (Int, Int) -> PaginatedList<T>
): Flow<PaginationState<T>> = flow {
    val manager = PaginationManager<T>(config)
    
    // Load first page
    var state = manager.loadPage(0, loader)
    emit(state)
    
    // Continue loading pages as needed
    while (state.currentPage?.hasNextPage == true && !state.hasReachedEnd) {
        state = manager.loadNextPage(loader)
        emit(state)
    }
} 