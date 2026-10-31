package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FlashcardDeck
import com.example.data.model.FlashcardItem
import com.example.data.model.MindMapData
import com.example.data.model.MindMapNode
import com.example.data.model.QuizQuestion
import com.example.data.model.StudyToolParser
import com.example.quiz.QuizParser
import com.example.ui.MainViewModel
import com.example.ui.components.PixelArtEmblem
import com.example.ui.theme.CoinGold
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ICSEBlue

@Composable
fun StudyToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Quiz", "Flashcards", "Mind Maps")
    val tabIcons = listOf(Icons.Default.Quiz, Icons.Default.Style, Icons.Default.AccountTree)

    val errorMessage by viewModel.studyToolErrorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearStudyToolError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Study Tools Header with Material 3 TabRow
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelArtEmblem(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Study Tools",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Active Recall & Conceptual Mastery",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_tools_tab_row"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title, fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    imageVector = tabIcons[index],
                                    contentDescription = title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.testTag("tab_${title.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> QuizTabContent(viewModel = viewModel)
                1 -> FlashcardsTabContent(viewModel = viewModel)
                2 -> MindMapTabContent(viewModel = viewModel)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 1: FLASHCARDS TAB (3D FLIP ANIMATION)
// ----------------------------------------------------------------------------
@Composable
fun FlashcardsTabContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val deck by viewModel.flashcardDeck.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()

    var chapterInput by remember { mutableStateOf("Physics - Force") }
    var jsonInput by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }

    fun copyPrompt() {
        val chapter = chapterInput.ifBlank { "Physics - Force" }
        val prompt = "Generate 10-15 flashcards for chapter: $chapter. Output strictly valid JSON, no markdown. Schema: {\"chapter\": \"String\", \"cards\": [{\"front\": \"Question\", \"back\": \"Answer\"}]}."
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Flashcard Prompt", prompt)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Flashcard AI Prompt copied!", Toast.LENGTH_SHORT).show()
    }

    val cards = deck?.cards ?: emptyList()
    val currentCard = cards.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Prompt Banner & Actions
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AI Flashcard Generator",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chapterInput,
                        onValueChange = { chapterInput = it },
                        label = { Text("Chapter Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flashcard_chapter_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { copyPrompt() },
                        modifier = Modifier.testTag("btn_copy_flashcard_prompt")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy AI Prompt")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showImportSection = !showImportSection }) {
                        Icon(
                            imageVector = if (showImportSection) Icons.Default.Done else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showImportSection) "Hide JSON Input" else "Paste JSON / Import")
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSampleFlashcards() },
                        modifier = Modifier.testTag("btn_sample_flashcards")
                    ) {
                        Text("Sample Deck", fontSize = 12.sp)
                    }
                }

                // Collapsible JSON Input Field
                AnimatedVisibility(visible = showImportSection) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { jsonInput = it },
                            placeholder = { Text("Paste JSON here: {\"chapter\": \"...\", \"cards\": [...]}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("flashcard_json_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (jsonInput.isBlank()) {
                                    Toast.makeText(context, "Please paste JSON first", Toast.LENGTH_SHORT).show()
                                } else {
                                    val success = viewModel.loadFlashcardsFromJson(jsonInput)
                                    if (success) {
                                        Toast.makeText(context, "Loaded ${viewModel.flashcardDeck.value?.cards?.size ?: 0} cards!", Toast.LENGTH_SHORT).show()
                                        showImportSection = false
                                        jsonInput = ""
                                    } else {
                                        Toast.makeText(context, "Invalid JSON format. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_load_flashcards")
                        ) {
                            Text("Load Flashcards")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deck Title & Progress
        if (deck != null && cards.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deck?.chapter ?: "Flashcards",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Card ${currentIndex + 1} of ${cards.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Flipping Card
            if (currentCard != null) {
                var isFlipped by remember(currentIndex, deck) { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "CardFlip"
                )

                val density = LocalDensity.current.density

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 14f * density
                        }
                        .clickable { isFlipped = !isFlipped }
                        .testTag("flashcard_flip_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rotation <= 90f)
                            MaterialTheme.colorScheme.surface
                        else
                            ForestGreenContainer.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            // Front of card (Question)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "QUESTION",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = "Tap to Flip",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = currentCard.front,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Text(
                                    text = "💡 Tap card to flip answer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Back of card (Answer) - Counter-rotate so text is readable
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = ForestGreenPrimary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "ANSWER",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Answer Revealed",
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = currentCard.back,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 22.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Text(
                                    text = "🔄 Tap card to flip back",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Navigation Controls (Previous / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.prevCard() },
                        enabled = currentIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_prev_flashcard")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Previous")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.nextCard() },
                        enabled = currentIndex < cards.size - 1,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_next_flashcard")
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        } else {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Flashcards Loaded",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.loadSampleFlashcards() }) {
                    Text("Load Sample Flashcards")
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 2: MIND MAP TAB (CUSTOM CANVAS + PAN/ZOOM GESTURES)
// ----------------------------------------------------------------------------
@Composable
fun MindMapTabContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val mindMapData by viewModel.mindMapData.collectAsStateWithLifecycle()

    var chapterInput by remember { mutableStateOf("Physics - Force") }
    var jsonInput by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }

    fun copyPrompt() {
        val chapter = chapterInput.ifBlank { "Physics - Force" }
        val prompt = "Generate a hierarchical mind map for chapter: $chapter. Output strictly valid JSON, no markdown. Schema: {\"chapter\": \"String\", \"root\": {\"title\": \"Topic\", \"children\": [{\"title\": \"Sub\", \"children\": []}]}}."
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mind Map Prompt", prompt)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Mind Map AI Prompt copied!", Toast.LENGTH_SHORT).show()
    }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Controls / AI Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chapterInput,
                        onValueChange = { chapterInput = it },
                        label = { Text("Chapter") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mindmap_chapter_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { copyPrompt() },
                        modifier = Modifier.testTag("btn_copy_mindmap_prompt")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy AI Prompt")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showImportSection = !showImportSection }) {
                        Icon(
                            imageVector = if (showImportSection) Icons.Default.Done else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showImportSection) "Hide JSON Input" else "Paste JSON / Load Map")
                    }

                    Row {
                        IconButton(onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                        }) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View")
                        }
                        IconButton(onClick = { scale = (scale + 0.2f).coerceAtMost(3.0f) }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                        }
                        IconButton(onClick = { scale = (scale - 0.2f).coerceAtLeast(0.4f) }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                        }
                    }
                }

                // Collapsible JSON Input Field
                AnimatedVisibility(visible = showImportSection) {
                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { jsonInput = it },
                            placeholder = { Text("Paste JSON here: {\"chapter\": \"...\", \"root\": {\"title\": \"...\", \"children\": [...]}}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("mindmap_json_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.loadSampleMindMap() },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Sample Map")
                            }
                            Button(
                                onClick = {
                                    if (jsonInput.isBlank()) {
                                        Toast.makeText(context, "Please paste JSON first", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val success = viewModel.loadMindMapFromJson(jsonInput)
                                        if (success) {
                                            Toast.makeText(context, "Mind Map Loaded!", Toast.LENGTH_SHORT).show()
                                            showImportSection = false
                                            jsonInput = ""
                                            scale = 1.0f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            Toast.makeText(context, "Invalid JSON format. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("btn_load_mindmap")
                            ) {
                                Text("Load Mind Map")
                            }
                        }
                    }
                }
            }
        }

        // Mind Map Infinite Canvas
        val rootNode = mindMapData?.root
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val surfaceColor = MaterialTheme.colorScheme.surface
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val outlineColor = MaterialTheme.colorScheme.outlineVariant

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.4f, 3.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .testTag("mind_map_canvas_container")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasCenter = Offset(size.width / 2f + offsetX, size.height / 2f + offsetY)

                if (rootNode != null) {
                    drawHorizontalMindMap(
                        root = rootNode,
                        center = canvasCenter,
                        scale = scale,
                        textMeasurer = textMeasurer,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        lineColor = outlineColor
                    )
                }
            }

            // Legend / Gesture hint
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = "🖐️ Drag to pan • Pinch to zoom • Tap Reset to re-center",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Draws the hierarchical Mind Map branching horizontally left and right from the root node.
 */
