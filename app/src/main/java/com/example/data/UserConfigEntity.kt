package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfigEntity(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val masterPasswordHash: String,
    val masterPasswordSalt: String,
    val pinCode: String? = null, // Store 4-digit PIN
    val useBiometrics: Boolean = false,
    val useAutoLock: Boolean = true,
    val isDarkTheme: Boolean = true, // Default to modern secure dark theme
    val languageCode: String = "fa", // "fa" or "en"
    val themeName: String = "BENTO_SLATE", // "BENTO_SLATE", "ROYAL_AMETHYST", "EMERALD_FOREST", "SOLAR_AMBER"
    val fontScale: Float = 1.0f // 1.0f (Normal), 1.25f (Medium), 1.5f (Large)
)
