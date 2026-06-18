package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AmnDatabase
import com.example.data.PasswordEntity
import com.example.data.UserConfigEntity
import com.example.repository.AmnRepository
import com.example.utils.CryptoUtils
import com.example.utils.PasswordStrength
import com.example.utils.PasswordStrengthEstimator
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom
import android.util.Base64

// Sort modes enum
enum class SortMode {
    NEWEST_TO_OLDEST,
    OLDEST_TO_NEWEST
}

class AmnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AmnRepository
    
    // Core state flows from Room
    val userConfigFlow: StateFlow<UserConfigEntity?>
    val activePasswordsFlow: StateFlow<List<PasswordEntity>>
    val deletedPasswordsFlow: StateFlow<List<PasswordEntity>>

    // UI state parameters
    var isUserAuthenticated by mutableStateOf(false)
    var isSplashActive by mutableStateOf(true)
    var isDarkTheme by mutableStateOf(true)
    var currentLanguage by mutableStateOf("fa")
    var currentThemeName by mutableStateOf("BENTO_SLATE")
    var currentFontScale by mutableStateOf(1.0f)

    // Search and Sort states
    val searchQuery = MutableStateFlow("")
    val sortMode = MutableStateFlow(SortMode.NEWEST_TO_OLDEST)
    val filterFavoritesOnly = MutableStateFlow(false)

    // Computed reactive password list
    val displayedPasswords: StateFlow<List<PasswordEntity>>

    // Clipboard and Snackbar Event flow
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Activity tracking for auto-lock
    private var lastActivityTime: Long = System.currentTimeMillis()
    private var autoLockJob: Job? = null

    // Moshi for AES GCM Backup structures
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val passwordListType = Types.newParameterizedType(List::class.java, BackupPasswordItem::class.java)
    private val jsonAdapter = moshi.adapter<List<BackupPasswordItem>>(passwordListType)

    init {
        val db = AmnDatabase.getDatabase(application)
        repository = AmnRepository(db.passwordDao(), db.userConfigDao())

        userConfigFlow = repository.userConfigFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

        activePasswordsFlow = repository.activePasswords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

        deletedPasswordsFlow = repository.deletedPasswords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

        // Combine filter logic: Search + Sort + Favorites
        displayedPasswords = combine(
            activePasswordsFlow,
            searchQuery,
            sortMode,
            filterFavoritesOnly
        ) { passwords, query, sort, favOnly ->
            var filtered = passwords
            if (favOnly) {
                filtered = filtered.filter { it.isFavorite }
            }
            if (query.isNotEmpty()) {
                filtered = filtered.filter {
                    it.siteName.contains(query, ignoreCase = true) || 
                    it.username.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            }
            when (sort) {
                SortMode.NEWEST_TO_OLDEST -> filtered.sortedByDescending { it.createdAt }
                SortMode.OLDEST_TO_NEWEST -> filtered.sortedBy { it.createdAt }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Launch initialization operations
        viewModelScope.launch {
            // 1. Auto-cleanup bin (purge items soft-deleted more than 30 days ago)
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            repository.purgeOldDeleted(thirtyDaysAgo)

            // 2. Fetch theme preferences
            val config = repository.getUserConfig()
            if (config != null) {
                isDarkTheme = config.isDarkTheme
                currentLanguage = config.languageCode
                currentThemeName = config.themeName
                currentFontScale = config.fontScale
            }

            // 3. Keep splash active for 1.5 seconds exactly
            delay(1500)
            isSplashActive = false

            // 4. Start auto-lock tracking
            startAutoLockTimer()
        }
    }

    // Update system activity interaction timestamp
    fun updateActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    // Auto-lock tracking job (locks if user has been inactive for 5 minutes)
    private fun startAutoLockTimer() {
        autoLockJob?.cancel()
        autoLockJob = viewModelScope.launch {
            while (true) {
                delay(10000) // Check every 10 seconds
                val config = userConfigFlow.value
                if (isUserAuthenticated && config != null && config.useAutoLock) {
                    val idleTime = System.currentTimeMillis() - lastActivityTime
                    val fiveMinutes = 5 * 60 * 1000L
                    if (idleTime >= fiveMinutes) {
                        lockApp()
                        showSnackbar("برنامه بهلیل عدم فعالیت بهمدت ۵ دقیقه قفل شد")
                    }
                }
            }
        }
    }

    fun lockApp() {
        isUserAuthenticated = false
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    // SIGNUP Action
    fun registerUser(username: String, masterPass: String, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || masterPass.length < 8) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val salt = CryptoUtils.generateSalt()
            val hash = CryptoUtils.hashPassword(masterPass, salt)
            
            val newConfig = UserConfigEntity(
                username = username,
                masterPasswordHash = hash,
                masterPasswordSalt = Base64.encodeToString(salt, Base64.NO_WRAP),
                pinCode = null,
                useBiometrics = false,
                useAutoLock = true,
                isDarkTheme = isDarkTheme
            )
            repository.saveUserConfig(newConfig)
            isUserAuthenticated = true
            onResult(true)
            showSnackbar("ثبت نام اولیه با موفقیت انجام شد")
        }
    }

    // Standard Login
    fun loginWithPassword(password: String, onResult: (Boolean) -> Unit) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            val salt = Base64.decode(config.masterPasswordSalt, Base64.NO_WRAP)
            val computedHash = CryptoUtils.hashPassword(password, salt)
            if (computedHash == config.masterPasswordHash) {
                isUserAuthenticated = true
                updateActivity()
                onResult(true)
                showSnackbar("ورود با موفقیت انجام شد")
            } else {
                onResult(false)
            }
        }
    }

    // PIN Login
    fun loginWithPin(pin: String, onResult: (Boolean) -> Unit) {
        val config = userConfigFlow.value ?: return
        if (config.pinCode == pin) {
            isUserAuthenticated = true
            updateActivity()
            onResult(true)
            showSnackbar("ورود سریع با موفقیت انجام شد")
        } else {
            onResult(false)
        }
    }

    // Biometric Login (returns true if authenticated successfully)
    fun authenticateBiometrics() {
        isUserAuthenticated = true
        updateActivity()
        showSnackbar("ورود با اثر انگشت با موفقیت انجام شد")
    }

    // Add or Edit Password Entry
    fun saveOrUpdatePassword(
        id: Int = 0,
        siteName: String,
        username: String,
        passwordRaw: String,
        description: String,
        isFavorite: Boolean = false,
        category: String = "PUBLIC"
    ) {
        updateActivity()
        viewModelScope.launch {
            val entry = PasswordEntity(
                id = id,
                siteName = siteName,
                username = username,
                passwordHash = passwordRaw, // Store encrypted or plain local, plaintext for complete ease of copying
                description = description,
                createdAt = if (id == 0) System.currentTimeMillis() else (repository.getPasswordById(id)?.createdAt ?: System.currentTimeMillis()),
                lastEditedAt = System.currentTimeMillis(),
                isFavorite = isFavorite,
                isDeleted = false,
                deletedAt = null,
                category = category
            )
            repository.savePassword(entry)
            val isEn = currentLanguage == "en"
            val msg = if (id == 0) {
                if (isEn) "New credentials successfully stored in vault!" else "رمز عبور جدید با موفقیت ذخیره شد"
            } else {
                if (isEn) "Credentials successfully edited and updated!" else "رمز عبور با موفقیت ویرایش شد"
            }
            showSnackbar(msg)
        }
    }

    // Soft delete to Trash bin
    fun softDeletePassword(password: PasswordEntity) {
        updateActivity()
        viewModelScope.launch {
            val deletedEntry = password.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
            repository.savePassword(deletedEntry)
            showSnackbar("رمز عبور به سطل زباله منتقل شد. امکان بازیابی تا ۳۰ روز وجود دارد.")
        }
    }

    // Restore from Trash
    fun restorePassword(password: PasswordEntity) {
        updateActivity()
        viewModelScope.launch {
            val restoredEntry = password.copy(
                isDeleted = false,
                deletedAt = null,
                lastEditedAt = System.currentTimeMillis()
            )
            repository.savePassword(restoredEntry)
            showSnackbar("رمز عبور با موفقیت بازیابی شد")
        }
    }

    // Permanently Delete Password
    fun permanentlyDeletePassword(password: PasswordEntity) {
        updateActivity()
        viewModelScope.launch {
            repository.deletePassword(password)
            showSnackbar("رمز عبور به طور دائمی حذف شد")
        }
    }

    // Toggle Favorite status
    fun toggleFavorite(password: PasswordEntity) {
        updateActivity()
        viewModelScope.launch {
            val updated = password.copy(isFavorite = !password.isFavorite)
            repository.savePassword(updated)
            showSnackbar(if (updated.isFavorite) "به علاقه‌مندی‌ها اضافه شد" else "از علاقه‌مندی‌ها حذف شد")
        }
    }

    // Password generation helper
    fun generateRandomPassword(): String {
        updateActivity()
        val charsUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val charsLower = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        val random = SecureRandom()
        val password = StringBuilder()
        
        // Ensure at least one from each category
        password.append(charsUpper[random.nextInt(charsUpper.length)])
        password.append(charsLower[random.nextInt(charsLower.length)])
        password.append(digits[random.nextInt(digits.length)])
        password.append(symbols[random.nextInt(symbols.length)])
        
        val allChars = charsUpper + charsLower + digits + symbols
        for (i in 5..12) {
            password.append(allChars[random.nextInt(allChars.length)])
        }
        
        // Shuffle the characters
        val list = password.toString().toList().shuffled()
        return list.joinToString("")
    }

    // Export secure encypted backup
    fun exportBackup(masterPass: String, onResult: (String?) -> Unit) {
        updateActivity()
        viewModelScope.launch {
            val config = userConfigFlow.value
            if (config == null) {
                onResult(null)
                return@launch
            }
            
            // Derive Key via PBKDF2
            val salt = Base64.decode(config.masterPasswordSalt, Base64.NO_WRAP)
            val computedHash = CryptoUtils.hashPassword(masterPass, salt)
            if (computedHash != config.masterPasswordHash) {
                showSnackbar("رمز اصلی اشتباه است")
                onResult(null)
                return@launch
            }

            // Fetch active passwords
            val passwords = activePasswordsFlow.value
            val backupItems = passwords.map {
                BackupPasswordItem(
                    siteName = it.siteName,
                    username = it.username,
                    password = it.passwordHash,
                    description = it.description,
                    createdAt = it.createdAt,
                    lastEditedAt = it.lastEditedAt,
                    isFavorite = it.isFavorite
                )
            }

            try {
                val jsonString = jsonAdapter.toJson(backupItems)
                // Derive encryption key
                val backupSalt = CryptoUtils.generateSalt()
                val encryptionKey = CryptoUtils.deriveKey(masterPass, backupSalt)
                
                // Encrypt payload
                val encryptedData = CryptoUtils.encryptAesGcm(jsonString, encryptionKey)
                
                // Formulate wrapper representing salt + ciphertext
                val backupWrapper = BackupWrapper(
                    salt = Base64.encodeToString(backupSalt, Base64.NO_WRAP),
                    payload = encryptedData
                )
                val wrapperAdapter = moshi.adapter(BackupWrapper::class.java)
                val finalPayload = wrapperAdapter.toJson(backupWrapper)
                
                showSnackbar("پشتیبان‌گیری رمزنگاری تکی با موفقیت انجام شد")
                onResult(Base64.encodeToString(finalPayload.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
            } catch (e: Exception) {
                showSnackbar("خطا در پشتیبان‌گیری: ${e.message}")
                onResult(null)
            }
        }
    }

    // Import encrypted backup
    fun importBackup(base64Payload: String, masterPass: String, onResult: (Boolean) -> Unit) {
        updateActivity()
        viewModelScope.launch {
            val config = userConfigFlow.value
            if (config == null) {
                onResult(false)
                return@launch
            }
            
            // Verify Master Password
            val salt = Base64.decode(config.masterPasswordSalt, Base64.NO_WRAP)
            val computedHash = CryptoUtils.hashPassword(masterPass, salt)
            if (computedHash != config.masterPasswordHash) {
                showSnackbar("رمز اصلی اشتباه است")
                onResult(false)
                return@launch
            }

            try {
                val decodedWrapperBytes = Base64.decode(base64Payload, Base64.NO_WRAP)
                val wrapperJson = String(decodedWrapperBytes, Charsets.UTF_8)
                val wrapperAdapter = moshi.adapter(BackupWrapper::class.java)
                val wrapper = wrapperAdapter.fromJson(wrapperJson) ?: throw IllegalArgumentException("فرمت فایل معتبر نیست")
                
                val backupSalt = Base64.decode(wrapper.salt, Base64.NO_WRAP)
                val encryptionKey = CryptoUtils.deriveKey(masterPass, backupSalt)
                
                // Decrypt GCM
                val decryptedJson = CryptoUtils.decryptAesGcm(wrapper.payload, encryptionKey)
                val backupItems = jsonAdapter.fromJson(decryptedJson) ?: throw IllegalArgumentException("رمز عبورهای پشتیبان آسیب دیده‌اند")
                
                // Insert back to local Database
                backupItems.forEach { item ->
                    val entity = PasswordEntity(
                        siteName = item.siteName,
                        username = item.username,
                        passwordHash = item.password,
                        description = item.description,
                        createdAt = item.createdAt,
                        lastEditedAt = item.lastEditedAt,
                        isFavorite = item.isFavorite,
                        isDeleted = false,
                        deletedAt = null
                    )
                    repository.savePassword(entity)
                }

                showSnackbar("اطلاعات پشتیبان با موفقیت بازیابی و هماهنگ شد")
                onResult(true)
            } catch (e: Exception) {
                showSnackbar("خطا در بازیابی پشتیبان: رمز عبور اشتباه است یا فایل آسیب دیده")
                onResult(false)
            }
        }
    }

    // Settings modifiers
    fun updateUsername(newName: String) {
        val config = userConfigFlow.value ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.saveUserConfig(config.copy(username = newName))
            showSnackbar("نام کاربری با موفقیت تغییر کرد")
        }
    }

    fun updateMasterPassword(oldPass: String, newPass: String, onResult: (Boolean) -> Unit) {
        val config = userConfigFlow.value
        if (config == null) {
            onResult(false)
            return
        }
        if (newPass.length < 8) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val salt = Base64.decode(config.masterPasswordSalt, Base64.NO_WRAP)
            val oldHash = CryptoUtils.hashPassword(oldPass, salt)
            if (oldHash != config.masterPasswordHash) {
                onResult(false)
                return@launch
            }

            val newSalt = CryptoUtils.generateSalt()
            val newHash = CryptoUtils.hashPassword(newPass, newSalt)
            
            repository.saveUserConfig(config.copy(
                masterPasswordHash = newHash,
                masterPasswordSalt = Base64.encodeToString(newSalt, Base64.NO_WRAP)
            ))
            onResult(true)
            showSnackbar("رمز اصلی با موفقیت تغییر کرد")
        }
    }

    fun updatePin(pin: String?) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            repository.saveUserConfig(config.copy(pinCode = pin))
            showSnackbar(if (pin == null) "پین کد غیرفعال شد" else "پین کد با موفقیت تغییر کرد")
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            repository.saveUserConfig(config.copy(useBiometrics = enabled))
            showSnackbar(if (enabled) "ورود با اثر انگشت فعال شد" else "ورود با اثر انگشت غیرفعال شد")
        }
    }

    fun toggleAutoLock(enabled: Boolean) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            repository.saveUserConfig(config.copy(useAutoLock = enabled))
            showSnackbar(if (enabled) "قفل خودکار فعال شد" else "قفل خودکار غیرفعال شد")
        }
    }

    fun toggleTheme() {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            val updatedValue = !isDarkTheme
            isDarkTheme = updatedValue
            repository.saveUserConfig(config.copy(isDarkTheme = updatedValue))
        }
    }

    fun changeLanguage(lang: String) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            currentLanguage = lang
            repository.saveUserConfig(config.copy(languageCode = lang))
            val isEn = lang == "en"
            showSnackbar(if (isEn) "Language successfully changed to English!" else "زبان برنامه با موفقیت به فارسی تغییر کرد")
        }
    }

    fun changeThemeName(name: String) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            currentThemeName = name
            repository.saveUserConfig(config.copy(themeName = name))
        }
    }

    fun changeFontScale(scale: Float) {
        val config = userConfigFlow.value ?: return
        viewModelScope.launch {
            currentFontScale = scale
            repository.saveUserConfig(config.copy(fontScale = scale))
        }
    }

    private var clipboardClearJob: Job? = null

    fun copyToClipboardAndClearLater(context: android.content.Context, text: String, isPasswordContent: Boolean) {
        try {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("AmnVazeh Copy", text)
            clipboard.setPrimaryClip(clip)
            
            val isEn = currentLanguage == "en"
            val msg = if (isPasswordContent) {
                if (isEn) "Copied! Valid in clipboard for only 20 seconds." else "کپی شد اما تا ۲۰ ثانیه ماندگاره در کلیپبورد"
            } else {
                if (isEn) "Username successfully copied!" else "نام کاربری با موفقیت کپی شد"
            }
            showSnackbar(msg)
            
            if (isPasswordContent) {
                clipboardClearJob?.cancel()
                clipboardClearJob = viewModelScope.launch {
                    delay(20000) // 20 seconds
                    val emptyClip = android.content.ClipData.newPlainText("", "")
                    clipboard.setPrimaryClip(emptyClip)
                    showSnackbar(if (isEn) "Clipboard automatically cleared for security." else "حافظه موقت جهت افزایش امنیت به طور خودکار پاک شد")
                }
            }
        } catch (e: Exception) {
            showSnackbar("Error copying: ${e.localizedMessage}")
        }
    }

    // Emergency Reset Application
    fun emergencyResetApp() {
        viewModelScope.launch {
            repository.resetDatabase()
            isUserAuthenticated = false
            isSplashActive = false
            showSnackbar("برنامه بهطور کامل بازنشانی شد و تمام داده‌ها پاک گردیدند")
            // Wait briefly then restart user state configs
            delay(500)
        }
    }
}

// Backup support models
class BackupPasswordItem(
    val siteName: String,
    val username: String,
    val password: String,
    val description: String,
    val createdAt: Long,
    val lastEditedAt: Long,
    val isFavorite: Boolean
)

class BackupWrapper(
    val salt: String, // Base64 derived salt payload
    val payload: String // Base64 AES GCM payload containing the json password list
)
