package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WebToApkViewModel

@Composable
fun ToolsScreen(
    viewModel: WebToApkViewModel,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val analysisResult by viewModel.analysisResult.collectAsState()
    var testUrlInput by remember { mutableStateOf("https://aistudio.google.com") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AI Studio Tools & Diagnostics",
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inspect web readiness, SSL certs, and mobile compatibility for APK packaging",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        // Web Health & Compatibility Analyzer Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = CyanLight, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Web App Health & Security Inspector", color = Slate100, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Test any site before generating Android build", color = Slate400, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testUrlInput,
                            onValueChange = { testUrlInput = it },
                            placeholder = { Text("https://your-url.com", color = Slate600, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analyzer_url_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate850,
                                unfocusedContainerColor = Slate850,
                                focusedBorderColor = CyanLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            ),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.analyzeUrl(testUrlInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("analyze_button")
                        ) {
                            Text("Analyze", color = IndigoDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Analysis Score & Checks
                    if (analysisResult != null) {
                        val res = analysisResult!!
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Slate850,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Compatibility & Readiness Score", color = Slate200, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (res.securityScore >= 80) EmeraldAccent.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${res.securityScore}/100",
                                            color = if (res.securityScore >= 80) EmeraldAccent else AmberAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { res.securityScore / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (res.securityScore >= 80) EmeraldAccent else AmberAccent,
                                    trackColor = Slate800
                                )

                                Divider(color = Slate700, thickness = 1.dp)

                                res.recommendations.forEach { rec ->
                                    Text(
                                        text = rec,
                                        color = Slate300,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.quickConvertUrl(res.url)
                                            onNavigateToTab(AppTab.CONFIGURATOR)
                                        }
                                    ) {
                                        Text("Create Package with this URL", color = CyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanLight, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Best Practices & AI Studio Guides Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IndigoLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Best Practices for Web to APK", color = Slate100, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Tips to pass Google Play & optimize UX", color = Slate400, fontSize = 12.sp)
                        }
                    }

                    val tips = listOf(
                        "🔒 Always serve web apps over HTTPS with valid SSL certificates to avoid Mixed Content block in Android 9+.",
                        "📱 Include `<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">` in your HTML `<head>` tag for responsive scaling.",
                        "⚡ Enable DOM Storage (`domStorageEnabled = true`) if your web app uses Firebase Auth, LocalStorage, or session tokens.",
                        "🔄 Keep Pull-to-Refresh enabled so users can easily reconnect if their network temporarily drops.",
                        "🎨 Match the Android Status Bar color in your Theme to your website's header background for a seamless native look."
                    )

                    tips.forEach { tip ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Slate850
                        ) {
                            Text(
                                text = tip,
                                color = Slate300,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
