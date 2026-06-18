package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PasswordEntity::class, UserConfigEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AmnDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    abstract fun userConfigDao(): UserConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AmnDatabase? = null

        fun getDatabase(context: Context): AmnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmnDatabase::class.java,
                    "amn_vajeh_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
