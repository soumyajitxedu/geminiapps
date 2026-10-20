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
@Config(sdk = [34])
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
            scheduleDao = database.scheduleDao()
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
}
