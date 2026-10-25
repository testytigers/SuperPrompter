package com.superprompter.ui.prompter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superprompter.data.prefs.UserPreferencesRepository
import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.Script
import com.superprompter.domain.UserPreferences
import com.superprompter.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PrompterScript {
    data object Loading : PrompterScript
    data object Missing : PrompterScript
    data class Loaded(val script: Script) : PrompterScript
}

@HiltViewModel
class PrompterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    scriptRepository: ScriptRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val scriptId: Long = checkNotNull(savedStateHandle.get<Long>(Routes.ARG_SCRIPT_ID))

    val script: StateFlow<PrompterScript> = scriptRepository.observeById(scriptId)
        .map<Script?, PrompterScript> { if (it == null) PrompterScript.Missing else PrompterScript.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrompterScript.Loading)

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    init {
        viewModelScope.launch { preferencesRepository.setActiveScriptId(scriptId) }
    }

    fun adjustSpeed(delta: Float) {
        viewModelScope.launch { preferencesRepository.setScrollSpeed(preferences.value.scrollSpeed + delta) }
    }

    fun adjustFontSize(delta: Float) {
        viewModelScope.launch { preferencesRepository.setFontSize(preferences.value.fontSize + delta) }
    }

    fun cycleSize() {
        viewModelScope.launch { preferencesRepository.setPrompterSize(preferences.value.prompterSize.next()) }
    }

    fun toggleMode() {
        viewModelScope.launch { preferencesRepository.setPrompterMode(preferences.value.prompterMode.toggled()) }
    }

    fun toggleMirror() {
        viewModelScope.launch { preferencesRepository.setMirrorHorizontal(!preferences.value.mirrorHorizontal) }
    }
}
