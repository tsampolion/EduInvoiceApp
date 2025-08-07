package gr.eduinvoice.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(
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
fun ShimmerText(
    width: Int,
    height: Int,
    modifier: Modifier = Modifier
) {
    ShimmerBox(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
    ) {
        // Empty content for shimmer effect
    }
}

@Composable
fun ModernStudentSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        repeat(3) {
            ModernSkeletonCard()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ModernSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Modern skeleton avatar with shimmer
            ShimmerBox(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                // Empty content for shimmer effect
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                ShimmerText(width = 140, height = 18)
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerText(width = 100, height = 14)
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerText(width = 80, height = 12)
            }
        }
    }
}

@Composable
fun ModernLessonSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        repeat(4) {
            ModernLessonSkeletonCard()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ModernLessonSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerText(width = 120, height = 20)
                ShimmerText(width = 60, height = 16)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ShimmerText(width = 200, height = 14)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerText(width = 80, height = 12)
                ShimmerText(width = 60, height = 12)
            }
        }
    }
}

@Composable
fun ModernInvoiceSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        repeat(2) {
            ModernInvoiceSkeletonCard()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ModernInvoiceSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(width = 100, height = 18)
                ShimmerText(width = 80, height = 16)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ShimmerText(width = 180, height = 14)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ShimmerText(width = 150, height = 14)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerText(width = 60, height = 12)
                ShimmerText(width = 80, height = 16)
            }
        }
    }
}

@Composable
fun ModernListSkeleton(
    itemCount: Int = 5,
    itemHeight: Int = 80
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        repeat(itemCount) {
            ShimmerBox(
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
