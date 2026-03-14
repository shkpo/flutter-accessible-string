package com.shkpo.accessiblestring

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class DefaultGenerateInvoker(private val project: Project) : IGenerateInvoker {
    override fun invoke() {
        ApplicationManager.getApplication().invokeLater {
            project.service<AccessibleStringService>().generate()
        }
    }
}
