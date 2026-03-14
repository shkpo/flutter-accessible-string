package com.shkpo.accessiblestring

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class GenerateAction(
    private val invokerFactory: (Project) -> IGenerateInvoker = { project -> DefaultGenerateInvoker(project) }
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        invokerFactory(project).invoke()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
