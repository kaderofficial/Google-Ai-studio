package com.example

import com.example.data.model.WebProject
import com.example.util.ZipExporter
import org.junit.Assert.*
import org.junit.Test

class ZipExporterTest {

    @Test
    fun testZipExporter_includesAllRequiredScriptFiles() {
        val project = WebProject(
            id = 1,
            title = "Test Web App",
            url = "https://example.com",
            packageName = "com.test.webapp",
            versionName = "1.0.0",
            versionCode = 1
        )

        val files = ZipExporter.getAllProjectFiles(project)
        val relativePaths = files.map { it.relativePath }

        // Verify key script and config files are present in the ZIP manifest
        assertTrue(relativePaths.contains("build.sh"))
        assertTrue(relativePaths.contains("FastBuildGuide.md"))
        assertTrue(relativePaths.contains("README.md"))
        assertTrue(relativePaths.contains("project.json"))
        assertTrue(relativePaths.contains("settings.gradle.kts"))
        assertTrue(relativePaths.contains("build.gradle.kts"))
        assertTrue(relativePaths.contains("gradle.properties"))
        assertTrue(relativePaths.contains("app/build.gradle.kts"))
        assertTrue(relativePaths.contains("app/proguard-rules.pro"))
        assertTrue(relativePaths.contains("app/src/main/AndroidManifest.xml"))
        assertTrue(relativePaths.contains("app/src/main/java/com/test/webapp/MainActivity.kt"))
        assertTrue(relativePaths.contains("app/src/main/res/layout/activity_main.xml"))
        assertTrue(relativePaths.contains("app/src/main/assets/offline_fallback.html"))

        val summary = ZipExporter.getZipSummary(project)
        assertEquals(files.size, summary.totalFiles)
        assertTrue(summary.totalUncompressedBytes > 0)
        assertTrue(summary.fileName.endsWith(".zip"))
    }
}
