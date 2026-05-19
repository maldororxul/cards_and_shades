package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import com.example.cardsandshades.model.CardModel

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var draggableCard: CardModel? by mutableStateOf(null)
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
                            scaleX = 0.95f
                            scaleY = 0.95f
                        }
                        .onGloballyPositioned { targetSize = it.size }
                ) {
                    // ИСПРАВЛЕНО: Корректное имя функции CardComponent
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
    content: @Composable () -> Unit
) {
    val currentDragTargetInfo = LocalDragTargetInfo.current
    var startPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDragY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .onGloballyPositioned { startPositionInWindow = it.positionInWindow() }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    accumulatedDragY += delta
                    if (accumulatedDragY < -10f && !currentDragTargetInfo.isDragging) {
                        currentDragTargetInfo.isDragging = true
                        currentDragTargetInfo.draggableCard = card
                        currentDragTargetInfo.dragPosition = startPositionInWindow + Offset(50f, 0f)
                    }

                    if (currentDragTargetInfo.isDragging) {
                        currentDragTargetInfo.dragPosition = Offset(
                            currentDragTargetInfo.dragPosition.x,
                            currentDragTargetInfo.dragPosition.y + delta
                        )
                    }
                },
                onDragStarted = { localOffset ->
                    accumulatedDragY = 0f
                    // ИСПРАВЛЕНО: Прямое обращение к свойству dragPosition вместо вызова метода
                    if (currentDragTargetInfo.isDragging) {
                        currentDragTargetInfo.dragPosition = Offset(
                            startPositionInWindow.x + localOffset.x,
                            currentDragTargetInfo.dragPosition.y
                        )
                    }
                },
                onDragStopped = {
                    currentDragTargetInfo.isDragging = false
                }
            )
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
    var isHovered by remember { mutableStateOf(false) }
    var globalBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned {
            val position = it.positionInWindow()
            globalBounds = Rect(position, Offset(position.x + it.size.width, position.y + it.size.height))
        }
    ) {
        isHovered = dragInfo.isDragging && globalBounds.contains(dragInfo.dragPosition)

        var wasDragging by remember { mutableStateOf(false) }

        LaunchedEffect(dragInfo.isDragging) {
            if (wasDragging && !dragInfo.isDragging && isHovered) {
                dragInfo.draggableCard?.let { card ->
                    onCardDropped(card)
                }
            }
            wasDragging = dragInfo.isDragging
        }

        content(isHovered)
    }
}
