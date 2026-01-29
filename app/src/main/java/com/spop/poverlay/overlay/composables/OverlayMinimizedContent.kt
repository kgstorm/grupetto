package com.spop.poverlay.overlay.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spop.poverlay.R
import com.spop.poverlay.overlay.BackgroundColorDefault
import com.spop.poverlay.overlay.OverlayLocation


@Composable
fun OverlayMinimizedContent(
    isMinimized: Boolean,
    showTimerWhenMinimized: Boolean,
    location: OverlayLocation,
    powerLabel: String,
    cadenceLabel: String,
    speedLabel: String,
    resistanceLabel: String,
    heartLabel: String,
    // optional additional stats
    showCalories: Boolean = false,
    caloriesLabel: String = "",
    contentAlpha: Float,
    timerLabel: String,
    timerPaused: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onMinimizeToggle: () -> Unit,
    onSendResults: () -> Unit = {},
    onResetNoSend: () -> Unit = {},
    heartAvailable: Boolean = false,
    onLayout: (IntSize) -> Unit
) {
    val showConfirm = remember { mutableStateOf(false) }
    val backgroundShape = if (isMinimized) {
        RoundedCornerShape(8.dp)
    } else {
        when (location) {
            OverlayLocation.Top -> RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            OverlayLocation.Bottom -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        }
    }
    val expandedVerticalPadding = if (isMinimized) {
        1.dp
    } else {
        0.dp
    }
    val size = remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = Modifier
            .alpha(contentAlpha)
            .wrapContentSize().onSizeChanged {
                if (it.width != size.value.width || it.height != size.value.height) {
                    size.value = it
                    onLayout(size.value)
                }
            }
            .padding(vertical = expandedVerticalPadding)
            .background(
                color = BackgroundColorDefault,
                shape = backgroundShape,
            )
            .padding(horizontal = 10.dp)
            .padding(top = 1.dp)
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                    onLongPress = {
                        onLongPress()
                    }
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        if (!isMinimized || showTimerWhenMinimized || timerPaused) {

            val timerAlpha = if (timerPaused) {
                infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                ).value
            } else {
                1f
            }

            OverlayTimerField(
                modifier = Modifier
                    .width(80.dp)
                    .alpha(timerAlpha),
                timerLabel = timerLabel,
                iconDrawable = R.drawable.ic_timer,
                onClick = { showConfirm.value = true }
            )
        }

    if (showConfirm.value) {
        // In-overlay confirmation panel (avoid platform Dialogs inside a service/window overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .background(Color(30, 30, 30), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Send results to HA?", color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        showConfirm.value = false
                        // Don't send but reset
                        onResetNoSend()
                    }) { Text("Don't Send") }
                    TextButton(onClick = {
                        showConfirm.value = false
                        onSendResults()
                    }) { Text("Send") }
                    TextButton(onClick = { showConfirm.value = false }) { Text("Cancel") }
                }
            }
        }
    }

        // Minimize/Maximize button
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = if (isMinimized) {
                when (location) {
                    OverlayLocation.Top -> Icons.Filled.KeyboardArrowDown
                    OverlayLocation.Bottom -> Icons.Filled.KeyboardArrowUp
                }
            } else {
                when (location) {
                    OverlayLocation.Top -> Icons.Filled.KeyboardArrowUp
                    OverlayLocation.Bottom -> Icons.Filled.KeyboardArrowDown
                }
            },
            contentDescription = if (isMinimized) "Expand" else "Minimize",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .clickable { onMinimizeToggle() }
        )

        if (isMinimized) {
            Spacer(modifier = Modifier.width(4.dp))
            OverlayTimerField(
                modifier = Modifier.width(58.dp),
                timerLabel = powerLabel,
                iconDrawable = R.drawable.ic_power
            )
            Spacer(modifier = Modifier.width(4.dp))
            OverlayTimerField(
                modifier = Modifier.width(58.dp),
                timerLabel = cadenceLabel,
                iconDrawable = R.drawable.ic_cadence
            )
            Spacer(modifier = Modifier.width(4.dp))
            OverlayTimerField(
                modifier = Modifier.width(58.dp),
                timerLabel = resistanceLabel,
                iconDrawable = R.drawable.ic_resistance
            )
            Spacer(modifier = Modifier.width(4.dp))
            OverlayTimerField(
                modifier = Modifier.width(58.dp),
                timerLabel = speedLabel,
                iconDrawable = R.drawable.ic_speed
            )
            if (heartAvailable) {
                Spacer(modifier = Modifier.width(4.dp))
                OverlayTimerField(
                    modifier = Modifier.width(62.dp),
                    timerLabel = heartLabel,
                    iconDrawable = R.drawable.ic_heart
                )
            }
            if (showCalories) {
                Spacer(modifier = Modifier.width(4.dp))
                OverlayTimerField(
                    modifier = Modifier.width(62.dp),
                    timerLabel = caloriesLabel,
                    iconDrawable = R.drawable.ic_calories
                )
            }
        }
    }
}

@Composable
private fun OverlayTimerField(
    modifier: Modifier,
    timerLabel: String,
    iconDrawable: Int,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            modifier = Modifier
                .requiredHeight(20.dp)
                .requiredWidth(16.dp)
                .align(Alignment.CenterVertically)
                .padding(vertical = 4.dp),
            painter = painterResource(id = iconDrawable),
            contentDescription = null,
        )
        Text(
            timerLabel,
            color = Color.White,
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
