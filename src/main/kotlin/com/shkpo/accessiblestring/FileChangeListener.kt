package com.shkpo.accessiblestring

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class FileChangeListener(private val project: Project) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        val basePath = project.basePath ?: return

        // Read config to determine the trigger file path; skip if config not found
        val config = ConfigReader.read(basePath) ?: return

        val triggerSuffix = config.triggerFile.replace('\\', '/')

        val shouldGenerate = events.any { event ->
            val path = event.path.replace('\\', '/')
            path.endsWith(triggerSuffix)
        }

        if (shouldGenerate) {
            ApplicationManager.getApplication().invokeLater {
                project.service<AccessibleStringService>().generate()
            }
        }
    }
}
