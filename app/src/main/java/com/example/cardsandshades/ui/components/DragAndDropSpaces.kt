package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import com.example.cardsandshades.model.CardModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

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
                forEachGesture {
                    awaitPointerEventScope {
                        // 1. Ожидаем касание пальца
                        val down = awaitPointerJointomPress(pass = PointerEventPass.Initial) ?: return@awaitPointerEventScope
                        var isDragStarted = false

                        try {
                            // 2. Инициализируем короткую задержку взлета карты (150 мс вместо дефолтных 500 мс)
                            withTimeout(150) {
                                val upOrDrag = awaitPointerEvent(pass = PointerEventPass.Main)
                                upOrDrag.changes.forEach { it.consume() }
                            }
                        } catch (e: TimeoutCancellationException) {
                            // Если палец удержан дольше 150 мс — активируем взлет карты!
                            isDragStarted = true
                            currentDragTargetInfo.isDragging = true
                            currentDragTargetInfo.dragPosition = startPositionInWindow + down.position
                            currentDragTargetInfo.draggableCard = card
                        }

                        if (isDragStarted) {
                            // 3. Фаза активного перемещения карты
                            drag(down.id) { change ->
                                change.consume()
                                val positionChange = change.positionChange()
                                currentDragTargetInfo.dragPosition = Offset(
                                    currentDragTargetInfo.dragPosition.x + positionChange.x,
                                    currentDragTargetInfo.dragPosition.y + positionChange.y
                                )
                            }

                            // 4. Гарантированный сброс состояния при отрыве пальца
                            currentDragTargetInfo.isDragging = false
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
    var isHovered by remember { mutableStateOf(false) }
    var globalBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned {
            val position = it.positionInWindow()
            globalBounds = Rect(position, Offset(position.x + it.size.width, position.y + it.size.height))
        }
    ) {
        isHovered = dragInfo.isDragging && globalBounds.contains(dragInfo.dragPosition)

        // ИСПРАВЛЕНИЕ DROP: Реагируем реактивно на изменение флага перетаскивания.
        // Как только палец оторван, а мы находились в зоне — немедленно производим Drop.
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

// Вспомогательная функция для регистрации первого нажатия
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.awaitPointerJointomPress(pass: PointerEventPass): PointerInputChange? {
    val event = awaitPointerEvent(pass)
    return event.changes.firstOrNull { it.pressed }
}
