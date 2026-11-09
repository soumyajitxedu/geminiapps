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

import com.example.data.local.FlashcardDao
import com.example.data.local.UserProfileDao
import com.example.data.model.AppCustomFont
import com.example.data.model.AppThemePalette
import com.example.data.model.PetSpecies
import com.example.data.model.SavedFlashcardDeckEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject

class FocusGroveRepository(
    private val treeDao: TreeDao,
    private val syllabusDao: SyllabusDao,
    private val economyDao: EconomyDao,
    private val quizDao: QuizDao,
    private val scheduleDao: ScheduleDao,
    private val resourceDao: ResourceDao,
    private val userProfileDao: UserProfileDao,
    private val flashcardDao: FlashcardDao? = null
) {
    val allTrees: Flow<List<TreeRecord>> = treeDao.getAllTrees()
    val allChapters: Flow<List<ChapterProgress>> = syllabusDao.getAllChapters()
    val economy: Flow<UserEconomy?> = economyDao.getEconomy()
    val allScheduleEvents: Flow<List<ScheduleEventEntity>> = scheduleDao.getAllEvents()
    val allResources: Flow<List<ResourceEntity>> = resourceDao.getAllResources()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val allSavedDecks: Flow<List<SavedFlashcardDeckEntity>> = flashcardDao?.getAllDecks() ?: flowOf(emptyList())

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

    suspend fun purchasePet(pet: PetSpecies): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        if (currentEcon.coins < pet.cost) return@withContext false

        val currentPets = currentEcon.unlockedPets.split(",").map { it.trim() }.toMutableSet()
        if (currentPets.contains(pet.id)) return@withContext false

        currentPets.add(pet.id)
        val newPetsStr = currentPets.joinToString(",")
        val remainingCoins = currentEcon.coins - pet.cost

        economyDao.setCoins(remainingCoins)
        economyDao.setUnlockedPets(newPetsStr)
        economyDao.setEquippedPet(pet.id)
        true
    }

    suspend fun equipPet(pet: PetSpecies) = withContext(Dispatchers.IO) {
        economyDao.setEquippedPet(pet.id)
    }

    suspend fun purchaseTheme(theme: AppThemePalette): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        if (currentEcon.coins < theme.cost) return@withContext false

        val currentThemes = currentEcon.unlockedThemes.split(",").map { it.trim() }.toMutableSet()
        if (currentThemes.contains(theme.id)) return@withContext false

        currentThemes.add(theme.id)
        val newThemesStr = currentThemes.joinToString(",")
        val remainingCoins = currentEcon.coins - theme.cost

        economyDao.setCoins(remainingCoins)
        economyDao.setUnlockedThemes(newThemesStr)
        economyDao.setEquippedTheme(theme.id)
        true
    }

    suspend fun equipTheme(theme: AppThemePalette) = withContext(Dispatchers.IO) {
        economyDao.setEquippedTheme(theme.id)
    }

    suspend fun purchaseFont(font: AppCustomFont): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        if (currentEcon.coins < font.cost) return@withContext false

        val currentFonts = currentEcon.unlockedFonts.split(",").map { it.trim() }.toMutableSet()
        if (currentFonts.contains(font.id)) return@withContext false

        currentFonts.add(font.id)
        val newFontsStr = currentFonts.joinToString(",")
        val remainingCoins = currentEcon.coins - font.cost

        economyDao.setCoins(remainingCoins)
        economyDao.setUnlockedFonts(newFontsStr)
        economyDao.setEquippedFont(font.id)
        true
    }

    suspend fun equipFont(font: AppCustomFont) = withContext(Dispatchers.IO) {
        economyDao.setEquippedFont(font.id)
    }

    suspend fun setCustomBackgroundUri(uri: String?) = withContext(Dispatchers.IO) {
        economyDao.setCustomBackgroundUri(uri)
    }

    suspend fun waterTree(treeId: Long): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        val waterCost = 5
        if (currentEcon.coins >= waterCost) {
            economyDao.addCoins(-waterCost)
        }
        treeDao.updateTreeHydration(treeId, 100, System.currentTimeMillis())
        true
    }

    suspend fun waterAllTrees(): Boolean = withContext(Dispatchers.IO) {
        val currentEcon = economyDao.getEconomy().firstOrNull() ?: return@withContext false
        val waterCost = 10
        if (currentEcon.coins >= waterCost) {
            economyDao.addCoins(-waterCost)
        }
        treeDao.waterAllTrees(System.currentTimeMillis())
        true
    }

    suspend fun saveFlashcardDeck(
        folderName: String,
        chapter: String,
        cardsJson: String,
        cardCount: Int
    ): Long = withContext(Dispatchers.IO) {
        val entity = SavedFlashcardDeckEntity(
            folderName = folderName,
            chapter = chapter,
            cardsJson = cardsJson,
            cardCount = cardCount,
            createdAt = System.currentTimeMillis()
        )
        flashcardDao?.insertDeck(entity) ?: 0L
    }

    suspend fun deleteFlashcardDeck(id: Long) = withContext(Dispatchers.IO) {
        flashcardDao?.deleteDeckById(id)
    }

    suspend fun clearAllFlashcardDecks() = withContext(Dispatchers.IO) {
        flashcardDao?.clearAllDecks()
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

    suspend fun setCoins(amount: Int) = withContext(Dispatchers.IO) {
        economyDao.setCoins(amount)
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

    // --- Database Backup: Export & Import ---

    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "FocusGrove")
        root.put("version", "1.3")
        root.put("exportTimestamp", System.currentTimeMillis())

        // Economy
        val econ = economyDao.getEconomyOnce()
        if (econ != null) {
            val econObj = JSONObject().apply {
                put("id", econ.id)
                put("coins", econ.coins)
                put("selectedSpecies", econ.selectedSpecies)
                put("unlockedItems", econ.unlockedItems)
                put("demoModeEnabled", econ.demoModeEnabled)
                put("activeAmbientSound", econ.activeAmbientSound)
                put("unlockedPets", econ.unlockedPets)
                put("equippedPet", econ.equippedPet)
                put("unlockedThemes", econ.unlockedThemes)
                put("equippedTheme", econ.equippedTheme)
                put("unlockedFonts", econ.unlockedFonts)
                put("equippedFont", econ.equippedFont)
                put("customBackgroundUri", econ.customBackgroundUri ?: "")
            }
            root.put("economy", econObj)
        }

        // User Profile
        val prof = userProfileDao.getUserProfileOnce()
        if (prof != null) {
            val profObj = JSONObject().apply {
                put("id", prof.id)
                put("name", prof.name)
                put("hobbies", prof.hobbies)
                put("dailyTargetMinutes", prof.dailyTargetMinutes)
                put("isOnboarded", prof.isOnboarded)
                put("usePixelFont", prof.usePixelFont)
                put("currentStreak", prof.currentStreak)
                put("lastActiveDate", prof.lastActiveDate)
            }
            root.put("userProfile", profObj)
        }

        // Trees
        val treesList = treeDao.getAllTreesList()
        val treesArr = JSONArray()
        treesList.forEach { t ->
            treesArr.put(JSONObject().apply {
                put("id", t.id)
                put("species", t.species)
                put("status", t.status)
                put("subjectTag", t.subjectTag)
                put("chapterTag", t.chapterTag)
                put("durationSeconds", t.durationSeconds)
                put("timestamp", t.timestamp)
                put("hydrationLevel", t.hydrationLevel)
                put("lastWateredTimestamp", t.lastWateredTimestamp)
            })
        }
        root.put("trees", treesArr)

        // Chapters
        val chaptersList = syllabusDao.getAllChaptersList()
        val chaptersArr = JSONArray()
        chaptersList.forEach { c ->
            chaptersArr.put(JSONObject().apply {
                put("id", c.id)
                put("subject", c.subject)
                put("chapterName", c.chapterName)
                put("stepFundamental", c.stepFundamental)
                put("stepEx1", c.stepEx1)
                put("stepEx2", c.stepEx2)
                put("stepEx3", c.stepEx3)
                put("stepPyqs", c.stepPyqs)
                put("isLearned", c.isLearned)
                put("isRevised", c.isRevised)
                put("isPyqsDone", c.isPyqsDone)
                put("notes", c.notes)
                put("customUrl", c.customUrl)
                put("orderIndex", c.orderIndex)
            })
        }
        root.put("chapters", chaptersArr)

        // Schedule Events
        val eventsList = scheduleDao.getAllEventsList()
        val eventsArr = JSONArray()
        eventsList.forEach { ev ->
            eventsArr.put(JSONObject().apply {
                put("id", ev.id)
                put("title", ev.title)
                put("description", ev.description)
                put("date", ev.date)
                put("time", ev.time)
                put("durationMinutes", ev.durationMinutes)
                put("subjectTag", ev.subjectTag)
                put("chapterTag", ev.chapterTag)
                put("isActive", ev.isActive)
                put("isCompleted", ev.isCompleted)
                put("isGoogleSynced", ev.isGoogleSynced)
                put("googleEventId", ev.googleEventId ?: "")
                put("avatarType", ev.avatarType)
                put("colorTheme", ev.colorTheme)
                put("timestamp", ev.timestamp)
            })
        }
        root.put("scheduleEvents", eventsArr)

        // Flashcards
        if (flashcardDao != null) {
            val decksList = flashcardDao.getAllDecksList()
            val decksArr = JSONArray()
            decksList.forEach { d ->
                decksArr.put(JSONObject().apply {
                    put("id", d.id)
                    put("folderName", d.folderName)
                    put("chapter", d.chapter)
                    put("cardsJson", d.cardsJson)
                    put("cardCount", d.cardCount)
                    put("createdAt", d.createdAt)
                })
            }
            root.put("flashcardDecks", decksArr)
        }

        // Resources
        val resList = resourceDao.getAllResourcesList()
        val resArr = JSONArray()
        resList.forEach { r ->
            resArr.put(JSONObject().apply {
                put("id", r.id)
                put("subject", r.subject)
                put("title", r.title)
                put("url", r.url)
                put("isUserAdded", r.isUserAdded)
            })
        }
        root.put("resources", resArr)

        // Quiz results
        val quizList = quizDao.getAllQuizResultsList()
        val quizArr = JSONArray()
        quizList.forEach { q ->
            quizArr.put(JSONObject().apply {
                put("id", q.id)
                put("chapter", q.chapter)
                put("score", q.score)
                put("totalQuestions", q.totalQuestions)
                put("timestamp", q.timestamp)
            })
        }
        root.put("quizResults", quizArr)

        root.toString(2)
    }

    suspend fun importDatabaseFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Restore economy if present
            if (root.has("economy")) {
                val econObj = root.getJSONObject("economy")
                val restoredEconomy = UserEconomy(
                    id = 1,
                    coins = econObj.optInt("coins", 0),
                    selectedSpecies = econObj.optString("selectedSpecies", "OAK"),
                    unlockedItems = econObj.optString("unlockedItems", "OAK"),
                    demoModeEnabled = econObj.optBoolean("demoModeEnabled", false),
                    activeAmbientSound = econObj.optString("activeAmbientSound", "NONE"),
                    unlockedPets = econObj.optString("unlockedPets", "DUCK"),
                    equippedPet = econObj.optString("equippedPet", "DUCK"),
                    unlockedThemes = econObj.optString("unlockedThemes", "DEFAULT"),
                    equippedTheme = econObj.optString("equippedTheme", "DEFAULT"),
                    unlockedFonts = econObj.optString("unlockedFonts", "DEFAULT"),
                    equippedFont = econObj.optString("equippedFont", "DEFAULT"),
                    customBackgroundUri = econObj.optString("customBackgroundUri", "").ifBlank { null }
                )
                economyDao.saveEconomy(restoredEconomy)
            }

            // Restore user profile if present
            if (root.has("userProfile")) {
                val profObj = root.getJSONObject("userProfile")
                val restoredProfile = UserProfileEntity(
                    id = 1,
                    name = profObj.optString("name", ""),
                    hobbies = profObj.optString("hobbies", ""),
                    dailyTargetMinutes = profObj.optInt("dailyTargetMinutes", 120),
                    isOnboarded = profObj.optBoolean("isOnboarded", true),
                    usePixelFont = profObj.optBoolean("usePixelFont", true),
                    currentStreak = profObj.optInt("currentStreak", 0),
                    lastActiveDate = profObj.optString("lastActiveDate", "")
                )
                userProfileDao.saveUserProfile(restoredProfile)
            }

            // Restore Trees
            if (root.has("trees")) {
                val treesArr = root.getJSONArray("trees")
                val list = ArrayList<TreeRecord>(treesArr.length())
                for (i in 0 until treesArr.length()) {
                    val obj = treesArr.getJSONObject(i)
                    list.add(
                        TreeRecord(
                            id = obj.optLong("id", 0L),
                            species = obj.optString("species", "OAK"),
                            status = obj.optString("status", "ALIVE"),
                            subjectTag = obj.optString("subjectTag", "General"),
                            chapterTag = obj.optString("chapterTag", ""),
                            durationSeconds = obj.optInt("durationSeconds", 1800),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            hydrationLevel = obj.optInt("hydrationLevel", 100),
                            lastWateredTimestamp = obj.optLong("lastWateredTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                treeDao.clearAll()
                treeDao.insertTrees(list)
            }

            // Restore Chapters
            if (root.has("chapters")) {
                val chaptersArr = root.getJSONArray("chapters")
                val list = ArrayList<ChapterProgress>(chaptersArr.length())
                for (i in 0 until chaptersArr.length()) {
                    val obj = chaptersArr.getJSONObject(i)
                    list.add(
                        ChapterProgress(
                            id = obj.optString("id"),
                            subject = obj.optString("subject", "General"),
                            chapterName = obj.optString("chapterName", ""),
                            stepFundamental = obj.optBoolean("stepFundamental", false),
                            stepEx1 = obj.optBoolean("stepEx1", false),
                            stepEx2 = obj.optBoolean("stepEx2", false),
                            stepEx3 = obj.optBoolean("stepEx3", false),
                            stepPyqs = obj.optBoolean("stepPyqs", false),
                            isLearned = obj.optBoolean("isLearned", false),
                            isRevised = obj.optBoolean("isRevised", false),
                            isPyqsDone = obj.optBoolean("isPyqsDone", false),
                            notes = obj.optString("notes", ""),
                            customUrl = obj.optString("customUrl", ""),
                            orderIndex = obj.optInt("orderIndex", i)
                        )
                    )
                }
                syllabusDao.clearAllChapters()
                syllabusDao.insertAllChapters(list)
            }

            // Restore Schedule Events
            if (root.has("scheduleEvents")) {
                val eventsArr = root.getJSONArray("scheduleEvents")
                val list = ArrayList<ScheduleEventEntity>(eventsArr.length())
                for (i in 0 until eventsArr.length()) {
                    val obj = eventsArr.getJSONObject(i)
                    list.add(
                        ScheduleEventEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", "Event"),
                            description = obj.optString("description", ""),
                            date = obj.optString("date", ""),
                            time = obj.optString("time", "9:00 AM"),
                            durationMinutes = obj.optInt("durationMinutes", 30),
                            subjectTag = obj.optString("subjectTag", "General"),
                            chapterTag = obj.optString("chapterTag", ""),
                            isActive = obj.optBoolean("isActive", false),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isGoogleSynced = obj.optBoolean("isGoogleSynced", false),
                            googleEventId = obj.optString("googleEventId", "").ifBlank { null },
                            avatarType = obj.optString("avatarType", "SOLO"),
                            colorTheme = obj.optString("colorTheme", "PASTEL"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                scheduleDao.clearAllEvents()
                scheduleDao.insertEvents(list)
            }

            // Restore Flashcards
            if (flashcardDao != null && root.has("flashcardDecks")) {
                val decksArr = root.getJSONArray("flashcardDecks")
                val list = ArrayList<SavedFlashcardDeckEntity>(decksArr.length())
                for (i in 0 until decksArr.length()) {
                    val obj = decksArr.getJSONObject(i)
                    list.add(
                        SavedFlashcardDeckEntity(
                            id = obj.optLong("id", 0L),
                            folderName = obj.optString("folderName", "Deck"),
                            chapter = obj.optString("chapter", "General"),
                            cardsJson = obj.optString("cardsJson", "{}"),
                            cardCount = obj.optInt("cardCount", 0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                flashcardDao.clearAllDecks()
                flashcardDao.insertDecks(list)
            }

            // Restore Resources
            if (root.has("resources")) {
                val resArr = root.getJSONArray("resources")
                val list = ArrayList<ResourceEntity>(resArr.length())
                for (i in 0 until resArr.length()) {
                    val obj = resArr.getJSONObject(i)
                    list.add(
                        ResourceEntity(
                            id = obj.optLong("id", 0L),
                            subject = obj.optString("subject", "General"),
                            title = obj.optString("title", "Resource"),
                            url = obj.optString("url", ""),
                            isUserAdded = obj.optBoolean("isUserAdded", false)
                        )
                    )
                }
                resourceDao.clearAll()
                resourceDao.insertResources(list)
            }

            // Restore Quiz Results
            if (root.has("quizResults")) {
                val quizArr = root.getJSONArray("quizResults")
                val list = ArrayList<QuizResultEntity>(quizArr.length())
                for (i in 0 until quizArr.length()) {
                    val obj = quizArr.getJSONObject(i)
                    list.add(
                        QuizResultEntity(
                            id = obj.optLong("id", 0L),
                            chapter = obj.optString("chapter", "General"),
                            score = obj.optInt("score", 0),
                            totalQuestions = obj.optInt("totalQuestions", 5),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                quizDao.clearAll()
                quizDao.insertQuizResults(list)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
