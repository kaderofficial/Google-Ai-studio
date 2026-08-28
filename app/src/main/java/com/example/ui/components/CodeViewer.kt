package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

enum class CodeFileType(val fileName: String, val extension: String, val icon: String) {
    MANIFEST("AndroidManifest.xml", "xml", "📄"),
    MAIN_ACTIVITY("MainActivity.kt", "kt", "☕"),
    BUILD_GRADLE("build.gradle.kts", "kts", "🐘"),
    OFFLINE_HTML("offline_fallback.html", "html", "🌐"),
    BUILD_GUIDE("FastBuildGuide.md", "md", "🚀"),
    PROJECT_JSON("project.json", "json", "📦")
}

@Composable
fun CodeViewer(
    project: WebProject,
    modifier: Modifier = Modifier
) {
    var selectedFile by remember { mutableStateOf(CodeFileType.MANIFEST) }
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }

    val codeContent = remember(project, selectedFile) {
        when (selectedFile) {
            CodeFileType.MANIFEST -> CodeGenerator.generateManifest(project)
            CodeFileType.MAIN_ACTIVITY -> CodeGenerator.generateMainActivity(project)
            CodeFileType.BUILD_GRADLE -> CodeGenerator.generateBuildGradle(project)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // File Tabs Row
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
                Tab(
                    selected = selectedFile == fileType,
                    onClick = { selectedFile = fileType },
                    modifier = Modifier.testTag("code_tab_${fileType.name}"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(fileType.icon, fontSize = 14.sp)
                            Text(
                                text = fileType.fileName,
                                fontSize = 12.sp,
                                fontWeight = if (selectedFile == fileType) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFile == fileType) CyanLight else Slate400,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                )
            }
        }

        // Code Action Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
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
                            .background(IndigoLight)
                    )
                    Text(
                        text = selectedFile.fileName,
                        color = Slate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
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
                        contentColor = if (copiedFeedback) EmeraldAccent else CyanLight
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
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
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

private fun formatSyntaxHighlight(code: String, ext: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { index, line ->
            var remaining = line

            if (remaining.trimStart().startsWith("//") || remaining.trimStart().startsWith("<!--") || remaining.trimStart().startsWith("#")) {
                withStyle(SpanStyle(color = CodeComment)) {
                    append(remaining)
                }
            } else {
                val tokens = remaining.split(Regex("(?<=[\\s(),.:;\"=<>{}])|(?=[\\s(),.:;\"=<>{}])"))
                tokens.forEach { token ->
                    when {
                        token in listOf("package", "import", "class", "fun", "override", "val", "var", "private", "public", "object", "return", "if", "else", "true", "false", "plugins", "android", "dependencies", "defaultConfig", "buildTypes") -> {
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
