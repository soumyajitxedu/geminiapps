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
import androidx.compose.foundation.gestures.detectTapGestures
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
// TAB 2: INTERACTIVE MIND MAP TAB (MULTI-LAYOUT, NON-OVERLAPPING, EXPANDABLE)
// ----------------------------------------------------------------------------
enum class MindMapLayoutStyle(val displayName: String) {
    HIERARCHICAL("Hierarchical (Linear)"),
    RADIAL("Radial (Spider Web)"),
    SCATTER("Scatter / Neighbors")
}

data class RenderedMindMapNode(
    val id: String,
    val title: String,
    val center: Offset,
    val width: Float,
    val height: Float,
    val level: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val parentCenter: Offset? = null
)

@Composable
fun MindMapTabContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val mindMapData by viewModel.mindMapData.collectAsStateWithLifecycle()

    var chapterInput by remember { mutableStateOf("Physics - Force") }
    var jsonInput by remember { mutableStateOf("") }
    var showImportSection by remember { mutableStateOf(false) }

    var selectedLayout by remember { mutableStateOf(MindMapLayoutStyle.HIERARCHICAL) }
    var showLayoutMenu by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Set of node IDs that are expanded; default expands root and first level
    var expandedNodeIds by remember {
        mutableStateOf(setOf("root", "node_0", "node_1", "node_2", "node_3", "node_4", "node_5"))
    }

    val textMeasurer = rememberTextMeasurer()
    var renderedNodesList by remember { mutableStateOf<List<RenderedMindMapNode>>(emptyList()) }

    fun copyPrompt() {
        val chapter = chapterInput.ifBlank { "Physics - Force" }
        val prompt = "Generate a hierarchical mind map for chapter: $chapter. Output strictly valid JSON, no markdown. Schema: {\"chapter\": \"String\", \"root\": {\"title\": \"Topic\", \"children\": [{\"title\": \"Sub\", \"children\": []}]}}."
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mind Map Prompt", prompt)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Mind Map AI Prompt copied!", Toast.LENGTH_SHORT).show()
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
                    // Layout style dropdown button
                    Box {
                        OutlinedButton(
                            onClick = { showLayoutMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("mindmap_layout_selector")
                        ) {
                            Icon(
                                imageVector = when (selectedLayout) {
                                    MindMapLayoutStyle.HIERARCHICAL -> Icons.Default.AccountTree
                                    MindMapLayoutStyle.RADIAL -> Icons.Default.FilterVintage
                                    MindMapLayoutStyle.SCATTER -> Icons.Default.Hub
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(selectedLayout.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showLayoutMenu,
                            onDismissRequest = { showLayoutMenu = false }
                        ) {
                            MindMapLayoutStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when (style) {
                                                    MindMapLayoutStyle.HIERARCHICAL -> Icons.Default.AccountTree
                                                    MindMapLayoutStyle.RADIAL -> Icons.Default.FilterVintage
                                                    MindMapLayoutStyle.SCATTER -> Icons.Default.Hub
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(style.displayName, fontWeight = if (style == selectedLayout) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
                                    onClick = {
                                        selectedLayout = style
                                        showLayoutMenu = false
                                        scale = 1.0f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showImportSection = !showImportSection }) {
                            Icon(
                                imageVector = if (showImportSection) Icons.Default.Done else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showImportSection) "Hide" else "Paste JSON")
                        }

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
                                            expandedNodeIds = setOf("root", "node_0", "node_1", "node_2", "node_3", "node_4")
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

        // Mind Map Interactive Canvas
        val rootNode = mindMapData?.root
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val surfaceColor = MaterialTheme.colorScheme.surface
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val outlineColor = MaterialTheme.colorScheme.outlineVariant

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .pointerInput(renderedNodesList, expandedNodeIds) {
                    detectTapGestures { tapOffset ->
                        val tappedNode = renderedNodesList.find { node ->
                            tapOffset.x in (node.center.x - node.width / 2f)..(node.center.x + node.width / 2f) &&
                            tapOffset.y in (node.center.y - node.height / 2f)..(node.center.y + node.height / 2f)
                        }
                        if (tappedNode != null && tappedNode.hasChildren) {
                            expandedNodeIds = if (expandedNodeIds.contains(tappedNode.id)) {
                                expandedNodeIds - tappedNode.id
                            } else {
                                expandedNodeIds + tappedNode.id
                            }
                        }
                    }
                }
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
                    val nodesToRender = calculateMindMapLayout(
                        root = rootNode,
                        center = canvasCenter,
                        scale = scale,
                        layoutStyle = selectedLayout,
                        expandedNodeIds = expandedNodeIds
                    )
                    renderedNodesList = nodesToRender

                    // 1. Draw Curved Bezier Connector Lines
                    nodesToRender.forEach { node ->
                        node.parentCenter?.let { pCenter ->
                            val path = Path().apply {
                                moveTo(pCenter.x, pCenter.y)
                                when (selectedLayout) {
                                    MindMapLayoutStyle.HIERARCHICAL -> {
                                        val ctrl1X = (pCenter.x + node.center.x) / 2f
                                        cubicTo(ctrl1X, pCenter.y, ctrl1X, node.center.y, node.center.x, node.center.y)
                                    }
                                    else -> {
                                        val midX = (pCenter.x + node.center.x) / 2f
                                        val midY = (pCenter.y + node.center.y) / 2f
                                        val dx = node.center.x - pCenter.x
                                        val dy = node.center.y - pCenter.y
                                        val dist = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
                                        val bend = 16f * scale
                                        val normX = -dy / dist * bend
                                        val normY = dx / dist * bend
                                        quadraticTo(midX + normX, midY + normY, node.center.x, node.center.y)
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = if (node.level == 1) secondaryColor.copy(alpha = 0.85f) else outlineColor,
                                style = Stroke(width = if (node.level == 1) 2.2f * scale else 1.5f * scale, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 2. Draw Nodes with non-overlapping bounds & expansion badges
                    nodesToRender.forEach { node ->
                        val nodeRect = Offset(node.center.x - node.width / 2f, node.center.y - node.height / 2f)
                        val cornerRadius = CornerRadius(12f * scale, 12f * scale)

                        val bgColor = when (node.level) {
                            0 -> primaryColor
                            1 -> surfaceColor
                            else -> surfaceColor
                        }
                        val strokeColor = when (node.level) {
                            0 -> Color.White.copy(alpha = 0.4f)
                            1 -> secondaryColor
                            else -> outlineColor
                        }

                        // Node background rounded rect
                        drawRoundRect(
                            color = bgColor,
                            topLeft = nodeRect,
                            size = Size(node.width, node.height),
                            cornerRadius = cornerRadius
                        )
                        drawRoundRect(
                            color = strokeColor,
                            topLeft = nodeRect,
                            size = Size(node.width, node.height),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = if (node.level == 0) 2.5f * scale else 1.5f * scale)
                        )

                        // Title text
                        val textStyle = TextStyle(
                            color = if (node.level == 0) Color.White else onSurfaceColor,
                            fontSize = when (node.level) {
                                0 -> (12.5f * scale).sp
                                1 -> (10.5f * scale).sp
                                else -> (9.5f * scale).sp
                            },
                            fontWeight = if (node.level == 0) FontWeight.Bold else FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        val textLayout = textMeasurer.measure(
                            text = node.title,
                            style = textStyle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val textOffset = Offset(
                            node.center.x - textLayout.size.width / 2f,
                            node.center.y - textLayout.size.height / 2f
                        )
                        drawText(textLayout, topLeft = textOffset)

                        // If node has children, draw interactive Accordion/Expand indicator badge
                        if (node.hasChildren) {
                            val badgeRadius = 7f * scale
                            val badgeCenter = Offset(node.center.x + node.width / 2f - 4f * scale, node.center.y - node.height / 2f + 4f * scale)
                            drawCircle(
                                color = if (node.isExpanded) ForestGreenPrimary else Color(0xFFF57C00),
                                radius = badgeRadius,
                                center = badgeCenter
                            )
                            val symbol = if (node.isExpanded) "−" else "+"
                            val badgeText = textMeasurer.measure(
                                text = symbol,
                                style = TextStyle(color = Color.White, fontSize = (9f * scale).sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            )
                            drawText(badgeText, topLeft = Offset(badgeCenter.x - badgeText.size.width / 2f, badgeCenter.y - badgeText.size.height / 2f))
                        }
                    }
                }
            }

            // Legend & Interactive Guidance Banner
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Tap nodes to expand/collapse subtopics • Pan & zoom freely",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Calculates complete non-overlapping node layout for Hierarchical, Radial, and Scatter styles.
 */
private fun calculateMindMapLayout(
    root: MindMapNode,
    center: Offset,
    scale: Float,
    layoutStyle: MindMapLayoutStyle,
    expandedNodeIds: Set<String>
): List<RenderedMindMapNode> {
    val results = mutableListOf<RenderedMindMapNode>()

    val rootWidth = 145f * scale
    val rootHeight = 52f * scale
    val isRootExpanded = expandedNodeIds.contains("root")

    results.add(
        RenderedMindMapNode(
            id = "root",
            title = root.title,
            center = center,
            width = rootWidth,
            height = rootHeight,
            level = 0,
            hasChildren = root.children.isNotEmpty(),
            isExpanded = isRootExpanded,
            parentCenter = null
        )
    )

    if (!isRootExpanded || root.children.isEmpty()) return results

    val children = root.children

    when (layoutStyle) {
        MindMapLayoutStyle.HIERARCHICAL -> {
            // Symmetrical horizontal split: half to the right, half to the left
            val half = (children.size + 1) / 2
            val rightChildren = children.take(half)
            val leftChildren = children.drop(half)

            layoutHierarchicalSide(
                parentCenter = center,
                children = rightChildren,
                isRight = true,
                startIndex = 0,
                scale = scale,
                expandedNodeIds = expandedNodeIds,
                outList = results
            )

            layoutHierarchicalSide(
                parentCenter = center,
                children = leftChildren,
                isRight = false,
                startIndex = half,
                scale = scale,
                expandedNodeIds = expandedNodeIds,
                outList = results
            )
        }

        MindMapLayoutStyle.RADIAL -> {
            // Spider web radiating circularly from root
            val n = children.size
            val radius1 = 190f * scale
            val radius2 = 340f * scale

            children.forEachIndexed { i, child ->
                val childId = "node_$i"
                val angle = (2 * Math.PI * i / n) - (Math.PI / 2)
                val childCenter = Offset(
                    (center.x + radius1 * kotlin.math.cos(angle)).toFloat(),
                    (center.y + radius1 * kotlin.math.sin(angle)).toFloat()
                )
                val isExpanded = expandedNodeIds.contains(childId)
                val childWidth = 125f * scale
                val childHeight = 44f * scale

                results.add(
                    RenderedMindMapNode(
                        id = childId,
                        title = child.title,
                        center = childCenter,
                        width = childWidth,
                        height = childHeight,
                        level = 1,
                        hasChildren = child.children.isNotEmpty(),
                        isExpanded = isExpanded,
                        parentCenter = center
                    )
                )

                if (isExpanded && child.children.isNotEmpty()) {
                    val m = child.children.size
                    val arcSpan = (2 * Math.PI / n * 0.85).coerceAtMost(Math.PI / 2)
                    child.children.forEachIndexed { j, leaf ->
                        val leafAngle = if (m > 1) {
                            angle - (arcSpan / 2) + (arcSpan * j / (m - 1))
                        } else {
                            angle
                        }
                        val leafCenter = Offset(
                            (center.x + radius2 * kotlin.math.cos(leafAngle)).toFloat(),
                            (center.y + radius2 * kotlin.math.sin(leafAngle)).toFloat()
                        )
                        results.add(
                            RenderedMindMapNode(
                                id = "${childId}_$j",
                                title = leaf.title,
                                center = leafCenter,
                                width = 110f * scale,
                                height = 38f * scale,
                                level = 2,
                                hasChildren = false,
                                isExpanded = false,
                                parentCenter = childCenter
                            )
                        )
                    }
                }
            }
        }

        MindMapLayoutStyle.SCATTER -> {
            // Organic clustered force look with comfortable separation
            val n = children.size
            children.forEachIndexed { i, child ->
                val childId = "node_$i"
                val angle = (i * 2.39996) // Golden angle for organic dispersion
                val dist = (175f + (i % 2) * 45f) * scale
                val childCenter = Offset(
                    (center.x + dist * kotlin.math.cos(angle)).toFloat(),
                    (center.y + dist * kotlin.math.sin(angle)).toFloat()
                )
                val isExpanded = expandedNodeIds.contains(childId)
                val childWidth = 125f * scale
                val childHeight = 44f * scale

                results.add(
                    RenderedMindMapNode(
                        id = childId,
                        title = child.title,
                        center = childCenter,
                        width = childWidth,
                        height = childHeight,
                        level = 1,
                        hasChildren = child.children.isNotEmpty(),
                        isExpanded = isExpanded,
                        parentCenter = center
                    )
                )

                if (isExpanded && child.children.isNotEmpty()) {
                    val m = child.children.size
                    val fanDist = 130f * scale
                    child.children.forEachIndexed { j, leaf ->
                        val fanAngle = angle + (j - (m - 1) / 2f) * 0.55
                        val leafCenter = Offset(
                            (childCenter.x + fanDist * kotlin.math.cos(fanAngle)).toFloat(),
                            (childCenter.y + fanDist * kotlin.math.sin(fanAngle)).toFloat()
                        )
                        results.add(
                            RenderedMindMapNode(
                                id = "${childId}_$j",
                                title = leaf.title,
                                center = leafCenter,
                                width = 110f * scale,
                                height = 38f * scale,
                                level = 2,
                                hasChildren = false,
                                isExpanded = false,
                                parentCenter = childCenter
                            )
                        )
                    }
                }
            }
        }
    }

    return results
}

/**
 * Calculates vertical positions for a side of the hierarchical tree ensuring no branches or leaves overlap.
 */
private fun layoutHierarchicalSide(
    parentCenter: Offset,
    children: List<MindMapNode>,
    isRight: Boolean,
    startIndex: Int,
    scale: Float,
    expandedNodeIds: Set<String>,
    outList: MutableList<RenderedMindMapNode>
) {
    if (children.isEmpty()) return

    val horizontalDist = 180f * scale
    val leafHorizontalDist = 160f * scale
    val unitLeafHeight = 48f * scale

    // 1. Calculate required subtree height for each child
    val heights = children.mapIndexed { idx, child ->
        val childId = "node_${startIndex + idx}"
        val isExpanded = expandedNodeIds.contains(childId)
        if (isExpanded && child.children.isNotEmpty()) {
            child.children.size * unitLeafHeight
        } else {
            54f * scale
        }
    }

    val totalSideHeight = heights.sum()
    var currentY = parentCenter.y - (totalSideHeight / 2f)

    children.forEachIndexed { idx, child ->
        val childId = "node_${startIndex + idx}"
        val isExpanded = expandedNodeIds.contains(childId)
        val branchHeight = heights[idx]
        val childY = currentY + (branchHeight / 2f)
        val childX = if (isRight) parentCenter.x + horizontalDist else parentCenter.x - horizontalDist
        val childCenter = Offset(childX, childY)
        val childWidth = 130f * scale
        val childHeight = 44f * scale

        outList.add(
            RenderedMindMapNode(
                id = childId,
                title = child.title,
                center = childCenter,
                width = childWidth,
                height = childHeight,
                level = 1,
                hasChildren = child.children.isNotEmpty(),
                isExpanded = isExpanded,
                parentCenter = parentCenter
            )
        )

        // If expanded, position grandchildren within this child's allocated height band
        if (isExpanded && child.children.isNotEmpty()) {
            val leaves = child.children
            val leafStartY = currentY + (unitLeafHeight / 2f)
            leaves.forEachIndexed { lIdx, leaf ->
                val leafY = leafStartY + lIdx * unitLeafHeight
                val leafX = if (isRight) childX + leafHorizontalDist else childX - leafHorizontalDist
                val leafCenter = Offset(leafX, leafY)

                outList.add(
                    RenderedMindMapNode(
                        id = "${childId}_$lIdx",
                        title = leaf.title,
                        center = leafCenter,
                        width = 115f * scale,
                        height = 38f * scale,
                        level = 2,
                        hasChildren = false,
                        isExpanded = false,
                        parentCenter = childCenter
                    )
                )
            }
        }

        currentY += branchHeight
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
