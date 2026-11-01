package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FocusGroveRepository
import com.example.data.InitialSyllabusData
import com.example.data.local.FocusGroveDatabase
import com.example.data.model.AmbientSound
import com.example.data.model.TreeSpecies
import com.example.data.model.TreeStatus
import com.example.data.model.UserEconomy
import com.example.quiz.QuizParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: FocusGroveDatabase
    private lateinit var repository: FocusGroveRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusGroveDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FocusGroveRepository(
            treeDao = database.treeDao(),
            syllabusDao = database.syllabusDao(),
            economyDao = database.economyDao(),
            quizDao = database.quizDao(),
            scheduleDao = database.scheduleDao(),
            resourceDao = database.resourceDao(),
            userProfileDao = database.userProfileDao(),
            flashcardDao = database.flashcardDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string app_name returns Focus Grove`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Focus Grove", appName)
    }

    @Test
    fun `initial syllabus data contains all 9 ICSE Class 10 subjects and valid chapters`() {
        val subjects = InitialSyllabusData.subjectsWithChapters
        assertEquals(9, subjects.size)
        assertTrue(subjects.containsKey("Physics"))
        assertTrue(subjects.containsKey("Chemistry"))
        assertTrue(subjects.containsKey("Biology"))
        assertTrue(subjects.containsKey("Maths"))
        assertTrue(subjects.containsKey("History"))
        assertTrue(subjects.containsKey("Geography"))
        assertTrue(subjects.containsKey("Literature"))
        assertTrue(subjects.containsKey("L2"))
        assertTrue(subjects.containsKey("Computer Applications"))

        val chapters = InitialSyllabusData.createInitialChaptersList()
        assertTrue("Total chapters should be greater than 100", chapters.size >= 100)
    }

    @Test
    fun `successful session awards 10 coins and saves alive tree`() = runTest {
        repository.checkAndInitializeData()

        val initialCoins = repository.economy.first()?.coins ?: 0
        assertEquals(0, initialCoins)

        val treeId = repository.recordSession(
            species = TreeSpecies.OAK,
            status = TreeStatus.ALIVE,
            subjectTag = "Physics",
            durationSeconds = 1800
        )
        assertTrue(treeId > 0)

        val updatedCoins = repository.economy.first()?.coins ?: 0
        assertEquals(10, updatedCoins)

        val aliveCount = repository.aliveTreesCount.first()
        val deadCount = repository.deadTreesCount.first()
        assertEquals(1, aliveCount)
        assertEquals(0, deadCount)
    }

    @Test
    fun `failed session leaves coins unchanged and saves dead tree`() = runTest {
        repository.checkAndInitializeData()

        val initialCoins = repository.economy.first()?.coins ?: 0

        val treeId = repository.recordSession(
            species = TreeSpecies.OAK,
            status = TreeStatus.DEAD,
            subjectTag = "Chemistry",
            durationSeconds = 300
        )
        assertTrue(treeId > 0)

        val coinsAfterFail = repository.economy.first()?.coins ?: 0
        assertEquals(initialCoins, coinsAfterFail)

        val aliveCount = repository.aliveTreesCount.first()
        val deadCount = repository.deadTreesCount.first()
        assertEquals(0, aliveCount)
        assertEquals(1, deadCount)
    }

    @Test
    fun `chapter completion toggling works reactively`() = runTest {
        repository.checkAndInitializeData()

        val allChapters = repository.allChapters.first()
        assertTrue(allChapters.isNotEmpty())

        val firstChapter = allChapters.first()
        assertFalse(firstChapter.isCompleted)

        repository.setChapterCompletion(firstChapter.id, true)

        val completedCount = repository.completedChaptersCount.first()
        assertEquals(1, completedCount)
    }

    @Test
    fun `purchasing tree skin in shop with sufficient coins succeeds`() = runTest {
        repository.checkAndInitializeData()

        database.economyDao().addCoins(60)
        var econ = repository.economy.first()
        assertEquals(60, econ?.coins)

        val purchaseSuccess = repository.purchaseShopItem(
            itemId = TreeSpecies.CHERRY_BLOSSOM.id,
            cost = TreeSpecies.CHERRY_BLOSSOM.cost,
            isTreeSpecies = true
        )
        assertTrue(purchaseSuccess)

        econ = repository.economy.first()
        assertEquals(10, econ?.coins)
        assertTrue(econ?.unlockedItems?.contains(TreeSpecies.CHERRY_BLOSSOM.id) == true)
        assertEquals(TreeSpecies.CHERRY_BLOSSOM.id, econ?.selectedSpecies)
    }

    @Test
    fun `quiz parser parses strict schema json correctly`() {
        val sampleJson = """
        {
          "chapter": "Union Parliament",
          "questions": [
            {
              "question": "What is the maximum strength of the Lok Sabha?",
              "options": ["545", "550", "552", "560"],
              "answer": "552",
              "explanation": "The maximum strength is 552. Option 545 is the current strength."
            }
          ]
        }
        """.trimIndent()

        val parseResult = QuizParser.parse(sampleJson)
        assertTrue(parseResult.isSuccess)

        val payload = parseResult.getOrThrow()
        assertEquals("Union Parliament", payload.chapter)
        assertEquals(1, payload.questions.size)

        val q = payload.questions[0]
        assertEquals("What is the maximum strength of the Lok Sabha?", q.question)
        assertEquals(4, q.options.size)
        assertEquals("552", q.answer)
        assertTrue(q.explanation.contains("maximum strength is 552"))
    }

    @Test
    fun `saving quiz result persists in database reactively`() = runTest {
        val initialCount = repository.totalQuizzesTaken.first()
        assertEquals(0, initialCount)

        val insertId = repository.saveQuizResult(
            chapter = "Union Parliament",
            score = 8,
            totalQuestions = 10
        )
        assertTrue(insertId > 0)

        val updatedCount = repository.totalQuizzesTaken.first()
        assertEquals(1, updatedCount)

        val results = repository.allQuizResults.first()
        assertEquals(1, results.size)
        assertEquals("Union Parliament", results[0].chapter)
        assertEquals(8, results[0].score)
        assertEquals(10, results[0].totalQuestions)
    }

    @Test
    fun `schedule event insertion and completion toggling works properly`() = runTest {
        val event = com.example.data.model.ScheduleEventEntity(
            title = "Physics - Force",
            description = "Deep study block on Newton's laws",
            date = "2026-10-20",
            time = "9:00 AM",
            durationMinutes = 45,
            subjectTag = "Physics",
            chapterTag = "Force",
            isActive = true,
            isCompleted = false
        )
        val id = repository.addScheduleEvent(event)
        assertTrue(id > 0)

        val eventsForDate = repository.getScheduleEventsForDate("2026-10-20").first()
        assertEquals(1, eventsForDate.size)
        assertEquals("Physics - Force", eventsForDate[0].title)
        assertFalse(eventsForDate[0].isCompleted)

        // Toggle complete
        repository.toggleEventCompletion(id, true)
        val updatedEvents = repository.getScheduleEventsForDate("2026-10-20").first()
        assertTrue(updatedEvents[0].isCompleted)
    }

    @Test
    fun `chapter checkpoints - 5 steps for Maths, 3 steps for other subjects`() = runTest {
        repository.checkAndInitializeData()
        val chapters = repository.allChapters.first()
        assertTrue(chapters.isNotEmpty())

        val mathsChapter = chapters.first { it.isMaths }
        val physicsChapter = chapters.first { !it.isMaths }

        // Test Maths 5-step progress
        assertEquals(0f, mathsChapter.completionFraction)
        repository.setChapterFundamental(mathsChapter.id, true)
        var updatedMaths = repository.allChapters.first().first { it.id == mathsChapter.id }
        assertEquals(0.2f, updatedMaths.completionFraction, 0.01f)

        repository.setChapterEx1(mathsChapter.id, true)
        updatedMaths = repository.allChapters.first().first { it.id == mathsChapter.id }
        assertEquals(0.4f, updatedMaths.completionFraction, 0.01f)

        repository.setChapterEx2(mathsChapter.id, true)
        updatedMaths = repository.allChapters.first().first { it.id == mathsChapter.id }
        assertEquals(0.6f, updatedMaths.completionFraction, 0.01f)

        repository.setChapterEx3(mathsChapter.id, true)
        updatedMaths = repository.allChapters.first().first { it.id == mathsChapter.id }
        assertEquals(0.8f, updatedMaths.completionFraction, 0.01f)

        repository.setChapterPyqs(mathsChapter.id, true)
        updatedMaths = repository.allChapters.first().first { it.id == mathsChapter.id }
        assertEquals(1.0f, updatedMaths.completionFraction, 0.01f)

        // Test Non-Maths 3-step progress (Learned / Revised / PYQs)
        assertEquals(0f, physicsChapter.completionFraction)
        repository.setChapterFundamental(physicsChapter.id, true)
        var updatedPhysics = repository.allChapters.first().first { it.id == physicsChapter.id }
        assertEquals(0.333f, updatedPhysics.completionFraction, 0.01f)

        repository.setChapterEx1(physicsChapter.id, true)
        updatedPhysics = repository.allChapters.first().first { it.id == physicsChapter.id }
        assertEquals(0.666f, updatedPhysics.completionFraction, 0.01f)

        repository.setChapterPyqs(physicsChapter.id, true)
        updatedPhysics = repository.allChapters.first().first { it.id == physicsChapter.id }
        assertEquals(1.0f, updatedPhysics.completionFraction, 0.01f)

        // Notes and Custom URL
        repository.updateChapterNotesAndUrl(physicsChapter.id, "**Key formulas**:\nF = ma", "https://drive.google.com/test-notes")
        updatedPhysics = repository.allChapters.first().first { it.id == physicsChapter.id }
        assertEquals("**Key formulas**:\nF = ma", updatedPhysics.notes)
        assertEquals("https://drive.google.com/test-notes", updatedPhysics.customUrl)
    }

    @Test
    fun `resetAllData wipes user profile, trees, coins, and checkpoints`() = runTest {
        repository.checkAndInitializeData()
        repository.saveUserProfile(
            com.example.data.model.UserProfileEntity(
                name = "Soumyajit",
                hobbies = "Coding & Music",
                dailyTargetMinutes = 120,
                isOnboarded = true
            )
        )
        repository.recordSession(
            species = TreeSpecies.OAK,
            status = TreeStatus.ALIVE,
            subjectTag = "Physics",
            durationSeconds = 1800
        )
        val profileBefore = repository.userProfile.first()
        assertTrue(profileBefore != null && profileBefore.isOnboarded)

        // Perform full reset
        repository.resetAllData()

        val profileAfter = repository.userProfile.first()
        assertEquals(null, profileAfter) // Wiped completely, back to onboarding!
        val treesCount = repository.totalTreesCount.first()
        assertEquals(0, treesCount)
        val coins = repository.economy.first()?.coins ?: 0
        assertEquals(0, coins)
    }

    @Test
    fun `user profile saving and font preference toggling works`() = runTest {
        val initialProfile = repository.userProfile.first()
        // If not initialized, can be null or default
        val profile = com.example.data.model.UserProfileEntity(
            id = 1,
            name = "Aarav",
            hobbies = "Chess & Guitar",
            dailyTargetMinutes = 180,
            isOnboarded = true,
            usePixelFont = true
        )
        repository.saveUserProfile(profile)

        val saved = repository.userProfile.first()
        assertEquals("Aarav", saved?.name)
        assertEquals("Chess & Guitar", saved?.hobbies)
        assertEquals(180, saved?.dailyTargetMinutes)
        assertTrue(saved?.isOnboarded == true)
        assertTrue(saved?.usePixelFont == true)

        // Toggle font to System Default
        repository.setFontPreference(false)
        val afterFontToggle = repository.userProfile.first()
        assertFalse(afterFontToggle?.usePixelFont == true)
    }

    @Test
    fun `study inventory custom resource add and delete works`() = runTest {
        val customRes = com.example.data.model.ResourceEntity(
            subject = "Physics",
            title = "Personal Handwritten Mechanics Notes",
            url = "https://drive.google.com/personal-notes",
            isUserAdded = true
        )
        val id = repository.addCustomResource(customRes)
        assertTrue(id > 0)

        val allRes = repository.allResources.first()
        val found = allRes.firstOrNull { it.id == id }
        assertTrue(found != null)
        assertEquals("Personal Handwritten Mechanics Notes", found?.title)
        assertTrue(found?.isUserAdded == true)

        // Delete custom resource
        repository.deleteResourceById(id)
        val afterDelete = repository.allResources.first()
        assertFalse(afterDelete.any { it.id == id })
    }

    @Test
    fun `StudyToolParser parses valid flashcards and rejects invalid json gracefully`() {
        val validJson = """
            ```json
            {
              "chapter": "Maths - Matrices",
              "cards": [
                {"front": "What is an identity matrix?", "back": "A square matrix with 1s on main diagonal and 0s elsewhere."},
                {"front": "Can two matrices of order 2x3 and 2x3 be multiplied?", "back": "No, number of columns in A must equal rows in B."}
              ]
            }
            ```
        """.trimIndent()

        val deck = com.example.data.model.StudyToolParser.parseFlashcards(validJson)
        assertEquals("Maths - Matrices", deck.chapter)
        assertEquals(2, deck.cards.size)
        assertEquals("What is an identity matrix?", deck.cards[0].front)

        // Invalid JSON must throw exception cleanly without crash
        try {
            com.example.data.model.StudyToolParser.parseFlashcards("invalid raw text {corrupted")
            org.junit.Assert.fail("Expected exception for malformed JSON")
        } catch (_: Exception) {
            // Success: safely caught
        }
    }

    @Test
    fun `StudyToolParser parses hierarchical mind map correctly`() {
        val mindMapJson = """
            {
              "chapter": "Chemistry - Mole Concept",
              "root": {
                "title": "MOLE CONCEPT",
                "children": [
                  {
                    "title": "Avogadro's Number",
                    "children": [
                      {"title": "6.022 x 10^23 particles", "children": []}
                    ]
                  },
                  {
                    "title": "Molar Volume",
                    "children": [
                      {"title": "22.4 L at STP", "children": []}
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mapData = com.example.data.model.StudyToolParser.parseMindMap(mindMapJson)
        assertEquals("Chemistry - Mole Concept", mapData.chapter)
        assertEquals("MOLE CONCEPT", mapData.root.title)
        assertEquals(2, mapData.root.children.size)
        assertEquals("Avogadro's Number", mapData.root.children[0].title)
    }

    @Test
    fun `MainViewModel preserves running timer state on backgrounding`() = runTest {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.MainViewModel(app)
        viewModel.setDemoMode(true) // 10 second timer
        viewModel.startTimer()

        assertEquals(com.example.ui.TimerStatus.RUNNING, viewModel.timerStatus.value)

        // Simulate app minimized / screen locked (ON_STOP)
        viewModel.onAppBackgrounded()

        // Timer must continue running! Must NOT be cancelled or FAILED!
        assertEquals(com.example.ui.TimerStatus.RUNNING, viewModel.timerStatus.value)
        assertEquals(null, viewModel.lastSessionResult.value)

        // Clean up
        viewModel.resetTimerToIdle()
        assertEquals(com.example.ui.TimerStatus.IDLE, viewModel.timerStatus.value)
    }

    @Test
    fun `tree watering restores hydration to 100`() = runTest {
        repository.checkAndInitializeData()
        val treeId = repository.recordSession(
            durationSeconds = 1800,
            subjectTag = "Mathematics",
            chapterTag = "Quadratic Equations",
            status = TreeStatus.ALIVE,
            species = TreeSpecies.GOLDEN_OAK
        )
        // Simulate dehydration
        database.treeDao().updateTreeHydration(treeId, 35, System.currentTimeMillis())
        val treeBefore = repository.allTrees.first().find { it.id == treeId }
        assertEquals(35, treeBefore?.hydrationLevel)

        // Water tree
        repository.waterTree(treeId)
        val treeAfter = repository.allTrees.first().find { it.id == treeId }
        assertEquals(100, treeAfter?.hydrationLevel)
    }

    @Test
    fun `pet purchase and equip updates economy`() = runTest {
        repository.checkAndInitializeData()
        repository.addCoins(500)

        val success = repository.purchasePet(com.example.data.model.PetSpecies.RABBIT)
        assertTrue(success)

        val economy = repository.economy.first()
        assertTrue(economy?.unlockedPets?.contains("RABBIT") == true)

        repository.equipPet(com.example.data.model.PetSpecies.RABBIT)
        val equippedEconomy = repository.economy.first()
        assertEquals("RABBIT", equippedEconomy?.equippedPet)
    }

    @Test
    fun `save and load flashcard decks in repository`() = runTest {
        val deckId = repository.saveFlashcardDeck(
            folderName = "Physics - Force Test",
            chapter = "Physics - Force",
            cardsJson = """{"chapter": "Physics - Force", "cards": [{"front": "Q1", "back": "A1"}]}""",
            cardCount = 1
        )
        assertTrue(deckId > 0)

        val decks = repository.allSavedDecks.first()
        assertEquals(1, decks.size)
        assertEquals("Physics - Force Test", decks[0].folderName)
        assertEquals(1, decks[0].cardCount)

        repository.deleteFlashcardDeck(deckId)
        val decksAfter = repository.allSavedDecks.first()
        assertTrue(decksAfter.isEmpty())
    }
}
