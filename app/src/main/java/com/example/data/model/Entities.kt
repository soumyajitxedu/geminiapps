package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TreeSpecies(
    val id: String,
    val displayName: String,
    val cost: Int,
    val description: String
) {
    OAK("OAK", "Classic Oak", 0, "A sturdy, time-tested forest monarch"),
    CHERRY_BLOSSOM("CHERRY_BLOSSOM", "Cherry Blossom", 50, "Delicate pink petals celebrating sweet focus"),
    PINE("PINE", "Alpine Pine", 100, "Evergreen resilience through stormy distractions"),
    BONSAI("BONSAI", "Zen Bonsai", 150, "Meticulous cultivation of deep inner discipline");

    companion object {
        fun fromId(id: String): TreeSpecies = entries.find { it.id == id } ?: OAK
    }
}

enum class TreeStatus {
    ALIVE,
    DEAD
}

enum class FocusTimerMode {
    COUNTDOWN,
    STOPWATCH
}

@Entity(tableName = "trees")
data class TreeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val species: String = TreeSpecies.OAK.id,
    val status: String = TreeStatus.ALIVE.name,
    val subjectTag: String = "General Focus",
    val chapterTag: String = "",
    val durationSeconds: Int = 1800,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class ChapterProgress(
    @PrimaryKey
    val id: String, // e.g. "Physics_Force"
    val subject: String,
    val chapterName: String,
    val stepFundamental: Boolean = false,
    val stepEx1: Boolean = false,
    val stepEx2: Boolean = false,
    val stepEx3: Boolean = false,
    val stepPyqs: Boolean = false,
    val isLearned: Boolean = false,
    val isRevised: Boolean = false,
    val isPyqsDone: Boolean = false,
    val notes: String = "",
    val customUrl: String = "",
    val orderIndex: Int = 0
) {
    val completionFraction: Float
        get() {
            var count = 0
            if (stepFundamental) count++
            if (stepEx1) count++
            if (stepEx2) count++
            if (stepEx3) count++
            if (stepPyqs) count++
            return count / 5f
        }

    val isCompleted: Boolean
        get() = stepFundamental && stepEx1 && stepEx2 && stepEx3 && stepPyqs
}

@Entity(tableName = "economy")
data class UserEconomy(
    @PrimaryKey
    val id: Int = 1,
    val coins: Int = 0,
    val selectedSpecies: String = TreeSpecies.OAK.id,
    val unlockedItems: String = "OAK", // Comma-separated list of unlocked IDs
    val demoModeEnabled: Boolean = false,
    val activeAmbientSound: String = "NONE" // NONE, RAIN, LOFI
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapter: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuizPayload(
    val chapter: String,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String
)

enum class AmbientSound(
    val id: String,
    val displayName: String,
    val cost: Int,
    val description: String
) {
    NONE("NONE", "Silence", 0, "Pure quiet focus"),
    RAIN("RAIN", "Gentle Rain", 30, "Calming raindrops pattering on forest leaves"),
    LOFI("LOFI", "Lo-Fi Beats", 30, "Warm mellow acoustic study pulse");

    companion object {
        fun fromId(id: String): AmbientSound = entries.find { it.id == id } ?: NONE
    }
}

@Entity(tableName = "schedule_events")
data class ScheduleEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // Format: "yyyy-MM-dd", e.g. "2026-10-20"
    val time: String, // e.g. "9:00 AM"
    val durationMinutes: Int = 30,
    val subjectTag: String = "General",
    val chapterTag: String = "",
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val isGoogleSynced: Boolean = false,
    val googleEventId: String? = null,
    val avatarType: String = "GROUP", // GROUP, SOLO, STUDY, MEETING
    val colorTheme: String = "BLUE", // BLUE, PASTEL
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val title: String,
    val url: String,
    val isUserAdded: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val hobbies: String = "",
    val dailyTargetMinutes: Int = 120, // 2 hours
    val isOnboarded: Boolean = false,
    val usePixelFont: Boolean = true,
    val currentStreak: Int = 0,
    val lastActiveDate: String = "" // "yyyy-MM-dd"
)



