package com.superprompter.ui.scripts

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.superprompter.ui.theme.DarkSurfaceVariant
import com.superprompter.ui.theme.GlassBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptListScreen(
    onOpenScript: (Long) -> Unit,
    onNewScript: () -> Unit,
    onPlayScript: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ScriptListViewModel = hiltViewModel(),
) {
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Script?>(null) }

    Scaffold(
        containerColor = DarkSurface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = CyanPrimary,
                    actionIconContentColor = CyanPrimary,
                ),
                title = { Text("SuperPrompter", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewScript,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New script") },
                containerColor = CyanPrimary,
                contentColor = DarkSurface,
            )
        },
    ) { padding ->
        val list = scripts
        when {
            list == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = CyanPrimary)
            }

            list.isEmpty() -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(padding),
                onCreateSample = { viewModel.createSample(onOpenScript) },
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.id }) { script ->
                    ScriptCard(
                        script = script,
                        onClick = { onOpenScript(script.id) },
                        onPlay = { onPlayScript(script.id) },
                        onDelete = { pendingDelete = script },
                    )
                }
            }
        }
    }

    pendingDelete?.let { script ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${script.title}\"?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(script.id)
                    pendingDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF5370))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = CyanPrimary)
                }
            },
        )
    }
}

@Composable
private fun ScriptCard(
    script: Script,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = script.title,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (script.content.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = script.content.trim(),
                        color = DarkOnSurfaceVariant,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${script.wordCount} words · ~${formatDuration(script.readingTimeSeconds)} · " +
                        DateUtils.getRelativeTimeSpanString(script.updatedAt),
                    color = DarkOnSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkOnSurfaceVariant)
            }
            FilledIconButton(
                onClick = onPlay,
                enabled = script.content.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkSurface,
                ),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start prompter")
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onCreateSample: () -> Unit,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("No scripts yet", color = DarkOnSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Write your first script, or load a sample to see how the prompter works.",
            color = DarkOnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onCreateSample,
            border = BorderStroke(1.dp, CyanPrimary),
        ) {
            Text("Try a sample script", color = CyanPrimary)
        }
    }
}
