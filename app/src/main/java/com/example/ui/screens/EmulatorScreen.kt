package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebProject
import com.example.ui.components.WebEmulatorView
import com.example.ui.theme.*
import com.example.ui.viewmodel.DevicePreviewMode
import com.example.ui.viewmodel.WebToApkViewModel

@Composable
fun EmulatorScreen(
    viewModel: WebToApkViewModel,
    project: WebProject,
    modifier: Modifier = Modifier
) {
    val emulatorState by viewModel.emulatorState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Device Mode Selector Bar (if not fullscreen)
        if (!emulatorState.isFullscreen) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PREVIEW DEVICE:",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        DevicePreviewMode.entries.forEach { mode ->
                            FilterChip(
                                selected = emulatorState.previewMode == mode,
                                onClick = { viewModel.setEmulatorPreviewMode(mode) },
                                label = { Text(mode.label, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            DevicePreviewMode.PHONE -> Icons.Default.Smartphone
                                            DevicePreviewMode.TABLET -> Icons.Default.Tablet
                                            DevicePreviewMode.DESKTOP -> Icons.Default.Laptop
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Slate100,
                                    containerColor = Slate800,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = emulatorState.previewMode == mode,
                                    borderColor = if (emulatorState.previewMode == mode) CyanLight else Slate700
                                ),
                                modifier = Modifier.testTag("preview_mode_${mode.name}")
                            )
                        }
                    }

                    // App Title Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800
                    ) {
                        Text(
                            text = project.title.ifBlank { "Live Web App" },
                            color = CyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Live WebView Component
        WebEmulatorView(
            project = project,
            currentUrl = emulatorState.currentUrl,
            previewMode = emulatorState.previewMode,
            isFullscreen = emulatorState.isFullscreen,
            onUrlChange = { viewModel.setEmulatorUrl(it) },
            onProgressChange = { progress, isLoading, title ->
                viewModel.updateEmulatorProgress(progress, isLoading, title)
            },
            onToggleFullscreen = { viewModel.toggleEmulatorFullscreen() },
            modifier = Modifier.weight(1f)
        )
    }
}
