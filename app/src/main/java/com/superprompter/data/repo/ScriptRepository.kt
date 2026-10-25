package com.superprompter.data.repo

import com.superprompter.data.db.ScriptDao
import com.superprompter.data.db.ScriptEntity
import com.superprompter.domain.Script
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptRepository @Inject constructor(
    private val dao: ScriptDao,
) {
    fun observeAll(): Flow<List<Script>> =
        dao.observeAll().map { list -> list.map(ScriptEntity::toDomain) }

    fun observeById(id: Long): Flow<Script?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun findById(id: Long): Script? =
        dao.findById(id)?.toDomain()

    suspend fun upsert(script: Script): Long {
        val now = System.currentTimeMillis()
        val entity = ScriptEntity.fromDomain(
            script.copy(
                createdAt = if (script.id == 0L) now else script.createdAt,
                updatedAt = now,
            )
        )
        return if (script.id == 0L) dao.insert(entity) else {
            dao.update(entity)
            script.id
        }
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
