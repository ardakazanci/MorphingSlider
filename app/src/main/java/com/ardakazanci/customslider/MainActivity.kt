package com.ardakazanci.customslider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ardakazanci.customslider.ui.theme.CustomSliderTheme
import com.ardakazanci.customslider.util.snapValue
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomSliderTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) { innerPadding ->
                    SliderDemoScreen(modifier = Modifier.padding(innerPadding)) // optional inner padding
                }
            }
        }
    }
}

@Composable
fun SliderDemoScreen(modifier: Modifier = Modifier) {
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    Box(modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Value: ${String.format("%.1f", sliderValue)}")
            Spacer(Modifier.height(24.dp))
            MorphingSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..10f,
                thumbBorderColor = Color.Black,
                thumbBackgroundColor = Color.White,
                trackColor = Color.LightGray,
                activeTrackColor = Color.Black,
                thumbSize = 40.dp,
                expandedThumbHeight = 100.dp,
                borderWidth = 2.dp,
                snapToTicks = false, // optional ...,....,....,
                tickCount = 5, // optional ,.,.,.,.,.
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun MorphingSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    thumbBorderColor: Color = Color.Black,
    thumbBackgroundColor: Color = Color.White,
    trackColor: Color = Color.LightGray,
    activeTrackColor: Color = Color.Black,
    thumbSize: Dp = 30.dp,
    expandedThumbHeight: Dp = 60.dp,
    borderWidth: Dp = 2.dp,
    snapToTicks: Boolean = false,
    tickCount: Int = 0
) {
    var isDragging by remember { mutableStateOf(false) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    val animatedHeight by animateDpAsState(
        targetValue = if (isDragging) expandedThumbHeight else thumbSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val animatedWidth = thumbSize
    val textOffsetY by animateDpAsState(
        targetValue = if (isDragging) 30.dp else 0.dp,
        animationSpec = spring()
    )

    val fraction by derivedStateOf {
        (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    }
    val thumbPosX by derivedStateOf {
        fraction.coerceIn(0f, 1f) * sliderWidth
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = animatedWidth / 2)
            .clipToBounds()
            .onGloballyPositioned { sliderWidth = it.size.width.toFloat() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val fx = (offset.x / sliderWidth).coerceIn(0f, 1f)
                        val newVal =
                            valueRange.start + fx * (valueRange.endInclusive - valueRange.start)
                        onValueChange(
                            if (snapToTicks) snapValue(
                                newVal,
                                valueRange,
                                tickCount
                            ) else newVal
                        )
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        val fx = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                        val newVal =
                            valueRange.start + fx * (valueRange.endInclusive - valueRange.start)
                        onValueChange(
                            if (snapToTicks) snapValue(
                                newVal,
                                valueRange,
                                tickCount
                            ) else newVal
                        )
                    }
                )
            }
    ) {
        Canvas(Modifier
            .fillMaxWidth()
            .align(Alignment.Center)
            .height(4.dp)) {
            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height
            )
            drawLine(
                color = activeTrackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(thumbPosX, size.height / 2),
                strokeWidth = size.height
            )
            if (tickCount > 1) {
                val step = size.width / (tickCount - 1)
                for (i in 0 until tickCount) {
                    val x = i * step
                    drawLine(
                        color = Color.Gray,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 2f
                    )
                }
            }
        }
        Box(
            Modifier
                .offset { IntOffset(thumbPosX.roundToInt(), 0) }
                .align(Alignment.CenterStart)
                .size(width = animatedWidth, height = animatedHeight)
                .background(thumbBackgroundColor, RoundedCornerShape(percent = 50))
                .border(
                    BorderStroke(borderWidth, thumbBorderColor),
                    RoundedCornerShape(percent = 50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.1f", value), // local optional 🍕
                color = thumbBorderColor,
                modifier = Modifier.offset(y = textOffsetY)
            )
        }
    }
}