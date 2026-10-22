package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChapterProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM chapters ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<ChapterProgress>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialChapters(chapters: List<ChapterProgress>)

    @Update
    suspend fun updateChapter(chapter: ChapterProgress)

    @Query("UPDATE chapters SET stepFundamental = :done WHERE id = :chapterId")
    suspend fun setFundamental(chapterId: String, done: Boolean)

    @Query("UPDATE chapters SET stepEx1 = :done WHERE id = :chapterId")
    suspend fun setEx1(chapterId: String, done: Boolean)

    @Query("UPDATE chapters SET stepEx2 = :done WHERE id = :chapterId")
    suspend fun setEx2(chapterId: String, done: Boolean)

    @Query("UPDATE chapters SET stepEx3 = :done WHERE id = :chapterId")
    suspend fun setEx3(chapterId: String, done: Boolean)

    @Query("UPDATE chapters SET stepPyqs = :done WHERE id = :chapterId")
    suspend fun setPyqs(chapterId: String, done: Boolean)

    @Query("UPDATE chapters SET isLearned = :learned WHERE id = :chapterId")
    suspend fun setLearned(chapterId: String, learned: Boolean)

    @Query("UPDATE chapters SET isRevised = :revised WHERE id = :chapterId")
    suspend fun setRevised(chapterId: String, revised: Boolean)

    @Query("UPDATE chapters SET isPyqsDone = :pyqsDone WHERE id = :chapterId")
    suspend fun setPyqsDone(chapterId: String, pyqsDone: Boolean)

    @Query("UPDATE chapters SET stepFundamental = :completed, stepEx1 = :completed, stepEx2 = :completed, stepEx3 = :completed, stepPyqs = :completed, isLearned = :completed, isRevised = :completed, isPyqsDone = :completed WHERE id = :chapterId")
    suspend fun setChapterCompletion(chapterId: String, completed: Boolean)

    @Query("UPDATE chapters SET notes = :notes, customUrl = :customUrl WHERE id = :chapterId")
    suspend fun updateNotesAndUrl(chapterId: String, notes: String, customUrl: String)

    @Query("SELECT COUNT(*) FROM chapters WHERE stepFundamental = 1 AND stepEx1 = 1 AND stepEx2 = 1 AND stepEx3 = 1 AND stepPyqs = 1")
    fun getCompletedChaptersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters")
    fun getTotalChaptersCount(): Flow<Int>
}
