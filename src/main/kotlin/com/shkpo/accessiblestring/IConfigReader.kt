package com.shkpo.accessiblestring

interface IConfigReader {
    fun read(projectBasePath: String): AccessibleGenConfig?
}
