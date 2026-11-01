package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedFlashcardDeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM saved_flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<SavedFlashcardDeckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: SavedFlashcardDeckEntity): Long

    @Query("DELETE FROM saved_flashcard_decks WHERE id = :id")
    suspend fun deleteDeckById(id: Long)

    @Query("DELETE FROM saved_flashcard_decks")
    suspend fun clearAllDecks()
}
