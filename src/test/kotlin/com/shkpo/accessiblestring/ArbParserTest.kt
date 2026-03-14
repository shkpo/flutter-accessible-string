package com.shkpo.accessiblestring

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ArbParserTest {

    private val parser = ArbParser()
    private val defaultConfig = AccessibleGenConfig()

    // --- parse() ---

    @Test
    fun `parse returns null when arbDir does not exist`(@TempDir tempDir: Path) {
        val config = defaultConfig.copy(arbDir = "nonexistent")
        assertNull(parser.parse(tempDir.toString(), config))
    }

    @Test
    fun `parse returns null when arbDir is a file not a directory`(@TempDir tempDir: Path) {
        val file = File(tempDir.toFile(), "lib").also { it.writeText("x") }
        val config = defaultConfig.copy(arbDir = "lib")
        assertNull(parser.parse(tempDir.toString(), config))
    }

    @Test
    fun `parse returns null when no ARB file matches masterLocale`(@TempDir tempDir: Path) {
        val arbDir = File(tempDir.toFile(), "lib/l10n").also { it.mkdirs() }
        arbDir.resolve("intl_en.arb").writeText("""{"@@locale": "en"}""")
        val config = defaultConfig.copy(masterLocale = "ja")
        assertNull(parser.parse(tempDir.toString(), config))
    }

    @Test
    fun `parse returns null when no ARB files exist in dir`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), "lib/l10n").mkdirs()
        assertNull(parser.parse(tempDir.toString(), defaultConfig))
    }

    @Test
    fun `parse succeeds when master locale ARB is found`(@TempDir tempDir: Path) {
        val arbDir = File(tempDir.toFile(), "lib/l10n").also { it.mkdirs() }
        arbDir.resolve("intl_ja.arb").writeText("""{"@@locale": "ja", "hoge": "ほげ"}""")

        val result = parser.parse(tempDir.toString(), defaultConfig)

        assertNotNull(result)
        assertEquals(1, result!!.uiKeys.size)
        assertEquals("hoge", result.uiKeys[0].key)
    }

    // --- findMasterArbFile() ---

    @Test
    fun `findMasterArbFile skips unparseable ARB files`(@TempDir tempDir: Path) {
        val arbDir = tempDir.toFile().also { it.mkdirs() }
        arbDir.resolve("bad.arb").writeText("not json {{{")
        arbDir.resolve("good.arb").writeText("""{"@@locale": "ja"}""")

        val result = parser.findMasterArbFile(arbDir, "ja")
        assertNotNull(result)
        assertEquals("good.arb", result!!.name)
    }

    @Test
    fun `findMasterArbFile returns null when listFiles returns null`(@TempDir tempDir: Path) {
        // arbDir が空でファイルなし
        val result = parser.findMasterArbFile(tempDir.toFile(), "ja")
        assertNull(result)
    }

    // --- parseArbFile() ---

    @Test
    fun `parseArbFile excludes reader-only keys from uiKeys`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("""{"@@locale":"ja","btnOk":"OK","btnOkReader":"オーケー"}""")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertEquals(listOf("btnOk"), result.uiKeys.map { it.key })
    }

    @Test
    fun `parseArbFile detects paired keys`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("""{"@@locale":"ja","btnOk":"OK","btnOkReader":"オーケー","btnBack":"戻る"}""")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertEquals(setOf("btnOk"), result.pairedKeys)
        assertFalse(result.pairedKeys.contains("btnBack"))
    }

    @Test
    fun `parseArbFile extracts argument names from placeholders`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("""{"@@locale":"ja","greeting":"こんにちは {name}、{age}歳"}""")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertEquals(listOf("name", "age"), result.uiKeys[0].argNames)
    }

    @Test
    fun `parseArbFile handles key with no args`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("""{"@@locale":"ja","btnOk":"OK"}""")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertTrue(result.uiKeys[0].argNames.isEmpty())
    }

    @Test
    fun `parseArbFile handles null map gracefully`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("null")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertTrue(result.uiKeys.isEmpty())
        assertTrue(result.pairedKeys.isEmpty())
    }

    @Test
    fun `parseArbFile skips metadata at-prefixed keys`(@TempDir tempDir: Path) {
        val arb = tempDir.toFile().resolve("test.arb").also {
            it.writeText("""{"@@locale":"ja","@btnOk":{"description":"ok"},"btnOk":"OK"}""")
        }
        val result = parser.parseArbFile(arb, "Reader")
        assertEquals(1, result.uiKeys.size)
        assertEquals("btnOk", result.uiKeys[0].key)
    }
}
