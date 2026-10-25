package com.superprompter.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.superprompter.data.prefs.UserPreferencesRepository
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.PrompterSize
import com.superprompter.overlay.FloatingPrompterService
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesRepositoryTest {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val repository = UserPreferencesRepository(
        PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") },
        )
    )

    @Test
    fun `fresh install returns defaults`() = testScope.runTest {
        assertEquals(UserPreferences(), repository.preferences.first())
    }

    @Test
    fun `values are saved and read back`() = testScope.runTest {
        repository.setScrollSpeed(120f)
        repository.setFontSize(40f)
        repository.setAlignment(TextAlignment.LEFT)
        repository.setMirrorHorizontal(true)
        repository.setAutoReverse(true)
        repository.setShowWordCounter(false)

        val prefs = repository.preferences.first()
        assertEquals(120f, prefs.scrollSpeed)
        assertEquals(40f, prefs.fontSize)
        assertEquals(TextAlignment.LEFT, prefs.textAlignment)
        assertTrue(prefs.mirrorHorizontal)
        assertTrue(prefs.autoReverse)
        assertEquals(false, prefs.showWordCounter)
    }

    @Test
    fun `out of range values are clamped`() = testScope.runTest {
        repository.setScrollSpeed(10_000f)
        repository.setFontSize(1f)
        repository.setOpacity(-3f)

        val prefs = repository.preferences.first()
        assertEquals(UserPreferences.MAX_SCROLL_SPEED, prefs.scrollSpeed)
        assertEquals(UserPreferences.MIN_FONT_SIZE, prefs.fontSize)
        assertEquals(UserPreferences.MIN_OPACITY, prefs.overlayOpacity)
    }

    @Test
    fun `active script id can be set and cleared`() = testScope.runTest {
        repository.setActiveScriptId(42L)
        assertEquals(42L, repository.preferences.first().activeScriptId)

        repository.setActiveScriptId(null)
        assertNull(repository.preferences.first().activeScriptId)
    }

    @Test
    fun `prompter size is saved and defaults to full screen`() = testScope.runTest {
        assertEquals(PrompterSize.FULL, repository.preferences.first().prompterSize)

        repository.setPrompterSize(PrompterSize.THIRD)

        assertEquals(PrompterSize.THIRD, repository.preferences.first().prompterSize)
    }

    @Test
    fun `prompter size cycles full, half, third, quarter, fifth`() {
        assertEquals(PrompterSize.HALF, PrompterSize.FULL.next())
        assertEquals(PrompterSize.THIRD, PrompterSize.HALF.next())
        assertEquals(PrompterSize.QUARTER, PrompterSize.THIRD.next())
        assertEquals(PrompterSize.FIFTH, PrompterSize.QUARTER.next())
        assertEquals(PrompterSize.FULL, PrompterSize.FIFTH.next())
    }

    @Test
    fun `floating prompter never goes full screen`() {
        assertEquals(PrompterSize.HALF, FloatingPrompterService.floatingSize(PrompterSize.FULL))
        assertEquals(PrompterSize.FIFTH, FloatingPrompterService.floatingSize(PrompterSize.FIFTH))
        assertEquals(PrompterSize.THIRD, FloatingPrompterService.floatingSizeAfter(PrompterSize.FULL))
        assertEquals(PrompterSize.HALF, FloatingPrompterService.floatingSizeAfter(PrompterSize.FIFTH))
    }

    @Test
    fun `prompter mode and sentence splitting are saved`() = testScope.runTest {
        val defaults = repository.preferences.first()
        assertEquals(PrompterMode.SCROLL, defaults.prompterMode)
        assertEquals(true, defaults.splitSentences)

        repository.setPrompterMode(PrompterMode.LINES)
        repository.setSplitSentences(false)

        val prefs = repository.preferences.first()
        assertEquals(PrompterMode.LINES, prefs.prompterMode)
        assertEquals(false, prefs.splitSentences)
        assertEquals(PrompterMode.SCROLL, PrompterMode.LINES.toggled())
    }
}
