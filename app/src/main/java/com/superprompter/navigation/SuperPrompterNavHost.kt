package com.superprompter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.superprompter.ui.editor.ScriptEditorScreen
import com.superprompter.ui.prompter.PrompterScreen
import com.superprompter.ui.scripts.ScriptListScreen
import com.superprompter.ui.settings.SettingsScreen

object Routes {
    const val ARG_SCRIPT_ID = "scriptId"

    const val SCRIPTS = "scripts"
    const val SETTINGS = "settings"
    const val EDITOR = "editor/{$ARG_SCRIPT_ID}"
    const val PROMPTER = "prompter/{$ARG_SCRIPT_ID}"

    /** An id of 0 opens the editor on a new, unsaved script. */
    fun editor(scriptId: Long = 0L) = "editor/$scriptId"
    fun prompter(scriptId: Long) = "prompter/$scriptId"
}

private val scriptIdArgument = listOf(
    navArgument(Routes.ARG_SCRIPT_ID) { type = NavType.LongType },
)

@Composable
fun SuperPrompterNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.SCRIPTS) {
        composable(Routes.SCRIPTS) {
            ScriptListScreen(
                onOpenScript = { navController.navigate(Routes.editor(it)) },
                onNewScript = { navController.navigate(Routes.editor()) },
                onPlayScript = { navController.navigate(Routes.prompter(it)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.EDITOR, arguments = scriptIdArgument) {
            ScriptEditorScreen(
                onBack = { navController.navigateUp() },
                onPlay = { navController.navigate(Routes.prompter(it)) },
            )
        }
        composable(Routes.PROMPTER, arguments = scriptIdArgument) {
            PrompterScreen(onClose = { navController.navigateUp() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.navigateUp() })
        }
    }
}
