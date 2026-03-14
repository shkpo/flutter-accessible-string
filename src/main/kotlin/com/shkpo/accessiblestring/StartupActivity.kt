package com.shkpo.accessiblestring

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFileManager

class StartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            FileChangeListener(project)
        )
    }
}
