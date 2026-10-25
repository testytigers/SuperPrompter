package com.superprompter.ui.editor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.Script
import com.superprompter.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class EditorState(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val isLoading: Boolean = false,
    val isSaved: Boolean = true,
) {
    val wordCount: Int get() = Script.countWords(content)
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ScriptRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val initialId: Long = savedStateHandle.get<Long>(Routes.ARG_SCRIPT_ID) ?: 0L

    private val _state = MutableStateFlow(EditorState(isLoading = initialId != 0L))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    private val saveMutex = Mutex()
    private var autosaveJob: Job? = null

    init {
        if (initialId != 0L) {
            viewModelScope.launch {
                val script = repository.findById(initialId)
                _state.value = if (script == null) {
                    EditorState()
                } else {
                    EditorState(
                        id = script.id,
                        title = script.title,
                        content = script.content,
                        createdAt = script.createdAt,
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title, isSaved = false) }
        scheduleAutosave()
    }

    fun onContentChange(content: String) {
        _state.update { it.copy(content = content, isSaved = false) }
        scheduleAutosave()
    }

    /** Saves any pending edits, then hands back the script id (0 if there was nothing to save). */
    fun flush(then: (Long) -> Unit) {
        autosaveJob?.cancel()
        viewModelScope.launch { then(persist()) }
    }

    fun delete(then: () -> Unit) {
        autosaveJob?.cancel()
        viewModelScope.launch {
            saveMutex.withLock {
                val id = _state.value.id
                if (id != 0L) repository.delete(id)
            }
            then()
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val resolver = context.contentResolver
                    val text = resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Couldn't open the file")
                    val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                    text to name?.substringBeforeLast('.')
                }
            }.onSuccess { (text, fileName) ->
                _state.update {
                    it.copy(
                        content = text,
                        title = it.title.ifBlank { fileName.orEmpty() },
                        isSaved = false,
                    )
                }
                scheduleAutosave()
                _messages.send("Imported ${Script.countWords(text)} words")
            }.onFailure {
                _messages.send("Import failed: ${it.message}")
            }
        }
    }

    fun exportTo(uri: Uri) {
        val content = _state.value.content
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(content) }
                        ?: error("Couldn't create the file")
                }
            }.onSuccess {
                _messages.send("Exported")
            }.onFailure {
                _messages.send("Export failed: ${it.message}")
            }
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(AUTOSAVE_DELAY_MS)
            persist()
        }
    }

    private suspend fun persist(): Long {
        saveMutex.withLock { return persistLocked() }
    }

    private suspend fun persistLocked(): Long {
        val snapshot = _state.value
        if (snapshot.isSaved) return snapshot.id
        if (snapshot.id == 0L && snapshot.title.isBlank() && snapshot.content.isBlank()) return 0L

        // Don't let a cancelled autosave abandon a write half way through.
        val id = withContext(NonCancellable) {
            repository.upsert(
                Script(
                    id = snapshot.id,
                    title = snapshot.title.trim().ifBlank { UNTITLED },
                    content = snapshot.content,
                    createdAt = snapshot.createdAt,
                )
            )
        }
        // Remember the id so a recreated process reopens this script rather than a blank one.
        savedStateHandle[Routes.ARG_SCRIPT_ID] = id
        _state.update {
            it.copy(
                id = id,
                isSaved = it.title == snapshot.title && it.content == snapshot.content,
            )
        }
        return id
    }

    private companion object {
        const val AUTOSAVE_DELAY_MS = 800L
        const val UNTITLED = "Untitled script"
    }
}
