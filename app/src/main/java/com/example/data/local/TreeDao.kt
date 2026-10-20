package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TreeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Query("SELECT * FROM trees ORDER BY timestamp DESC")
    fun getAllTrees(): Flow<List<TreeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTree(tree: TreeRecord): Long

    @Query("SELECT COUNT(*) FROM trees")
    fun getTotalTreesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trees WHERE status = 'ALIVE'")
    fun getAliveTreesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trees WHERE status = 'DEAD'")
    fun getDeadTreesCount(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM trees WHERE status = 'ALIVE'")
    fun getTotalFocusedSeconds(): Flow<Int?>

    @Query("DELETE FROM trees")
    suspend fun clearAll()
}
