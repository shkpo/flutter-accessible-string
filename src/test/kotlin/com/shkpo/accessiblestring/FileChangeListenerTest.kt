package com.shkpo.accessiblestring

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileChangeListenerTest {

    private val project = mockk<Project>()
    private val configReader = mockk<IConfigReader>()
    private val generateInvoker = mockk<IGenerateInvoker>(relaxed = true)

    private lateinit var listener: FileChangeListener

    private val defaultConfig = AccessibleGenConfig(triggerFile = "lib/generated/l18n.dart")

    @BeforeEach
    fun setUp() {
        listener = FileChangeListener(project, configReader, generateInvoker)
        every { project.basePath } returns "/project"
    }

    @Test
    fun `after does nothing when basePath is null`() {
        every { project.basePath } returns null

        listener.after(emptyList())

        verify { configReader wasNot Called }
        verify { generateInvoker wasNot Called }
    }

    @Test
    fun `after does nothing when config not found`() {
        every { configReader.read("/project") } returns null

        listener.after(emptyList())

        verify { generateInvoker wasNot Called }
    }

    @Test
    fun `after does nothing when no event matches trigger file`() {
        every { configReader.read("/project") } returns defaultConfig
        val event = mockk<VFileEvent>()
        every { event.path } returns "/project/lib/generated/other.dart"

        listener.after(listOf(event))

        verify { generateInvoker wasNot Called }
    }

    @Test
    fun `after invokes generate when event path ends with trigger file`() {
        every { configReader.read("/project") } returns defaultConfig
        val event = mockk<VFileEvent>()
        every { event.path } returns "/project/lib/generated/l18n.dart"

        listener.after(listOf(event))

        verify { generateInvoker.invoke() }
    }

    @Test
    fun `after normalizes backslash in event path`() {
        every { configReader.read("/project") } returns defaultConfig
        val event = mockk<VFileEvent>()
        every { event.path } returns "C:\\project\\lib\\generated\\l18n.dart"

        listener.after(listOf(event))

        verify { generateInvoker.invoke() }
    }
}
