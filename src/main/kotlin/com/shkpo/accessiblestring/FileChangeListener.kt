package com.shkpo.accessiblestring

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class FileChangeListener(
    private val project: Project,
    private val configReader: IConfigReader,
    private val generateInvoker: IGenerateInvoker,
) : BulkFileListener {
    // StartupActivity が使うコンストラクタ
    constructor(project: Project) : this(
        project,
        ConfigReader(),
        DefaultGenerateInvoker(project),
    )

    override fun after(events: List<VFileEvent>) {
        val basePath = project.basePath ?: return
        val config = configReader.read(basePath) ?: return
        val triggerSuffix = config.triggerFile.replace('\\', '/')

        val shouldGenerate =
            events.any { event ->
                event.path.replace('\\', '/').endsWith(triggerSuffix)
            }

        if (shouldGenerate) {
            generateInvoker.invoke()
        }
    }
}
