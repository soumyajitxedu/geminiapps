package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserEconomy
import kotlinx.coroutines.flow.Flow

@Dao
interface EconomyDao {
    @Query("SELECT * FROM economy WHERE id = 1")
    fun getEconomy(): Flow<UserEconomy?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEconomy(economy: UserEconomy)

    @Query("UPDATE economy SET coins = coins + :earned WHERE id = 1")
    suspend fun addCoins(earned: Int)

    @Query("UPDATE economy SET coins = :newAmount WHERE id = 1")
    suspend fun setCoins(newAmount: Int)

    @Query("UPDATE economy SET selectedSpecies = :species WHERE id = 1")
    suspend fun setSelectedSpecies(species: String)

    @Query("UPDATE economy SET unlockedItems = :unlocked WHERE id = 1")
    suspend fun setUnlockedItems(unlocked: String)

    @Query("UPDATE economy SET demoModeEnabled = :enabled WHERE id = 1")
    suspend fun setDemoMode(enabled: Boolean)

    @Query("UPDATE economy SET activeAmbientSound = :sound WHERE id = 1")
    suspend fun setActiveAmbientSound(sound: String)

    @Query("UPDATE economy SET unlockedPets = :pets WHERE id = 1")
    suspend fun setUnlockedPets(pets: String)

    @Query("UPDATE economy SET equippedPet = :pet WHERE id = 1")
    suspend fun setEquippedPet(pet: String)

    @Query("UPDATE economy SET unlockedThemes = :themes WHERE id = 1")
    suspend fun setUnlockedThemes(themes: String)

    @Query("UPDATE economy SET equippedTheme = :theme WHERE id = 1")
    suspend fun setEquippedTheme(theme: String)

    @Query("UPDATE economy SET unlockedFonts = :fonts WHERE id = 1")
    suspend fun setUnlockedFonts(fonts: String)

    @Query("UPDATE economy SET equippedFont = :font WHERE id = 1")
    suspend fun setEquippedFont(font: String)

    @Query("UPDATE economy SET customBackgroundUri = :uri WHERE id = 1")
    suspend fun setCustomBackgroundUri(uri: String?)

    @Query("SELECT * FROM economy WHERE id = 1")
    suspend fun getEconomyOnce(): UserEconomy?

    @Query("DELETE FROM economy")
    suspend fun clearEconomy()
}
