package com.superprompter.ui.editor

import androidx.lifecycle.SavedStateHandle
import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.Script
import com.superprompter.navigation.Routes
import com.superprompter.testing.FakeScriptDao
import com.superprompter.testing.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeScriptDao()
    private val repository = ScriptRepository(dao)
    private val savedStateHandle = SavedStateHandle()

    private fun createViewModel(scriptId: Long = 0L): EditorViewModel {
        savedStateHandle[Routes.ARG_SCRIPT_ID] = scriptId
        // Context is only used for file import/export, which these tests don't touch.
        return EditorViewModel(savedStateHandle, repository, mockk(relaxed = true))
    }

    @Test
    fun `typing autosaves after a short pause`() = runTest {
        val viewModel = createViewModel()

        viewModel.onContentChange("Hello world")
        advanceTimeBy(500)
        assertEquals("nothing saved mid-typing", 0, dao.insertCount)
        assertFalse(viewModel.state.value.isSaved)

        advanceUntilIdle()
        assertEquals(1, dao.insertCount)
        assertTrue(viewModel.state.value.isSaved)
        assertEquals("Hello world", dao.all.single().content)
    }

    @Test
    fun `rapid edits are debounced into a single save`() = runTest {
        val viewModel = createViewModel()

        listOf("H", "He", "Hel", "Hell", "Hello").forEach {
            viewModel.onContentChange(it)
            advanceTimeBy(200)
        }
        advanceUntilIdle()

        assertEquals(1, dao.insertCount)
        assertEquals(0, dao.updateCount)
        assertEquals("Hello", dao.all.single().content)
    }

    @Test
    fun `first save assigns an id and later saves update the same script`() = runTest {
        val viewModel = createViewModel()

        viewModel.onContentChange("Draft one")
        advanceUntilIdle()
        val id = viewModel.state.value.id
        assertTrue(id != 0L)
        assertEquals("id survives process death", id, savedStateHandle.get<Long>(Routes.ARG_SCRIPT_ID))

        viewModel.onContentChange("Draft two")
        advanceUntilIdle()

        assertEquals(1, dao.insertCount)
        assertEquals(1, dao.updateCount)
        assertEquals("Draft two", dao.all.single().content)
    }

    @Test
    fun `blank title is saved as Untitled script`() = runTest {
        val viewModel = createViewModel()

        viewModel.onContentChange("Some words")
        advanceUntilIdle()

        assertEquals("Untitled script", dao.all.single().title)
    }

    @Test
    fun `flush saves immediately without waiting for autosave`() = runTest {
        val viewModel = createViewModel()
        var flushedId = -1L

        viewModel.onTitleChange("Pitch")
        viewModel.flush { flushedId = it }
        advanceTimeBy(1) // well under the autosave delay
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        assertEquals(1, dao.insertCount)
        assertEquals(viewModel.state.value.id, flushedId)
    }

    @Test
    fun `an empty new script is never saved`() = runTest {
        val viewModel = createViewModel()
        var flushedId = -1L

        viewModel.onContentChange("")
        viewModel.flush { flushedId = it }
        advanceUntilIdle()

        assertEquals(0L, flushedId)
        assertEquals(0, dao.insertCount)
    }

    @Test
    fun `opening an existing script loads it and keeps its created date`() = runTest {
        val id = repository.upsert(Script(title = "Keynote", content = "Good morning"))
        val createdAt = repository.findById(id)!!.createdAt

        val viewModel = createViewModel(id)
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Keynote", state.title)
        assertEquals("Good morning", state.content)
        assertEquals(2, state.wordCount)

        viewModel.onContentChange("Good evening")
        advanceUntilIdle()
        assertEquals(createdAt, repository.findById(id)!!.createdAt)
    }

    @Test
    fun `deleting cancels a pending autosave so the script stays deleted`() = runTest {
        val id = repository.upsert(Script(title = "Old", content = "Old text"))
        val viewModel = createViewModel(id)
        advanceUntilIdle()
        var deleted = false

        viewModel.onContentChange("Unsaved edit")
        viewModel.delete { deleted = true }
        advanceUntilIdle()

        assertTrue(deleted)
        assertTrue(dao.all.isEmpty())
    }
}
