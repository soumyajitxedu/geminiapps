package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_results")
    fun getTotalQuizzesTaken(): Flow<Int>

    @Query("SELECT AVG(CAST(score AS FLOAT) / CAST(totalQuestions AS FLOAT) * 100.0) FROM quiz_results")
    fun getAverageQuizScorePercent(): Flow<Float?>

    @Query("DELETE FROM quiz_results")
    suspend fun clearAll()
}
