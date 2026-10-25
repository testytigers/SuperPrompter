package com.superprompter.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.PrompterSize
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            scrollSpeed = prefs[Keys.SCROLL_SPEED] ?: UserPreferences.DEFAULT_SCROLL_SPEED,
            fontSize = prefs[Keys.FONT_SIZE] ?: UserPreferences.DEFAULT_FONT_SIZE,
            overlayOpacity = prefs[Keys.OPACITY] ?: UserPreferences.DEFAULT_OPACITY,
            textAlignment = prefs[Keys.ALIGNMENT]
                ?.let { runCatching { TextAlignment.valueOf(it) }.getOrNull() }
                ?: TextAlignment.CENTER,
            autoReverse = prefs[Keys.AUTO_REVERSE] ?: false,
            mirrorHorizontal = prefs[Keys.MIRROR_H] ?: false,
            mirrorVertical = prefs[Keys.MIRROR_V] ?: false,
            showWordCounter = prefs[Keys.SHOW_WORD_COUNTER] ?: true,
            prompterSize = prefs[Keys.PROMPTER_SIZE]
                ?.let { runCatching { PrompterSize.valueOf(it) }.getOrNull() }
                ?: PrompterSize.FULL,
            prompterMode = prefs[Keys.PROMPTER_MODE]
                ?.let { runCatching { PrompterMode.valueOf(it) }.getOrNull() }
                ?: PrompterMode.SCROLL,
            splitSentences = prefs[Keys.SPLIT_SENTENCES] ?: true,
            activeScriptId = prefs[Keys.ACTIVE_SCRIPT_ID],
        )
    }

    suspend fun setScrollSpeed(value: Float) = edit { it[Keys.SCROLL_SPEED] = value.coerce() }

    suspend fun setFontSize(value: Float) = edit {
        it[Keys.FONT_SIZE] = value.coerceIn(
            UserPreferences.MIN_FONT_SIZE,
            UserPreferences.MAX_FONT_SIZE,
        )
    }

    suspend fun setOpacity(value: Float) = edit {
        it[Keys.OPACITY] = value.coerceIn(
            UserPreferences.MIN_OPACITY,
            UserPreferences.MAX_OPACITY,
        )
    }

    suspend fun setAlignment(alignment: TextAlignment) = edit {
        it[Keys.ALIGNMENT] = alignment.name
    }

    suspend fun setAutoReverse(enabled: Boolean) = edit { it[Keys.AUTO_REVERSE] = enabled }

    suspend fun setMirrorHorizontal(enabled: Boolean) = edit { it[Keys.MIRROR_H] = enabled }

    suspend fun setMirrorVertical(enabled: Boolean) = edit { it[Keys.MIRROR_V] = enabled }

    suspend fun setShowWordCounter(enabled: Boolean) = edit {
        it[Keys.SHOW_WORD_COUNTER] = enabled
    }

    suspend fun setPrompterSize(size: PrompterSize) = edit { it[Keys.PROMPTER_SIZE] = size.name }

    suspend fun setPrompterMode(mode: PrompterMode) = edit { it[Keys.PROMPTER_MODE] = mode.name }

    suspend fun setSplitSentences(enabled: Boolean) = edit { it[Keys.SPLIT_SENTENCES] = enabled }

    suspend fun setActiveScriptId(id: Long?) = edit {
        if (id == null) it.remove(Keys.ACTIVE_SCRIPT_ID) else it[Keys.ACTIVE_SCRIPT_ID] = id
    }

    private fun Float.coerce() = coerceIn(
        UserPreferences.MIN_SCROLL_SPEED,
        UserPreferences.MAX_SCROLL_SPEED,
    )

    private suspend inline fun edit(crossinline block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    private object Keys {
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val OPACITY = floatPreferencesKey("opacity")
        val ALIGNMENT = stringPreferencesKey("alignment")
        val AUTO_REVERSE = booleanPreferencesKey("auto_reverse")
        val MIRROR_H = booleanPreferencesKey("mirror_horizontal")
        val MIRROR_V = booleanPreferencesKey("mirror_vertical")
        val SHOW_WORD_COUNTER = booleanPreferencesKey("show_word_counter")
        val ACTIVE_SCRIPT_ID = longPreferencesKey("active_script_id")
        val PROMPTER_SIZE = stringPreferencesKey("prompter_size")
        val PROMPTER_MODE = stringPreferencesKey("prompter_mode")
        val SPLIT_SENTENCES = booleanPreferencesKey("split_sentences")
    }
}
