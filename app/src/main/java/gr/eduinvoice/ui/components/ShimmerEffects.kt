package gr.eduinvoice.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .background(brush)
            .clip(RoundedCornerShape(8.dp))
    ) {
        content()
    }
}

@Composable
fun ShimmerTextEffect(
    width: Int,
    height: Int,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerCircle(
    size: Int,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier.size(size.dp)
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerRectangle(
    width: Int,
    height: Int,
    cornerRadius: Int = 8,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerList(
    itemCount: Int = 5,
    itemHeight: Int = 80,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(itemCount) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Empty content for shimmer effect
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ShimmerGrid(
    columns: Int = 2,
    rows: Int = 3,
    itemHeight: Int = 120,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) { column ->
                    ShimmerEffect(
                        modifier = Modifier
                            .weight(1f)
                            .height(itemHeight.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Empty content for shimmer effect
                    }
                }
            }
            if (row < rows - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ShimmerProfileCard(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerInvoiceCard(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ShimmerLessonCard(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Empty content for shimmer effect
    }
}
