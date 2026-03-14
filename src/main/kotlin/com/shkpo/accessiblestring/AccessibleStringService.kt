package com.shkpo.accessiblestring

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class AccessibleStringService(
    private val project: Project,
    internal val configReader: IConfigReader,
    internal val arbParser: IArbParser,
    internal val codeGenerator: IDartCodeGenerator,
    internal val notifier: INotifier,
) {
    // IntelliJ が使うコンストラクタ
    constructor(project: Project) : this(
        project,
        ConfigReader(),
        ArbParser(),
        DartCodeGenerator(IntelliJVfsRefresher()),
        IntelliJNotifier(project),
    )

    fun generate() {
        val basePath =
            project.basePath ?: run {
                notifier.error("Could not determine project base path.")
                return
            }

        val config = configReader.read(basePath) ?: return

        try {
            val parseResult =
                arbParser.parse(basePath, config) ?: run {
                    notifier.error("Could not find or parse master ARB file (locale: ${config.masterLocale}) in ${config.arbDir}.")
                    return
                }
            codeGenerator.generate(basePath, config, parseResult)
            notifier.info("Generated s_accessible.g.dart")
        } catch (e: Exception) {
            notifier.error("Generation failed: ${e.message}")
        }
    }
}
