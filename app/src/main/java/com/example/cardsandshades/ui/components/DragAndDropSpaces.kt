package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import com.example.cardsandshades.model.CardModel
import kotlin.math.abs

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero) // Абсолютные координаты пальца на экране
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
                            // Идеально точная центровка летающего оверлея под пальцем
                            translationX = state.dragPosition.x - (targetSize.width / 2)
                            translationY = state.dragPosition.y - (targetSize.height / 2)
                            scaleX = 0.95f
                            scaleY = 0.95f
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
    content: @Composable () -> Unit
) {
    val currentDragTargetInfo = LocalDragTargetInfo.current
    var startPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }
    var isVerticalDragActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            // Корректно сохраняем абсолютную координату левого верхнего угла карты на экране
            .onGloballyPositioned { startPositionInWindow = it.positionInWindow() }
            .pointerInput(card.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    accumulatedDrag = Offset.Zero
                    isVerticalDragActive = false

                    drag(down.id) { change ->
                        val dragDelta = change.positionChange()
                        accumulatedDrag = Offset(accumulatedDrag.x + dragDelta.x, accumulatedDrag.y + dragDelta.y)

                        if (!isVerticalDragActive) {
                            // Если потянули карту вверх хотя бы на 15 пикселей
                            if (accumulatedDrag.y < -15f && abs(accumulatedDrag.y) > abs(accumulatedDrag.x)) {
                                isVerticalDragActive = true
                                currentDragTargetInfo.isDragging = true
                                currentDragTargetInfo.draggableCard = card
                            }
                        }

                        if (isVerticalDragActive) {
                            change.consume()
                            // ИСПРАВЛЕНИЕ: Рассчитываем глобальную точку на экране через сумму векторов.
                            // Точка левого верхнего угла карты в окне + текущая точка пальца на самой карте.
                            currentDragTargetInfo.dragPosition = Offset(
                                startPositionInWindow.x + change.position.x,
                                startPositionInWindow.y + change.position.y
                            )
                        }
                    }

                    if (isVerticalDragActive) {
                        currentDragTargetInfo.isDragging = false
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
    var isHovered by remember { mutableStateOf(false) }
    var globalBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned {
            val position = it.positionInWindow()
            globalBounds = Rect(position, Offset(position.x + it.size.width, position.y + it.size.height))
        }
    ) {
        // Сверка глобальных координат теперь работает идеально
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

// Расширение-хелпер для совместимости со стары appointments API Compose
private fun androidx.compose.ui.input.pointer.PointerInputChange.positionChange(): Offset {
    val previous = previousPosition
    val current = position
    return Offset(current.x - previous.x, current.y - previous.y)
}