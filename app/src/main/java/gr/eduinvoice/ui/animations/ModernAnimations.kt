package gr.eduinvoice.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SmoothTransition(
    targetState: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = targetState,
        enter = slideInVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(),
        content = content
    )
}

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    var oldCount by remember { mutableStateOf(count) }
    val animatedCount by animateFloatAsState(
        targetValue = count.toFloat(),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "counter"
    )

    LaunchedEffect(count) {
        oldCount = count
    }

    Text(
        text = animatedCount.toInt().toString(),
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun PulseAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    if (isVisible) {
        Box(
            modifier = Modifier.scale(scale),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun FadeInAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    Box(
        modifier = Modifier.alpha(alpha)
    ) {
        content()
    }
}

@Composable
fun SlideInAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "offset"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
        }
    ) {
        content()
    }
}

@Composable
fun LoadingAnimation(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box {
        content()

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ShimmerAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    if (isVisible) {
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = translateAnim
            }
        ) {
            content()
        }
    }
}

@Composable
fun BounceAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
