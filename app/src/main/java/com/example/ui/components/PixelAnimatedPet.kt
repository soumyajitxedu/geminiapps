package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PixelifySansFontFamily
import kotlin.math.roundToInt

enum class PetType(val displayName: String, val emoji: String) {
    DUCK("Pixel Duck", "🦆"),
    RABBIT("Pixel Bunny", "🐰"),
    SHEEP("Pixel Lamb", "🐑")
}

@Composable
fun PixelAnimatedPet(
    modifier: Modifier = Modifier,
    speechQuote: String = "Quack! You got this! Keep focusing!"
) {
    var petType by remember { mutableIntStateOf(0) }
    val currentPet = PetType.entries[petType % PetType.entries.size]

    val infiniteTransition = rememberInfiniteTransition(label = "petMovement")

    // Walk back and forth across 0f to 1f
    val walkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walkProgress"
    )

    // Bobbing / leg stepping animation
    val bobProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobProgress"
    )

    // Direction flip based on whether walkProgress is advancing
    // In Compose, we can approximate direction by seeing walk progress
    val isFacingRight = walkProgress < 0.98f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .testTag("animated_pet_container")
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val petWidthPx = 140f
        val maxTravelPx = (totalWidthPx - petWidthPx).coerceAtLeast(0f)
        val currentXPx = (maxTravelPx * walkProgress).roundToInt()
        val bobOffset = (bobProgress * 4f).roundToInt()

        // Walking ground line
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
        ) {
            drawLine(
                color = Color(0xFF6B7280).copy(alpha = 0.35f),
                start = Offset(0f, 2.dp.toPx()),
                end = Offset(size.width, 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Pet with Speech Bubble moving together
        Column(
            modifier = Modifier
                .offset { IntOffset(x = currentXPx, y = 0) }
                .width(170.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speech Bubble
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        petType = (petType + 1) % PetType.entries.size
                    }
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPet.emoji,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = speechQuote,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Animated Pet Canvas (Interactive click to switch pet!)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .offset { IntOffset(x = 0, y = bobOffset) }
                    .clickable {
                        petType = (petType + 1) % PetType.entries.size
                    }
            ) {
                when (currentPet) {
                    PetType.DUCK -> PixelDuckCanvas(isFacingRight = isFacingRight, legStep = bobProgress > 0.5f)
                    PetType.RABBIT -> PixelRabbitCanvas(isFacingRight = isFacingRight, legStep = bobProgress > 0.5f)
                    PetType.SHEEP -> PixelSheepCanvas(isFacingRight = isFacingRight, legStep = bobProgress > 0.5f)
                }
            }
        }
    }
}

@Composable
private fun PixelDuckCanvas(isFacingRight: Boolean, legStep: Boolean) {
    Canvas(
        modifier = Modifier
            .size(50.dp)
            .scale(scaleX = if (isFacingRight) 1f else -1f, scaleY = 1f)
    ) {
        val yellow = Color(0xFFFACC15)
        val darkYellow = Color(0xFFEAB308)
        val orange = Color(0xFFF97316)
        val eyeColor = Color(0xFF1F2937)
        val white = Color.White

        // Body
        drawRect(yellow, topLeft = Offset(10.dp.toPx(), 18.dp.toPx()), size = Size(26.dp.toPx(), 18.dp.toPx()))
        drawRect(darkYellow, topLeft = Offset(8.dp.toPx(), 22.dp.toPx()), size = Size(6.dp.toPx(), 12.dp.toPx())) // Tail

        // Head
        drawRect(yellow, topLeft = Offset(24.dp.toPx(), 8.dp.toPx()), size = Size(16.dp.toPx(), 14.dp.toPx()))

        // Eye
        drawRect(eyeColor, topLeft = Offset(32.dp.toPx(), 12.dp.toPx()), size = Size(3.dp.toPx(), 3.dp.toPx()))
        drawRect(white, topLeft = Offset(33.dp.toPx(), 12.dp.toPx()), size = Size(1.dp.toPx(), 1.dp.toPx()))

        // Beak
        drawRect(orange, topLeft = Offset(38.dp.toPx(), 14.dp.toPx()), size = Size(8.dp.toPx(), 5.dp.toPx()))

        // Wing
        drawRect(darkYellow, topLeft = Offset(16.dp.toPx(), 22.dp.toPx()), size = Size(12.dp.toPx(), 8.dp.toPx()))

        // Legs (animated step)
        val leg1Y = if (legStep) 36.dp.toPx() else 38.dp.toPx()
        val leg2Y = if (legStep) 38.dp.toPx() else 36.dp.toPx()

        drawRect(orange, topLeft = Offset(18.dp.toPx(), leg1Y), size = Size(3.dp.toPx(), 6.dp.toPx()))
        drawRect(orange, topLeft = Offset(16.dp.toPx(), leg1Y + 5.dp.toPx()), size = Size(7.dp.toPx(), 3.dp.toPx()))

        drawRect(orange, topLeft = Offset(28.dp.toPx(), leg2Y), size = Size(3.dp.toPx(), 6.dp.toPx()))
        drawRect(orange, topLeft = Offset(26.dp.toPx(), leg2Y + 5.dp.toPx()), size = Size(7.dp.toPx(), 3.dp.toPx()))
    }
}

