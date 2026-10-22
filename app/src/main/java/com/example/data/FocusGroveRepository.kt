package com.example.data

import android.content.Context
import com.example.data.local.EconomyDao
import com.example.data.local.QuizDao
import com.example.data.local.ResourceDao
import com.example.data.local.ScheduleDao
import com.example.data.local.SyllabusDao
import com.example.data.local.TreeDao
import com.example.data.model.AmbientSound
import com.example.data.model.ChapterProgress
import com.example.data.model.QuizResultEntity
import com.example.data.model.ResourceEntity
import com.example.data.model.ScheduleEventEntity
import com.example.data.model.TreeRecord
import com.example.data.model.TreeSpecies
import com.example.data.model.TreeStatus
import com.example.data.model.UserEconomy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.local.UserProfileDao
import com.example.data.model.UserProfileEntity

class FocusGroveRepository(
    private val treeDao: TreeDao,
    private val syllabusDao: SyllabusDao,
    private val economyDao: EconomyDao,
    private val quizDao: QuizDao,
    private val scheduleDao: ScheduleDao,
    private val resourceDao: ResourceDao,
    private val userProfileDao: UserProfileDao
) {
    val allTrees: Flow<List<TreeRecord>> = treeDao.getAllTrees()
    val allChapters: Flow<List<ChapterProgress>> = syllabusDao.getAllChapters()
    val economy: Flow<UserEconomy?> = economyDao.getEconomy()
    val allScheduleEvents: Flow<List<ScheduleEventEntity>> = scheduleDao.getAllEvents()
    val allResources: Flow<List<ResourceEntity>> = resourceDao.getAllResources()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>> =
        resourceDao.getResourcesBySubject(subject)

    fun getScheduleEventsForDate(date: String): Flow<List<ScheduleEventEntity>> =
        scheduleDao.getEventsForDate(date)

    val totalTreesCount: Flow<Int> = treeDao.getTotalTreesCount()
    val aliveTreesCount: Flow<Int> = treeDao.getAliveTreesCount()
    val deadTreesCount: Flow<Int> = treeDao.getDeadTreesCount()
    val totalFocusedSeconds: Flow<Int?> = treeDao.getTotalFocusedSeconds()

    val completedChaptersCount: Flow<Int> = syllabusDao.getCompletedChaptersCount()
    val totalChaptersCount: Flow<Int> = syllabusDao.getTotalChaptersCount()

    val allQuizResults: Flow<List<QuizResultEntity>> = quizDao.getAllQuizResults()
    val totalQuizzesTaken: Flow<Int> = quizDao.getTotalQuizzesTaken()
    val averageQuizScorePercent: Flow<Float?> = quizDao.getAverageQuizScorePercent()

    suspend fun checkAndInitializeData(context: Context? = null) = withContext(Dispatchers.IO) {
        val econ = economyDao.getEconomy().firstOrNull()
        if (econ == null) {
            economyDao.saveEconomy(
                UserEconomy(
                    id = 1,
                    coins = 0,
                    selectedSpecies = TreeSpecies.OAK.id,
                    unlockedItems = "${TreeSpecies.OAK.id},${AmbientSound.NONE.id}",
                    demoModeEnabled = false,
                    activeAmbientSound = AmbientSound.NONE.id
                )
            )
        }
        val chapters = syllabusDao.getAllChapters().firstOrNull()
        if (chapters.isNullOrEmpty()) {
            syllabusDao.insertInitialChapters(InitialSyllabusData.createInitialChaptersList())
        }

        // Seed initial schedule events if none exist
        if (scheduleDao.getEventCount() == 0) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            val sampleEvents = listOf(
                ScheduleEventEntity(
                    title = "Wakeup",
                    description = "Early wakeup from bed and fresh",
                    date = todayStr,
                    time = "7:00 AM",
                    durationMinutes = 30,
                    subjectTag = "Routine",
                    isCompleted = true,
                    isActive = false,
                    avatarType = "SOLO",
                    colorTheme = "PASTEL"
                ),
                ScheduleEventEntity(
                    title = "Morning Excercise",
                    description = "4 types of exercise",
                    date = todayStr,
                    time = "8:00 AM",
                    durationMinutes = 45,
                    subjectTag = "Fitness",
                    isCompleted = true,
                    isActive = false,
                    avatarType = "SOLO",
                    colorTheme = "PASTEL"
                ),
                ScheduleEventEntity(
                    title = "Meeting",
                    description = "Zoom call, Discuss team task for the day",
                    date = todayStr,
                    time = "9:00 AM",
                    durationMinutes = 60,
                    subjectTag = "Study Group",
                    isCompleted = false,
                    isActive = true,
                    avatarType = "GROUP",
                    colorTheme = "BLUE"
                ),
                ScheduleEventEntity(
                    title = "Breakfast",
                    description = "Morning breakfast with bread, banana egg bowel and tea.",
                    date = todayStr,
                    time = "10:00 AM",
                    durationMinutes = 30,
                    subjectTag = "Break",
                    isCompleted = false,
                    isActive = false,
                    avatarType = "SOLO",
                    colorTheme = "PASTEL"
                ),
                ScheduleEventEntity(
                    title = "Physics - Force & Laws",
                    description = "Deep study block on Newton's Laws & practice numericals.",
                    date = todayStr,
                    time = "11:30 AM",
                    durationMinutes = 45,
                    subjectTag = "Physics",
                    chapterTag = "Force",
                    isCompleted = false,
                    isActive = false,
                    avatarType = "STUDY",
                    colorTheme = "PASTEL"
                ),
                ScheduleEventEntity(
                    title = "Chemistry - Periodic Properties",
                    description = "Review periodic trends, electron affinity & atomic radii.",
                    date = todayStr,
                    time = "2:00 PM",
                    durationMinutes = 45,
                    subjectTag = "Chemistry",
                    chapterTag = "Periodic Table",
                    isCompleted = false,
                    isActive = false,
                    avatarType = "STUDY",
                    colorTheme = "PASTEL"
                )
            )
            scheduleDao.insertEvents(sampleEvents)
        }

        // Initialize library resources from JSON
        context?.let { parseAndInsertLibraryResources(it) }
    }

    suspend fun parseAndInsertLibraryResources(context: Context) = withContext(Dispatchers.IO) {
        if (resourceDao.getResourceCount() == 0) {
            val jsonString = try {
                context.assets.open("library_resources.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
            if (jsonString.isNotBlank()) {
                val jsonArray = JSONArray(jsonString)
                val list = ArrayList<ResourceEntity>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        ResourceEntity(
                            subject = obj.optString("subject", "General"),
                            title = obj.optString("title", "Untitled Document"),
                            url = obj.optString("url", "")
                        )
                    )
                }
                resourceDao.insertResources(list)
            }
        }
    }

    suspend fun addScheduleEvent(event: ScheduleEventEntity): Long = withContext(Dispatchers.IO) {
        scheduleDao.insertEvent(event)
    }

    suspend fun updateScheduleEvent(event: ScheduleEventEntity) = withContext(Dispatchers.IO) {
        scheduleDao.updateEvent(event)
    }

    suspend fun deleteScheduleEvent(id: Long) = withContext(Dispatchers.IO) {
        scheduleDao.deleteEventById(id)
    }

    suspend fun toggleEventCompletion(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        scheduleDao.setEventCompleted(id, completed)
    }

    suspend fun markEventGoogleSynced(id: Long, googleEventId: String) = withContext(Dispatchers.IO) {
        scheduleDao.markAsGoogleSynced(id, googleEventId)
    }

    suspend fun setActiveEvent(date: String, activeId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.setActiveEventForDate(date, activeId)
    }

    suspend fun recordSession(
        species: TreeSpecies,
        status: TreeStatus,
        subjectTag: String,
        chapterTag: String = "",
        durationSeconds: Int
    ): Long = withContext(Dispatchers.IO) {
        val record = TreeRecord(
            species = species.id,
            status = status.name,
            subjectTag = subjectTag,
            chapterTag = chapterTag,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        val treeId = treeDao.insertTree(record)
        if (status == TreeStatus.ALIVE) {
            // Core economy reward: 10 coins per grown tree!
            economyDao.addCoins(10)
            recordDailyStreak()
        }
        treeId
    }

    suspend fun recordDailyStreak(): Int = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        val newStreak = when (profile.lastActiveDate) {
            todayStr -> if (profile.currentStreak == 0) 1 else profile.currentStreak
            yesterdayStr -> profile.currentStreak + 1
            else -> 1
        }
        userProfileDao.updateStreak(newStreak, todayStr)
        newStreak
    }

    suspend fun simulateTreeDeath(
        species: TreeSpecies = TreeSpecies.OAK,
        subjectTag: String = "Physics",
        chapterTag: String = "Force"
    ): Long = withContext(Dispatchers.IO) {
        val record = TreeRecord(
            species = species.id,
            status = TreeStatus.DEAD.name,
            subjectTag = subjectTag,
            chapterTag = chapterTag,
            durationSeconds = 180,
            timestamp = System.currentTimeMillis()
        )
        treeDao.insertTree(record)
    }

    suspend fun setChapterCompletion(chapterId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setChapterCompletion(chapterId, completed)
    }

    suspend fun purchaseShopItem(
        itemId: String,
        cost: Int,
        isTreeSpecies: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        if (currentEcon.coins < cost) return@withContext false

        val currentUnlocked = currentEcon.unlockedItems.split(",").toMutableSet()
        if (currentUnlocked.contains(itemId)) return@withContext false

        currentUnlocked.add(itemId)
        val newUnlockedStr = currentUnlocked.joinToString(",")
        val remainingCoins = currentEcon.coins - cost

        economyDao.setCoins(remainingCoins)
        economyDao.setUnlockedItems(newUnlockedStr)

        if (isTreeSpecies) {
            economyDao.setSelectedSpecies(itemId)
        } else {
            economyDao.setActiveAmbientSound(itemId)
        }
        true
    }

    suspend fun setSelectedSpecies(species: TreeSpecies) = withContext(Dispatchers.IO) {
        economyDao.setSelectedSpecies(species.id)
    }

    suspend fun setActiveAmbientSound(sound: AmbientSound) = withContext(Dispatchers.IO) {
        economyDao.setActiveAmbientSound(sound.id)
    }

    suspend fun setDemoMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        economyDao.setDemoMode(enabled)
    }

    suspend fun saveQuizResult(
        chapter: String,
        score: Int,
        totalQuestions: Int
    ): Long = withContext(Dispatchers.IO) {
        val entity = QuizResultEntity(
            chapter = chapter,
            score = score,
            totalQuestions = totalQuestions,
            timestamp = System.currentTimeMillis()
        )
        quizDao.insertQuizResult(entity)
    }

    suspend fun addCoins(amount: Int) = withContext(Dispatchers.IO) {
        economyDao.addCoins(amount)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        treeDao.clearAll()
        quizDao.clearAll()
        userProfileDao.clearProfile()
        economyDao.saveEconomy(
            UserEconomy(
                id = 1,
                coins = 0,
                selectedSpecies = TreeSpecies.OAK.id,
                unlockedItems = "${TreeSpecies.OAK.id},${AmbientSound.NONE.id}",
                demoModeEnabled = false,
                activeAmbientSound = AmbientSound.NONE.id
            )
        )
        val initialList = InitialSyllabusData.createInitialChaptersList()
        initialList.forEach { chapter ->
            syllabusDao.setFundamental(chapter.id, false)
            syllabusDao.setEx1(chapter.id, false)
            syllabusDao.setEx2(chapter.id, false)
            syllabusDao.setEx3(chapter.id, false)
            syllabusDao.setPyqs(chapter.id, false)
            syllabusDao.updateNotesAndUrl(chapter.id, "", "")
        }
    }

    suspend fun clearAllTrees() = withContext(Dispatchers.IO) {
        treeDao.clearAll()
    }

    // --- Syllabus 5-Step Checkpoints & Notes ---

    suspend fun setChapterFundamental(chapterId: String, value: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setFundamental(chapterId, value)
    }

    suspend fun setChapterEx1(chapterId: String, value: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setEx1(chapterId, value)
    }

    suspend fun setChapterEx2(chapterId: String, value: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setEx2(chapterId, value)
    }

    suspend fun setChapterEx3(chapterId: String, value: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setEx3(chapterId, value)
    }

    suspend fun setChapterPyqs(chapterId: String, value: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setPyqs(chapterId, value)
    }

    suspend fun setChapterLearned(chapterId: String, learned: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setFundamental(chapterId, learned)
    }

    suspend fun setChapterRevised(chapterId: String, revised: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setEx1(chapterId, revised)
    }

    suspend fun setChapterPyqsDone(chapterId: String, pyqsDone: Boolean) = withContext(Dispatchers.IO) {
        syllabusDao.setPyqs(chapterId, pyqsDone)
    }

    suspend fun updateChapterNotesAndUrl(chapterId: String, notes: String, customUrl: String) = withContext(Dispatchers.IO) {
        syllabusDao.updateNotesAndUrl(chapterId, notes, customUrl)
    }

    // --- Study Inventory / Resources ---

    suspend fun addCustomResource(resource: ResourceEntity): Long = withContext(Dispatchers.IO) {
        resourceDao.insertResource(resource)
    }

    suspend fun updateCustomResource(resource: ResourceEntity) = withContext(Dispatchers.IO) {
        resourceDao.updateResource(resource)
    }

    suspend fun deleteCustomResource(resource: ResourceEntity) = withContext(Dispatchers.IO) {
        resourceDao.deleteResource(resource)
    }

    suspend fun deleteResourceById(id: Long) = withContext(Dispatchers.IO) {
        resourceDao.deleteResourceById(id)
    }

    // --- User Profile & Font Preference ---

    suspend fun saveUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        userProfileDao.saveUserProfile(profile)
    }

    suspend fun setFontPreference(usePixel: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.setFontPreference(usePixel)
    }
}
