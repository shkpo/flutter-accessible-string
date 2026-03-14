package com.shkpo.accessiblestring

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class DartCodeGeneratorTest {

    private val vfsRefresher = mockk<IVfsRefresher>(relaxed = true)
    private val generator = DartCodeGenerator(vfsRefresher)

    private val defaultConfig = AccessibleGenConfig(
        outputDir = "lib/generated/accessible",
        triggerFile = "lib/generated/l18n.dart"
    )

    // --- generate() ---

    @Test
    fun `generate creates output directory if not exists`(@TempDir tempDir: Path) {
        val result = ArbParseResult(uiKeys = emptyList(), pairedKeys = emptySet())
        generator.generate(tempDir.toString(), defaultConfig, result)

        assertTrue(File(tempDir.toFile(), "lib/generated/accessible").isDirectory)
    }

    @Test
    fun `generate writes s_accessible_g_dart file`(@TempDir tempDir: Path) {
        val result = ArbParseResult(uiKeys = emptyList(), pairedKeys = emptySet())
        generator.generate(tempDir.toString(), defaultConfig, result)

        assertTrue(File(tempDir.toFile(), "lib/generated/accessible/s_accessible.g.dart").exists())
    }

    @Test
    fun `generate calls vfsRefresher with output file`(@TempDir tempDir: Path) {
        val result = ArbParseResult(uiKeys = emptyList(), pairedKeys = emptySet())
        generator.generate(tempDir.toString(), defaultConfig, result)

        val expectedFile = File(tempDir.toFile(), "lib/generated/accessible/s_accessible.g.dart")
        verify { vfsRefresher.refresh(expectedFile) }
    }

    // --- buildDartCode() ---

    @Test
    fun `buildDartCode contains correct import path`() {
        val result = ArbParseResult(uiKeys = emptyList(), pairedKeys = emptySet())
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("import '../l18n.dart';"))
    }

    @Test
    fun `buildDartCode generates getter for unpaired key`() {
        val result = ArbParseResult(
            uiKeys = listOf(ArbEntry("btnOk", emptyList())),
            pairedKeys = emptySet()
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("AccessibleString get btnOk => AccessibleString("))
        assertFalse(code.contains("semanticsLabel: btnOkReader"))
    }

    @Test
    fun `buildDartCode generates getter with semanticsLabel for paired key`() {
        val result = ArbParseResult(
            uiKeys = listOf(ArbEntry("btnOk", emptyList())),
            pairedKeys = setOf("btnOk")
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("semanticsLabel: btnOkReader,"))
    }

    @Test
    fun `buildDartCode generates method for unpaired key with args`() {
        val result = ArbParseResult(
            uiKeys = listOf(ArbEntry("greeting", listOf("name"))),
            pairedKeys = emptySet()
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("AccessibleString greeting(dynamic name) => AccessibleString("))
        assertFalse(code.contains("semanticsLabel: greetingReader"))
    }

    @Test
    fun `buildDartCode generates method with semanticsLabel for paired key with args`() {
        val result = ArbParseResult(
            uiKeys = listOf(ArbEntry("greeting", listOf("name"))),
            pairedKeys = setOf("greeting")
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("semanticsLabel: greetingReader(name),"))
    }

    @Test
    fun `buildDartCode uses custom readerSuffix`() {
        val result = ArbParseResult(
            uiKeys = listOf(ArbEntry("btnOk", emptyList())),
            pairedKeys = setOf("btnOk")
        )
        val code = generator.buildDartCode("../l18n.dart", "A11y", result)

        assertTrue(code.contains("semanticsLabel: btnOkA11y,"))
    }
}