@Composable
private fun PixelRabbitCanvas(isFacingRight: Boolean, legStep: Boolean) {
    Canvas(
        modifier = Modifier
            .size(50.dp)
            .scale(scaleX = if (isFacingRight) 1f else -1f, scaleY = 1f)
    ) {
        val fur = Color(0xFFF3F4F6)
        val innerEar = Color(0xFFF472B6)
        val shadow = Color(0xFFE5E7EB)
        val eyeColor = Color(0xFFBE185D)

        // Body
        drawRect(fur, topLeft = Offset(10.dp.toPx(), 20.dp.toPx()), size = Size(24.dp.toPx(), 16.dp.toPx()))
        // Fluffy tail
        drawRect(fur, topLeft = Offset(6.dp.toPx(), 24.dp.toPx()), size = Size(6.dp.toPx(), 6.dp.toPx()))

        // Head
        drawRect(fur, topLeft = Offset(22.dp.toPx(), 12.dp.toPx()), size = Size(16.dp.toPx(), 14.dp.toPx()))

        // Long Ears
        drawRect(fur, topLeft = Offset(24.dp.toPx(), 2.dp.toPx()), size = Size(4.dp.toPx(), 11.dp.toPx()))
        drawRect(innerEar, topLeft = Offset(25.dp.toPx(), 4.dp.toPx()), size = Size(2.dp.toPx(), 8.dp.toPx()))

        drawRect(fur, topLeft = Offset(30.dp.toPx(), 0.dp.toPx()), size = Size(4.dp.toPx(), 13.dp.toPx()))
        drawRect(innerEar, topLeft = Offset(31.dp.toPx(), 2.dp.toPx()), size = Size(2.dp.toPx(), 9.dp.toPx()))

        // Eye & Nose
        drawRect(eyeColor, topLeft = Offset(31.dp.toPx(), 16.dp.toPx()), size = Size(3.dp.toPx(), 3.dp.toPx()))
        drawRect(innerEar, topLeft = Offset(37.dp.toPx(), 20.dp.toPx()), size = Size(3.dp.toPx(), 2.dp.toPx()))

        // Paws (animated)
        val paw1Y = if (legStep) 34.dp.toPx() else 36.dp.toPx()
        val paw2Y = if (legStep) 36.dp.toPx() else 34.dp.toPx()

        drawRect(shadow, topLeft = Offset(14.dp.toPx(), paw1Y), size = Size(6.dp.toPx(), 4.dp.toPx()))
        drawRect(shadow, topLeft = Offset(26.dp.toPx(), paw2Y), size = Size(6.dp.toPx(), 4.dp.toPx()))
    }
}

@Composable
private fun PixelSheepCanvas(isFacingRight: Boolean, legStep: Boolean) {
    Canvas(
        modifier = Modifier
            .size(50.dp)
            .scale(scaleX = if (isFacingRight) 1f else -1f, scaleY = 1f)
    ) {
        val wool = Color(0xFFF9FAFB)
        val woolDark = Color(0xFFE5E7EB)
        val skin = Color(0xFF374151)
        val eye = Color.White

        // Wool Cloud Body
        drawRect(wool, topLeft = Offset(8.dp.toPx(), 14.dp.toPx()), size = Size(26.dp.toPx(), 20.dp.toPx()))
        drawRect(woolDark, topLeft = Offset(12.dp.toPx(), 12.dp.toPx()), size = Size(18.dp.toPx(), 4.dp.toPx()))

        // Head
        drawRect(skin, topLeft = Offset(26.dp.toPx(), 16.dp.toPx()), size = Size(14.dp.toPx(), 12.dp.toPx()))
        drawRect(skin, topLeft = Offset(24.dp.toPx(), 18.dp.toPx()), size = Size(4.dp.toPx(), 8.dp.toPx())) // Ear

        // Eye
        drawRect(eye, topLeft = Offset(33.dp.toPx(), 19.dp.toPx()), size = Size(2.dp.toPx(), 2.dp.toPx()))

        // Legs
        val leg1Y = if (legStep) 33.dp.toPx() else 35.dp.toPx()
        val leg2Y = if (legStep) 35.dp.toPx() else 33.dp.toPx()

        drawRect(skin, topLeft = Offset(12.dp.toPx(), leg1Y), size = Size(3.dp.toPx(), 7.dp.toPx()))
        drawRect(skin, topLeft = Offset(26.dp.toPx(), leg2Y), size = Size(3.dp.toPx(), 7.dp.toPx()))
    }
}
