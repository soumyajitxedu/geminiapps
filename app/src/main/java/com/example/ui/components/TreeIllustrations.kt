package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.data.model.TreeSpecies

@Composable
fun TreeCanvas(
    species: TreeSpecies,
    isDead: Boolean,
    modifier: Modifier = Modifier,
    growthProgress: Float = 1f
) {
    val animatedGrowth by animateFloatAsState(
        targetValue = growthProgress.coerceIn(0.05f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "growthProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isDead) {
                drawPixelDeadTree(w, h)
            } else {
                when {
                    animatedGrowth < 0.25f -> {
                        // Stage 1: Tiny Seed in dirt
                        drawPixelSeed(w, h, animatedGrowth / 0.25f)
                    }
                    animatedGrowth < 0.55f -> {
                        // Stage 2: Cute Sprout
                        val sproutProgress = (animatedGrowth - 0.25f) / 0.3f
                        drawPixelSprout(w, h, sproutProgress)
                    }
                    animatedGrowth < 0.85f -> {
                        // Stage 3: Small Growing Plant
                        val plantProgress = (animatedGrowth - 0.55f) / 0.3f
                        drawPixelSmallPlant(w, h, species, plantProgress)
                    }
                    else -> {
                        // Stage 4: Fully Grown Pixel Tree
                        val matureScale = ((animatedGrowth - 0.85f) / 0.15f).coerceIn(0.85f, 1f)
                        when (species) {
                            TreeSpecies.OAK -> drawPixelOakTree(w, h, matureScale)
                            TreeSpecies.CHERRY_BLOSSOM -> drawPixelCherryBlossomTree(w, h, matureScale)
                            TreeSpecies.PINE -> drawPixelPineTree(w, h, matureScale)
                            TreeSpecies.BONSAI -> drawPixelBonsaiTree(w, h, matureScale)
                        }
                    }
                }
            }
        }
    }
}

// Utility to draw chunky pixel blocks on a coordinate grid
private fun DrawScope.drawPixelBlock(
    gridX: Float,
    gridY: Float,
    gridCols: Float,
    gridRows: Float,
    color: Color,
    w: Float,
    h: Float,
    spanX: Float = 1f,
    spanY: Float = 1f
) {
    val pixelW = w / gridCols
    val pixelH = h / gridRows
    drawRect(
        color = color,
        topLeft = Offset(gridX * pixelW, gridY * pixelH),
        size = Size(pixelW * spanX, pixelH * spanY)
    )
}

// STAGE 1: Cute Pixel Seed in Soil Mound
private fun DrawScope.drawPixelSeed(w: Float, h: Float, progress: Float) {
    val cols = 24f
    val rows = 24f

    // Dirt mound base
    val dirtDark = Color(0xFF5C381E)
    val dirtMid = Color(0xFF8B5E3C)
    val dirtLight = Color(0xFFA6754E)
    val grass = Color(0xFF48C774)

    // Base soil
    for (x in 4..19) {
        drawPixelBlock(x.toFloat(), 20f, cols, rows, dirtDark, w, h)
        drawPixelBlock(x.toFloat(), 19f, cols, rows, dirtMid, w, h)
    }
    for (x in 6..17) {
        drawPixelBlock(x.toFloat(), 18f, cols, rows, dirtLight, w, h)
    }
    // Grass specks
    drawPixelBlock(5f, 18f, cols, rows, grass, w, h)
    drawPixelBlock(8f, 17f, cols, rows, grass, w, h)
    drawPixelBlock(15f, 17f, cols, rows, grass, w, h)
    drawPixelBlock(18f, 18f, cols, rows, grass, w, h)

    // Cute Pixel Seed
    val seedDark = Color(0xFF4E2C14)
    val seedBody = Color(0xFFD4A373)
    val seedLight = Color(0xFFFAEDCD)
    val seedY = 16f - (progress * 1.5f)

    drawPixelBlock(10f, seedY + 1, cols, rows, seedDark, w, h, spanX = 4f, spanY = 2f)
    drawPixelBlock(11f, seedY, cols, rows, seedBody, w, h, spanX = 2f, spanY = 2f)
    drawPixelBlock(11f, seedY, cols, rows, seedLight, w, h, spanX = 1f, spanY = 1f)

    // Tiny green life shoot
    if (progress > 0.4f) {
        val shootColor = Color(0xFF55D480)
        drawPixelBlock(12f, seedY - 1, cols, rows, shootColor, w, h)
        if (progress > 0.7f) {
            drawPixelBlock(13f, seedY - 2, cols, rows, Color(0xFF85E3A8), w, h)
        }
    }
}

