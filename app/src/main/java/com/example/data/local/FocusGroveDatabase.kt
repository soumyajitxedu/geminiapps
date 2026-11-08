package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.InitialSyllabusData
import com.example.data.model.ChapterProgress
import com.example.data.model.QuizResultEntity
import com.example.data.model.TreeRecord
import com.example.data.model.UserEconomy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.ResourceEntity
import com.example.data.model.ScheduleEventEntity
import com.example.data.model.UserProfileEntity

import com.example.data.model.SavedFlashcardDeckEntity

@Database(
    entities = [
        TreeRecord::class,
        ChapterProgress::class,
        UserEconomy::class,
        QuizResultEntity::class,
        ScheduleEventEntity::class,
        ResourceEntity::class,
        UserProfileEntity::class,
        SavedFlashcardDeckEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class FocusGroveDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun economyDao(): EconomyDao
    abstract fun quizDao(): QuizDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun resourceDao(): ResourceDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: FocusGroveDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FocusGroveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusGroveDatabase::class.java,
                    "focus_grove_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Populate initial syllabus chapters
                    database.syllabusDao().insertInitialChapters(
                        InitialSyllabusData.createInitialChaptersList()
                    )
                    // Populate default economy
                    database.economyDao().saveEconomy(
                        UserEconomy(
                            id = 1,
                            coins = 0,
                            selectedSpecies = "OAK",
                            unlockedItems = "OAK",
                            demoModeEnabled = false,
                            activeAmbientSound = "NONE",
                            unlockedPets = "DUCK",
                            equippedPet = "DUCK",
                            unlockedThemes = "DEFAULT",
                            equippedTheme = "DEFAULT",
                            unlockedFonts = "DEFAULT",
                            equippedFont = "DEFAULT",
                            customBackgroundUri = null
                        )
                    )
                }
            }
        }
    }
}
