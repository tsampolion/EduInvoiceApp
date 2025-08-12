package gr.eduinvoice.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A virtual scrolling list component that efficiently handles large datasets
 * by only rendering visible items and using pagination for better performance.
 *
 * @param items The list of items to display
 * @param itemHeight The height of each item (used for virtual scrolling calculations)
 * @param itemContent The composable content for each item
 * @param listState Optional LazyListState for external control
 * @param modifier Modifier for the list
 * @param contentPadding Padding for the list content
 * @param onLoadMore Optional callback when more items need to be loaded
 * @param isLoadingMore Whether more items are currently being loaded
 */
@Composable
fun <T> VirtualScrollingList(
    items: List<T>,
    itemHeight: Dp,
    itemContent: @Composable (T, Int) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onLoadMore: (() -> Unit)? = null,
    isLoadingMore: Boolean = false
) {
    val visibleItemsInfo by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = visibleItems.lastOrNull()

            Triple(visibleItems, totalItems, lastVisibleItem)
        }
    }

    // Trigger load more when approaching the end
    LaunchedEffect(visibleItemsInfo) {
        val (_, totalItems, lastVisibleItem) = visibleItemsInfo
        if (onLoadMore != null && !isLoadingMore && lastVisibleItem != null) {
            val threshold = 5 // Load more when 5 items away from the end
            if (lastVisibleItem.index >= totalItems - threshold) {
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = items,
            key = { index, item ->
                // Use item hash or id if available, otherwise use index
                when (item) {
                    is gr.eduinvoice.domain.model.DomainStudent -> item.id
                    is gr.eduinvoice.ui.model.UiLessonWithStudent -> item.lesson.id
                    else -> index
                }
            }
        ) { index, item ->
            itemContent(item, index)
        }

        // Show loading indicator if loading more items
        if (isLoadingMore) {
            item {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * A specialized virtual scrolling list for students with optimized rendering
 */
@Composable
fun VirtualStudentList(
    students: List<gr.eduinvoice.ui.model.UiStudentWithEarnings>,
    onStudentClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
    isLoadingMore: Boolean = false
) {
    VirtualScrollingList(
        items = students,
        itemHeight = 80.dp, // Approximate height of StudentCard
        itemContent = { student, _ ->
            gr.eduinvoice.ui.components.StudentCard(
                studentWithEarnings = student,
                onStudentClick = { onStudentClick(student.student.id) },
                onDeleteClick = { onDeleteClick(student.student.id) }
            )
            androidx.compose.material3.HorizontalDivider()
        },
        listState = listState,
        modifier = modifier,
        onLoadMore = onLoadMore,
        isLoadingMore = isLoadingMore
    )
}

/**
 * A specialized virtual scrolling list for lessons with optimized rendering
 */
@Composable
fun VirtualLessonList(
    lessons: List<gr.eduinvoice.ui.model.UiLessonWithStudent>,
    onLessonClick: (Long, Long, Long) -> Unit,
    onPaidChange: (Long, Boolean) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
    isLoadingMore: Boolean = false
) {
    VirtualScrollingList(
        items = lessons,
        itemHeight = 60.dp, // Approximate height of lesson item
        itemContent = { lesson, _ ->
            gr.eduinvoice.ui.lessons.LessonItem(
                lessonWithStudent = lesson,
                onClick = {
                    onLessonClick(
                        lesson.student.id,
                        lesson.lesson.id,
                        lesson.lesson.groupId ?: 0L
                    )
                },
                onPaidChange = { onPaidChange(lesson.lesson.id, it) }
            )
            androidx.compose.material3.HorizontalDivider()
        },
        listState = listState,
        modifier = modifier,
        onLoadMore = onLoadMore,
        isLoadingMore = isLoadingMore
    )
}
