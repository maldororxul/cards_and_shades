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

// Глобальный реестр для отслеживания координат всех DropTarget на экране
internal class DropTargetBounds(
    val id: String,
    val bounds: Rect,
    val onDropped: (CardModel) -> Unit
)

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var draggableCard: CardModel? by mutableStateOf(null)

    // Список всех активных зон сброса на экране
    val activeDropTargets = mutableStateListOf<DropTargetBounds>()
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
    var currentPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }
    var isVerticalDragActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onGloballyPositioned { currentPositionInWindow = it.positionInWindow() }
            .pointerInput(card.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    accumulatedDrag = Offset.Zero
                    isVerticalDragActive = false

                    val initialFingerGlobalPosition = currentPositionInWindow + down.position

                    drag(down.id) { change ->
                        val dragDelta = change.positionChange()
                        accumulatedDrag = Offset(accumulatedDrag.x + dragDelta.x, accumulatedDrag.y + dragDelta.y)

                        if (!isVerticalDragActive) {
                            if (accumulatedDrag.y < -15f && abs(accumulatedDrag.y) > abs(accumulatedDrag.x)) {
                                isVerticalDragActive = true
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

                    // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Обрабатываем сброс прямо в момент отпускания пальца
                    if (isVerticalDragActive) {
                        val finalDropPosition = currentDragTargetInfo.dragPosition

                        // Ищем, в какую из зарегистрированных зон попал палец
                        val targetZone = currentDragTargetInfo.activeDropTargets.find { zone ->
                            zone.bounds.contains(finalDropPosition)
                        }

                        // Если зона найдена — принудительно активируем логику розыгрыша карты
                        targetZone?.onDropped?.invoke(card)

                        // Очищаем глобальное состояние
                        currentDragTargetInfo.isDragging = false
                        isVerticalDragActive = false
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
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInWindow()
            globalBounds = Rect(position, Offset(position.x + coordinates.size.width, position.y + coordinates.size.height))
        }
    ) {
        isHovered = dragInfo.isDragging && globalBounds.contains(dragInfo.dragPosition)

        // Регистрируем зону сброса в глобальном реестре при отрисовке и обновляем при изменении границ
        val currentZone = remember(globalBounds) {
            DropTargetBounds(
                id = "player_battle_board",
                bounds = globalBounds,
                onDropped = onCardDropped
            )
        }

        DisposableEffect(currentZone) {
            dragInfo.activeDropTargets.add(currentZone)
            onDispose {
                dragInfo.activeDropTargets.remove(currentZone)
            }
        }

        content(isHovered)
    }
}

private fun androidx.compose.ui.input.pointer.PointerInputChange.positionChange(): Offset {
    val previous = previousPosition
    val current = position
    return Offset(current.x - previous.x, current.y - previous.y)
}