private fun DrawScope.drawHorizontalMindMap(
    root: MindMapNode,
    center: Offset,
    scale: Float,
    textMeasurer: TextMeasurer,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    lineColor: Color
) {
    val rootWidth = 140f * scale
    val rootHeight = 56f * scale
    val rootRect = Offset(center.x - rootWidth / 2f, center.y - rootHeight / 2f)

    // Split children between right side and left side for symmetrical horizontal layout
    val children = root.children
    val half = (children.size + 1) / 2
    val rightChildren = children.take(half)
    val leftChildren = children.drop(half)

    // Draw Root Node
    drawRoundRect(
        color = primaryColor,
        topLeft = rootRect,
        size = Size(rootWidth, rootHeight),
        cornerRadius = CornerRadius(14f * scale, 14f * scale)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = rootRect,
        size = Size(rootWidth, rootHeight),
        cornerRadius = CornerRadius(14f * scale, 14f * scale),
        style = Stroke(width = 2.5f * scale)
    )

    // Draw Root Text
    val rootTextLayout = textMeasurer.measure(
        text = root.title,
        style = TextStyle(
            color = Color.White,
            fontSize = (13f * scale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    )
    val rootTextOffset = Offset(
        center.x - rootTextLayout.size.width / 2f,
        center.y - rootTextLayout.size.height / 2f
    )
    drawText(rootTextLayout, topLeft = rootTextOffset)

    // Branching metrics
    val horizontalSpacing = 160f * scale
    val verticalSpacing = 72f * scale

    // Draw Right Branches
    drawBranchGroup(
        parentCenter = center,
        children = rightChildren,
        isRight = true,
        horizontalSpacing = horizontalSpacing,
        verticalSpacing = verticalSpacing,
        scale = scale,
        textMeasurer = textMeasurer,
        surfaceColor = surfaceColor,
        secondaryColor = secondaryColor,
        onSurfaceColor = onSurfaceColor,
        lineColor = lineColor
    )

    // Draw Left Branches
    drawBranchGroup(
        parentCenter = center,
        children = leftChildren,
        isRight = false,
        horizontalSpacing = horizontalSpacing,
        verticalSpacing = verticalSpacing,
        scale = scale,
        textMeasurer = textMeasurer,
        surfaceColor = surfaceColor,
        secondaryColor = secondaryColor,
        onSurfaceColor = onSurfaceColor,
        lineColor = lineColor
    )
}

private fun DrawScope.drawBranchGroup(
    parentCenter: Offset,
    children: List<MindMapNode>,
    isRight: Boolean,
    horizontalSpacing: Float,
    verticalSpacing: Float,
    scale: Float,
    textMeasurer: TextMeasurer,
    surfaceColor: Color,
    secondaryColor: Color,
    onSurfaceColor: Color,
    lineColor: Color
) {
    if (children.isEmpty()) return

    val totalCount = children.size
    val totalHeight = (totalCount - 1) * verticalSpacing
    val startY = parentCenter.y - totalHeight / 2f

    children.forEachIndexed { index, child ->
        val childX = if (isRight) parentCenter.x + horizontalSpacing else parentCenter.x - horizontalSpacing
        val childY = startY + index * verticalSpacing
        val childCenter = Offset(childX, childY)

        // Draw curved connecting line between parent and child
        val path = Path().apply {
            moveTo(parentCenter.x, parentCenter.y)
            val ctrl1X = (parentCenter.x + childCenter.x) / 2f
            val ctrl1Y = parentCenter.y
            val ctrl2X = (parentCenter.x + childCenter.x) / 2f
            val ctrl2Y = childCenter.y
            cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, childCenter.x, childCenter.y)
        }
        drawPath(
            path = path,
            color = secondaryColor.copy(alpha = 0.8f),
            style = Stroke(width = 2f * scale, cap = StrokeCap.Round)
        )

        // Measure and Draw Child Node Chip
        val nodeWidth = 130f * scale
        val nodeHeight = 44f * scale
        val nodeRect = Offset(childCenter.x - nodeWidth / 2f, childCenter.y - nodeHeight / 2f)

        drawRoundRect(
            color = surfaceColor,
            topLeft = nodeRect,
            size = Size(nodeWidth, nodeHeight),
            cornerRadius = CornerRadius(10f * scale, 10f * scale)
        )
        drawRoundRect(
            color = secondaryColor,
            topLeft = nodeRect,
            size = Size(nodeWidth, nodeHeight),
            cornerRadius = CornerRadius(10f * scale, 10f * scale),
            style = Stroke(width = 1.5f * scale)
        )

        val childTextLayout = textMeasurer.measure(
            text = child.title,
            style = TextStyle(
                color = onSurfaceColor,
                fontSize = (11f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
        val childTextOffset = Offset(
            childCenter.x - childTextLayout.size.width / 2f,
            childCenter.y - childTextLayout.size.height / 2f
        )
        drawText(childTextLayout, topLeft = childTextOffset)

        // Draw Level 2 Leaves if any
        if (child.children.isNotEmpty()) {
            val leafHorizontalSpacing = 140f * scale
            val leafVerticalSpacing = 42f * scale
            val leafTotalHeight = (child.children.size - 1) * leafVerticalSpacing
            val leafStartY = childCenter.y - leafTotalHeight / 2f

            child.children.forEachIndexed { leafIndex, leaf ->
                val leafX = if (isRight) childCenter.x + leafHorizontalSpacing else childCenter.x - leafHorizontalSpacing
                val leafY = leafStartY + leafIndex * leafVerticalSpacing
                val leafCenter = Offset(leafX, leafY)

                val leafPath = Path().apply {
                    moveTo(childCenter.x, childCenter.y)
                    val c1X = (childCenter.x + leafCenter.x) / 2f
                    cubicTo(c1X, childCenter.y, c1X, leafCenter.y, leafCenter.x, leafCenter.y)
                }
                drawPath(
                    path = leafPath,
                    color = lineColor,
                    style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
                )

                val leafWidth = 110f * scale
                val leafHeight = 36f * scale
                val leafRect = Offset(leafCenter.x - leafWidth / 2f, leafCenter.y - leafHeight / 2f)

                drawRoundRect(
                    color = surfaceColor,
                    topLeft = leafRect,
                    size = Size(leafWidth, leafHeight),
                    cornerRadius = CornerRadius(8f * scale, 8f * scale)
                )
                drawRoundRect(
                    color = lineColor,
                    topLeft = leafRect,
                    size = Size(leafWidth, leafHeight),
                    cornerRadius = CornerRadius(8f * scale, 8f * scale),
                    style = Stroke(width = 1f * scale)
                )

                val leafText = textMeasurer.measure(
                    text = leaf.title,
                    style = TextStyle(
                        color = onSurfaceColor,
                        fontSize = (9.5f * scale).sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                val leafTextOffset = Offset(
                    leafCenter.x - leafText.size.width / 2f,
                    leafCenter.y - leafText.size.height / 2f
                )
                drawText(leafText, topLeft = leafTextOffset)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 0: QUIZ TAB (FULL INTEGRATION OF QUIZ SYSTEM)
// ----------------------------------------------------------------------------
@Composable
fun QuizTabContent(viewModel: MainViewModel) {
    // Delegated to QuizScreen implementation
    QuizScreen(viewModel = viewModel)
}
