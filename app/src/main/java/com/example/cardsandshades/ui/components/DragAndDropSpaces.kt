package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero) // Глобальная позиция пальца на экране
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

            // Летающий оверлей карты (рисуется поверх всего экрана)
            if (state.isDragging && state.draggableCard != null) {
                var targetSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Центрируем карту ровно под пальцем игрока
                            translationX = state.dragPosition.x - (targetSize.width / 2)
                            translationY = state.dragPosition.y - (targetSize.height / 2)
                            scaleX = 0.9f
                            scaleY = 0.9f
                        }
                        .onGloballyPositioned { targetSize = it.size }
                ) {
                    CardComponent(card = state.draggableCard!!, isPreview = true)
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

    Box(
        modifier = modifier
            .onGloballyPositioned { startPositionInWindow = it.positionInWindow() }
            .pointerInput(card.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        currentDragTargetInfo.isDragging = true
                        // Переводим стартовую позицию в абсолютные координаты экрана
                        currentDragTargetInfo.dragPosition = startPositionInWindow + localOffset
                        currentDragTargetInfo.draggableCard = card
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Накапливаем позицию в глобальной системе координат
                        currentDragTargetInfo.dragPosition += Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        currentDragTargetInfo.isDragging = false
                    },
                    onDragCancel = {
                        currentDragTargetInfo.isDragging = false
                    }
                )
            }
    ) {
        // Чтобы карточка не двоилась в руке при перетаскивании, скрываем оригинал
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
        // Сверяем глобальные координаты зоны и глобальный вектор пальца
        isHovered = dragInfo.isDragging && globalBounds.contains(dragInfo.dragPosition)

        // Фиксируем Drop при отпускании пальца над зоной
        LaunchedEffect(dragInfo.isDragging) {
            if (!dragInfo.isDragging && isHovered) {
                dragInfo.draggableCard?.let { card ->
                    onCardDropped(card)
                }
            }
        }

        content(isHovered)
    }
}