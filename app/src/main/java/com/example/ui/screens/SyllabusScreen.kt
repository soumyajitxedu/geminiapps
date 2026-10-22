package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterProgress
import com.example.ui.MainViewModel
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.PixelifySansFontFamily

@Composable
fun SyllabusScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allChapters by viewModel.chapters.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Chapters Checklist, 1: Exam Target & PYQs
    val expandedSubjects = remember { mutableStateMapOf<String, Boolean>() }
    var noteEditingChapter by remember { mutableStateOf<ChapterProgress?>(null) }

    val chaptersBySubject = remember(allChapters) {
        allChapters.groupBy { it.subject }
    }

    // 3-step completion calculation: each chapter has completionFraction (0, 0.33, 0.66, 1.0)
    val overallProgress = if (allChapters.isNotEmpty()) {
        (allChapters.sumOf { it.completionFraction.toDouble() }.toFloat() / allChapters.size.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val overallPercent = (overallProgress * 100).toInt()

    val totalCheckpoints = allChapters.size * 3
    val completedCheckpoints = allChapters.sumOf {
        (if (it.isLearned) 1 else 0) + (if (it.isRevised) 1 else 0) + (if (it.isPyqsDone) 1 else 0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title and Overall Banner
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ICSE Syllabus",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelifySansFontFamily,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Class 10 Board Curriculum • 3-Step Mastery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$overallPercent% Done",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Overall Progress Bar Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Syllabus Conquered",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$completedCheckpoints / $totalCheckpoints steps",
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { overallProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Each chapter requires 3 steps: 1. Learned 2. Revised 3. PYQs Done",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Chapters Checklist", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Exam & PYQ Targets", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // Chapters grouped by Subject Accordions
                items(chaptersBySubject.keys.toList().size) { idx ->
                    val subjectName = chaptersBySubject.keys.toList()[idx]
                    val subjectChapters = chaptersBySubject[subjectName] ?: emptyList()
                    val isExpanded = expandedSubjects[subjectName] ?: (idx == 0)

                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "expand_arrow"
                    )

                    // Calculate subject progress based on 3-step checkpoints
                    val subjectProgress = if (subjectChapters.isNotEmpty()) {
                        (subjectChapters.sumOf { it.completionFraction.toDouble() }.toFloat() / subjectChapters.size.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    val subjectPercent = (subjectProgress * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("subject_card_$subjectName"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Subject Accordion Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedSubjects[subjectName] = !isExpanded
                                    }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(38.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = subjectName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${subjectChapters.size} chapters • $subjectPercent% completed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand $subjectName",
                                    modifier = Modifier.rotate(rotationAngle),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Subject mini progress bar
                            LinearProgressIndicator(
                                progress = { subjectProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Chapters List (Animated Expandable)
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    subjectChapters.forEachIndexed { index, chapter ->
                                        ChapterRowWithCheckpoints(
                                            index = index + 1,
                                            chapter = chapter,
                                            onToggleLearned = { viewModel.toggleChapterLearned(chapter) },
                                            onToggleRevised = { viewModel.toggleChapterRevised(chapter) },
                                            onTogglePyqs = { viewModel.toggleChapterPyqsDone(chapter) },
                                            onOpenNotes = { noteEditingChapter = chapter }
                                        )
                                        if (index < subjectChapters.size - 1) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Exam Targets & PYQ Tab
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Official Exam Countdowns",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Class 10 Board Examinations are approaching. Finish all 3 checkpoints for maximum scores!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Markdown Notes & Custom URL Dialog
        noteEditingChapter?.let { chapter ->
            ChapterNotesDialog(
                chapter = chapter,
                onDismiss = { noteEditingChapter = null },
                onSave = { notes, url ->
                    viewModel.saveChapterNotes(chapter.id, notes, url)
                    noteEditingChapter = null
                    Toast.makeText(context, "Saved chapter notes!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ChapterRowWithCheckpoints(
    index: Int,
    chapter: ChapterProgress,
    onToggleLearned: () -> Unit,
    onToggleRevised: () -> Unit,
    onTogglePyqs: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val completionPercent = (chapter.completionFraction * 100).toInt()
    val hasNotesOrUrl = chapter.notes.isNotBlank() || chapter.customUrl.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(10.dp)
            .testTag("chapter_item_${chapter.id}")
    ) {
        // Top Row: Chapter Title and Notes Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$index. ${chapter.chapterName}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (chapter.customUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Linked resource attached",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Notes Button
            IconButton(
                onClick = onOpenNotes,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (hasNotesOrUrl) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .testTag("notes_btn_${chapter.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Chapter Notes",
                    tint = if (hasNotesOrUrl) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3-Step Checkpoint Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckpointPill(
                label = "Learned",
                checked = chapter.isLearned,
                onClick = onToggleLearned,
                modifier = Modifier.weight(1f)
            )
            CheckpointPill(
                label = "Revised",
                checked = chapter.isRevised,
                onClick = onToggleRevised,
                modifier = Modifier.weight(1f)
            )
            CheckpointPill(
                label = "PYQs Done",
                checked = chapter.isPyqsDone,
                onClick = onTogglePyqs,
                modifier = Modifier.weight(1.1f)
            )

            // Percent badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (completionPercent) {
                    100 -> Color(0xFF10B981).copy(alpha = 0.2f)
                    66 -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                    33 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = "$completionPercent%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (completionPercent) {
                        100 -> Color(0xFF047857)
                        66 -> Color(0xFF1D4ED8)
                        33 -> Color(0xFFB45309)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CheckpointPill(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (checked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChapterNotesDialog(
    chapter: ChapterProgress,
    onDismiss: () -> Unit,
    onSave: (notes: String, url: String) -> Unit
) {
    val context = LocalContext.current
    var notesText by remember { mutableStateOf(chapter.notes) }
    var urlText by remember { mutableStateOf(chapter.customUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Notes & Study Links",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelifySansFontFamily
                        )
                    )
                    Text(
                        text = chapter.chapterName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Markdown Toolbar Helpers
                Text(
                    text = "Markdown Formatting:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { notesText += "**Bold**" },
                        label = { Text("B", fontWeight = FontWeight.ExtraBold) },
                        leadingIcon = { Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { notesText += "*Italic*" },
                        label = { Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                        leadingIcon = { Icon(Icons.Default.FormatItalic, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { notesText += "\n• Bullet item" },
                        label = { Text("Bullet") },
                        leadingIcon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { notesText += "\n# Heading" },
                        label = { Text("Heading") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                // Notes Text Area
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Chapter Notes (Markdown supported)") },
                    placeholder = { Text("Write key formulas, concepts, or bullet points...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("chapter_notes_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Custom URL / Google Drive link
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Custom Link (Google Drive / Notes URL)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (urlText.isNotBlank()) {
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlText)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Invalid link: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Test link", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_url_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(notesText, urlText) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_notes_btn")
            ) {
                Text("Save Notes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
