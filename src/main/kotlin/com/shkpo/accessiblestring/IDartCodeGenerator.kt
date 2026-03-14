package com.shkpo.accessiblestring

interface IDartCodeGenerator {
    fun generate(projectBasePath: String, config: AccessibleGenConfig, parseResult: ArbParseResult)
}
