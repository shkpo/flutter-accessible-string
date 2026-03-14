package com.shkpo.accessiblestring

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class ArbEntry(
    val key: String,
    val argNames: List<String>
)

data class ArbParseResult(
    val uiKeys: List<ArbEntry>,
    val pairedKeys: Set<String>
)

object ArbParser {

    private val PLACEHOLDER_REGEX = Regex("""\{(\w+)\}""")

    fun parse(projectBasePath: String, config: AccessibleGenConfig): ArbParseResult? {
        val arbDirFile = File(projectBasePath, config.arbDir)
        if (!arbDirFile.exists() || !arbDirFile.isDirectory) return null

        val masterFile = findMasterArbFile(arbDirFile, config.masterLocale) ?: return null

        return parseArbFile(masterFile, config.readerSuffix)
    }

    private fun findMasterArbFile(arbDir: File, masterLocale: String): File? {
        val arbFiles = arbDir.listFiles { f -> f.extension == "arb" } ?: return null
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type

        for (file in arbFiles) {
            try {
                val map: Map<String, Any> = gson.fromJson(file.readText(), mapType) ?: continue
                val locale = map["@@locale"] as? String
                if (locale == masterLocale) return file
            } catch (_: Exception) {
                // Skip unparseable files
            }
        }
        return null
    }

    private fun parseArbFile(file: File, readerSuffix: String): ArbParseResult {
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(file.readText(), mapType) ?: emptyMap()

        // Extract all user-defined keys (skip @-prefixed metadata and @@locale)
        val allKeys = map.keys.filter { key ->
            !key.startsWith("@")
        }

        // Identify Reader keys
        val readerKeys = allKeys.filter { it.endsWith(readerSuffix) }.toSet()

        // UI keys = all keys that don't end with readerSuffix
        val uiKeyNames = allKeys.filter { !it.endsWith(readerSuffix) }

        // Paired keys = UI keys that have a matching {key}{readerSuffix}
        val pairedKeys = uiKeyNames.filter { key ->
            readerKeys.contains("$key$readerSuffix")
        }.toSet()

        val uiKeys = uiKeyNames.map { key ->
            val value = map[key] as? String ?: ""
            val argNames = PLACEHOLDER_REGEX.findAll(value).map { it.groupValues[1] }.toList()
            ArbEntry(key = key, argNames = argNames)
        }

        return ArbParseResult(uiKeys = uiKeys, pairedKeys = pairedKeys)
    }
}
