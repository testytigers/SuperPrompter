package com.superprompter.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superprompter.domain.Script
import com.superprompter.domain.formatDuration
import com.superprompter.ui.theme.CyanPrimary
import com.superprompter.ui.theme.DarkOnSurface
import com.superprompter.ui.theme.DarkOnSurfaceVariant
import com.superprompter.ui.theme.DarkSurface
import com.superprompter.ui.theme.GlassBorder
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    onBack: () -> Unit,
    onPlay: (Long) -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importFrom)
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let(viewModel::exportTo)
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    val leave: () -> Unit = { viewModel.flush { onBack() } }
    BackHandler(onBack = leave)

    Scaffold(
        containerColor = DarkSurface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = CyanPrimary,
                    navigationIconContentColor = CyanPrimary,
                    actionIconContentColor = CyanPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = leave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = if (state.id == 0L) "New script" else "Edit script",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.flush { id -> if (id != 0L) onPlay(id) } },
                        enabled = state.content.isNotBlank(),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start prompter")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Import .txt") },
                                leadingIcon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("text/*"))
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Export .txt") },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                enabled = state.content.isNotBlank(),
                                onClick = {
                                    showMenu = false
                                    exportLauncher.launch("${state.title.ifBlank { "script" }}.txt")
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            BasicTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                singleLine = true,
                cursorBrush = SolidColor(CyanPrimary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                textStyle = TextStyle(color = DarkOnSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                decorationBox = { inner ->
                    if (state.title.isEmpty()) {
                        Text("Title", color = DarkOnSurfaceVariant, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    inner()
                },
            )

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.2f))

            BasicTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                cursorBrush = SolidColor(CyanPrimary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                textStyle = TextStyle(color = DarkOnSurface, fontSize = 18.sp, lineHeight = 28.sp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                decorationBox = { inner ->
                    if (state.content.isEmpty()) {
                        Text(
                            text = "Type or paste your script here…",
                            color = DarkOnSurfaceVariant,
                            fontSize = 18.sp,
                        )
                    }
                    inner()
                },
            )

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val seconds = (state.wordCount * 60f / Script.WORDS_PER_MINUTE).roundToInt()
                Text(
                    text = "${state.wordCount} words · ~${formatDuration(seconds)} to read",
                    color = DarkOnSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (state.isSaved) "Saved" else "Saving…",
                    color = if (state.isSaved) DarkOnSurfaceVariant else CyanPrimary,
                    fontSize = 13.sp,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete script?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onBack)
                }) {
                    Text("Delete", color = Color(0xFFFF5370))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = CyanPrimary)
                }
            },
        )
    }
}
