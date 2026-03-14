package com.shkpo.accessiblestring

import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Paths

object DartCodeGenerator {

    fun generate(projectBasePath: String, config: AccessibleGenConfig, parseResult: ArbParseResult) {
        val outputDirFile = File(projectBasePath, config.outputDir)
        outputDirFile.mkdirs()

        val outputFile = File(outputDirFile, "s_accessible.g.dart")

        // Compute relative import path from outputDir to triggerFile
        val outputDirPath = Paths.get(outputDirFile.canonicalPath)
        val triggerFilePath = Paths.get(File(projectBasePath, config.triggerFile).canonicalPath)
        val relativePath = outputDirPath.relativize(triggerFilePath).toString().replace(File.separatorChar, '/')

        val dartCode = buildDartCode(relativePath, parseResult)
        outputFile.writeText(dartCode)

        // Refresh the file in IntelliJ's VFS
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFile)
    }

    private fun buildDartCode(importPath: String, parseResult: ArbParseResult): String {
        val sb = StringBuilder()

        sb.appendLine("// GENERATED CODE - DO NOT MODIFY BY HAND")
        sb.appendLine("// ignore_for_file: non_constant_identifier_names")
        sb.appendLine()
        sb.appendLine("import '$importPath';")
        sb.appendLine()
        sb.appendLine("class AccessibleString {")
        sb.appendLine("  final String label;")
        sb.appendLine("  final String semanticsLabel;")
        sb.appendLine()
        sb.appendLine("  const AccessibleString({")
        sb.appendLine("    required this.label,")
        sb.appendLine("    String? semanticsLabel,")
        sb.appendLine("  }) : semanticsLabel = semanticsLabel ?? label;")
        sb.appendLine("}")
        sb.appendLine()
        sb.appendLine("extension SAccessible on S {")

        for (entry in parseResult.uiKeys) {
            val isPaired = parseResult.pairedKeys.contains(entry.key)

            if (entry.argNames.isEmpty()) {
                // Getter form
                sb.appendLine("  // ${if (isPaired) "Paired (has Reader counterpart) - getter" else "Not paired - getter (semanticsLabel omitted → falls back to label)"}")
                sb.appendLine("  AccessibleString get ${entry.key} => AccessibleString(")
                sb.appendLine("    label: ${entry.key},")
                if (isPaired) {
                    sb.appendLine("    semanticsLabel: ${entry.key}Reader,")
                }
                sb.appendLine("  );")
            } else {
                // Method form
                val paramList = entry.argNames.joinToString(", ") { "dynamic $it" }
                val argList = entry.argNames.joinToString(", ")
                sb.appendLine("  // ${if (isPaired) "Paired with args - method (all dynamic)" else "Not paired with args - method (all dynamic)"}")
                sb.appendLine("  AccessibleString ${entry.key}($paramList) => AccessibleString(")
                sb.appendLine("    label: ${entry.key}($argList),")
                if (isPaired) {
                    sb.appendLine("    semanticsLabel: ${entry.key}Reader($argList),")
                }
                sb.appendLine("  );")
            }
            sb.appendLine()
        }

        // Remove the trailing blank line before closing brace
        val result = sb.toString().trimEnd()
        return "$result\n}\n"
    }
}
