package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ShopManager handles coins, unlocked themes, fonts, pets, and daily pomodoros
 * using Android SharedPreferences exclusively (preserving the existing Room database schema).
 */
class ShopManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("shop_economy_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COINS = "key_coins"
        private const val KEY_UNLOCKED_THEMES = "key_unlocked_themes"
        private const val KEY_EQUIPPED_THEME = "key_equipped_theme"
        private const val KEY_UNLOCKED_FONTS = "key_unlocked_fonts"
        private const val KEY_EQUIPPED_FONT = "key_equipped_font"
        private const val KEY_UNLOCKED_PETS = "key_unlocked_pets"
        private const val KEY_EQUIPPED_PET = "key_equipped_pet"
        private const val KEY_POMODORO_COUNT = "key_pomodoro_count"
        private const val KEY_POMODORO_DATE = "key_pomodoro_date"

        @Volatile
        private var INSTANCE: ShopManager? = null

        fun getInstance(context: Context): ShopManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShopManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _coins = MutableStateFlow(prefs.getInt(KEY_COINS, 500))
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _unlockedThemes = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_THEMES, setOf("NATURE")) ?: setOf("NATURE")
    )
    val unlockedThemes: StateFlow<Set<String>> = _unlockedThemes.asStateFlow()

    private val _equippedTheme = MutableStateFlow(
        prefs.getString(KEY_EQUIPPED_THEME, "NATURE") ?: "NATURE"
    )
    val equippedTheme: StateFlow<String> = _equippedTheme.asStateFlow()

    private val _unlockedFonts = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_FONTS, setOf("PIXEL")) ?: setOf("PIXEL")
    )
    val unlockedFonts: StateFlow<Set<String>> = _unlockedFonts.asStateFlow()

    private val _equippedFont = MutableStateFlow(
        prefs.getString(KEY_EQUIPPED_FONT, "PIXEL") ?: "PIXEL"
    )
    val equippedFont: StateFlow<String> = _equippedFont.asStateFlow()

    private val _unlockedPets = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_PETS, setOf("DUCK")) ?: setOf("DUCK")
    )
    val unlockedPets: StateFlow<Set<String>> = _unlockedPets.asStateFlow()

    private val _equippedPet = MutableStateFlow(
        prefs.getString(KEY_EQUIPPED_PET, "DUCK") ?: "DUCK"
    )
    val equippedPet: StateFlow<String> = _equippedPet.asStateFlow()

    private val _dailyPomodoroCount = MutableStateFlow(getTodayPomodoroCount())
    val dailyPomodoroCount: StateFlow<Int> = _dailyPomodoroCount.asStateFlow()

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getTodayPomodoroCount(): Int {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_POMODORO_DATE, "")
        return if (savedDate == today) {
            prefs.getInt(KEY_POMODORO_COUNT, 0)
        } else {
            0
        }
    }

    fun incrementDailyPomodoroCount() {
        val today = getTodayDateString()
        val current = getTodayPomodoroCount() + 1
        prefs.edit()
            .putString(KEY_POMODORO_DATE, today)
            .putInt(KEY_POMODORO_COUNT, current)
            .apply()
        _dailyPomodoroCount.value = current
    }

    fun addCoins(amount: Int) {
        val newBalance = _coins.value + amount
        prefs.edit().putInt(KEY_COINS, newBalance).apply()
        _coins.value = newBalance
    }

    fun spendCoins(amount: Int): Boolean {
        if (_coins.value >= amount) {
            val newBalance = _coins.value - amount
            prefs.edit().putInt(KEY_COINS, newBalance).apply()
            _coins.value = newBalance
            return true
        }
        return false
    }

    fun purchaseTheme(themeId: String, cost: Int): Boolean {
        val id = themeId.uppercase()
        if (_unlockedThemes.value.contains(id)) {
            equipTheme(id)
            return true
        }
        if (spendCoins(cost)) {
            val updated = _unlockedThemes.value + id
            prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, updated).apply()
            _unlockedThemes.value = updated
            equipTheme(id)
            return true
        }
        return false
    }

    fun equipTheme(themeId: String) {
        val id = themeId.uppercase()
        prefs.edit().putString(KEY_EQUIPPED_THEME, id).apply()
        _equippedTheme.value = id
    }

    fun purchaseFont(fontId: String, cost: Int): Boolean {
        val id = fontId.uppercase()
        if (_unlockedFonts.value.contains(id)) {
            equipFont(id)
            return true
        }
        if (spendCoins(cost)) {
            val updated = _unlockedFonts.value + id
            prefs.edit().putStringSet(KEY_UNLOCKED_FONTS, updated).apply()
            _unlockedFonts.value = updated
            equipFont(id)
            return true
        }
        return false
    }

    fun equipFont(fontId: String) {
        val id = fontId.uppercase()
        prefs.edit().putString(KEY_EQUIPPED_FONT, id).apply()
        _equippedFont.value = id
    }

    fun purchasePet(petId: String, cost: Int): Boolean {
        val id = petId.uppercase()
        if (_unlockedPets.value.contains(id)) {
            equipPet(id)
            return true
        }
        if (spendCoins(cost)) {
            val updated = _unlockedPets.value + id
            prefs.edit().putStringSet(KEY_UNLOCKED_PETS, updated).apply()
            _unlockedPets.value = updated
            equipPet(id)
            return true
        }
        return false
    }

    fun equipPet(petId: String) {
        val id = petId.uppercase()
        prefs.edit().putString(KEY_EQUIPPED_PET, id).apply()
        _equippedPet.value = id
    }
}
