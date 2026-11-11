package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.screens.AnalysisShopScreen
import com.example.ui.screens.ForestScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.StudyToolsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyllabusScreen
import com.example.ui.screens.TimerScreen

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalLibrary
import com.example.ui.screens.ExamRulesTimetableScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ScheduleScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PixelArtEmblem
import com.example.ui.theme.CoinGold

enum class GroveScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    FOREST("Forest", Icons.Filled.Park, Icons.Outlined.Park, "nav_forest"),
    TIMER("Timer", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_timer"),
    SCHEDULE("Schedule", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_schedule"),
    LIBRARY("Library", Icons.Filled.LocalLibrary, Icons.Outlined.LocalLibrary, "nav_library"),
    SYLLABUS("Syllabus", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_syllabus"),
    QUIZZES("Study Tools", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_quizzes"),
    ANALYSIS_SHOP("Shop", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_shop")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusGroveApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(GroveScreen.FOREST) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isTimetableOpen by remember { mutableStateOf(false) }

    // Lifecycle observer: kills the tree ONLY if user locks screen or leaves the app (ON_STOP)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            viewModel = viewModel,
            onClose = { isSettingsOpen = false },
            onOpenTimetable = { isTimetableOpen = true },
            modifier = modifier
        )
    } else if (isTimetableOpen) {
        ExamRulesTimetableScreen(
            viewModel = viewModel,
            onClose = { isTimetableOpen = false },
            modifier = modifier
        )
    } else {
        // Back handling: pop back to Forest screen if on secondary tabs without killing timer
        if (currentScreen != GroveScreen.FOREST) {
            BackHandler {
                currentScreen = GroveScreen.FOREST
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelArtEmblem(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentScreen) {
                                    GroveScreen.FOREST -> "Focus Grove"
                                    GroveScreen.TIMER -> "Study Timer"
                                    GroveScreen.SCHEDULE -> "Weekly Schedule"
                                    GroveScreen.LIBRARY -> "Study Library"
                                    GroveScreen.SYLLABUS -> "ICSE Syllabus"
                                    GroveScreen.QUIZZES -> "Study Tools"
                                    GroveScreen.ANALYSIS_SHOP -> "Grove Shop"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = com.example.ui.theme.PixelifySansFontFamily
                                ),
                                maxLines = 1
                            )
                        }
                    },
                    actions = {
                        val coins by viewModel.coins.collectAsStateWithLifecycle()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .testTag("top_bar_coins")
                                .border(1.dp, CoinGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = CoinGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coins",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                viewModel.soundEffectManager.playUiClick()
                                isTimetableOpen = true
                            },
                            modifier = Modifier.testTag("top_bar_timetable_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "ICSE 2027 Timetable & Rules",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.soundEffectManager.playUiClick()
                                isSettingsOpen = true
                            },
                            modifier = Modifier.testTag("forest_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    GroveScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                viewModel.soundEffectManager.playUiClick()
                                currentScreen = screen
                            },
                            alwaysShowLabel = false,
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text(screen.title, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(screen.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        GroveScreen.FOREST -> ForestScreen(
                            viewModel = viewModel,
                            onNavigateToTimer = { currentScreen = GroveScreen.TIMER },
                            onOpenSettings = { isSettingsOpen = true },
                            onOpenTimetable = { isTimetableOpen = true }
                        )
                        GroveScreen.TIMER -> TimerScreen(
                            viewModel = viewModel,
                            onOpenSettings = { isSettingsOpen = true }
                        )
                        GroveScreen.SCHEDULE -> ScheduleScreen(
                            viewModel = viewModel,
                            onNavigateToTimer = { currentScreen = GroveScreen.TIMER },
                            onOpenSettings = { isSettingsOpen = true }
                        )
                        GroveScreen.LIBRARY -> LibraryScreen(
                            viewModel = viewModel
                        )
                        GroveScreen.SYLLABUS -> SyllabusScreen(
                            viewModel = viewModel,
                            onOpenTimetable = { isTimetableOpen = true }
                        )
                        GroveScreen.QUIZZES -> StudyToolsScreen(
                            viewModel = viewModel
                        )
                        GroveScreen.ANALYSIS_SHOP -> AnalysisShopScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
