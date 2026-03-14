package com.shkpo.accessiblestring

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class ArbEntry(
    val key: String,
    val argNames: List<String>,
    val labelValue: String,
    val readerValue: String?,
)

data class ArbParseResult(val entries: List<ArbEntry>)

class ArbParser : IArbParser {
    private val placeholderRegex = Regex("""\{(\w+)\}""")

    override fun parse(
        projectBasePath: String,
        config: AccessibleGenConfig,
    ): ArbParseResult? {
        val arbDirFile = File(projectBasePath, config.arbDir)
        if (!arbDirFile.exists() || !arbDirFile.isDirectory) return null

        val masterFile = findMasterArbFile(arbDirFile, config.masterLocale) ?: return null
        return parseArbFile(masterFile, config.readerSuffix)
    }

    internal fun findMasterArbFile(
        arbDir: File,
        masterLocale: String,
    ): File? {
        val arbFiles = arbDir.listFiles { f -> f.extension == "arb" } ?: return null
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type

        for (file in arbFiles) {
            try {
                val map: Map<String, Any> = gson.fromJson(file.readText(), mapType) ?: continue
                if (map["@@locale"] as? String == masterLocale) return file
            } catch (_: Exception) {
                // skip unparseable
            }
        }
        return null
    }

    internal fun parseArbFile(
        file: File,
        readerSuffix: String,
    ): ArbParseResult {
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(file.readText(), mapType) ?: emptyMap()

        val allKeys = map.keys.filter { !it.startsWith("@") }
        val readerKeys = allKeys.filter { it.endsWith(readerSuffix) }.toSet()
        val uiKeyNames = allKeys.filter { !it.endsWith(readerSuffix) }

        val entries =
            uiKeyNames.map { key ->
                val labelValue = map[key] as? String ?: ""
                val argNames = placeholderRegex.findAll(labelValue).map { it.groupValues[1] }.toList()
                val readerValue =
                    if (readerKeys.contains("$key$readerSuffix")) {
                        map["$key$readerSuffix"] as? String
                    } else {
                        null
                    }
                ArbEntry(key = key, argNames = argNames, labelValue = labelValue, readerValue = readerValue)
            }

        return ArbParseResult(entries = entries)
    }
}
