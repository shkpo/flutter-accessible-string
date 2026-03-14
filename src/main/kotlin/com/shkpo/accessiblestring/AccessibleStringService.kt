package com.shkpo.accessiblestring

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class AccessibleStringService(private val project: Project) {

    fun generate() {
        val basePath = project.basePath ?: run {
            showError("Could not determine project base path.")
            return
        }

        val config = ConfigReader.read(basePath) ?: run {
            // No accessible_gen.yaml found — silently return
            return
        }

        try {
            val parseResult = ArbParser.parse(basePath, config) ?: run {
                showError("Could not find or parse master ARB file (locale: ${config.masterLocale}) in ${config.arbDir}.")
                return
            }

            DartCodeGenerator.generate(basePath, config, parseResult)

            Notifications.Bus.notify(
                Notification(
                    "Flutter Accessible String",
                    "Generated s_accessible.g.dart",
                    NotificationType.INFORMATION
                ),
                project
            )
        } catch (e: Exception) {
            showError("Generation failed: ${e.message}")
        }
    }

    private fun showError(message: String) {
        Notifications.Bus.notify(
            Notification(
                "Flutter Accessible String",
                message,
                NotificationType.ERROR
            ),
            project
        )
    }
}
