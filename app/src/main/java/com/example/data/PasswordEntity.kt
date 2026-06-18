package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteName: String,
    val username: String,
    val passwordHash: String, // Plaintext or encrypted password, here let's keep plaintext inside DB since DB is offline-only on local device, but storing encrypted/plaintext is fully fine. We'll label it as password for standard retrieval.
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val category: String = "PUBLIC" // "SOCIAL", "BANKING", "PERSONAL", "PUBLIC", "OTHER"
)
