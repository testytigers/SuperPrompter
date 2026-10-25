package com.superprompter.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScriptEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SuperPrompterDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        const val DATABASE_NAME = "superprompter.db"
    }
}
