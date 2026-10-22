package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.PixelAnimatedPet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSyllabusData
import com.example.data.model.AmbientSound
import com.example.data.model.FocusTimerMode
import com.example.data.model.TreeSpecies
import com.example.data.model.TreeStatus
import com.example.ui.MainViewModel
import com.example.ui.TimerStatus
import com.example.ui.components.TreeCanvas
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DeadTreeDark
import com.example.ui.theme.DeadTreeGrey
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun TimerScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timerStatus by viewModel.timerStatus.collectAsStateWithLifecycle()
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val customMinutes by viewModel.customMinutes.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val elapsedStopwatchSeconds by viewModel.elapsedStopwatchSeconds.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val lastSessionResult by viewModel.lastSessionResult.collectAsStateWithLifecycle()
    val economy by viewModel.economy.collectAsStateWithLifecycle()
    val customAudioUri by viewModel.customAudioUri.collectAsStateWithLifecycle()
    val customAudioName by viewModel.customAudioName.collectAsStateWithLifecycle()
    val isCustomAudioPlaying by viewModel.isCustomAudioPlaying.collectAsStateWithLifecycle()
    val activeQuoteIndex by viewModel.activeQuoteIndex.collectAsStateWithLifecycle()
    val activeGratitudeIndex by viewModel.activeGratitudeIndex.collectAsStateWithLifecycle()

    val currentQuote = viewModel.motivationalQuotes.getOrElse(activeQuoteIndex) { "Stay focused!" }
    val currentGratitude = viewModel.gratitudePrompts.getOrElse(activeGratitudeIndex) { "What are you thankful for today?" }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected Audio Track"
            viewModel.setCustomAudio(uri, fileName)
        }
    }

    val currentSpecies = TreeSpecies.fromId(economy?.selectedSpecies ?: "OAK")
    val ambientSound = AmbientSound.fromId(economy?.activeAmbientSound ?: "NONE")
    val demoModeEnabled = economy?.demoModeEnabled == true

    var showGiveUpConfirmation by remember { mutableStateOf(false) }
    var showChapterSelectModal by remember { mutableStateOf(false) }

    val isRunning = timerStatus == TimerStatus.RUNNING
    val isFailed = timerStatus == TimerStatus.FAILED
    val isCompleted = timerStatus == TimerStatus.COMPLETED

    // Intercept back presses while timer is running so tree isn't unintentionally abandoned
    BackHandler(enabled = isRunning) {
        showGiveUpConfirmation = true
    }

    // Calculate growth progress based on countdown or stopwatch
    val growthProgress = when {
        isCompleted -> 1f
        isFailed -> 0f
        timerMode == FocusTimerMode.COUNTDOWN -> {
            if (isRunning && totalSeconds > 0) {
                ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0.15f, 1f)
            } else {
                0.15f // Seed
            }
        }
        else -> {
            // Stopwatch mode
            if (isRunning) {
                val demo = demoModeEnabled
                val seconds = elapsedStopwatchSeconds
                when {
                    demo && seconds >= 10 -> 1f
                    demo && seconds >= 7 -> 0.7f
                    demo && seconds >= 4 -> 0.4f
                    demo -> 0.15f
                    seconds < 60 -> 0.15f // < 1 min: tiny seed
                    seconds < 300 -> 0.35f // 1-5 mins: sprout
                    seconds < 900 -> 0.65f // 5-15 mins: small plant
                    else -> 1f // 15+ mins: mature tree
                }
            } else {
                0.15f
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFailed -> Color(0xFF262626)
            isRunning -> MaterialTheme.colorScheme.background
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(600),
        label = "timerBackground"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Section: Title Bar with Settings Gear & Demo Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Sanctuary",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.example.ui.theme.PixelifySansFontFamily,
                            color = if (isFailed) Color.White else MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = if (isRunning) "Stay in the app or your tree will wither!" else "Choose your subject & duration below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFailed) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Demo Mode Switch
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Demo Mode",
                                modifier = Modifier.size(14.dp),
                                tint = if (demoModeEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "10s",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (demoModeEnabled) FontWeight.Bold else FontWeight.Normal
                            )
                            Switch(
                                checked = demoModeEnabled,
                                onCheckedChange = { viewModel.setDemoMode(it) },
                                modifier = Modifier
                                    .scale(0.65f)
                                    .testTag("demo_mode_switch"),
                                enabled = !isRunning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Settings Gear Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("timer_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = if (isFailed) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Mode Selector: Countdown Timer vs Stopwatch (when IDLE)
            if (!isRunning) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isCountdown = timerMode == FocusTimerMode.COUNTDOWN
                        ElevatedButton(
                            onClick = { viewModel.setTimerMode(FocusTimerMode.COUNTDOWN) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("mode_countdown_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (isCountdown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isCountdown) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Countdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        ElevatedButton(
                            onClick = { viewModel.setTimerMode(FocusTimerMode.STOPWATCH) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("mode_stopwatch_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (!isCountdown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (!isCountdown) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stopwatch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Subject & Chapter Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isRunning) {
                        // Subject horizontal chips
                        Text(
                            text = "Select ICSE Subject:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(InitialSyllabusData.subjectsWithChapters.keys.toList()) { subject ->
                                FilterChip(
                                    selected = selectedSubject == subject,
                                    onClick = { viewModel.setSelectedSubject(subject) },
                                    label = { Text(subject, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Chapter Selection Button / Dropdown Trigger
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { showChapterSelectModal = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Chapter: $selectedChapter",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Tap to switch chapter in $selectedSubject",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Chapter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // While Running: Prominent Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$selectedSubject • $selectedChapter",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = if (timerMode == FocusTimerMode.COUNTDOWN) "Strict Focus Countdown in progress" else "Open-Ended Study Stopwatch in progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Custom Duration Selector (Only in Countdown mode & when not running)
            if (!isRunning && timerMode == FocusTimerMode.COUNTDOWN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session Length:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$customMinutes Minutes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset Buttons: 15m, 30m, 45m, 60m
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                val isSelected = customMinutes == mins
                                Button(
                                    onClick = { viewModel.setCustomMinutes(mins) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .testTag("preset_${mins}m_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Slider (5 to 120 minutes)
                        Slider(
                            value = customMinutes.toFloat(),
                            onValueChange = { viewModel.setCustomMinutes(it.toInt()) },
                            valueRange = 5f..120f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("timer_duration_slider")
                        )
                    }
                }
            }

            // Center Circular Timer & Pixel Tree Visualization
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .testTag("circular_focus_timer"),
                contentAlignment = Alignment.Center
            ) {
                // Background & Animated Circular Progress Ring
                val strokeColor = when {
                    isFailed -> DeadTreeGrey
                    isRunning -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                }
                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress Ring
                    val sweep = if (timerMode == FocusTimerMode.COUNTDOWN) {
                        360f * (if (isRunning) growthProgress else if (isCompleted) 1f else 0f)
                    } else {
                        // In stopwatch, loop or fill based on 30m milestone
                        val progress30m = (elapsedStopwatchSeconds.toFloat() / 1800f).coerceIn(0f, 1f)
                        360f * (if (isRunning) progress30m else if (isCompleted) 1f else 0f)
                    }

                    drawArc(
                        color = strokeColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content: Pixel Tree Canvas & Digital Clock
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(125.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TreeCanvas(
                            species = currentSpecies,
                            isDead = isFailed,
                            growthProgress = growthProgress,
                            modifier = Modifier.size(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Digital Time Display
                    val timeString = if (timerMode == FocusTimerMode.COUNTDOWN) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    } else {
                        val mins = elapsedStopwatchSeconds / 60
                        val secs = elapsedStopwatchSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    }

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = com.example.ui.theme.PixelifySansFontFamily,
                            color = if (isFailed) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("digital_timer_display")
                    )

                    val stageLabel = when {
                        isFailed -> "Tree Withered"
                        isCompleted -> "Session Complete!"
                        !isRunning -> "Seed Ready to Plant"
                        growthProgress < 0.25f -> "Stage 1: Planted Seed"
                        growthProgress < 0.55f -> "Stage 2: Baby Sprout"
                        growthProgress < 0.85f -> "Stage 3: Growing Plant"
                        else -> "Stage 4: ${currentSpecies.displayName}"
                    }

                    Text(
                        text = stageLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isFailed) Color.LightGray else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Ambient Sound Indicator
            if (ambientSound != AmbientSound.NONE) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ambient Audio: ${ambientSound.displayName}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Custom Background Audio Player (Music/Lo-fi from phone storage)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = customAudioName ?: "Study Audio (Loops in Background)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (customAudioUri != null) {
                                    if (isCustomAudioPlaying) "Playing & looping in background 🎵" else "Audio loaded • loops during focus"
                                } else "Pick audio file from phone storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (customAudioUri != null) {
                            IconButton(
                                onClick = { viewModel.toggleCustomAudio() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCustomAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Audio",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setCustomAudio(null, null) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Audio",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Pick Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mindful Study Fuel Card (Motivational Quote & Gratitude Prompt during focus)
            if (isRunning) {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ Motivational Fuel",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { viewModel.cycleQuote() },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Next Quote ↻", fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = currentQuote,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🙏 Gratitude Reflection",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TextButton(
                                onClick = { viewModel.cycleGratitude() },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("New Prompt ↻", fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = currentGratitude,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isRunning) {
                    // Start Button
                    val startLabel = if (timerMode == FocusTimerMode.COUNTDOWN) {
                        "🌱 Plant Seed & Start (${customMinutes}m)"
                    } else {
                        "🌱 Plant Seed & Start Stopwatch"
                    }

                    ElevatedButton(
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("plant_seed_start_button")
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    // Running Controls
                    if (timerMode == FocusTimerMode.STOPWATCH) {
                        // Finish & Harvest Tree button for Stopwatch
                        ElevatedButton(
                            onClick = { viewModel.finishStopwatchSession() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("finish_stopwatch_button")
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✅ Finish & Harvest Tree",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Give Up Button (Ends session immediately and tree dies)
                    OutlinedButton(
                        onClick = { viewModel.giveUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("give_up_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Dangerous, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Give Up (Kills Tree)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = "⚠️ Tree dies ONLY if you press Give Up or leave the timer screen.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isFailed) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Animated Pet at bottom with motivational speech bubble
            PixelAnimatedPet(
                speechQuote = if (isRunning) "Focus on $selectedChapter! $currentQuote".take(42) + "..." else "Ready to plant a tree! Pick your subject! 🌿",
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // Chapter Selection Modal Dialog
        if (showChapterSelectModal) {
            val chapters = InitialSyllabusData.subjectsWithChapters[selectedSubject] ?: emptyList()

            AlertDialog(
                onDismissRequest = { showChapterSelectModal = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Select Chapter ($selectedSubject)",
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.example.ui.theme.PixelifySansFontFamily
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(chapters) { chapter ->
                            val isSelected = selectedChapter == chapter
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSelectedChapter(chapter)
                                        showChapterSelectModal = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = chapter,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChapterSelectModal = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Give Up Confirmation Dialog
        if (showGiveUpConfirmation) {
            AlertDialog(
                onDismissRequest = { showGiveUpConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Abandon Focus Session?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "If you give up now, your virtual ${currentSpecies.displayName} will immediately wither and die. A dead tree tagged with '$selectedSubject • $selectedChapter' will be logged in your graveyard permanently."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGiveUpConfirmation = false
                            viewModel.giveUp()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_give_up_button")
                    ) {
                        Text("Give Up & Kill Tree")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGiveUpConfirmation = false }) {
                        Text("Stay Focused")
                    }
                }
            )
        }

        // Completion / Failure Result Modal
        lastSessionResult?.let { result ->
            val isSuccess = result.status == TreeStatus.ALIVE

            AlertDialog(
                onDismissRequest = { viewModel.resetTimerToIdle() },
                icon = {
                    Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                        TreeCanvas(
                            species = result.species,
                            isDead = !isSuccess,
                            modifier = Modifier.size(86.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (isSuccess) "Focus Victorious!" else "Tree Withered",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isSuccess) ForestGreenPrimary else MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSuccess) {
                            Text(
                                text = "Congratulations! You remained focused for ${result.durationSeconds / 60}m ${result.durationSeconds % 60}s. Your ${result.species.displayName} is now flourishing in your grove.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tagged: ${result.subjectTag} • ${result.chapterTag}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = CoinGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+10 Study Coins Earned!",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Distraction won this round! ${result.reason}. A dead tree tagged with '${result.subjectTag} • ${result.chapterTag}' has been logged in your grove graveyard.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Focus Elapsed: ${result.durationSeconds / 60}m ${result.durationSeconds % 60}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetTimerToIdle() },
                        modifier = Modifier.testTag("result_dialog_close_button")
                    ) {
                        Text(if (isSuccess) "Continue to Forest" else "Try Again")
                    }
                }
            )
        }
    }
}
