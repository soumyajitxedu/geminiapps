package com.example.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSoundGenerator
import com.example.data.FocusGroveRepository
import com.example.data.InitialSyllabusData
import com.example.data.local.FocusGroveDatabase
import com.example.data.model.AmbientSound
import com.example.data.model.ChapterProgress
import com.example.data.model.FocusTimerMode
import com.example.data.model.QuizPayload
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizResultEntity
import com.example.data.model.TreeRecord
import com.example.data.model.TreeSpecies
import com.example.data.model.TreeStatus
import com.example.data.model.UserEconomy
import com.example.quiz.QuizParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.calendar.GoogleCalendarManager
import com.example.data.model.ResourceEntity
import com.example.data.model.ScheduleEventEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimerStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED
}

data class SessionResult(
    val species: TreeSpecies,
    val status: TreeStatus,
    val coinsEarned: Int,
    val durationSeconds: Int,
    val subjectTag: String = "",
    val chapterTag: String = "",
    val reason: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FocusGroveDatabase.getDatabase(application, viewModelScope)
    val repository = FocusGroveRepository(
        treeDao = database.treeDao(),
        syllabusDao = database.syllabusDao(),
        economyDao = database.economyDao(),
        quizDao = database.quizDao(),
        scheduleDao = database.scheduleDao(),
        resourceDao = database.resourceDao(),
        userProfileDao = database.userProfileDao()
    )

    val userProfile: StateFlow<com.example.data.model.UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val calendarManager = GoogleCalendarManager(application)
    private val soundGenerator = AmbientSoundGenerator()

    // Google Calendar StateFlows
    val isGoogleCalendarConnected: StateFlow<Boolean> = calendarManager.isConnected
    val googleCalendarEmail: StateFlow<String> = calendarManager.connectedEmail
    val googleCalendarAutoSync: StateFlow<Boolean> = calendarManager.autoSyncEnabled
    val googleCalendarLastSync: StateFlow<String> = calendarManager.lastSyncMessage

    // Schedule & Calendar Timeline StateFlows
    private val _selectedScheduleDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedScheduleDate: StateFlow<String> = _selectedScheduleDate.asStateFlow()

    val scheduleEvents: StateFlow<List<ScheduleEventEntity>> = _selectedScheduleDate
        .flatMapLatest { date -> repository.getScheduleEventsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleEvents: StateFlow<List<ScheduleEventEntity>> = repository.allScheduleEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Library StateFlows
    private val _selectedLibrarySubject = MutableStateFlow("All")
    val selectedLibrarySubject: StateFlow<String> = _selectedLibrarySubject.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    val allResources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredResources: StateFlow<List<ResourceEntity>> = combine(
        repository.allResources,
        _selectedLibrarySubject,
        _librarySearchQuery
    ) { resources, subject, query ->
        resources.filter { item ->
            val matchesSubject = if (subject == "All") true else item.subject.equals(subject, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else item.title.contains(query, ignoreCase = true) || item.subject.contains(query, ignoreCase = true)
            matchesSubject && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLibrarySubject(subject: String) {
        _selectedLibrarySubject.value = subject
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    // Timer States
    private val _timerStatus = MutableStateFlow(TimerStatus.IDLE)
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private val _timerMode = MutableStateFlow(FocusTimerMode.COUNTDOWN)
    val timerMode: StateFlow<FocusTimerMode> = _timerMode.asStateFlow()

    private val _customMinutes = MutableStateFlow(30)
    val customMinutes: StateFlow<Int> = _customMinutes.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(1800) // 30 minutes
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(1800)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _elapsedStopwatchSeconds = MutableStateFlow(0)
    val elapsedStopwatchSeconds: StateFlow<Int> = _elapsedStopwatchSeconds.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Physics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedChapter = MutableStateFlow("Force")
    val selectedChapter: StateFlow<String> = _selectedChapter.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _lastSessionResult = MutableStateFlow<SessionResult?>(null)
    val lastSessionResult: StateFlow<SessionResult?> = _lastSessionResult.asStateFlow()

    // Custom Audio Player State (Storage picker & loop during timer)
    private var customMediaPlayer: android.media.MediaPlayer? = null
    private val _customAudioUri = MutableStateFlow<android.net.Uri?>(null)
    val customAudioUri: StateFlow<android.net.Uri?> = _customAudioUri.asStateFlow()
    private val _customAudioName = MutableStateFlow<String?>(null)
    val customAudioName: StateFlow<String?> = _customAudioName.asStateFlow()
    private val _isCustomAudioPlaying = MutableStateFlow(false)
    val isCustomAudioPlaying: StateFlow<Boolean> = _isCustomAudioPlaying.asStateFlow()

    // Motivational Quotes & Gratitude Prompts for Focus Session
    val motivationalQuotes = listOf(
        "\"Small daily improvements over time lead to stunning results.\" — Robin Sharma",
        "\"Focus is a muscle. The more you practice, the stronger it grows.\" — Cal Newport",
        "\"You don't have to be extreme, just consistent.\" — Atomic Habits",
        "\"The secret of getting ahead is getting started.\" — Mark Twain",
        "\"Do what you have to do until you can do what you want to do.\" — Oprah Winfrey",
        "\"Excellence is not an act, but a habit.\" — Aristotle",
        "\"The future depends on what you do today.\" — Mahatma Gandhi",
        "\"Deep work is the superpower of the 21st century.\" — Cal Newport"
    )

    val gratitudePrompts = listOf(
        "Take a breath: What is one small thing you are thankful for today?",
        "Reflect: Who supported you or brought a smile to your face recently?",
        "Mindful moment: What part of your study journey are you proud of?",
        "Gratitude: A warm drink, quiet space, or a peaceful moment right now.",
        "Appreciate: The chance to learn, understand, and grow your intellect."
    )

    private val _activeQuoteIndex = MutableStateFlow(0)
    val activeQuoteIndex: StateFlow<Int> = _activeQuoteIndex.asStateFlow()

    private val _activeGratitudeIndex = MutableStateFlow(0)
    val activeGratitudeIndex: StateFlow<Int> = _activeGratitudeIndex.asStateFlow()

    fun cycleQuote() {
        _activeQuoteIndex.value = (_activeQuoteIndex.value + 1) % motivationalQuotes.size
    }

    fun cycleGratitude() {
        _activeGratitudeIndex.value = (_activeGratitudeIndex.value + 1) % gratitudePrompts.size
    }

    fun setCustomAudio(uri: android.net.Uri?, name: String?) {
        stopCustomAudio()
        _customAudioUri.value = uri
        _customAudioName.value = name
    }

    fun playCustomAudio() {
        val uri = _customAudioUri.value ?: return
        try {
            stopCustomAudio()
            customMediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(getApplication<Application>(), uri)
                isLooping = true
                prepare()
                start()
            }
            _isCustomAudioPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isCustomAudioPlaying.value = false
        }
    }

    fun stopCustomAudio() {
        try {
            customMediaPlayer?.stop()
            customMediaPlayer?.release()
        } catch (_: Exception) {}
        customMediaPlayer = null
        _isCustomAudioPlaying.value = false
    }

    fun toggleCustomAudio() {
        if (_isCustomAudioPlaying.value) {
            stopCustomAudio()
        } else {
            playCustomAudio()
        }
    }

    private var timerJob: Job? = null

    // Room Flows
    val trees: StateFlow<List<TreeRecord>> = repository.allTrees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterProgress>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val economy: StateFlow<UserEconomy?> = repository.economy
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalTreesCount: StateFlow<Int> = repository.totalTreesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aliveTreesCount: StateFlow<Int> = repository.aliveTreesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val deadTreesCount: StateFlow<Int> = repository.deadTreesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalFocusedSeconds: StateFlow<Int?> = repository.totalFocusedSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedChaptersCount: StateFlow<Int> = repository.completedChaptersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalChaptersCount: StateFlow<Int> = repository.totalChaptersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val quizResults: StateFlow<List<QuizResultEntity>> = repository.allQuizResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalQuizzesTaken: StateFlow<Int> = repository.totalQuizzesTaken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageQuizScorePercent: StateFlow<Float?> = repository.averageQuizScorePercent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Interactive Quiz Engine States
    private val _activeQuiz = MutableStateFlow<QuizPayload?>(null)
    val activeQuiz: StateFlow<QuizPayload?> = _activeQuiz.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _userScore = MutableStateFlow(0)
    val userScore: StateFlow<Int> = _userScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndInitializeData(application)
        }
    }

    fun setTimerMode(mode: FocusTimerMode) {
        if (_timerStatus.value == TimerStatus.RUNNING) return
        _timerMode.value = mode
        resetTimerToIdle()
    }

    fun setCustomMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 120)
        _customMinutes.value = clamped
        if (_timerStatus.value == TimerStatus.IDLE && _timerMode.value == FocusTimerMode.COUNTDOWN) {
            val demo = economy.value?.demoModeEnabled == true
            val duration = if (demo) 10 else (clamped * 60)
            _totalDurationSeconds.value = duration
            _remainingSeconds.value = duration
        }
    }

    fun setSelectedSubject(subject: String) {
        _selectedSubject.value = subject
        val chapterList = InitialSyllabusData.subjectsWithChapters[subject] ?: emptyList()
        _selectedChapter.value = chapterList.firstOrNull() ?: "General Focus"
    }

    fun setSelectedChapter(chapter: String) {
        _selectedChapter.value = chapter
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDemoMode(enabled)
            if (_timerStatus.value == TimerStatus.IDLE) {
                val duration = if (enabled) 10 else (_customMinutes.value * 60)
                _totalDurationSeconds.value = duration
                _remainingSeconds.value = duration
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun startTimer() {
        if (_timerStatus.value == TimerStatus.RUNNING) return

        val demo = economy.value?.demoModeEnabled == true
        _timerStatus.value = TimerStatus.RUNNING
        _lastSessionResult.value = null

        // Play ambient audio if configured
        val sound = AmbientSound.fromId(economy.value?.activeAmbientSound ?: "NONE")
        soundGenerator.start(sound)

        // Loop custom picked audio if loaded
        if (_customAudioUri.value != null) {
            playCustomAudio()
        }

        vibrate(100)
        timerJob?.cancel()

        if (_timerMode.value == FocusTimerMode.COUNTDOWN) {
            val duration = if (demo) 10 else (_customMinutes.value * 60)
            _totalDurationSeconds.value = duration
            _remainingSeconds.value = duration

            timerJob = viewModelScope.launch {
                while (_remainingSeconds.value > 0) {
                    delay(1000)
                    _remainingSeconds.value -= 1
                }
                // Success! Countdown completed!
                completeCountdownSession()
            }
        } else {
            // Stopwatch Mode (Count-up)
            _elapsedStopwatchSeconds.value = 0
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _elapsedStopwatchSeconds.value += 1
                }
            }
        }
    }

    private fun completeCountdownSession() {
        soundGenerator.stop()
        stopCustomAudio()
        _timerStatus.value = TimerStatus.COMPLETED
        val species = TreeSpecies.fromId(economy.value?.selectedSpecies ?: "OAK")
        val duration = _totalDurationSeconds.value
        val subject = _selectedSubject.value
        val chapter = _selectedChapter.value

        viewModelScope.launch {
            repository.recordSession(
                species = species,
                status = TreeStatus.ALIVE,
                subjectTag = subject,
                chapterTag = chapter,
                durationSeconds = duration
            )
            _lastSessionResult.value = SessionResult(
                species = species,
                status = TreeStatus.ALIVE,
                coinsEarned = 10,
                durationSeconds = duration,
                subjectTag = subject,
                chapterTag = chapter
            )

            // Requirement 2: When Focus Timer session is completed, automatically create Google Calendar event
            if (calendarManager.isConnected.value && calendarManager.autoSyncEnabled.value) {
                calendarManager.createFocusSessionEvent(
                    species = species,
                    subject = subject,
                    chapter = chapter,
                    durationSeconds = duration
                )
            }
        }
        vibrate(300)
    }

    fun finishStopwatchSession() {
        if (_timerStatus.value != TimerStatus.RUNNING) return
        timerJob?.cancel()
        soundGenerator.stop()
        stopCustomAudio()

        val elapsed = _elapsedStopwatchSeconds.value
        val species = TreeSpecies.fromId(economy.value?.selectedSpecies ?: "OAK")
        val subject = _selectedSubject.value
        val chapter = _selectedChapter.value

        _timerStatus.value = TimerStatus.COMPLETED

        viewModelScope.launch {
            repository.recordSession(
                species = species,
                status = TreeStatus.ALIVE,
                subjectTag = subject,
                chapterTag = chapter,
                durationSeconds = elapsed
            )
            _lastSessionResult.value = SessionResult(
                species = species,
                status = TreeStatus.ALIVE,
                coinsEarned = 10,
                durationSeconds = elapsed,
                subjectTag = subject,
                chapterTag = chapter
            )

            // Requirement 2: Auto-sync stopwatch session to Google Calendar
            if (calendarManager.isConnected.value && calendarManager.autoSyncEnabled.value) {
                calendarManager.createFocusSessionEvent(
                    species = species,
                    subject = subject,
                    chapter = chapter,
                    durationSeconds = elapsed
                )
            }
        }
        vibrate(300)
    }

    fun giveUp() {
        if (_timerStatus.value != TimerStatus.RUNNING) return
        timerJob?.cancel()
        soundGenerator.stop()
        stopCustomAudio()

        failSession("Gave up during focus session")
    }

    // Called when the user leaves or navigates away from the Timer screen
    fun onExitTimerScreen() {
        if (_timerStatus.value == TimerStatus.RUNNING) {
            giveUp()
        }
    }

    // Critical constraint: The timer runs in the foreground; tree dies ONLY on Give Up or exiting timer screen.
    // Backgrounding does NOT kill the tree unless user explicitly gave up or navigated away.
    fun onAppBackgrounded() {
        // No-op to strictly fulfill foreground constraint where tree dies only on Give Up or leaving timer screen.
    }

    private fun failSession(reason: String) {
        _timerStatus.value = TimerStatus.FAILED
        val species = TreeSpecies.fromId(economy.value?.selectedSpecies ?: "OAK")
        val elapsed = if (_timerMode.value == FocusTimerMode.COUNTDOWN) {
            (_totalDurationSeconds.value - _remainingSeconds.value).coerceAtLeast(0)
        } else {
            _elapsedStopwatchSeconds.value.coerceAtLeast(0)
        }
        val subject = _selectedSubject.value
        val chapter = _selectedChapter.value

        viewModelScope.launch {
            repository.recordSession(
                species = species,
                status = TreeStatus.DEAD,
                subjectTag = subject,
                chapterTag = chapter,
                durationSeconds = elapsed
            )
            _lastSessionResult.value = SessionResult(
                species = species,
                status = TreeStatus.DEAD,
                coinsEarned = 0,
                durationSeconds = elapsed,
                subjectTag = subject,
                chapterTag = chapter,
                reason = reason
            )
        }
        vibrate(500)
    }

    fun resetTimerToIdle() {
        timerJob?.cancel()
        soundGenerator.stop()
        stopCustomAudio()
        _timerStatus.value = TimerStatus.IDLE
        val demo = economy.value?.demoModeEnabled == true
        val duration = if (demo) 10 else (_customMinutes.value * 60)
        _totalDurationSeconds.value = duration
        _remainingSeconds.value = duration
        _elapsedStopwatchSeconds.value = 0
        _lastSessionResult.value = null
    }

    // Debug Menu Actions
    fun addTestCoins(amount: Int = 100) {
        viewModelScope.launch {
            repository.addCoins(amount)
        }
        vibrate(100)
    }

    fun simulateTreeDeath() {
        viewModelScope.launch {
            repository.simulateTreeDeath(
                species = TreeSpecies.fromId(economy.value?.selectedSpecies ?: "OAK"),
                subjectTag = _selectedSubject.value,
                chapterTag = _selectedChapter.value
            )
        }
        vibrate(300)
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            resetTimerToIdle()
            _selectedSubject.value = "Physics"
            _selectedChapter.value = "Force"
            _customMinutes.value = 30
        }
        vibrate(400)
    }

    fun toggleChapterCompletion(chapterId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setChapterCompletion(chapterId, !currentStatus)
        }
    }

    // --- 3-Step Checkpoints & Notes ---
    fun toggleChapterLearned(chapter: ChapterProgress) {
        viewModelScope.launch {
            repository.setChapterLearned(chapter.id, !chapter.isLearned)
        }
    }

    fun toggleChapterRevised(chapter: ChapterProgress) {
        viewModelScope.launch {
            repository.setChapterRevised(chapter.id, !chapter.isRevised)
        }
    }

    fun toggleChapterPyqsDone(chapter: ChapterProgress) {
        viewModelScope.launch {
            repository.setChapterPyqsDone(chapter.id, !chapter.isPyqsDone)
        }
    }

    fun saveChapterNotes(chapterId: String, notes: String, customUrl: String) {
        viewModelScope.launch {
            repository.updateChapterNotesAndUrl(chapterId, notes, customUrl)
        }
    }

    // --- Study Inventory (Resource CRUD) ---
    fun addCustomResource(title: String, subject: String, url: String) {
        viewModelScope.launch {
            repository.addCustomResource(
                ResourceEntity(
                    subject = subject,
                    title = title,
                    url = url,
                    isUserAdded = true
                )
            )
        }
    }

    fun updateCustomResource(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.updateCustomResource(resource)
        }
    }

    fun deleteResource(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.deleteCustomResource(resource)
        }
    }

    // --- User Profile & Global Pixel Font ---
    fun saveUserProfile(name: String, hobbies: String, dailyTargetMinutes: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = (current ?: com.example.data.model.UserProfileEntity()).copy(
                name = name,
                hobbies = hobbies,
                dailyTargetMinutes = dailyTargetMinutes,
                isOnboarded = true
            )
            repository.saveUserProfile(updated)
        }
    }

    fun togglePixelFont() {
        viewModelScope.launch {
            val current = userProfile.value?.usePixelFont ?: true
            repository.setFontPreference(!current)
        }
    }

    fun setPixelFont(usePixel: Boolean) {
        viewModelScope.launch {
            repository.setFontPreference(usePixel)
        }
    }

    fun purchaseSpecies(species: TreeSpecies, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.purchaseShopItem(
                itemId = species.id,
                cost = species.cost,
                isTreeSpecies = true
            )
            onResult(success)
        }
    }

    fun equipSpecies(species: TreeSpecies) {
        viewModelScope.launch {
            repository.setSelectedSpecies(species)
        }
    }

    fun purchaseAmbientSound(sound: AmbientSound, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.purchaseShopItem(
                itemId = sound.id,
                cost = sound.cost,
                isTreeSpecies = false
            )
            onResult(success)
        }
    }

    fun setAmbientSound(sound: AmbientSound) {
        viewModelScope.launch {
            repository.setActiveAmbientSound(sound)
            if (_timerStatus.value == TimerStatus.RUNNING) {
                soundGenerator.start(sound)
            }
        }
    }

    // Quiz Engine Logic
    fun loadQuiz(rawJson: String): Result<QuizPayload> {
        val result = QuizParser.parse(rawJson)
        if (result.isSuccess) {
            val payload = result.getOrThrow()
            _activeQuiz.value = payload
            _currentQuestionIndex.value = 0
            _selectedOption.value = null
            _isAnswerRevealed.value = false
            _userScore.value = 0
            _isQuizFinished.value = false
        }
        return result
    }

    fun selectQuizOption(option: String) {
        if (_isAnswerRevealed.value) return
        val currentQuiz = _activeQuiz.value ?: return
        val question = currentQuiz.questions.getOrNull(_currentQuestionIndex.value) ?: return

        _selectedOption.value = option
        _isAnswerRevealed.value = true

        val isCorrect = option.trim().equals(question.answer.trim(), ignoreCase = true)
        if (isCorrect) {
            _userScore.value += 1
            vibrate(100)
        } else {
            vibrate(250)
        }
    }

    fun nextQuizQuestion() {
        val currentQuiz = _activeQuiz.value ?: return
        if (_currentQuestionIndex.value + 1 < currentQuiz.questions.size) {
            _currentQuestionIndex.value += 1
            _selectedOption.value = null
            _isAnswerRevealed.value = false
        } else {
            // Finished quiz!
            _isQuizFinished.value = true
            saveCurrentQuizResult()
        }
    }

    private fun saveCurrentQuizResult() {
        val quiz = _activeQuiz.value ?: return
        val score = _userScore.value
        val total = quiz.questions.size

        viewModelScope.launch {
            repository.saveQuizResult(
                chapter = quiz.chapter,
                score = score,
                totalQuestions = total
            )
        }
    }

    fun resetQuiz() {
        _activeQuiz.value = null
        _currentQuestionIndex.value = 0
        _selectedOption.value = null
        _isAnswerRevealed.value = false
        _userScore.value = 0
        _isQuizFinished.value = false
    }

    // --- Schedule & Calendar Timeline Management ---

    fun setSelectedScheduleDate(date: String) {
        _selectedScheduleDate.value = date
    }

    fun addScheduleEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        durationMinutes: Int = 30,
        subjectTag: String = "General",
        chapterTag: String = "",
        avatarType: String = "SOLO",
        syncToGoogle: Boolean = true
    ) {
        viewModelScope.launch {
            val event = ScheduleEventEntity(
                title = title,
                description = description,
                date = date,
                time = time,
                durationMinutes = durationMinutes,
                subjectTag = subjectTag,
                chapterTag = chapterTag,
                avatarType = avatarType,
                colorTheme = if (avatarType == "GROUP") "BLUE" else "PASTEL"
            )
            val id = repository.addScheduleEvent(event)
            if (syncToGoogle && calendarManager.isConnected.value) {
                calendarManager.createScheduleEvent(event.copy(id = id))
                repository.markEventGoogleSynced(id, "sync_${id}_${System.currentTimeMillis()}")
            }
        }
    }

    fun toggleScheduleEventCompleted(event: ScheduleEventEntity) {
        viewModelScope.launch {
            repository.toggleEventCompletion(event.id, !event.isCompleted)
        }
    }

    fun deleteScheduleEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleEvent(id)
        }
    }

    fun setActiveScheduleEvent(date: String, activeId: Long) {
        viewModelScope.launch {
            repository.setActiveEvent(date, activeId)
        }
    }

    fun startTimerForScheduleEvent(event: ScheduleEventEntity) {
        if (event.subjectTag.isNotBlank() && event.subjectTag != "Routine" && event.subjectTag != "Fitness" && event.subjectTag != "Break") {
            setSelectedSubject(event.subjectTag)
            if (event.chapterTag.isNotBlank()) {
                setSelectedChapter(event.chapterTag)
            }
        }
        if (event.durationMinutes in 5..120) {
            setCustomMinutes(event.durationMinutes)
        }
    }

    // --- Google Calendar Sync Actions ---

    fun connectGoogleCalendar(email: String = GoogleCalendarManager.DEFAULT_USER_EMAIL) {
        calendarManager.connectCalendar(email)
    }

    fun disconnectGoogleCalendar() {
        calendarManager.disconnectCalendar()
    }

    fun setCalendarAutoSync(enabled: Boolean) {
        calendarManager.setAutoSync(enabled)
    }

    fun setCalendarCustomToken(token: String) {
        calendarManager.setCustomAccessToken(token)
    }

    fun syncAllStudyBlocksToGoogleCalendar() {
        viewModelScope.launch {
            val events = scheduleEvents.value
            events.forEach { event ->
                if (!event.isGoogleSynced) {
                    calendarManager.createScheduleEvent(event)
                    repository.markEventGoogleSynced(event.id, "sync_${event.id}_${System.currentTimeMillis()}")
                }
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Application.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        soundGenerator.stop()
        stopCustomAudio()
        timerJob?.cancel()
    }
}
