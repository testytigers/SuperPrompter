package com.superprompter.data

import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.Script
import com.superprompter.testing.FakeScriptDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptRepositoryTest {

    private val dao = FakeScriptDao()
    private val repository = ScriptRepository(dao)

    @Test
    fun `upserting a new script inserts it and returns the generated id`() = runTest {
        val id = repository.upsert(Script(title = "Intro", content = "Hello"))

        assertEquals(1, dao.insertCount)
        assertEquals("Intro", repository.findById(id)?.title)
    }

    @Test
    fun `upserting an existing script updates it and keeps createdAt`() = runTest {
        val id = repository.upsert(Script(title = "Intro", content = "Hello"))
        val original = repository.findById(id)!!

        repository.upsert(original.copy(content = "Hello again"))

        val updated = repository.findById(id)!!
        assertEquals(1, dao.insertCount)
        assertEquals(1, dao.updateCount)
        assertEquals("Hello again", updated.content)
        assertEquals(original.createdAt, updated.createdAt)
        assertTrue(updated.updatedAt >= original.updatedAt)
    }

    @Test
    fun `delete removes the script`() = runTest {
        val id = repository.upsert(Script(title = "Intro", content = "Hello"))

        repository.delete(id)

        assertNull(repository.findById(id))
        assertTrue(repository.observeAll().first().isEmpty())
    }
}
