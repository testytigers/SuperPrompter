package com.superprompter.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {

    @Query("SELECT * FROM scripts ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun findById(id: Long): ScriptEntity?

    @Query("SELECT * FROM scripts WHERE id = :id")
    fun observeById(id: Long): Flow<ScriptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: ScriptEntity): Long

    @Update
    suspend fun update(script: ScriptEntity)

    @Delete
    suspend fun delete(script: ScriptEntity)

    @Query("DELETE FROM scripts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
