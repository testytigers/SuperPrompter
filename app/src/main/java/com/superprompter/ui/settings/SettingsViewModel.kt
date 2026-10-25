package com.superprompter.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superprompter.data.prefs.UserPreferencesRepository
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.PrompterSize
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences?> = repository.preferences
        .stateIn<UserPreferences?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setScrollSpeed(value: Float) = persist { repository.setScrollSpeed(value) }
    fun setFontSize(value: Float) = persist { repository.setFontSize(value) }
    fun setOpacity(value: Float) = persist { repository.setOpacity(value) }
    fun setAlignment(value: TextAlignment) = persist { repository.setAlignment(value) }
    fun setAutoReverse(value: Boolean) = persist { repository.setAutoReverse(value) }
    fun setShowWordCounter(value: Boolean) = persist { repository.setShowWordCounter(value) }
    fun setMirrorHorizontal(value: Boolean) = persist { repository.setMirrorHorizontal(value) }
    fun setMirrorVertical(value: Boolean) = persist { repository.setMirrorVertical(value) }
    fun setPrompterSize(value: PrompterSize) = persist { repository.setPrompterSize(value) }
    fun setPrompterMode(value: PrompterMode) = persist { repository.setPrompterMode(value) }
    fun setSplitSentences(value: Boolean) = persist { repository.setSplitSentences(value) }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
