package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TreeRecord
import com.example.data.model.TreeSpecies
import com.example.data.model.TreeStatus
import com.example.ui.MainViewModel
import com.example.ui.components.TreeCanvas
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DeadTreeDark
import com.example.ui.theme.DeadTreeGrey
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import com.example.ui.components.PixelArtEmblem

@Composable
fun ForestScreen(
    viewModel: MainViewModel,
    onNavigateToTimer: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val economy by viewModel.economy.collectAsStateWithLifecycle()
    val completedChapters by viewModel.completedChaptersCount.collectAsStateWithLifecycle()
    val totalChapters by viewModel.totalChaptersCount.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val isOnboarded = userProfile?.isOnboarded == true
    var showEditProfileDialog by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ALIVE, DEAD
    var inspectedTree by remember { mutableStateOf<TreeRecord?>(null) }

    val filteredTrees = remember(trees, selectedFilter) {
        when (selectedFilter) {
            "ALIVE" -> trees.filter { it.status == TreeStatus.ALIVE.name }
            "DEAD" -> trees.filter { it.status == TreeStatus.DEAD.name }
            else -> trees
        }
    }

    val syllabusProgress = if (totalChapters > 0) {
        (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val syllabusPercent = (syllabusProgress * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Top Stats & Progress Card
            item(span = { GridItemSpan(3) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title and Coin Balance Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelArtEmblem(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Focus Grove",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = com.example.ui.theme.PixelifySansFontFamily,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "✨ ICSE Pixel Study Sanctuary ✨",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right header: Streak Badge, Coin Badge & Settings Gear Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Streak Counter with 🔥 Fire icon
                            val streak = userProfile?.currentStreak ?: 0
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF97316).copy(alpha = 0.15f),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .testTag("streak_badge")
                                    .border(
                                        1.dp,
                                        Color(0xFFF97316).copy(alpha = 0.6f),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔥", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${streak}d",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFEA580C)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .testTag("coin_balance")
                                    .border(
                                        1.dp,
                                        CoinGold.copy(alpha = 0.5f),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "Study Coins",
                                        tint = CoinGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${economy?.coins ?: 0}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("forest_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ICSE Countdown Cards Banner - dynamic calculation
                    val preBoardDays = com.example.util.ExamCountdownUtil.getPreBoardDaysLeft()
                    val boardExamDays = com.example.util.ExamCountdownUtil.getBoardExamDaysLeft()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Pre-Board",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "$preBoardDays days left",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1.1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "ICSE Board Exam",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "$boardExamDays days left",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Personalized Suggested Study Plan Card
                    val userName = userProfile?.name?.ifBlank { "Scholar" } ?: "Scholar"
                    val userHobbies = userProfile?.hobbies?.ifBlank { "Recreation & Hobbies" } ?: "Recreation & Hobbies"
                    val targetHours = (userProfile?.dailyTargetMinutes ?: 120) / 60

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Welcome, $userName! 🌟",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = com.example.ui.theme.PixelifySansFontFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Personalized ICSE Plan • $targetHours hrs/day target",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onNavigateToTimer() }
                                ) {
                                    Text(
                                        text = "Start Focus ⏱️",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "💡 Today's Suggested Routine:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "• Block 1 (45m): Science & Math High-Weightage Chapters",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "• Recharge Break (15m): Enjoy $userHobbies!",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "• Block 2 (45m): Chapter Revision & 3-Step PYQs",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overall Syllabus Progress Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Syllabus Conquered",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$syllabusPercent%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { syllabusProgress },
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
                                text = "$completedChapters of $totalChapters Chapters Mastered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All Trees (${trees.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "ALIVE",
                            onClick = { selectedFilter = "ALIVE" },
                            label = { Text("Lush (${trees.count { it.status == TreeStatus.ALIVE.name }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "DEAD",
                            onClick = { selectedFilter = "DEAD" },
                            label = { Text("Graveyard (${trees.count { it.status == TreeStatus.DEAD.name }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Empty state if no trees
            if (filteredTrees.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Park,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (trees.isEmpty()) "Your Forest Awaits Its First Tree" else "No trees matching filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Start a 30-minute focus session to plant a seed and earn 10 study coins!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = onNavigateToTimer,
                                modifier = Modifier.testTag("start_first_session_button")
                            ) {
                                Text("Plant A Seed Now →", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Trees Grid Items
                items(filteredTrees, key = { it.id }) { tree ->
                    val isDead = tree.status == TreeStatus.DEAD.name
                    val species = TreeSpecies.fromId(tree.species)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("tree_card_${tree.id}")
                            .clickable { inspectedTree = tree },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDead) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isDead) {
                            androidx.compose.foundation.BorderStroke(1.5.dp, DeadTreeDark)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TreeCanvas(
                                    species = species,
                                    isDead = isDead,
                                    growthProgress = 1f,
                                    modifier = Modifier.size(72.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isDead) "Withered" else species.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDead) DeadTreeDark else MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                val tagText = if (tree.chapterTag.isNotBlank()) "${tree.subjectTag} • ${tree.chapterTag}" else tree.subjectTag
                                Text(
                                    text = tagText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tree Details Dialog
        inspectedTree?.let { tree ->
            val isDead = tree.status == TreeStatus.DEAD.name
            val species = TreeSpecies.fromId(tree.species)
            val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
            val formattedDate = formatter.format(Date(tree.timestamp))

            AlertDialog(
                onDismissRequest = { inspectedTree = null },
                icon = {
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        TreeCanvas(species = species, isDead = isDead, modifier = Modifier.size(76.dp))
                    }
                },
                title = {
                    Text(
                        text = if (isDead) "Withered Tree" else "${species.displayName} in Full Bloom",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isDead) DeadTreeDark else ForestGreenPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isDead) {
                                "This tree withered and died when the focus session was interrupted or abandoned."
                            } else {
                                "Successfully cultivated during a strict, distraction-free study session! +10 Coins earned."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Subject: ${tree.subjectTag}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (tree.chapterTag.isNotBlank()) {
                            Text(
                                text = "• Chapter: ${tree.chapterTag}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "• Duration: ${tree.durationSeconds / 60}m ${tree.durationSeconds % 60}s",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Planted: $formattedDate",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { inspectedTree = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // First-Time User Onboarding Flow Modal
        if (!isOnboarded) {
            var inputName by remember { mutableStateOf("") }
            var inputHobbies by remember { mutableStateOf("") }
            var selectedHours by remember { mutableStateOf(2) }

            AlertDialog(
                onDismissRequest = { /* Force complete or skip */ },
                icon = {
                    PixelArtEmblem(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Welcome to Focus Grove!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = com.example.ui.theme.PixelifySansFontFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Let's personalize your ICSE Study Sanctuary",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("What should we call you?") },
                            placeholder = { Text("e.g. Aarav, Meera") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = inputHobbies,
                            onValueChange = { inputHobbies = it },
                            label = { Text("Your Hobbies / Breaks") },
                            placeholder = { Text("e.g. Chess, Badminton, Gaming") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_hobbies_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Text(
                            text = "Daily Study Target: $selectedHours Hours",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { hours ->
                                val isSelected = selectedHours == hours
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedHours = hours }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${hours}h",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = if (inputName.isBlank()) "Scholar" else inputName.trim()
                            val hobbies = if (inputHobbies.isBlank()) "Gaming & Music" else inputHobbies.trim()
                            viewModel.saveUserProfile(
                                name = name,
                                hobbies = hobbies,
                                dailyTargetMinutes = selectedHours * 60
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_start_button")
                    ) {
                        Text("🌱 Begin My Study Grove")
                    }
                }
            )
        }
    }
}
