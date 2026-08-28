package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebProject
import com.example.ui.components.CodeViewer
import com.example.ui.theme.*
import com.example.util.CodeGenerator
import com.example.util.ZipExporter

@Composable
fun CodeExportScreen(
    project: WebProject,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportSummaryDialog by remember { mutableStateOf(false) }

    val zipSummary = remember(project) {
        ZipExporter.getZipSummary(project)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Top Info & Quick Export Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            tonalElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Android Source & Scripts",
                            color = Slate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoDark
                        ) {
                            Text(
                                text = "${zipSummary.totalFiles} files in .ZIP",
                                color = IndigoPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Complete production Gradle project & build scripts for ${project.title}",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Share ZIP
                    IconButton(
                        onClick = {
                            val res = ZipExporter.shareZipFile(context, project)
                            if (!res.isSuccess) {
                                Toast.makeText(context, "Could not open share sheet", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("quick_share_zip_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share ZIP", tint = IndigoPrimary)
                    }

                    // Build Guide Button
                    Button(
                        onClick = { showExportSummaryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("build_guide_button")
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Build Guide", color = IndigoDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Full Code & ZIP Viewer
        CodeViewer(
            project = project,
            modifier = Modifier.weight(1f)
        )
    }

    if (showExportSummaryDialog) {
        val guide = CodeGenerator.generateBuildGuide(project)
        AlertDialog(
            onDismissRequest = { showExportSummaryDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = IndigoPrimary)
                    Text("APK Compilation & ZIP Export", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Follow these quick steps to generate your standalone APK file or export all scripts as a ZIP archive:",
                        color = Slate300,
                        fontSize = 13.sp
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = CodeBg
                    ) {
                        Text(
                            text = guide,
                            color = Slate300,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            ZipExporter.shareZipFile(context, project)
                            showExportSummaryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share ZIP", color = IndigoDark, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Build Guide", guide)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Commands copied to clipboard", Toast.LENGTH_SHORT).show()
                            showExportSummaryDialog = false
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = Slate100)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportSummaryDialog = false }) {
                    Text("Close", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
