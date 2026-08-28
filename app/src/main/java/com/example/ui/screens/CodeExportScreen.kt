package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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

@Composable
fun CodeExportScreen(
    project: WebProject,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportSummaryDialog by remember { mutableStateOf(false) }

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
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android Source Code & Build Kit",
                        color = Slate100,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Production Kotlin & Gradle artifacts for ${project.title}",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showExportSummaryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("build_guide_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Build Guide", color = IndigoDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Full Code Viewer
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
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanLight)
                    Text("APK Compilation Instructions", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Follow these quick steps to generate your standalone APK file:",
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
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Build Guide", guide)
                        clipboard.setPrimaryClip(clip)
                        showExportSummaryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Commands", color = IndigoDark, fontWeight = FontWeight.Bold)
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
