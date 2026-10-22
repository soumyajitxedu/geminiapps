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
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyllabusScreen
import com.example.ui.screens.TimerScreen

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalLibrary
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ScheduleScreen

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
    QUIZZES("Quizzes", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_quizzes"),
    ANALYSIS_SHOP("Shop", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_shop")
}

@Composable
fun FocusGroveApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(GroveScreen.FOREST) }
    var isSettingsOpen by remember { mutableStateOf(false) }

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
                            onOpenSettings = { isSettingsOpen = true }
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
                            viewModel = viewModel
                        )
                        GroveScreen.QUIZZES -> QuizScreen(
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
