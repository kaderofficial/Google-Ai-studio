package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.WebProject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ProjectFileItem(
    val relativePath: String,
    val content: String,
    val fileType: String,
    val description: String,
    val icon: String
)

data class ZipArchiveSummary(
    val fileName: String,
    val totalFiles: Int,
    val totalUncompressedBytes: Long,
    val formattedSize: String,
    val fileItems: List<ProjectFileItem>
)

object ZipExporter {

    fun getAllProjectFiles(project: WebProject): List<ProjectFileItem> {
        val packageFolder = project.packageName.replace('.', '/')

        return listOf(
            ProjectFileItem(
                relativePath = "build.sh",
                content = CodeGenerator.generateBuildScript(project),
                fileType = "Shell Script",
                description = "Automated Bash script to compile debug & release APKs",
                icon = "⚡"
            ),
            ProjectFileItem(
                relativePath = "FastBuildGuide.md",
                content = CodeGenerator.generateBuildGuide(project),
                fileType = "Markdown",
                description = "Quick-start guide with Gradle & AI Studio commands",
                icon = "🚀"
            ),
            ProjectFileItem(
                relativePath = "README.md",
                content = CodeGenerator.generateReadme(project),
                fileType = "Markdown",
                description = "Project overview, features, and documentation",
                icon = "📖"
            ),
            ProjectFileItem(
                relativePath = "project.json",
                content = CodeGenerator.generateProjectJson(project),
                fileType = "JSON Config",
                description = "WebToAPK package manifest and feature configuration",
                icon = "📦"
            ),
            ProjectFileItem(
                relativePath = "settings.gradle.kts",
                content = CodeGenerator.generateSettingsGradle(project),
                fileType = "Gradle Script",
                description = "Root Gradle settings and module inclusions",
                icon = "🐘"
            ),
            ProjectFileItem(
                relativePath = "build.gradle.kts",
                content = CodeGenerator.generateRootBuildGradle(),
                fileType = "Gradle Script",
                description = "Top-level root build script and Gradle plugins",
                icon = "🐘"
            ),
            ProjectFileItem(
                relativePath = "gradle.properties",
                content = CodeGenerator.generateGradleProperties(),
                fileType = "Properties",
                description = "JVM memory configuration and AndroidX build flags",
                icon = "⚙️"
            ),
            ProjectFileItem(
                relativePath = "app/build.gradle.kts",
                content = CodeGenerator.generateBuildGradle(project),
                fileType = "Gradle Script",
                description = "App module dependencies, target SDK, and packaging",
                icon = "🐘"
            ),
            ProjectFileItem(
                relativePath = "app/proguard-rules.pro",
                content = CodeGenerator.generateProguardRules(),
                fileType = "ProGuard",
                description = "R8/ProGuard obfuscation rules for WebView bridge",
                icon = "🛡️"
            ),
            ProjectFileItem(
                relativePath = "app/src/main/AndroidManifest.xml",
                content = CodeGenerator.generateManifest(project),
                fileType = "XML Manifest",
                description = "Android OS permissions, intents, and screen orientations",
                icon = "📄"
            ),
            ProjectFileItem(
                relativePath = "app/src/main/java/$packageFolder/MainActivity.kt",
                content = CodeGenerator.generateMainActivity(project),
                fileType = "Kotlin Source",
                description = "Native WebView Activity with cache & script injection",
                icon = "☕"
            ),
            ProjectFileItem(
                relativePath = "app/src/main/res/layout/activity_main.xml",
                content = CodeGenerator.generateActivityLayout(project),
                fileType = "XML Layout",
                description = "SwipeRefreshLayout, WebView, and ProgressBar views",
                icon = "🎨"
            ),
            ProjectFileItem(
                relativePath = "app/src/main/assets/offline_fallback.html",
                content = CodeGenerator.generateOfflineHtml(project),
                fileType = "HTML / Web",
                description = "Offline fallback webpage displayed when disconnected",
                icon = "🌐"
            )
        )
    }

    fun getZipSummary(project: WebProject): ZipArchiveSummary {
        val files = getAllProjectFiles(project)
        val totalBytes = files.sumOf { it.content.toByteArray(Charsets.UTF_8).size.toLong() }
        val sanitizedName = project.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "WebToAPK" }
        val fileName = "${sanitizedName}_v${project.versionName}_source.zip"

        val formattedSize = when {
            totalBytes < 1024 -> "$totalBytes B"
            totalBytes < 1024 * 1024 -> String.format("%.1f KB", totalBytes / 1024.0)
            else -> String.format("%.2f MB", totalBytes / (1024.0 * 1024.0))
        }

        return ZipArchiveSummary(
            fileName = fileName,
            totalFiles = files.size,
            totalUncompressedBytes = totalBytes,
            formattedSize = formattedSize,
            fileItems = files
        )
    }

    fun createZipArchive(context: Context, project: WebProject): File {
        val sanitizedName = project.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "WebToAPK" }
        val zipFileName = "${sanitizedName}_v${project.versionName}_source.zip"
        val zipFile = File(context.cacheDir, zipFileName)

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val files = getAllProjectFiles(project)
            for (fileItem in files) {
                val entry = ZipEntry(fileItem.relativePath)
                zos.putNextEntry(entry)
                zos.write(fileItem.content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return zipFile
    }

    fun shareZipFile(context: Context, project: WebProject): Result<File> {
        return try {
            val zipFile = createZipArchive(context, project)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "${project.title} - Android Project ZIP")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the full Android project source & script files ZIP for ${project.title} (v${project.versionName})."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Script & Project ZIP")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Result.success(zipFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun saveZipToDownloads(context: Context, project: WebProject): Result<String> {
        return try {
            val summary = getZipSummary(project)
            val files = getAllProjectFiles(project)
            val fileName = summary.fileName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/WebToAPK")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(Exception("Failed to create download record"))

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    ZipOutputStream(os).use { zos ->
                        for (fileItem in files) {
                            val entry = ZipEntry(fileItem.relativePath)
                            zos.putNextEntry(entry)
                            zos.write(fileItem.content.toByteArray(Charsets.UTF_8))
                            zos.closeEntry()
                        }
                    }
                }
                Result.success("Saved $fileName to Downloads/WebToAPK")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, fileName)
                ZipOutputStream(FileOutputStream(targetFile)).use { zos ->
                    for (fileItem in files) {
                        val entry = ZipEntry(fileItem.relativePath)
                        zos.putNextEntry(entry)
                        zos.write(fileItem.content.toByteArray(Charsets.UTF_8))
                        zos.closeEntry()
                    }
                }
                Result.success("Saved $fileName to Downloads")
            }
        } catch (e: Exception) {
            // Fallback to internal cache and return path
            val fallbackFile = createZipArchive(context, project)
            Result.success("Generated ZIP in cache: ${fallbackFile.name}")
        }
    }
}
