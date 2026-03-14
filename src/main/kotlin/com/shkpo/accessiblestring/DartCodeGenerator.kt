package com.shkpo.accessiblestring

import java.io.File
import java.nio.file.Paths

class DartCodeGenerator(private val vfsRefresher: IVfsRefresher) : IDartCodeGenerator {

    override fun generate(projectBasePath: String, config: AccessibleGenConfig, parseResult: ArbParseResult) {
        val outputDirFile = File(projectBasePath, config.outputDir)
        outputDirFile.mkdirs()

        val outputFile = File(outputDirFile, "s_accessible.g.dart")

        val outputDirPath = Paths.get(outputDirFile.canonicalPath)
        val triggerFilePath = Paths.get(File(projectBasePath, config.triggerFile).canonicalPath)
        val relativePath = outputDirPath.relativize(triggerFilePath).toString().replace(File.separatorChar, '/')

        outputFile.writeText(buildDartCode(relativePath, config.readerSuffix, parseResult))
        vfsRefresher.refresh(outputFile)
    }

    internal fun buildDartCode(importPath: String, readerSuffix: String, parseResult: ArbParseResult): String {
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
                sb.appendLine("  AccessibleString get ${entry.key} => AccessibleString(")
                sb.appendLine("    label: ${entry.key},")
                if (isPaired) sb.appendLine("    semanticsLabel: ${entry.key}$readerSuffix,")
                sb.appendLine("  );")
            } else {
                val paramList = entry.argNames.joinToString(", ") { "dynamic $it" }
                val argList = entry.argNames.joinToString(", ")
                sb.appendLine("  AccessibleString ${entry.key}($paramList) => AccessibleString(")
                sb.appendLine("    label: ${entry.key}($argList),")
                if (isPaired) sb.appendLine("    semanticsLabel: ${entry.key}$readerSuffix($argList),")
                sb.appendLine("  );")
            }
            sb.appendLine()
        }

        val result = sb.toString().trimEnd()
        return "$result\n}\n"
    }
}
