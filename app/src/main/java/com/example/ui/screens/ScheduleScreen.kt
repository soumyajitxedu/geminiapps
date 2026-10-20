package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSyllabusData
import com.example.data.model.ScheduleEventEntity
import com.example.ui.MainViewModel
import com.example.ui.components.PixelArtEmblem
import com.example.ui.theme.PixelifySansFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: MainViewModel,
    onNavigateToTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduleEvents by viewModel.scheduleEvents.collectAsStateWithLifecycle()
    val selectedDateStr by viewModel.selectedScheduleDate.collectAsStateWithLifecycle()
    val isGoogleConnected by viewModel.isGoogleCalendarConnected.collectAsStateWithLifecycle()
    val googleEmail by viewModel.googleCalendarEmail.collectAsStateWithLifecycle()
    val lastSyncMsg by viewModel.googleCalendarLastSync.collectAsStateWithLifecycle()

    var showAddEventDialog by remember { mutableStateOf(false) }

    // Week navigation offset (0 = current week, +1 = next week, -1 = prev week)
    var weekOffset by remember { mutableIntStateOf(0) }

    // Calculate dates for current week view (Monday to Sunday)
    val calendar = remember(weekOffset) {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }

    val weekDays = remember(calendar.timeInMillis) {
        val days = mutableListOf<DayInfo>()
        val calClone = calendar.clone() as Calendar
        val dayFormat = SimpleDateFormat("EEE", Locale.US) // Mon, Tue...
        val numFormat = SimpleDateFormat("d", Locale.US) // 4, 5...
        val fullFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in 0 until 7) {
            val d = calClone.time
            days.add(
                DayInfo(
                    dayOfWeek = dayFormat.format(d),
                    dayNumber = numFormat.format(d),
                    fullDate = fullFormat.format(d)
                )
            )
            calClone.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val headerDateFormatted = remember(selectedDateStr) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDateStr)
            SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date ?: Date())
        } catch (e: Exception) {
            SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())
        }
    }

    val isSelectedDateToday = selectedDateStr == todayStr

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = Color(0xFF3B82F6), // Vibrant blue matching reference
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(58.dp)
                    .testTag("add_schedule_event_fab")
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule Task",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Top Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = headerDateFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSelectedDateToday) "Today" else {
                                try {
                                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDateStr)
                                    SimpleDateFormat("EEEE", Locale.US).format(d ?: Date())
                                } catch (e: Exception) {
                                    "Schedule"
                                }
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = PixelifySansFontFamily,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // Week switcher & Google Sync badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { weekOffset -= 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Week",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (weekOffset != 0) {
                            TextButton(
                                onClick = {
                                    weekOffset = 0
                                    viewModel.setSelectedScheduleDate(todayStr)
                                }
                            ) {
                                Text("Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = { weekOffset += 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Week",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Google Calendar Sync Status Pill
                if (isGoogleConnected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable {
                                viewModel.syncAllStudyBlocksToGoogleCalendar()
                                Toast.makeText(context, "Syncing study blocks with Google Calendar...", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("google_calendar_sync_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Calendar Active • $googleEmail",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            }

            // Horizontal Scrolling Week View (Mon - Sun)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEach { day ->
                        val isSelected = selectedDateStr == day.fullDate
                        val isToday = day.fullDate == todayStr

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setSelectedScheduleDate(day.fullDate)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("day_${day.dayOfWeek}_${day.dayNumber}")
                        ) {
                            Text(
                                text = day.dayOfWeek,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day.dayNumber,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Dot indicator for selected or today
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF3B82F6)
                                            isToday -> MaterialTheme.colorScheme.outline
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vertical Timeline View
            if (scheduleEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PixelArtEmblem(modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Events Scheduled",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelifySansFontFamily
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to add study blocks, ICSE revision sessions, or routines!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddEventDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Study Block")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(scheduleEvents) { index, event ->
                        TimelineEventItem(
                            event = event,
                            isFirst = index == 0,
                            isLast = index == scheduleEvents.lastIndex,
                            onToggleComplete = { viewModel.toggleScheduleEventCompleted(event) },
                            onStartFocus = {
                                viewModel.startTimerForScheduleEvent(event)
                                onNavigateToTimer()
                            },
                            onDelete = { viewModel.deleteScheduleEvent(event.id) },
                            onSetActive = {
                                viewModel.setActiveScheduleEvent(event.date, event.id)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Schedule Event Dialog
    if (showAddEventDialog) {
        AddScheduleEventDialog(
            selectedDate = selectedDateStr,
            isGoogleConnected = isGoogleConnected,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, desc, time, duration, subject, chapter, avatar, syncToGoogle ->
                viewModel.addScheduleEvent(
                    title = title,
                    description = desc,
                    date = selectedDateStr,
                    time = time,
                    durationMinutes = duration,
                    subjectTag = subject,
                    chapterTag = chapter,
                    avatarType = avatar,
                    syncToGoogle = syncToGoogle
                )
                showAddEventDialog = false
                Toast.makeText(context, "Added to Schedule${if (syncToGoogle) " & synced to Google Calendar!" else "!"}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

data class DayInfo(
    val dayOfWeek: String,
    val dayNumber: String,
    val fullDate: String
)

@Composable
fun TimelineEventItem(
    event: ScheduleEventEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit,
    onSetActive: () -> Unit
) {
    val timelineColor = Color(0xFF60A5FA) // Sky blue timeline line
    val isActive = event.isActive

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
            .testTag("event_item_${event.id}")
    ) {
        // Vertical Timeline Column (Line + Circle Node)
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Continuous connecting line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineX = size.width / 2
                val topY = if (isFirst) size.height * 0.25f else 0f
                val bottomY = if (isLast) size.height * 0.25f else size.height

                drawLine(
                    color = timelineColor.copy(alpha = 0.55f),
                    start = Offset(lineX, topY),
                    end = Offset(lineX, bottomY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Timeline Node Circle
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(if (isActive) 22.dp else 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    // Double concentric circle for active event (like in image)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.25f))
                            .border(2.5.dp, Color(0xFF3B82F6), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                    )
                } else {
                    // Normal hollow circle with blue border
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 2.5.dp,
                                color = if (event.isCompleted) Color(0xFF10B981) else Color(0xFF60A5FA),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Event Card
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
        ) {
            if (isActive) {
                // Active Event Card (Vibrant Blue Card like in reference image)
                ActiveEventCard(
                    event = event,
                    onToggleComplete = onToggleComplete,
                    onStartFocus = onStartFocus,
                    onDelete = onDelete
                )
            } else {
                // Normal / Soft Pastel Event Card
                PastelEventCard(
                    event = event,
                    onToggleComplete = onToggleComplete,
                    onStartFocus = onStartFocus,
                    onSetActive = onSetActive,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun ActiveEventCard(
    event: ScheduleEventEntity,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B82F6) // Bright vibrant pixel blue
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = event.time,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom Row: Avatars + White Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatars row
                PixelAvatarCluster()

                // White action button with blue icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .clickable { onStartFocus() }
                        .testTag("start_focus_event_${event.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Focus",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Focus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastelEventCard(
    event: ScheduleEventEntity,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSetActive() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (MaterialTheme.colorScheme.surfaceVariant.hashCode() != 0) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                Color(0xFFF3F4F6)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (event.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Text(
                    text = event.time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 32.dp)
                )
            }

            // Tags row (Subject + Chapter tag if applicable)
            if (event.subjectTag.isNotBlank() && event.subjectTag != "Routine" && event.subjectTag != "Fitness") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = if (event.chapterTag.isNotBlank()) "${event.subjectTag} • ${event.chapterTag}" else event.subjectTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onStartFocus,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Focus on this",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders cute pixel avatar faces matching the reference image avatar stack!
 */
@Composable
fun PixelAvatarCluster() {
    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        PixelFaceAvatar(baseColor = Color(0xFFF97316), hairColor = Color(0xFF451A03)) // Boy
        PixelFaceAvatar(baseColor = Color(0xFFFBBF24), hairColor = Color(0xFF78350F)) // Student
        PixelFaceAvatar(baseColor = Color(0xFFEC4899), hairColor = Color(0xFF1E293B)) // Girl
        PixelFaceAvatar(baseColor = Color(0xFF10B981), hairColor = Color(0xFF991B1B)) // Glasses
    }
}

@Composable
fun PixelFaceAvatar(baseColor: Color, hairColor: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val step = size.width / 8f
            // Skin
            drawRect(
                color = Color(0xFFFFDDBB),
                topLeft = Offset(1 * step, 1 * step),
                size = androidx.compose.ui.geometry.Size(6 * step, 6 * step)
            )
            // Hair
            drawRect(
                color = hairColor,
                topLeft = Offset(1 * step, 1 * step),
                size = androidx.compose.ui.geometry.Size(6 * step, 2.5f * step)
            )
            // Eyes
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(2 * step, 4 * step),
                size = androidx.compose.ui.geometry.Size(1 * step, 1 * step)
            )
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(5 * step, 4 * step),
                size = androidx.compose.ui.geometry.Size(1 * step, 1 * step)
            )
            // Cheeks / Accessory
            drawRect(
                color = baseColor,
                topLeft = Offset(3.5f * step, 5.5f * step),
                size = androidx.compose.ui.geometry.Size(1 * step, 0.5f * step)
            )
        }
    }
}

@Composable
fun AddScheduleEventDialog(
    selectedDate: String,
    isGoogleConnected: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        time: String,
        durationMinutes: Int,
        subject: String,
        chapter: String,
        avatarType: String,
        syncToGoogle: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("9:00 AM") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedChapter by remember { mutableStateOf("Force") }
    var isTeamBlock by remember { mutableStateOf(false) }
    var syncToGoogle by remember { mutableStateOf(isGoogleConnected) }

    val presetTimes = listOf("7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:30 AM", "2:00 PM", "4:00 PM", "6:00 PM", "8:00 PM")
    val presetDurations = listOf(15, 30, 45, 60, 90)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelArtEmblem(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Schedule Event",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelifySansFontFamily
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Date: $selectedDate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event / Task Title *") },
                    placeholder = { Text("e.g. Physics - Force Numerical Practice") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Study with team, finish ICSE sample questions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Start Time:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetTimes.forEach { t ->
                        FilterChip(
                            selected = time == t,
                            onClick = { time = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetDurations.forEach { d ->
                        FilterChip(
                            selected = durationMinutes == d,
                            onClick = { durationMinutes = d },
                            label = { Text("${d}m", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("ICSE Subject:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InitialSyllabusData.subjectsWithChapters.keys.forEach { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = {
                                selectedSubject = sub
                                selectedChapter = InitialSyllabusData.subjectsWithChapters[sub]?.firstOrNull() ?: ""
                            },
                            label = { Text(sub, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Chapter: $selectedChapter", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Highlight as Team/Active event
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isTeamBlock,
                        onCheckedChange = { isTeamBlock = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Highlight as Active Event (Solid Blue Card with Avatars)",
                        fontSize = 12.sp
                    )
                }

                // Google Calendar Sync Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncToGoogle,
                        onCheckedChange = { syncToGoogle = it },
                        enabled = isGoogleConnected
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGoogleConnected) "Sync to Google Calendar via API" else "Connect Google Calendar in Settings to enable sync",
                        fontSize = 12.sp,
                        color = if (isGoogleConnected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            description,
                            time,
                            durationMinutes,
                            selectedSubject,
                            selectedChapter,
                            if (isTeamBlock) "GROUP" else "SOLO",
                            syncToGoogle
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("Add to Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
