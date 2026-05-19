package com.example.cardsandshades.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.cardsandshades.model.CardModel

// Глобальное состояние перетаскивания
internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
    var dragOffset: Offset by mutableStateOf(Offset.Zero)
    var draggableCard: CardModel? by mutableStateOf(null)
    var dataToDrop: Any? by mutableStateOf(null)
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
                var targetSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = state.dragPosition + state.dragOffset
                            translationX = offset.x - targetSize.width / 2
                            translationY = offset.y - targetSize.height / 2
                            scaleX = 0.9f // Слегка уменьшаем карту при таске
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
    Box(
        modifier = modifier.pointerInput(card.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    currentDragTargetInfo.isDragging = true
                    currentDragTargetInfo.dragPosition = offset
                    currentDragTargetInfo.draggableCard = card
                    currentDragTargetInfo.dataToDrop = card
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    currentDragTargetInfo.dragOffset += Offset(dragAmount.x, dragAmount.y)
                },
                onDragEnd = {
                    currentDragTargetInfo.isDragging = false
                    currentDragTargetInfo.dragOffset = Offset.Zero
                },
                onDragCancel = {
                    currentDragTargetInfo.isDragging = false
                    currentDragTargetInfo.dragOffset = Offset.Zero
                }
            )
        }
    ) {
        content()
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
    var bounds: Offset by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { bounds = it.localToWindow(Rect(Offset.Zero, it.size.toSize())) }
    ) {
        // Проверяем, находится ли перетаскиваемый объект над зоной дропа
        val dragPosition = dragInfo.dragPosition + dragInfo.dragOffset
        isHovered = dragInfo.isDragging && bounds.contains(dragPosition)

        // Ловим момент отпускания пальца
        LaunchedEffect(dragInfo.isDragging) {
            if (!dragInfo.isDragging && isHovered) {
                val card = dragInfo.dataToDrop as? CardModel
                if (card != null) {
                    onCardDropped(card)
                }
            }
        }

        content(isHovered)
    }
}

// Расширение для приведения IntSize в Size
private fun androidx.compose.ui.unit.IntSize.toSize() = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
