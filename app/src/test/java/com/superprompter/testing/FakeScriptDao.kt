package com.superprompter.testing

import com.superprompter.data.db.ScriptDao
import com.superprompter.data.db.ScriptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory stand-in for the Room DAO that mirrors its auto-generated ids and ordering. */
class FakeScriptDao : ScriptDao {

    private val rows = MutableStateFlow<Map<Long, ScriptEntity>>(emptyMap())
    private var nextId = 1L

    var insertCount = 0
        private set
    var updateCount = 0
        private set

    val all: List<ScriptEntity> get() = rows.value.values.toList()

    override fun observeAll(): Flow<List<ScriptEntity>> =
        rows.map { map -> map.values.sortedByDescending { it.updatedAt } }

    override suspend fun findById(id: Long): ScriptEntity? = rows.value[id]

    override fun observeById(id: Long): Flow<ScriptEntity?> = rows.map { it[id] }

    override suspend fun insert(script: ScriptEntity): Long {
        insertCount++
        val id = if (script.id == 0L) nextId++ else script.id
        rows.update { it + (id to script.copy(id = id)) }
        return id
    }

    override suspend fun update(script: ScriptEntity) {
        updateCount++
        rows.update { if (script.id in it) it + (script.id to script) else it }
    }

    override suspend fun delete(script: ScriptEntity) = deleteById(script.id)

    override suspend fun deleteById(id: Long) {
        rows.update { it - id }
    }
}
