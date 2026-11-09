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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.launch
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
    val savedDecks by viewModel.savedFlashcardDecks.collectAsStateWithLifecycle()

    var chapterInput by remember { mutableStateOf("Physics - Force") }
    var jsonInput by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveFolderName by remember(deck) { mutableStateOf(deck?.chapter ?: "Physics - Force") }

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

        // Deck Title, Progress & Save Deck Button
        if (deck != null && cards.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck?.chapter ?: "Flashcards",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Card ${currentIndex + 1} of ${cards.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            saveFolderName = deck?.chapter ?: "Deck ${System.currentTimeMillis() % 1000}"
                            showSaveDialog = true
                        },
                        modifier = Modifier.testTag("btn_save_flashcard_deck"),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Deck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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

        // Saved Folders Section
        if (savedDecks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_decks_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saved Folders (${savedDecks.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    savedDecks.forEach { savedDeck ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = savedDeck.folderName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${savedDeck.cardCount} Cards • Chapter: ${savedDeck.chapter}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            viewModel.loadSavedFlashcardDeck(savedDeck)
                                            Toast.makeText(context, "Opened ${savedDeck.folderName}!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("open_saved_deck_${savedDeck.id}")
                                    ) {
                                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteSavedFlashcardDeck(savedDeck.id)
                                            Toast.makeText(context, "Deleted deck", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_saved_deck_${savedDeck.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Deck Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Flashcard Deck", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Enter a folder name for this deck so you can open it anytime from your library:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = saveFolderName,
                            onValueChange = { saveFolderName = it },
                            label = { Text("Folder Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_deck_folder_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveCurrentFlashcardDeck(saveFolderName)
                            showSaveDialog = false
                            Toast.makeText(context, "Deck saved to folder '$saveFolderName'!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("confirm_save_deck_button")
                    ) {
                        Text("Save to Library")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 2: INTERACTIVE MIND MAP TAB (COMPOSE BOX/ROW/COLUMN TREE & DRAGGABLE NODES)
// ----------------------------------------------------------------------------
data class FlatMindMapNode(
    val id: String,
    val title: String,
    val parentId: String?,
    val level: Int
)

data class MindMapLine(
    val parentId: String,
    val childId: String,
    val level: Int
)

@Composable
fun MindMapTabContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val mindMapData by viewModel.mindMapData.collectAsStateWithLifecycle()

    var chapterInput by remember { mutableStateOf("Physics - Force") }
    var jsonInput by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }

    fun copyPrompt() {
        val chapter = chapterInput.ifBlank { "Physics - Force" }
        val prompt = "Generate a deep 3-layer hierarchical mind map for the chapter: $chapter. Output strictly valid JSON, no markdown. Include detailed event blocks. Schema: {\"chapter\": \"String\", \"root\": {\"title\": \"Main Topic\", \"children\": [{\"title\": \"Sub Topic 1\", \"children\": [{\"title\": \"Detail A\", \"children\": [{\"title\": \"Deep Detail\"}]}]}]}}."
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mind Map AI Prompt", prompt)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "3-Layer Mind Map AI Prompt copied!", Toast.LENGTH_SHORT).show()
    }

    val rootNode = mindMapData?.root
    val (flatNodes, connections) = remember(rootNode) {
        val nodes = mutableListOf<FlatMindMapNode>()
        val conns = mutableListOf<MindMapLine>()
        if (rootNode != null) {
            fun recurse(node: MindMapNode, id: String, parentId: String?, level: Int) {
                nodes.add(FlatMindMapNode(id, node.title, parentId, level))
                if (parentId != null) {
                    conns.add(MindMapLine(parentId, id, level))
                }
                node.children.forEachIndexed { idx, child ->
                    recurse(child, "${id}_$idx", id, level + 1)
                }
            }
            recurse(rootNode, "root", null, 0)
        }
        Pair(nodes, conns)
    }

    val nodePositions = remember { mutableStateMapOf<String, Offset>() }

    fun resetPositions() {
        nodePositions.clear()
        if (rootNode == null) return

        val rootXPx = with(density) { 1500.dp.toPx() }
        val rootYPx = with(density) { 1500.dp.toPx() }
        nodePositions["root"] = Offset(rootXPx, rootYPx)

        val childrenByParent = flatNodes.filter { it.parentId != null }.groupBy { it.parentId!! }

        fun positionChildren(parentId: String, parentOffset: Offset, level: Int) {
            val children = childrenByParent[parentId] ?: return
            val count = children.size
            val xOffsetStep = when (level) {
                1 -> with(density) { 320.dp.toPx() }
                2 -> with(density) { 280.dp.toPx() }
                else -> with(density) { 260.dp.toPx() }
            }
            val ySpacing = when (level) {
                1 -> with(density) { 240.dp.toPx() }
                2 -> with(density) { 100.dp.toPx() }
                else -> with(density) { 65.dp.toPx() }
            }

            children.forEachIndexed { index, child ->
                val childX = parentOffset.x + xOffsetStep
                val childY = parentOffset.y + (index - (count - 1) / 2.0f) * ySpacing
                val pos = Offset(childX, childY)
                nodePositions[child.id] = pos
                positionChildren(child.id, pos, level + 1)
            }
        }

        positionChildren("root", Offset(rootXPx, rootYPx), 1)
    }

    LaunchedEffect(rootNode) {
        resetPositions()
    }

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    LaunchedEffect(nodePositions.size) {
        if (nodePositions.isNotEmpty()) {
            val centerXPx = with(density) { 1500.dp.toPx() }
            val centerYPx = with(density) { 1500.dp.toPx() }
            hScroll.scrollTo((centerXPx - with(density) { 180.dp.toPx() }).roundToInt().coerceAtLeast(0))
            vScroll.scrollTo((centerYPx - with(density) { 250.dp.toPx() }).roundToInt().coerceAtLeast(0))
        }
    }

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
                    // Paste JSON toggle button
                    OutlinedButton(
                        onClick = { showImportSection = !showImportSection },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("mindmap_toggle_import")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showImportSection) "Hide JSON" else "Paste JSON", fontSize = 12.sp)
                    }

                    // Canvas Toolbar Controls (Center View, Reset Node Positions)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Reset Nodes Positions
                        IconButton(
                            onClick = {
                                resetPositions()
                                Toast.makeText(context, "Node positions reset", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Node Positions")
                        }

                        // Center View
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val centerXPx = with(density) { 1500.dp.toPx() }
                                    val centerYPx = with(density) { 1500.dp.toPx() }
                                    hScroll.animateScrollTo((centerXPx - with(density) { 180.dp.toPx() }).roundToInt().coerceAtLeast(0))
                                    vScroll.animateScrollTo((centerYPx - with(density) { 250.dp.toPx() }).roundToInt().coerceAtLeast(0))
                                }
                            }
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center View")
                        }
                    }
                }

                // Collapsible JSON Input Field
                AnimatedVisibility(visible = showImportSection) {
                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { jsonInput = it },
                            placeholder = { Text("Paste 3-layer JSON here: {\"chapter\": \"...\", \"root\": {\"title\": \"...\", \"children\": [...]}}") },
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
                                onClick = {
                                    viewModel.loadSampleMindMap()
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Load Sample 3-Layer Map")
                            }
                            Button(
                                onClick = {
                                    if (jsonInput.isBlank()) {
                                        Toast.makeText(context, "Please paste JSON first", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val success = viewModel.loadMindMapFromJson(jsonInput)
                                        if (success) {
                                            Toast.makeText(context, "3-Layer Mind Map Loaded!", Toast.LENGTH_SHORT).show()
                                            showImportSection = false
                                            jsonInput = ""
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

        // --------------------------------------------------------------------
        // Infinite Canvas: 3000dp x 3000dp Scrollable Box with Compose Box Nodes
        // --------------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .horizontalScroll(hScroll)
                .verticalScroll(vScroll)
                .testTag("mind_map_canvas_container")
        ) {
            Box(
                modifier = Modifier
                    .size(3000.dp)
                    .testTag("mind_map_inner_board")
            ) {
                // Background Canvas drawing smooth curved Bezier lines connecting parent to child
                Canvas(modifier = Modifier.fillMaxSize()) {
                    connections.forEach { conn ->
                        val p1 = nodePositions[conn.parentId] ?: return@forEach
                        val p2 = nodePositions[conn.childId] ?: return@forEach

                        val start = p1 + Offset(170.dp.toPx(), 26.dp.toPx())
                        val end = p2 + Offset(0f, 26.dp.toPx())

                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            val midX = (start.x + end.x) / 2f
                            cubicTo(midX, start.y, midX, end.y, end.x, end.y)
                        }

                        val lineColor = when (conn.level) {
                            1 -> Color(0xFF1B5E20)
                            2 -> Color(0xFF2E7D32)
                            else -> Color(0xFF4CAF50)
                        }
                        val strokeWidth = when (conn.level) {
                            1 -> 3.dp.toPx()
                            2 -> 2.dp.toPx()
                            else -> 1.5.dp.toPx()
                        }

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Render each node as a standard Compose Box with padding, background, border, and drag gesture
                flatNodes.forEach { node ->
                    val pos = nodePositions[node.id] ?: Offset(1500f, 1500f)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                            .pointerInput(node.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val current = nodePositions[node.id] ?: Offset(1500f, 1500f)
                                    nodePositions[node.id] = current + dragAmount
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(
                                width = if (node.level == 0) 2.5.dp else 2.dp,
                                color = when (node.level) {
                                    0 -> Color(0xFF1B5E20)
                                    1 -> Color(0xFF2E7D32)
                                    2 -> Color(0xFF388E3C)
                                    else -> Color(0xFF4CAF50)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                            .widthIn(min = 110.dp, max = 220.dp)
                            .testTag("mindmap_node_${node.id}")
                    ) {
                        Column {
                            if (node.level == 0) {
                                Text(
                                    text = "ROOT TOPIC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = node.title,
                                color = Color(0xFF1F2937),
                                fontSize = when (node.level) {
                                    0 -> 15.sp
                                    1 -> 13.sp
                                    else -> 12.sp
                                },
                                fontWeight = if (node.level <= 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Floating Interactive Hint Banner
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ Drag any node to move • Scroll horizontally and vertically across the canvas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
