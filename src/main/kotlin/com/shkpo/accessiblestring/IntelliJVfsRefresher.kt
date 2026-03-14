package com.shkpo.accessiblestring

import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

class IntelliJVfsRefresher : IVfsRefresher {
    override fun refresh(file: File) {
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
    }
}
