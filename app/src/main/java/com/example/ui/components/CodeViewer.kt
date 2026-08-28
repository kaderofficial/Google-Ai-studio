package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebProject
import com.example.ui.theme.*
import com.example.util.CodeGenerator
import com.example.util.ProjectFileItem
import com.example.util.ZipExporter

enum class CodeFileType(val fileName: String, val extension: String, val icon: String, val badge: String) {
    PROJECT_ZIP("📦 Full Project.zip", "zip", "📦", "ZIP Bundle"),
    BUILD_SCRIPT("build.sh", "sh", "⚡", "Script"),
    MANIFEST("AndroidManifest.xml", "xml", "📄", "Manifest"),
    MAIN_ACTIVITY("MainActivity.kt", "kt", "☕", "Kotlin"),
    BUILD_GRADLE("app/build.gradle.kts", "kts", "🐘", "Gradle"),
    SETTINGS_GRADLE("settings.gradle.kts", "kts", "🐘", "Settings"),
    ROOT_BUILD_GRADLE("build.gradle.kts", "kts", "🐘", "Root Gradle"),
    GRADLE_PROPERTIES("gradle.properties", "properties", "⚙️", "Config"),
    PROGUARD("proguard-rules.pro", "pro", "🛡️", "R8 Rules"),
    ACTIVITY_LAYOUT("activity_main.xml", "xml", "🎨", "Layout"),
    OFFLINE_HTML("offline_fallback.html", "html", "🌐", "HTML"),
    BUILD_GUIDE("FastBuildGuide.md", "md", "🚀", "Guide"),
    PROJECT_JSON("project.json", "json", "📦", "JSON")
}

