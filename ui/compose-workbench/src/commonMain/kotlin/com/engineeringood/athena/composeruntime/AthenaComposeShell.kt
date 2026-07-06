package com.engineeringood.athena.composeruntime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Renders the first real Athena desktop workbench shell above runtime-managed state.
 */
@Composable
fun AthenaComposeShell(
    shellState: AthenaComposeShellState = AthenaComposeShellState(),
    onIntent: (AthenaComposeShellIntent) -> Unit = {},
) {
    AthenaComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AthenaShellTopBar(shellState = shellState)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AthenaDockPanel(
                        title = "Workspace",
                        modifier = Modifier
                            .width(248.dp)
                            .fillMaxHeight(),
                    ) {
                        AthenaWorkspaceTree(shellState.workspaceTreeItems)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AthenaWorkbenchPanel(
                            shellState = shellState,
                            modifier = Modifier.weight(1f),
                        )
                        AthenaBottomPanels(shellState = shellState)
                    }
                    AthenaInspectorColumn(
                        shellState = shellState,
                        onIntent = onIntent,
                        modifier = Modifier
                            .width(304.dp)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible bridge for older display-only call sites.
 */
@Composable
fun AthenaComposeShell(
    descriptor: AthenaComposeShellDescriptor = AthenaComposeShellDescriptor(),
    scene: AthenaSemanticViewerScene? = null,
) {
    AthenaComposeShell(
        shellState = AthenaComposeShellState(
            descriptor = descriptor,
            workspaceName = "workspace",
            projectName = scene?.systemName ?: "No active project",
            sourceDocument = null,
            scene = scene,
        ),
        onIntent = {},
    )
}

@Composable
private fun AthenaShellTopBar(shellState: AthenaComposeShellState) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = shellState.descriptor.windowTitle,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "File  Edit  View  Build  Tools  Window  Help",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            AthenaChromePill("Workspace ${shellState.workspaceName}")
            AthenaChromePill(shellState.projectName)
            Button(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Compile")
            }
        }
    }
}

@Composable
private fun AthenaWorkbenchPanel(
    shellState: AthenaComposeShellState,
    modifier: Modifier = Modifier,
) {
    AthenaDockPanel(
        title = "Workbench",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AthenaWorkbenchTab(label = "Source", isActive = shellState.sourceDocument != null)
                AthenaWorkbenchTab(label = "Render", isActive = shellState.scene != null)
                AthenaWorkbenchTab(
                    label = "Split",
                    isActive = shellState.sourceDocument != null && shellState.scene != null,
                )
                Spacer(modifier = Modifier.weight(1f))
                AthenaChromePill("Quiet competence")
            }
            if (shellState.sourceDocument == null && shellState.scene == null) {
                AthenaEmptyWorkbenchState()
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (shellState.sourceDocument != null) {
                        AthenaSourcePane(
                            sourceDocument = shellState.sourceDocument,
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight(),
                        )
                    }
                    AthenaRenderPane(
                        scene = shellState.scene,
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AthenaBottomPanels(shellState: AthenaComposeShellState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AthenaDockPanel(
            title = "Diagnostics",
            modifier = Modifier.weight(0.48f),
        ) {
            AthenaMachineList(shellState.diagnosticsEntries)
        }
        AthenaDockPanel(
            title = "Console",
            modifier = Modifier.weight(0.52f),
        ) {
            AthenaMachineList(shellState.consoleEntries)
        }
    }
}

@Composable
private fun AthenaInspectorColumn(
    shellState: AthenaComposeShellState,
    onIntent: (AthenaComposeShellIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        shellState.commandPanel?.let { commandPanel ->
            AthenaDockPanel(
                title = commandPanel.title,
                modifier = Modifier.weight(0.42f),
            ) {
                AthenaCommandPanel(
                    commandPanel = commandPanel,
                    onIntent = onIntent,
                )
            }
        }
        AthenaDockPanel(
            title = "Inspector",
            modifier = Modifier.weight(if (shellState.commandPanel == null) 1f else 0.58f),
        ) {
            AthenaInspector(shellState = shellState)
        }
    }
}

@Composable
private fun AthenaWorkspaceTree(items: List<AthenaComposeWorkspaceTreeItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (item.isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (8 + (item.depth * 12)).dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (item.isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (item.meta != null) {
                        Text(
                            text = item.meta,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AthenaSourcePane(
    sourceDocument: AthenaComposeSourceDocument,
    modifier: Modifier = Modifier,
) {
    AthenaDockPanel(
        title = "Source",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = sourceDocument.path,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    sourceDocument.lines.forEach { line ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = line.number.toString().padStart(3, ' '),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = line.content.ifBlank { " " },
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AthenaRenderPane(
    scene: AthenaSemanticViewerScene?,
    modifier: Modifier = Modifier,
) {
    AthenaDockPanel(
        title = "Render",
        modifier = modifier,
    ) {
        if (scene == null) {
            AthenaPanelEmptyState("No active runtime-managed project is loaded yet.")
        } else {
            AthenaSemanticViewerStage(
                scene = scene,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AthenaInspector(shellState: AthenaComposeShellState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        shellState.inspectorGroups.forEach { group ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    group.fields.forEach { field ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = field.value,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AthenaCommandPanel(
    commandPanel: AthenaComposeCommandPanelState,
    onIntent: (AthenaComposeShellIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = commandPanel.statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AthenaCommandOptionSection(
            title = "Source Port",
            options = commandPanel.sourcePortOptions,
            selectedSemanticId = commandPanel.selectedSourcePortSemanticId,
            onSelect = { semanticId ->
                onIntent(AthenaComposeShellIntent.SelectSourcePort(semanticId))
            },
        )
        AthenaCommandOptionSection(
            title = "Target Port",
            options = commandPanel.targetPortOptions,
            selectedSemanticId = commandPanel.selectedTargetPortSemanticId,
            onSelect = { semanticId ->
                onIntent(AthenaComposeShellIntent.SelectTargetPort(semanticId))
            },
        )
        Button(
            onClick = { onIntent(AthenaComposeShellIntent.ExecuteConnectPorts) },
            enabled = commandPanel.canExecute,
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(commandPanel.actionLabel)
        }
    }
}

@Composable
private fun AthenaCommandOptionSection(
    title: String,
    options: List<AthenaComposeCommandOption>,
    selectedSemanticId: String?,
    onSelect: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (options.isEmpty()) {
            AthenaPanelEmptyState("No runtime-derived options available yet.")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    val isSelected = option.semanticId == selectedSemanticId
                    OutlinedButton(
                        onClick = { onSelect(option.semanticId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            Text(
                                text = option.meta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AthenaMachineList(entries: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        entries.forEach { entry ->
            Text(
                text = entry,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun AthenaDockPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun AthenaWorkbenchTab(
    label: String,
    isActive: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun AthenaChromePill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AthenaEmptyWorkbenchState() {
    AthenaPanelEmptyState(
        message = "Open a runtime-managed project to populate the source and render panes.",
    )
}

@Composable
private fun AthenaPanelEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Preview for the default Athena shell bootstrap state.
 */
@Preview
@Composable
fun AthenaComposeShellPreview() {
    AthenaComposeShell(shellState = AthenaComposeShellState())
}
