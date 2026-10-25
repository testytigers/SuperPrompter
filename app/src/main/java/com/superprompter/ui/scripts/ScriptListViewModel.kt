package com.superprompter.ui.scripts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.Script
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScriptListViewModel @Inject constructor(
    private val repository: ScriptRepository,
) : ViewModel() {

    /** `null` while the first database read is in flight. */
    val scripts: StateFlow<List<Script>?> = repository.observeAll()
        .stateIn<List<Script>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun createSample(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.upsert(Script(title = "Welcome to SuperPrompter", content = SAMPLE_SCRIPT))
            onCreated(id)
        }
    }

    private companion object {
        val SAMPLE_SCRIPT = """
            Hi there, and welcome to SuperPrompter.

            Keep your eyes near the top of the screen, right by the camera lens. The highlighted band is your reading line: whatever sits inside it is what you say next.

            Tap anywhere on the text to pause, and tap again to carry on. You can also drag the script up or down at any time to find your place.

            Use the plus and minus buttons to match the scroll speed to your natural pace, and the text buttons to make the words bigger or smaller.

            Press the red record button to film yourself. You'll get a three second countdown, then the script and the recording start together. Your videos are saved to the Movies folder on your phone.

            That's it. Replace this text with your own script and you're ready to go.
        """.trimIndent()
    }
}