@Composable
fun CodeViewer(
    project: WebProject,
    modifier: Modifier = Modifier
) {
    var selectedFile by remember { mutableStateOf(CodeFileType.PROJECT_ZIP) }
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }
    var zipExportStatus by remember { mutableStateOf<String?>(null) }

    val zipSummary = remember(project) {
        ZipExporter.getZipSummary(project)
    }

    val codeContent = remember(project, selectedFile) {
        when (selectedFile) {
            CodeFileType.PROJECT_ZIP -> ""
            CodeFileType.BUILD_SCRIPT -> CodeGenerator.generateBuildScript(project)
            CodeFileType.MANIFEST -> CodeGenerator.generateManifest(project)
            CodeFileType.MAIN_ACTIVITY -> CodeGenerator.generateMainActivity(project)
            CodeFileType.BUILD_GRADLE -> CodeGenerator.generateBuildGradle(project)
            CodeFileType.SETTINGS_GRADLE -> CodeGenerator.generateSettingsGradle(project)
            CodeFileType.ROOT_BUILD_GRADLE -> CodeGenerator.generateRootBuildGradle()
            CodeFileType.GRADLE_PROPERTIES -> CodeGenerator.generateGradleProperties()
            CodeFileType.PROGUARD -> CodeGenerator.generateProguardRules()
            CodeFileType.ACTIVITY_LAYOUT -> CodeGenerator.generateActivityLayout(project)
            CodeFileType.OFFLINE_HTML -> CodeGenerator.generateOfflineHtml(project)
            CodeFileType.BUILD_GUIDE -> CodeGenerator.generateBuildGuide(project)
            CodeFileType.PROJECT_JSON -> CodeGenerator.generateProjectJson(project)
        }
    }

    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            kotlinx.coroutines.delay(2000)
            copiedFeedback = false
        }
    }

    LaunchedEffect(zipExportStatus) {
        if (zipExportStatus != null) {
            kotlinx.coroutines.delay(3500)
            zipExportStatus = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // File & Zip Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedFile.ordinal,
            containerColor = Slate900,
            contentColor = IndigoLight,
            edgePadding = 8.dp,
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            CodeFileType.entries.forEach { fileType ->
                val isSelected = selectedFile == fileType
                Tab(
                    selected = isSelected,
                    onClick = { selectedFile = fileType },
                    modifier = Modifier.testTag("code_tab_${fileType.name}"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(fileType.icon, fontSize = 14.sp)
                            Text(
                                text = fileType.fileName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) IndigoPrimary else Slate400,
                                fontFamily = FontFamily.Monospace
                            )
                            if (fileType == CodeFileType.PROJECT_ZIP) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) IndigoDark else Slate800
                                ) {
                                    Text(
                                        text = "${zipSummary.totalFiles} files",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IndigoPrimary else Slate300,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // Action Status Toast if active
        if (zipExportStatus != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Slate800,
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                    Text(text = zipExportStatus ?: "", color = Slate100, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (selectedFile == CodeFileType.PROJECT_ZIP) {
            // Interactive ZIP Bundle Overview & Export Manager
            ZipBundleOverview(
                project = project,
                summary = zipSummary,
                onSelectFile = { item ->
                    val matchingType = CodeFileType.entries.firstOrNull { it.fileName == item.relativePath || it.fileName.endsWith(item.relativePath.substringAfterLast('/')) }
                    if (matchingType != null) {
                        selectedFile = matchingType
                    }
                },
                onShareZip = {
                    val result = ZipExporter.shareZipFile(context, project)
                    if (result.isSuccess) {
                        zipExportStatus = "ZIP Archive prepared & shared successfully!"
                    } else {
                        Toast.makeText(context, "Failed to share ZIP", Toast.LENGTH_SHORT).show()
                    }
                },
                onSaveZip = {
                    val result = ZipExporter.saveZipToDownloads(context, project)
                    if (result.isSuccess) {
                        zipExportStatus = result.getOrNull() ?: "ZIP file saved successfully"
                    } else {
                        Toast.makeText(context, "Error saving ZIP", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Code Action Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(IndigoPrimary)
                        )
                        Text(
                            text = selectedFile.fileName,
                            color = Slate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Slate800
                        ) {
                            Text(
                                text = selectedFile.badge,
                                color = Slate400,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "(${codeContent.lines().size} lines)",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }

                    // Copy Action Button
                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Source Code", codeContent)
                            clipboard.setPrimaryClip(clip)
                            copiedFeedback = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (copiedFeedback) EmeraldAccent.copy(alpha = 0.2f) else Slate800,
                            contentColor = if (copiedFeedback) EmeraldAccent else IndigoPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = if (copiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copiedFeedback) "Copied!" else "Copy Snippet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Code Editor/Display Body with Syntax Highlighting
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = CodeBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line Numbers Gutter
                    Column(
                        modifier = Modifier
                            .background(Slate950)
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        val lineCount = codeContent.lines().size
                        for (i in 1..lineCount) {
                            Text(
                                text = i.toString(),
                                color = Slate600,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Code Text Body
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp)
                            .horizontalScroll(horizontalScroll)
                    ) {
                        Text(
                            text = formatSyntaxHighlight(codeContent, selectedFile.extension),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZipBundleOverview(
    project: WebProject,
    summary: com.example.util.ZipArchiveSummary,
    onSelectFile: (ProjectFileItem) -> Unit,
    onShareZip: () -> Unit,
    onSaveZip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Slate900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card with Primary ZIP Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Slate850,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = summary.fileName,
                                    color = Slate100,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${summary.totalFiles} Bundled Files • ${summary.formattedSize} Uncompressed",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoDark
                        ) {
                            Text(
                                text = "READY",
                                color = IndigoPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onShareZip,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_zip_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share ZIP File", color = IndigoDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        FilledTonalButton(
                            onClick = onSaveZip,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Slate800,
                                contentColor = IndigoPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_zip_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Device", color = IndigoPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // File Manifest Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ZIP ARCHIVE CONTENTS (${summary.totalFiles} FILES):",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Click file to view code",
                    color = Slate500,
                    fontSize = 11.sp
                )
            }

            // Files List in Archive
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(summary.fileItems) { item ->
                    Surface(
                        onClick = { onSelectFile(item) },
                        shape = RoundedCornerShape(10.dp),
                        color = Slate850,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(item.icon, fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = item.relativePath,
                                        color = Slate100,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = item.description,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate800
                                ) {
                                    Text(
                                        text = "${item.content.toByteArray(Charsets.UTF_8).size} B",
                                        color = Slate300,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSyntaxHighlight(code: String, ext: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { index, line ->
            val remaining = line

            if (remaining.trimStart().startsWith("//") || remaining.trimStart().startsWith("<!--") || remaining.trimStart().startsWith("#")) {
                withStyle(SpanStyle(color = CodeComment)) {
                    append(remaining)
                }
            } else {
                val tokens = remaining.split(Regex("(?<=[\\s(),.:;\"=<>{}])|(?=[\\s(),.:;\"=<>{}])"))
                tokens.forEach { token ->
                    when {
                        token in listOf("package", "import", "class", "fun", "override", "val", "var", "private", "public", "object", "return", "if", "else", "true", "false", "plugins", "android", "dependencies", "defaultConfig", "buildTypes", "chmod", "echo", "set", "exit") -> {
                            withStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold)) {
                                append(token)
                            }
                        }
                        token.startsWith("\"") || token.endsWith("\"") -> {
                            withStyle(SpanStyle(color = CodeString)) {
                                append(token)
                            }
                        }
                        token.startsWith("<") || token.startsWith("</") || token.endsWith(">") -> {
                            withStyle(SpanStyle(color = CodeTag, fontWeight = FontWeight.Bold)) {
                                append(token)
                            }
                        }
                        token in listOf("android:name", "android:exported", "android:label", "android:theme", "xmlns:android", "versionCode", "versionName", "applicationId") -> {
                            withStyle(SpanStyle(color = CodeAttr)) {
                                append(token)
                            }
                        }
                        else -> {
                            withStyle(SpanStyle(color = Slate100)) {
                                append(token)
                            }
                        }
                    }
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}
