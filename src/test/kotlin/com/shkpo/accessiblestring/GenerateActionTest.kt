package com.shkpo.accessiblestring

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import io.mockk.*
import org.junit.jupiter.api.Test

class GenerateActionTest {
    private val generateInvoker = mockk<IGenerateInvoker>(relaxed = true)
    private val project = mockk<Project>()
    private val action = GenerateAction(invokerFactory = { generateInvoker })

    @Test
    fun `actionPerformed does nothing when project is null`() {
        val event = mockk<AnActionEvent>()
        every { event.project } returns null

        action.actionPerformed(event)

        verify { generateInvoker wasNot Called }
    }

    @Test
    fun `actionPerformed invokes generate when project is present`() {
        val event = mockk<AnActionEvent>()
        every { event.project } returns project

        action.actionPerformed(event)

        verify { generateInvoker.invoke() }
    }

    @Test
    fun `update disables action when project is null`() {
        val event = mockk<AnActionEvent>()
        val presentation = mockk<Presentation>(relaxed = true)
        every { event.project } returns null
        every { event.presentation } returns presentation

        action.update(event)

        verify { presentation.isEnabled = false }
    }

    @Test
    fun `update enables action when project is present`() {
        val event = mockk<AnActionEvent>()
        val presentation = mockk<Presentation>(relaxed = true)
        every { event.project } returns project
        every { event.presentation } returns presentation

        action.update(event)

        verify { presentation.isEnabled = true }
    }
}
