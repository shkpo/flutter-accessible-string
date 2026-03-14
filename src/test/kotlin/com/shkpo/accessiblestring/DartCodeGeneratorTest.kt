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
        val result = ArbParseResult(entries = emptyList())
        generator.generate(tempDir.toString(), defaultConfig, result)

        assertTrue(File(tempDir.toFile(), "lib/generated/accessible").isDirectory)
    }

    @Test
    fun `generate writes s_accessible_g_dart file`(@TempDir tempDir: Path) {
        val result = ArbParseResult(entries = emptyList())
        generator.generate(tempDir.toString(), defaultConfig, result)

        assertTrue(File(tempDir.toFile(), "lib/generated/accessible/s_accessible.g.dart").exists())
    }

    @Test
    fun `generate calls vfsRefresher with output file`(@TempDir tempDir: Path) {
        val result = ArbParseResult(entries = emptyList())
        generator.generate(tempDir.toString(), defaultConfig, result)

        val expectedFile = File(tempDir.toFile(), "lib/generated/accessible/s_accessible.g.dart")
        verify { vfsRefresher.refresh(expectedFile) }
    }

    // --- buildDartCode() ---

    @Test
    fun `buildDartCode contains correct import path`() {
        val result = ArbParseResult(entries = emptyList())
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("import '../l18n.dart';"))
    }

    @Test
    fun `buildDartCode contains flutter widgets import`() {
        val result = ArbParseResult(entries = emptyList())
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("import 'package:flutter/widgets.dart';"))
    }

    @Test
    fun `buildDartCode generates SR class with static of method`() {
        val result = ArbParseResult(entries = emptyList())
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("class SR {"))
        assertTrue(code.contains("final S _s;"))
        assertTrue(code.contains("SR(this._s);"))
        assertTrue(code.contains("static SR of(BuildContext context) => SR(S.of(context));"))
    }

    @Test
    fun `buildDartCode generates getter for unpaired key`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("btnOk", emptyList(), labelValue = "OK", readerValue = null))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("AccessibleString get btnOk => AccessibleString("))
        assertTrue(code.contains("label: _s.btnOk,"))
        assertFalse(code.contains("semanticsLabel: _s.btnOkReader"))
    }

    @Test
    fun `buildDartCode generates getter with semanticsLabel for paired key`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("btnOk", emptyList(), labelValue = "OK", readerValue = "オーケー"))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("semanticsLabel: _s.btnOkReader,"))
    }

    @Test
    fun `buildDartCode generates method for unpaired key with args`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("greeting", listOf("name"), labelValue = "こんにちは {name}", readerValue = null))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("AccessibleString greeting(dynamic name) => AccessibleString("))
        assertTrue(code.contains("label: _s.greeting(name),"))
        assertFalse(code.contains("semanticsLabel: _s.greetingReader"))
    }

    @Test
    fun `buildDartCode generates method with semanticsLabel for paired key with args`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("greeting", listOf("name"), labelValue = "こんにちは {name}", readerValue = "グリーティング {name}"))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("semanticsLabel: _s.greetingReader(name),"))
    }

    @Test
    fun `buildDartCode uses custom readerSuffix`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("btnOk", emptyList(), labelValue = "OK", readerValue = "オーケー"))
        )
        val code = generator.buildDartCode("../l18n.dart", "A11y", result)

        assertTrue(code.contains("semanticsLabel: _s.btnOkA11y,"))
    }

    @Test
    fun `buildDartCode includes label value comment for unpaired key`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("hoge", emptyList(), labelValue = "ほーじ", readerValue = null))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("// label: \"ほーじ\""))
        assertFalse(code.contains("// semanticsLabel:"))
    }

    @Test
    fun `buildDartCode includes both label and semanticsLabel comments for paired key`() {
        val result = ArbParseResult(
            entries = listOf(ArbEntry("btnAgreeToTos", emptyList(), labelValue = "同意", readerValue = "同意。このボタンは利用規約を最後までスクロールすると有効"))
        )
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertTrue(code.contains("// label: \"同意\""))
        assertTrue(code.contains("// semanticsLabel: \"同意。このボタンは利用規約を最後までスクロールすると有効\""))
    }

    @Test
    fun `buildDartCode does not contain extension SAccessible`() {
        val result = ArbParseResult(entries = emptyList())
        val code = generator.buildDartCode("../l18n.dart", "Reader", result)

        assertFalse(code.contains("extension SAccessible"))
        assertFalse(code.contains("on S {"))
    }
}
