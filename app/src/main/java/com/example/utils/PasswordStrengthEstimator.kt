package com.example.utils

import androidx.compose.ui.graphics.Color

enum class PasswordStrength(
    val score: Int,
    val label: String,
    val color: Color
) {
    EMPTY(0, "بدون رمز", Color(0xFF9E9E9E)),
    WEAK(1, "ضعیف", Color(0xFFE53935)),        // Red
    MEDIUM(2, "متوسط", Color(0xFFFDD835)),     // Yellow/Amber
    STRONG(3, "قوی", Color(0xFF43A047)),        // Green
    VERY_STRONG(4, "بسیار قوی", Color(0xFF00ACC1)) // Cyan/Blue
}

object PasswordStrengthEstimator {
    fun estimate(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY
        
        var score = 0
        val length = password.length
        
        // Criteria 1: Length
        if (length >= 12) {
            score += 2
        } else if (length >= 8) {
            score += 1
        }
        
        // Content checks
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        
        var typesCount = 0
        if (hasLowercase) typesCount++
        if (hasUppercase) typesCount++
        if (hasDigit) typesCount++
        if (hasSymbol) typesCount++
        
        // Criteria 2: Character diversity
        if (typesCount >= 4) {
            score += 2
        } else if (typesCount >= 2) {
            score += 1
        }
        
        // Adjustment based on minimal conditions
        if (length < 6) return PasswordStrength.WEAK
        
        return when {
            score >= 4 && length >= 12 && typesCount >= 4 -> PasswordStrength.VERY_STRONG
            score >= 3 && length >= 8 && typesCount >= 3 -> PasswordStrength.STRONG
            score >= 2 && length >= 6 && typesCount >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
