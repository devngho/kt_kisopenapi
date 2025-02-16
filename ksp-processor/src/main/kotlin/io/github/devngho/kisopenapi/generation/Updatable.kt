package io.github.devngho.kisopenapi.generation

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.devngho.kisopenapi.Updatable

object Updatable {
    fun generate(resolver: Resolver, codeGenerator: CodeGenerator) {
        resolveDelegations(resolver)
            .forEach {
                val m = generateDelegation(it, resolver)
                m.first.writeTo(codeGenerator, m.second)
            }
    }

    private fun findUpperTypes(
        resolver: Resolver,
        p: KSClassDeclaration,
        prop: KSPropertyDeclaration
    ): List<KSClassDeclaration> {
        if (!p.getAllProperties()
                .any { it.simpleName == prop.simpleName && it.type.resolve() == prop.type.resolve() }
        ) {
            return emptyList()
        }

        val superTypes = p.superTypes.mapNotNull { it.resolve().declaration as? KSClassDeclaration }.toList()

        if (superTypes.isEmpty()) {
            return listOf(p)
        }

        val upperClasses = superTypes.also {
            it.forEach {
            }
        }.filter {
            it.getAllProperties().any { it.simpleName == prop.simpleName && it.type.resolve() == prop.type.resolve() }
        }

        return if (upperClasses.isNotEmpty()) {
            upperClasses.flatMap { findUpperTypes(resolver, it, prop) }
        } else {
            listOf(p)
        }
    }

    private fun generateDelegation(p: KSClassDeclaration, resolver: Resolver): Pair<FileSpec, Dependencies> {
        val name = "Updatable${p.simpleName.asString()}"

        val file = FileSpec.builder(p.packageName.asString(), name).apply {
            val properties: MutableList<Pair<KSPropertyDeclaration, List<KSClassDeclaration>>> = mutableListOf()

            p.getAllProperties().forEach { prop ->
                properties.add(prop to findUpperTypes(resolver, p, prop).distinct())
            }

            TypeSpec.classBuilder(name).apply {
                addAnnotation(ClassName("io.github.devngho.kisopenapi.requests.util", "InternalApi"))
                addSuperinterface(p.toClassName())
                addProperty(
                    PropertySpec.builder(
                        "manager",
                        ClassName("io.github.devngho.kisopenapi.layer", "UpdatableManager")
                    )
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("%T()", ClassName("io.github.devngho.kisopenapi.layer", "UpdatableManager"))
                        .build()
                )

                properties.forEach { (prop, upperClass) ->
                    addProperty(
                        PropertySpec.builder(
                            prop.simpleName.asString(),
                            prop.type.resolve().toClassName()
                        )
                            .addModifiers(KModifier.OVERRIDE)
                            .delegate(
                                """
                            |manager.tracked { when(it) {
                            |    null -> null to false
                            ${upperClass.joinToString("\n") { "|    is %T -> it.${prop.simpleName.asString()} to true" }}
                            |    else -> null to false
                            |} }
                            """.trimMargin(),
                                *upperClass.map { it.toClassName() }.toTypedArray()
                            )
                            .build()
                    )
                }

                addFunction(
                    FunSpec.builder("broadcast")
                        .addParameter("value", Any::class.asClassName().copy(nullable = true))
                        .addStatement("manager.broadcastUpdate(value)")
                        .build()
                )


            }.build().let {
                addType(it)
            }
        }.build()

        return file to Dependencies(
            true,
            p.containingFile!!,
        )
    }

    private fun resolveDelegations(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(Updatable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
    }
}