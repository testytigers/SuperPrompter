package com.superprompter.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs against a real (in-memory) Room database, so it needs a device or emulator. */
@RunWith(AndroidJUnit4::class)
class ScriptDaoTest {

    private lateinit var database: SuperPrompterDatabase
    private lateinit var dao: ScriptDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SuperPrompterDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.scriptDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun scriptsAreListedMostRecentlyEditedFirst() = runTest {
        dao.insert(entity("Old", updatedAt = 1_000))
        dao.insert(entity("New", updatedAt = 3_000))
        dao.insert(entity("Middle", updatedAt = 2_000))

        assertEquals(listOf("New", "Middle", "Old"), dao.observeAll().first().map { it.title })
    }

    @Test
    fun insertGeneratesIdsAndUpdateChangesTheRow() = runTest {
        val id = dao.insert(entity("Draft"))

        dao.update(dao.findById(id)!!.copy(content = "Final"))

        assertEquals("Final", dao.findById(id)?.content)
        assertEquals("Final", dao.observeById(id).first()?.content)
    }

    @Test
    fun deleteByIdRemovesTheRow() = runTest {
        val id = dao.insert(entity("Temp"))

        dao.deleteById(id)

        assertNull(dao.findById(id))
    }

    private fun entity(title: String, updatedAt: Long = 0L) = ScriptEntity(
        title = title,
        content = "content",
        createdAt = 0L,
        updatedAt = updatedAt,
    )
}
