package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WebToApkViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: WebToApkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: WebToApkViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val editingProject by viewModel.editingProject.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = Slate800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                        ) {
                            Text(
                                text = "⚡ WebToAPK",
                                color = CyanLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = when (currentTab) {
                                AppTab.DASHBOARD -> "App Packages"
                                AppTab.CONFIGURATOR -> "Manifest Studio"
                                AppTab.EMULATOR -> "Live Preview"
                                AppTab.CODE_EXPORT -> "Source Code"
                                AppTab.AI_STUDIO_TOOLS -> "AI Studio Tools"
                            },
                            color = Slate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (currentTab == AppTab.CONFIGURATOR) {
                        IconButton(
                            onClick = { viewModel.saveCurrentProject() },
                            modifier = Modifier.testTag("topbar_save_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Project", tint = CyanLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Slate100
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = Slate400,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = getTabIcon(tab, isSelected),
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoDark,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Slate800,
                        contentColor = Slate100,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    projects = allProjects,
                    onNavigateToTab = { viewModel.setTab(it) }
                )
                AppTab.CONFIGURATOR -> ConfiguratorScreen(
                    viewModel = viewModel,
                    project = editingProject,
                    onNavigateToTab = { viewModel.setTab(it) }
                )
                AppTab.EMULATOR -> EmulatorScreen(
                    viewModel = viewModel,
                    project = editingProject
                )
                AppTab.CODE_EXPORT -> CodeExportScreen(
                    project = editingProject
                )
                AppTab.AI_STUDIO_TOOLS -> ToolsScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.setTab(it) }
                )
            }
        }
    }
}

private fun getTabIcon(tab: AppTab, isSelected: Boolean): ImageVector {
    return when (tab) {
        AppTab.DASHBOARD -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
        AppTab.CONFIGURATOR -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
        AppTab.EMULATOR -> if (isSelected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
        AppTab.CODE_EXPORT -> if (isSelected) Icons.Filled.Code else Icons.Outlined.Code
        AppTab.AI_STUDIO_TOOLS -> if (isSelected) Icons.Filled.BuildCircle else Icons.Outlined.BuildCircle
    }
}
