package com.shkpo.accessiblestring

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

class IntelliJNotifier(private val project: Project) : INotifier {
    override fun info(message: String) {
        Notifications.Bus.notify(
            Notification("Flutter Accessible String", message, NotificationType.INFORMATION),
            project,
        )
    }

    override fun error(message: String) {
        Notifications.Bus.notify(
            Notification("Flutter Accessible String", message, NotificationType.ERROR),
            project,
        )
    }
}
