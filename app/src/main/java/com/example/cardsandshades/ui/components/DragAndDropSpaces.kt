package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.example.cardsandshades.model.CardModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

data class DropTargetBounds(
    val id: String,
    val bounds: Rect,
    val onDropped: (CardModel) -> Unit
)

class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
    var draggableCard: CardModel? by mutableStateOf(null)

    val activeDropTargets: SnapshotStateList<DropTargetBounds> = mutableStateListOf()
}

@Composable
fun DragAndDropContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(LocalDragTargetInfo provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            if (state.isDragging && state.draggableCard != null) {
                var targetSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = state.dragPosition.x - (targetSize.width / 2)
                            translationY = state.dragPosition.y - (targetSize.height / 2)
                            scaleX = 1.0f
                            scaleY = 1.0f
                        }
                        .onGloballyPositioned { targetSize = it.size }
                ) {
                    state.draggableCard?.let { currentCard ->
                        CardComponent(card = currentCard, isPreview = true)
                    }
                }
            }
        }
    }
}

@Composable
fun DragTarget(
    card: CardModel,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onLongClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val currentDragTargetInfo = LocalDragTargetInfo.current
    var currentPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }
    var isVerticalDragActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onGloballyPositioned { currentPositionInWindow = it.positionInWindow() }
            .pointerInput(card.id) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        accumulatedDrag = Offset.Zero
                        isVerticalDragActive = false
                        var isLongClickTriggered = false

                        val initialFingerGlobalPosition = currentPositionInWindow + down.position

                        val longPressJob = launch {
                            delay(viewConfiguration.longPressTimeoutMillis)
                            if (!isVerticalDragActive && accumulatedDrag.getDistance() < 10f) {
                                isLongClickTriggered = true
                                onLongClick()
                            }
                        }

                        drag(down.id) { change ->
                            val dragDelta = change.positionChange()
                            accumulatedDrag = Offset(accumulatedDrag.x + dragDelta.x, accumulatedDrag.y + dragDelta.y)

                            if (!isVerticalDragActive) {
                                if (accumulatedDrag.y < -15f && abs(accumulatedDrag.y) > abs(accumulatedDrag.x)) {
                                    isVerticalDragActive = true
                                    longPressJob.cancel()
                                    currentDragTargetInfo.isDragging = true
                                    currentDragTargetInfo.draggableCard = card
                                    currentDragTargetInfo.dragPosition = initialFingerGlobalPosition
                                }
                            }

                            if (isVerticalDragActive) {
                                change.consume()
                                currentDragTargetInfo.dragPosition = Offset(
                                    initialFingerGlobalPosition.x + accumulatedDrag.x,
                                    initialFingerGlobalPosition.y + accumulatedDrag.y
                                )
                            }
                        }

                        longPressJob.cancel()

                        if (!isVerticalDragActive && !isLongClickTriggered && accumulatedDrag.getDistance() < 10f) {
                            onTap()
                        }

                        if (isVerticalDragActive) {
                            val finalDropPosition = currentDragTargetInfo.dragPosition
                            val targetZone = currentDragTargetInfo.activeDropTargets.find { zone ->
                                zone.bounds.contains(finalDropPosition)
                            }
                            targetZone?.onDropped?.invoke(card)
                            currentDragTargetInfo.isDragging = false
                            isVerticalDragActive = false
                        }
                    }
                }
            }
    ) {
        val isCurrentDragging = currentDragTargetInfo.isDragging && currentDragTargetInfo.draggableCard?.id == card.id
        Box(modifier = Modifier.graphicsLayer { alpha = if (isCurrentDragging) 0.0f else 1.0f }) {
            content()
        }
    }
}

@Composable
fun DropTarget(
    modifier: Modifier = Modifier,
    onCardDropped: (CardModel) -> Unit,
    content: @Composable BoxScope.(isHovered: Boolean) -> Unit
) {
    val dragInfo = LocalDragTargetInfo.current
    val dropTargetId = remember { java.util.UUID.randomUUID().toString() }
    var bounds by remember { mutableStateOf(Rect.Zero) }

    val isHovered = dragInfo.isDragging && bounds.contains(dragInfo.dragPosition)

    Box(
        modifier = modifier
            .onGloballyPositioned { layoutNodes ->
                val rect = Rect(layoutNodes.positionInWindow(), layoutNodes.size.toSize())
                bounds = rect
                
                val existing = dragInfo.activeDropTargets.find { it.id == dropTargetId }
                if (existing != null) {
                    dragInfo.activeDropTargets.remove(existing)
                }
                dragInfo.activeDropTargets.add(DropTargetBounds(dropTargetId, rect, onCardDropped))
            }
    ) {
        content(isHovered)
    }

    DisposableEffect(dropTargetId) {
        onDispose {
            val existing = dragInfo.activeDropTargets.find { it.id == dropTargetId }
            if (existing != null) dragInfo.activeDropTargets.remove(existing)
        }
    }
}
