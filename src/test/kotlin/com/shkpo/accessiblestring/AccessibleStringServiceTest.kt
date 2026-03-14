package com.shkpo.accessiblestring

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.intellij.openapi.project.Project

class AccessibleStringServiceTest {

    private val project = mockk<Project>()
    private val configReader = mockk<IConfigReader>()
    private val arbParser = mockk<IArbParser>()
    private val codeGenerator = mockk<IDartCodeGenerator>(relaxed = true)
    private val notifier = mockk<INotifier>(relaxed = true)

    private lateinit var service: AccessibleStringService

    private val defaultConfig = AccessibleGenConfig()
    private val defaultParseResult = ArbParseResult(entries = emptyList())

    @BeforeEach
    fun setUp() {
        service = AccessibleStringService(project, configReader, arbParser, codeGenerator, notifier)
    }

    @Test
    fun `generate shows error when basePath is null`() {
        every { project.basePath } returns null

        service.generate()

        verify { notifier.error(any()) }
        verify { configReader wasNot Called }
    }

    @Test
    fun `generate returns silently when config not found`() {
        every { project.basePath } returns "/project"
        every { configReader.read("/project") } returns null

        service.generate()

        verify { configReader.read("/project") }
        verify { arbParser wasNot Called }
        verify { notifier wasNot Called }
    }

    @Test
    fun `generate shows error when ARB parse returns null`() {
        every { project.basePath } returns "/project"
        every { configReader.read("/project") } returns defaultConfig
        every { arbParser.parse("/project", defaultConfig) } returns null

        service.generate()

        verify { arbParser.parse("/project", defaultConfig) }
        verify { notifier.error(any()) }
        verify { codeGenerator wasNot Called }
    }

    @Test
    fun `generate calls codeGenerator and shows info on success`() {
        every { project.basePath } returns "/project"
        every { configReader.read("/project") } returns defaultConfig
        every { arbParser.parse("/project", defaultConfig) } returns defaultParseResult

        service.generate()

        verify { codeGenerator.generate("/project", defaultConfig, defaultParseResult) }
        verify { notifier.info(any()) }
        verify(exactly = 0) { notifier.error(any()) }
    }

    @Test
    fun `generate shows error when codeGenerator throws`() {
        every { project.basePath } returns "/project"
        every { configReader.read("/project") } returns defaultConfig
        every { arbParser.parse("/project", defaultConfig) } returns defaultParseResult
        every { codeGenerator.generate(any(), any(), any()) } throws RuntimeException("disk full")

        service.generate()

        verify { notifier.error(match { it.contains("disk full") }) }
        verify(exactly = 0) { notifier.info(any()) }
    }
}
