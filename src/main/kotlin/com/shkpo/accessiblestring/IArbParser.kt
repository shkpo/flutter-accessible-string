package com.shkpo.accessiblestring

interface IArbParser {
    fun parse(
        projectBasePath: String,
        config: AccessibleGenConfig,
    ): ArbParseResult?
}
