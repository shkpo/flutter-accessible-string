package com.shkpo.accessiblestring

import org.yaml.snakeyaml.Yaml
import java.io.File

data class AccessibleGenConfig(
    val arbDir: String = "lib/l10n",
    val masterLocale: String = "ja",
    val readerSuffix: String = "Reader",
    val triggerFile: String = "lib/generated/l10n.dart",
    val outputDir: String = "lib/generated/accessible",
)

class ConfigReader : IConfigReader {
    override fun read(projectBasePath: String): AccessibleGenConfig? {
        val configFile = File(projectBasePath, "accessible_gen.yaml")
        if (!configFile.exists()) return null

        return try {
            val yaml = Yaml()
            val rawMap: Map<String, Any> = yaml.load(configFile.inputStream()) ?: return null

            @Suppress("UNCHECKED_CAST")
            val genMap = rawMap["accessible_gen"] as? Map<String, Any> ?: return null

            AccessibleGenConfig(
                arbDir = genMap["arb_dir"] as? String ?: "lib/l10n",
                masterLocale = genMap["master_locale"] as? String ?: "ja",
                readerSuffix = genMap["reader_suffix"] as? String ?: "Reader",
                triggerFile = genMap["trigger_file"] as? String ?: "lib/generated/l18n.dart",
                outputDir = genMap["output_dir"] as? String ?: "lib/generated/accessible",
            )
        } catch (e: Exception) {
            null
        }
    }
}
