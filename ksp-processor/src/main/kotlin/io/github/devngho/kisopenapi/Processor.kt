package io.github.devngho.kisopenapi

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.devngho.kisopenapi.generation.Updatable

class Processor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        Updatable.generate(resolver, codeGenerator)

        return emptyList()
    }
}

class ProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(environment.codeGenerator)
    }
}