package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChapterProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM chapters ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<ChapterProgress>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialChapters(chapters: List<ChapterProgress>)

    @Query("UPDATE chapters SET isCompleted = :completed WHERE id = :chapterId")
    suspend fun setChapterCompletion(chapterId: String, completed: Boolean)

    @Query("SELECT COUNT(*) FROM chapters WHERE isCompleted = 1")
    fun getCompletedChaptersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters")
    fun getTotalChaptersCount(): Flow<Int>
}
