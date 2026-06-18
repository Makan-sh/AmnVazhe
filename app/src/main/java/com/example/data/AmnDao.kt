package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords WHERE isDeleted = 0 ORDER BY lastEditedAt DESC")
    fun getActivePasswordsFlow(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedPasswordsFlow(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    suspend fun getPasswordById(id: Int): PasswordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntity)

    @Update
    suspend fun updatePassword(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Int)

    // Automatically purge soft-deleted items older than 30 days
    @Query("DELETE FROM passwords WHERE isDeleted = 1 AND deletedAt < :purgeTimestamp")
    suspend fun purgeOldDeletedPasswords(purgeTimestamp: Long)

    @Query("DELETE FROM passwords")
    suspend fun clearAllPasswords()
}

@Dao
interface UserConfigDao {
    @Query("SELECT * FROM user_config WHERE id = 1 LIMIT 1")
    fun getUserConfigFlow(): Flow<UserConfigEntity?>

    @Query("SELECT * FROM user_config WHERE id = 1 LIMIT 1")
    suspend fun getUserConfig(): UserConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserConfig(config: UserConfigEntity)

    @Query("DELETE FROM user_config")
    suspend fun clearUserConfig()
}
