package com.shkpo.accessiblestring

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ConfigReaderTest {

    private val reader = ConfigReader()

    @Test
    fun `read returns null when file does not exist`(@TempDir tempDir: Path) {
        val result = reader.read(tempDir.toString())
        assertNull(result)
    }

    @Test
    fun `read returns null when accessible_gen key is missing`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), "accessible_gen.yaml").writeText("other_key: {}")
        val result = reader.read(tempDir.toString())
        assertNull(result)
    }

    @Test
    fun `read returns null when YAML is invalid`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), "accessible_gen.yaml").writeText(":\ninvalid:\n  - yaml: [")
        val result = reader.read(tempDir.toString())
        assertNull(result)
    }

    @Test
    fun `read returns config with all fields when fully specified`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), "accessible_gen.yaml").writeText("""
            accessible_gen:
              arb_dir: lib/i18n
              master_locale: en
              reader_suffix: A11y
              trigger_file: lib/gen/l18n.dart
              output_dir: lib/gen/accessible
        """.trimIndent())

        val result = reader.read(tempDir.toString())

        assertNotNull(result)
        assertEquals("lib/i18n", result!!.arbDir)
        assertEquals("en", result.masterLocale)
        assertEquals("A11y", result.readerSuffix)
        assertEquals("lib/gen/l18n.dart", result.triggerFile)
        assertEquals("lib/gen/accessible", result.outputDir)
    }

    @Test
    fun `read returns defaults for missing fields`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), "accessible_gen.yaml").writeText("accessible_gen: {}")
        val result = reader.read(tempDir.toString())

        assertNotNull(result)
        assertEquals("lib/l10n", result!!.arbDir)
        assertEquals("ja", result.masterLocale)
        assertEquals("Reader", result.readerSuffix)
        assertEquals("lib/generated/l18n.dart", result.triggerFile)
        assertEquals("lib/generated/accessible", result.outputDir)
    }
}