// STAGE 2: Cute Pixel Sprout with Baby Leaves
private fun DrawScope.drawPixelSprout(w: Float, h: Float, progress: Float) {
    val cols = 24f
    val rows = 24f

    // Soil mound
    for (x in 5..18) {
        drawPixelBlock(x.toFloat(), 20f, cols, rows, Color(0xFF5C381E), w, h)
        drawPixelBlock(x.toFloat(), 19f, cols, rows, Color(0xFF8B5E3C), w, h)
    }
    drawPixelBlock(7f, 18f, cols, rows, Color(0xFF48C774), w, h)
    drawPixelBlock(16f, 18f, cols, rows, Color(0xFF48C774), w, h)

    // Sprout Stem
    val stemH = (4f + (progress * 4f)).toInt()
    val stemTop = 18 - stemH
    for (y in stemTop..18) {
        drawPixelBlock(11f, y.toFloat(), cols, rows, Color(0xFF2E9A53), w, h, spanX = 2f)
    }

    // Two Adorable Leaves
    val leafColor = Color(0xFF48C774)
    val leafHighlight = Color(0xFF85E3A8)

    // Left Leaf
    drawPixelBlock(8f, stemTop.toFloat() - 1, cols, rows, leafColor, w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(9f, stemTop.toFloat() - 1, cols, rows, leafHighlight, w, h, spanX = 1f, spanY = 1f)

    // Right Leaf
    drawPixelBlock(13f, stemTop.toFloat() - 1, cols, rows, leafColor, w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(14f, stemTop.toFloat() - 1, cols, rows, leafHighlight, w, h, spanX = 1f, spanY = 1f)

    // Cute Dewdrop sparkle
    drawPixelBlock(11f, stemTop.toFloat() - 2, cols, rows, Color.White, w, h, spanX = 1f, spanY = 1f)
}

// STAGE 3: Small Growing Plant with Buds
private fun DrawScope.drawPixelSmallPlant(w: Float, h: Float, species: TreeSpecies, progress: Float) {
    val cols = 28f
    val rows = 28f

    // Soil base
    for (x in 7..20) {
        drawPixelBlock(x.toFloat(), 24f, cols, rows, Color(0xFF5C381E), w, h)
        drawPixelBlock(x.toFloat(), 23f, cols, rows, Color(0xFF8B5E3C), w, h)
    }

    // Growing Trunk / Stem
    for (y in 14..22) {
        drawPixelBlock(13f, y.toFloat(), cols, rows, Color(0xFF6F4426), w, h, spanX = 2f)
    }

    val foliageMain = if (species == TreeSpecies.CHERRY_BLOSSOM) Color(0xFFFF85A1) else Color(0xFF48C774)
    val foliageHighlight = if (species == TreeSpecies.CHERRY_BLOSSOM) Color(0xFFFFD1DC) else Color(0xFF85E3A8)
    val foliageShadow = if (species == TreeSpecies.CHERRY_BLOSSOM) Color(0xFFD84A75) else Color(0xFF2E9A53)

    // Lower foliage clusters
    drawPixelBlock(8f, 15f, cols, rows, foliageShadow, w, h, spanX = 5f, spanY = 4f)
    drawPixelBlock(9f, 14f, cols, rows, foliageMain, w, h, spanX = 4f, spanY = 3f)
    drawPixelBlock(10f, 14f, cols, rows, foliageHighlight, w, h, spanX = 2f, spanY = 1f)

    drawPixelBlock(15f, 14f, cols, rows, foliageShadow, w, h, spanX = 5f, spanY = 4f)
    drawPixelBlock(15f, 13f, cols, rows, foliageMain, w, h, spanX = 4f, spanY = 3f)
    drawPixelBlock(16f, 13f, cols, rows, foliageHighlight, w, h, spanX = 2f, spanY = 1f)

    // Top crown bud
    val topH = (4f + progress * 2f).toInt()
    drawPixelBlock(11f, 10f, cols, rows, foliageShadow, w, h, spanX = 6f, spanY = topH.toFloat())
    drawPixelBlock(12f, 9f, cols, rows, foliageMain, w, h, spanX = 4f, spanY = topH.toFloat())
    drawPixelBlock(13f, 8f, cols, rows, foliageHighlight, w, h, spanX = 2f, spanY = 2f)

    // Cute blossom or fruit dot
    val budColor = if (species == TreeSpecies.CHERRY_BLOSSOM) Color(0xFFFFD166) else Color(0xFFFF6584)
    drawPixelBlock(13f, 11f, cols, rows, budColor, w, h, spanX = 2f, spanY = 2f)
}

// STAGE 4: Fully Grown Pixel Oak Tree
private fun DrawScope.drawPixelOakTree(w: Float, h: Float, scale: Float) {
    val cols = 32f
    val rows = 32f

    // Soil mound
    for (x in 7..24) {
        drawPixelBlock(x.toFloat(), 28f, cols, rows, Color(0xFF5C381E), w, h)
        drawPixelBlock(x.toFloat(), 27f, cols, rows, Color(0xFF8B5E3C), w, h)
    }
    // Grass tufts
    drawPixelBlock(8f, 26f, cols, rows, Color(0xFF48C774), w, h, spanX = 2f)
    drawPixelBlock(22f, 26f, cols, rows, Color(0xFF48C774), w, h, spanX = 2f)

    // Sturdy Pixel Trunk
    val trunkDark = Color(0xFF4E2C14)
    val trunkMain = Color(0xFF754522)
    val trunkLight = Color(0xFF9E6334)

    for (y in 17..26) {
        drawPixelBlock(14f, y.toFloat(), cols, rows, trunkDark, w, h, spanX = 4f)
        drawPixelBlock(15f, y.toFloat(), cols, rows, trunkMain, w, h, spanX = 2f)
        drawPixelBlock(16f, y.toFloat(), cols, rows, trunkLight, w, h, spanX = 1f)
    }
    // Roots flare
    drawPixelBlock(12f, 26f, cols, rows, trunkDark, w, h, spanX = 2f, spanY = 1f)
    drawPixelBlock(18f, 26f, cols, rows, trunkMain, w, h, spanX = 2f, spanY = 1f)

    // Lush Layered Pixel Canopy
    val greenDeep = Color(0xFF1B5E20)
    val greenMid = Color(0xFF2E9A53)
    val greenBright = Color(0xFF48C774)
    val greenHighlight = Color(0xFF85E3A8)

    // Bottom dark canopy layer
    drawPixelBlock(6f, 11f, cols, rows, greenDeep, w, h, spanX = 20f, spanY = 7f)
    drawPixelBlock(8f, 8f, cols, rows, greenDeep, w, h, spanX = 16f, spanY = 10f)

    // Main vibrant foliage
    drawPixelBlock(7f, 10f, cols, rows, greenMid, w, h, spanX = 18f, spanY = 6f)
    drawPixelBlock(9f, 6f, cols, rows, greenMid, w, h, spanX = 14f, spanY = 10f)

    // Bright foliage puffs
    drawPixelBlock(8f, 9f, cols, rows, greenBright, w, h, spanX = 6f, spanY = 5f)
    drawPixelBlock(18f, 9f, cols, rows, greenBright, w, h, spanX = 6f, spanY = 5f)
    drawPixelBlock(11f, 5f, cols, rows, greenBright, w, h, spanX = 10f, spanY = 6f)

    // Minty sunlight highlights
    drawPixelBlock(12f, 4f, cols, rows, greenHighlight, w, h, spanX = 6f, spanY = 2f)
    drawPixelBlock(9f, 8f, cols, rows, greenHighlight, w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(19f, 8f, cols, rows, greenHighlight, w, h, spanX = 3f, spanY = 2f)

    // Cute Pixel Red Apples in Canopy
    val apple = Color(0xFFFF5252)
    val appleShine = Color(0xFFFFD1D1)
    val applePositions = listOf(
        Pair(10f, 11f),
        Pair(15f, 9f),
        Pair(20f, 12f),
        Pair(13f, 14f),
        Pair(17f, 13f)
    )
    for ((ax, ay) in applePositions) {
        drawPixelBlock(ax, ay, cols, rows, apple, w, h, spanX = 2f, spanY = 2f)
        drawPixelBlock(ax, ay, cols, rows, appleShine, w, h, spanX = 1f, spanY = 1f)
    }
}

// STAGE 4: Fully Grown Pixel Cherry Blossom Tree
private fun DrawScope.drawPixelCherryBlossomTree(w: Float, h: Float, scale: Float) {
    val cols = 32f
    val rows = 32f

    // Soft pink petal ground
    for (x in 7..24) {
        drawPixelBlock(x.toFloat(), 28f, cols, rows, Color(0xFFF7CAD0), w, h)
        drawPixelBlock(x.toFloat(), 27f, cols, rows, Color(0xFFFFE5EC), w, h)
    }

    // Curved Dark Wood Trunk
    for (y in 17..26) {
        drawPixelBlock(14f, y.toFloat(), cols, rows, Color(0xFF4A3528), w, h, spanX = 3f)
        drawPixelBlock(15f, y.toFloat(), cols, rows, Color(0xFF6B4D3B), w, h, spanX = 1f)
    }
    // Branching arms
    drawPixelBlock(11f, 16f, cols, rows, Color(0xFF4A3528), w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(17f, 16f, cols, rows, Color(0xFF4A3528), w, h, spanX = 4f, spanY = 2f)

    // Cute Fluffy Pink Blossom Canopy
    val pinkDeep = Color(0xFFE05780)
    val pinkMid = Color(0xFFFF85A1)
    val pinkSoft = Color(0xFFFFADC6)
    val pinkWhite = Color(0xFFFFF0F5)

    // Base deep pink
    drawPixelBlock(6f, 10f, cols, rows, pinkDeep, w, h, spanX = 20f, spanY = 7f)
    drawPixelBlock(8f, 7f, cols, rows, pinkDeep, w, h, spanX = 16f, spanY = 10f)

    // Mid puff
    drawPixelBlock(7f, 9f, cols, rows, pinkMid, w, h, spanX = 18f, spanY = 6f)
    drawPixelBlock(10f, 5f, cols, rows, pinkMid, w, h, spanX = 12f, spanY = 8f)

    // Soft highlight clouds
    drawPixelBlock(8f, 8f, cols, rows, pinkSoft, w, h, spanX = 6f, spanY = 4f)
    drawPixelBlock(18f, 8f, cols, rows, pinkSoft, w, h, spanX = 6f, spanY = 4f)
    drawPixelBlock(12f, 4f, cols, rows, pinkSoft, w, h, spanX = 8f, spanY = 4f)

    // White sparkle petals
    drawPixelBlock(13f, 3f, cols, rows, pinkWhite, w, h, spanX = 4f, spanY = 2f)
    drawPixelBlock(9f, 7f, cols, rows, pinkWhite, w, h, spanX = 2f, spanY = 1f)

    // Floating pixel petals
    drawPixelBlock(5f, 18f, cols, rows, pinkMid, w, h, spanX = 1f, spanY = 1f)
    drawPixelBlock(23f, 16f, cols, rows, pinkSoft, w, h, spanX = 1f, spanY = 1f)
    drawPixelBlock(25f, 21f, cols, rows, pinkMid, w, h, spanX = 1f, spanY = 1f)
}

// STAGE 4: Fully Grown Pixel Pine Tree
private fun DrawScope.drawPixelPineTree(w: Float, h: Float, scale: Float) {
    val cols = 32f
    val rows = 32f

    // Ground
    for (x in 8..23) {
        drawPixelBlock(x.toFloat(), 28f, cols, rows, Color(0xFF5C381E), w, h)
        drawPixelBlock(x.toFloat(), 27f, cols, rows, Color(0xFF8B5E3C), w, h)
    }

    // Straight Pine Trunk
    for (y in 21..26) {
        drawPixelBlock(14f, y.toFloat(), cols, rows, Color(0xFF5C381E), w, h, spanX = 4f)
        drawPixelBlock(15f, y.toFloat(), cols, rows, Color(0xFF8B5E3C), w, h, spanX = 2f)
    }

    // Tier 1 (Bottom, widest)
    drawPixelBlock(7f, 18f, cols, rows, Color(0xFF1B5E20), w, h, spanX = 18f, spanY = 4f)
    drawPixelBlock(8f, 17f, cols, rows, Color(0xFF2E9A53), w, h, spanX = 16f, spanY = 2f)
    drawPixelBlock(9f, 16f, cols, rows, Color(0xFF48C774), w, h, spanX = 14f, spanY = 2f)

    // Tier 2 (Middle)
    drawPixelBlock(9f, 13f, cols, rows, Color(0xFF1B5E20), w, h, spanX = 14f, spanY = 4f)
    drawPixelBlock(10f, 12f, cols, rows, Color(0xFF2E9A53), w, h, spanX = 12f, spanY = 2f)
    drawPixelBlock(11f, 11f, cols, rows, Color(0xFF48C774), w, h, spanX = 10f, spanY = 2f)

    // Tier 3 (Top point)
    drawPixelBlock(11f, 8f, cols, rows, Color(0xFF1B5E20), w, h, spanX = 10f, spanY = 4f)
    drawPixelBlock(12f, 6f, cols, rows, Color(0xFF2E9A53), w, h, spanX = 8f, spanY = 3f)
    drawPixelBlock(13f, 4f, cols, rows, Color(0xFF48C774), w, h, spanX = 6f, spanY = 3f)
    drawPixelBlock(15f, 2f, cols, rows, Color(0xFF85E3A8), w, h, spanX = 2f, spanY = 3f)
}

// STAGE 4: Fully Grown Pixel Bonsai Tree
private fun DrawScope.drawPixelBonsaiTree(w: Float, h: Float, scale: Float) {
    val cols = 32f
    val rows = 32f

    // Ceramic Zen Planter
    val potDark = Color(0xFF2B2D42)
    val potMain = Color(0xFF4A4E69)
    val potHighlight = Color(0xFF6C757D)

    drawPixelBlock(8f, 25f, cols, rows, potDark, w, h, spanX = 16f, spanY = 4f)
    drawPixelBlock(9f, 26f, cols, rows, potMain, w, h, spanX = 14f, spanY = 2f)
    drawPixelBlock(7f, 24f, cols, rows, potHighlight, w, h, spanX = 18f, spanY = 1f)

    // Gnarled S-curved Bonsai Trunk
    val woodDark = Color(0xFF382212)
    val woodMid = Color(0xFF5C381E)

    // Base to middle curve
    drawPixelBlock(14f, 22f, cols, rows, woodDark, w, h, spanX = 4f, spanY = 3f)
    drawPixelBlock(12f, 19f, cols, rows, woodMid, w, h, spanX = 4f, spanY = 3f)
    drawPixelBlock(14f, 16f, cols, rows, woodDark, w, h, spanX = 5f, spanY = 3f)
    drawPixelBlock(13f, 13f, cols, rows, woodMid, w, h, spanX = 3f, spanY = 3f)

    // Zen Foliage Cloud Pads
    val cloudDark = Color(0xFF2E7D32)
    val cloudMid = Color(0xFF43A047)
    val cloudLight = Color(0xFF7CB342)

    // Left lower pad
    drawPixelBlock(7f, 17f, cols, rows, cloudDark, w, h, spanX = 7f, spanY = 3f)
    drawPixelBlock(8f, 16f, cols, rows, cloudMid, w, h, spanX = 5f, spanY = 2f)
    drawPixelBlock(9f, 15f, cols, rows, cloudLight, w, h, spanX = 3f, spanY = 1f)

    // Right middle pad
    drawPixelBlock(18f, 14f, cols, rows, cloudDark, w, h, spanX = 8f, spanY = 4f)
    drawPixelBlock(19f, 13f, cols, rows, cloudMid, w, h, spanX = 6f, spanY = 2f)
    drawPixelBlock(20f, 12f, cols, rows, cloudLight, w, h, spanX = 4f, spanY = 1f)

    // Top crown pad
    drawPixelBlock(11f, 10f, cols, rows, cloudDark, w, h, spanX = 8f, spanY = 4f)
    drawPixelBlock(12f, 8f, cols, rows, cloudMid, w, h, spanX = 6f, spanY = 3f)
    drawPixelBlock(13f, 7f, cols, rows, cloudLight, w, h, spanX = 4f, spanY = 1f)
}

// DEAD / WITHERED PIXEL TREE (Game Over / Abandoned session)
private fun DrawScope.drawPixelDeadTree(w: Float, h: Float) {
    val cols = 32f
    val rows = 32f

    // Barren grey cracked dirt
    val dirtDeadDark = Color(0xFF3D405B)
    val dirtDeadMid = Color(0xFF6C757D)
    val dirtDeadLight = Color(0xFF8D99AE)

    for (x in 7..24) {
        drawPixelBlock(x.toFloat(), 28f, cols, rows, dirtDeadDark, w, h)
        drawPixelBlock(x.toFloat(), 27f, cols, rows, dirtDeadMid, w, h)
    }
    drawPixelBlock(10f, 26f, cols, rows, dirtDeadLight, w, h, spanX = 3f)
    drawPixelBlock(19f, 26f, cols, rows, dirtDeadLight, w, h, spanX = 3f)

    // Twisted, Snapped, Leafless Grey-Brown Trunk
    val deadBarkDark = Color(0xFF2B2D42)
    val deadBarkMid = Color(0xFF4A4E69)
    val deadBarkLight = Color(0xFF6C757D)

    // Main cracked trunk
    for (y in 17..26) {
        drawPixelBlock(14f, y.toFloat(), cols, rows, deadBarkDark, w, h, spanX = 4f)
        drawPixelBlock(15f, y.toFloat(), cols, rows, deadBarkMid, w, h, spanX = 2f)
        drawPixelBlock(16f, y.toFloat(), cols, rows, deadBarkLight, w, h, spanX = 1f)
    }

    // Broken left jagged branch
    drawPixelBlock(10f, 17f, cols, rows, deadBarkDark, w, h, spanX = 4f, spanY = 2f)
    drawPixelBlock(8f, 15f, cols, rows, deadBarkMid, w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(6f, 14f, cols, rows, deadBarkLight, w, h, spanX = 2f, spanY = 1f)

    // Broken right jagged branch
    drawPixelBlock(18f, 16f, cols, rows, deadBarkDark, w, h, spanX = 4f, spanY = 2f)
    drawPixelBlock(21f, 14f, cols, rows, deadBarkMid, w, h, spanX = 3f, spanY = 2f)
    drawPixelBlock(23f, 13f, cols, rows, deadBarkLight, w, h, spanX = 2f, spanY = 1f)

    // Snapped top stump (no canopy leaves!)
    drawPixelBlock(13f, 14f, cols, rows, deadBarkDark, w, h, spanX = 3f, spanY = 3f)
    drawPixelBlock(14f, 11f, cols, rows, deadBarkMid, w, h, spanX = 2f, spanY = 3f)
    drawPixelBlock(17f, 13f, cols, rows, deadBarkLight, w, h, spanX = 2f, spanY = 2f)

    // Fallen crumbled dead pixel leaves on the ground
    val deadLeaf = Color(0xFF5C6770)
    drawPixelBlock(9f, 26f, cols, rows, deadLeaf, w, h, spanX = 2f, spanY = 1f)
    drawPixelBlock(17f, 26f, cols, rows, deadLeaf, w, h, spanX = 1f, spanY = 1f)
    drawPixelBlock(21f, 26f, cols, rows, deadLeaf, w, h, spanX = 2f, spanY = 1f)
    drawPixelBlock(13f, 27f, cols, rows, deadLeaf, w, h, spanX = 2f, spanY = 1f)
}

/**
 * PixelArtEmblem: A pure Jetpack Compose Canvas vector logo for Focus Grove.
 * Replaces raster logos with sharp, scalable pixel-art vector geometry.
 */
@Composable
fun PixelArtEmblem(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cols = 16f
        val rows = 16f

        // Soft sky backdrop
        drawRect(Color(0xFFE8F5E9), size = size)

        // Dirt ground
        for (x in 2..13) {
            drawPixelBlock(x.toFloat(), 13f, cols, rows, Color(0xFF5C381E), w, h)
            drawPixelBlock(x.toFloat(), 14f, cols, rows, Color(0xFF382212), w, h)
        }

        // Tree trunk
        for (y in 9..12) {
            drawPixelBlock(7f, y.toFloat(), cols, rows, Color(0xFF754522), w, h, spanX = 2f)
        }

        // Foliage canopy
        val darkGreen = Color(0xFF1B5E20)
        val brightGreen = Color(0xFF48C774)
        val highlightGreen = Color(0xFF85E3A8)

        drawPixelBlock(4f, 5f, cols, rows, darkGreen, w, h, spanX = 8f, spanY = 5f)
        drawPixelBlock(5f, 4f, cols, rows, brightGreen, w, h, spanX = 6f, spanY = 5f)
        drawPixelBlock(6f, 3f, cols, rows, brightGreen, w, h, spanX = 4f, spanY = 3f)
        drawPixelBlock(6f, 3f, cols, rows, highlightGreen, w, h, spanX = 2f, spanY = 2f)

        // Red pixel apple
        drawPixelBlock(9f, 6f, cols, rows, Color(0xFFFF5252), w, h, spanX = 2f, spanY = 2f)
        drawPixelBlock(9f, 6f, cols, rows, Color(0xFFFFD1D1), w, h, spanX = 1f, spanY = 1f)

        // Golden sparkle
        drawPixelBlock(3f, 3f, cols, rows, Color(0xFFFFD166), w, h, spanX = 1f, spanY = 1f)
        drawPixelBlock(12f, 4f, cols, rows, Color(0xFFFFD166), w, h, spanX = 1f, spanY = 1f)
    }
}
