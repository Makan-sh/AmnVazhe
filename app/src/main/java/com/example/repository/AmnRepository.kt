package com.example.repository

import com.example.data.PasswordDao
import com.example.data.PasswordEntity
import com.example.data.UserConfigDao
import com.example.data.UserConfigEntity
import kotlinx.coroutines.flow.Flow

class AmnRepository(
    private val passwordDao: PasswordDao,
    private val userConfigDao: UserConfigDao
) {
    val activePasswords: Flow<List<PasswordEntity>> = passwordDao.getActivePasswordsFlow()
    val deletedPasswords: Flow<List<PasswordEntity>> = passwordDao.getDeletedPasswordsFlow()
    val userConfigFlow: Flow<UserConfigEntity?> = userConfigDao.getUserConfigFlow()

    suspend fun getPasswordById(id: Int): PasswordEntity? {
        return passwordDao.getPasswordById(id)
    }

    suspend fun savePassword(password: PasswordEntity) {
        passwordDao.insertPassword(password)
    }

    suspend fun updatePassword(password: PasswordEntity) {
        passwordDao.updatePassword(password)
    }

    suspend fun deletePassword(password: PasswordEntity) {
        passwordDao.deletePassword(password)
    }

    suspend fun deletePasswordById(id: Int) {
        passwordDao.deletePasswordById(id)
    }

    suspend fun purgeOldDeleted(purgeTimestamp: Long) {
        passwordDao.purgeOldDeletedPasswords(purgeTimestamp)
    }

    suspend fun clearAllPasswords() {
        passwordDao.clearAllPasswords()
    }

    suspend fun getUserConfig(): UserConfigEntity? {
        return userConfigDao.getUserConfig()
    }

    suspend fun saveUserConfig(config: UserConfigEntity) {
        userConfigDao.saveUserConfig(config)
    }

    suspend fun clearUserConfig() {
        userConfigDao.clearUserConfig()
    }

    // Emergency Reset: Wipes all passwords AND settings to start from scratch
    suspend fun resetDatabase() {
        passwordDao.clearAllPasswords()
        userConfigDao.clearUserConfig()
    }
}
