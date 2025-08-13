package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gr.eduinvoice.utils.PaginatedList
import gr.eduinvoice.utils.PaginationState

/**
 * A lazy loading list component that efficiently handles large datasets with pagination.
 * Provides automatic loading of more items when the user scrolls near the end.
 */
@Composable
fun <T> LazyLoadingList(
    paginationState: PaginationState<T>,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    loadingItem: @Composable (() -> Unit)? = null,
    errorItem: @Composable ((String) -> Unit)? = null,
    emptyItem: @Composable (() -> Unit)? = null,
    threshold: Int = 3
) {
    val currentPage = paginationState.currentPage

    // Check if we should load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleItem >= totalItems - threshold
        }
    }

    // Load more items when threshold is reached
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !paginationState.isLoading && !paginationState.hasReachedEnd) {
            onLoadMore()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Show loading state
            paginationState.isLoading && currentPage == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Show error state
            paginationState.error != null && currentPage == null -> {
                errorItem?.invoke(paginationState.error!!) ?: run {
                    Text(
                        text = "Error: ${paginationState.error}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Show empty state
            currentPage?.isEmpty == true -> {
                emptyItem?.invoke() ?: run {
                    Text(
                        text = "No items found",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Show list with items
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Add items
                    currentPage?.items?.let { items ->
                        items(items) { item ->
                            itemContent(item)
                        }
                    }

                    // Add loading indicator at the end
                    if (paginationState.isLoading) {
                        item {
                            loadingItem?.invoke() ?: run {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    // Add error indicator at the end
                    if (paginationState.error != null && currentPage != null) {
                        item {
                            errorItem?.invoke(paginationState.error!!) ?: run {
                                Text(
                                    text = "Error loading more items: ${paginationState.error}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A simplified version of LazyLoadingList that works with a simple list and loading state.
 */
@Composable
fun <T> SimpleLazyLoadingList(
    items: List<T>,
    isLoading: Boolean = false,
    hasMoreItems: Boolean = true,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    loadingItem: @Composable (() -> Unit)? = null,
    threshold: Int = 3
) {
    // Check if we should load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleItem >= totalItems - threshold && hasMoreItems
        }
    }

    // Load more items when threshold is reached
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        // Add items
        items(items) { item ->
            itemContent(item)
        }

        // Add loading indicator at the end
        if (isLoading) {
            item {
                loadingItem?.invoke() ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * A paginated list component that works with PaginatedList data structure.
 */
@Composable
fun <T> PaginatedLazyList(
    paginatedList: PaginatedList<T>?,
    onLoadNextPage: () -> Unit,
    onLoadPreviousPage: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    loadingItem: @Composable (() -> Unit)? = null,
    threshold: Int = 3
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleItem >= totalItems - threshold && paginatedList?.hasNextPage == true
        }
    }

    // Load more items when threshold is reached
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadNextPage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Show empty state
            paginatedList == null || paginatedList.isEmpty -> {
                Text(
                    text = "No items found",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Show list with items
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Add items
                    items(paginatedList.items) { item ->
                        itemContent(item)
                    }

                    // Add loading indicator at the end if there are more pages
                    if (paginatedList.hasNextPage) {
                        item {
                            loadingItem?.invoke() ?: run {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
