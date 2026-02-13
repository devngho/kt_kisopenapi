@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("org.jetbrains.dokka") version libs.versions.dokka
    id("io.kotest") version libs.versions.kotest
//    id("io.github.gradle-nexus.publish-plugin") version libs.versions.gradle.publish
    id("com.google.devtools.ksp") version libs.versions.ksp
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "0.2.12"

repositories {
    mavenCentral()
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

signing {
    sign(publishing.publications)
}

publishing {
    repositories {
        val id: String =
            if (project.hasProperty("repoUsername")) project.property("repoUsername") as String
            else System.getenv("repoUsername")
        val pw: String =
            if (project.hasProperty("repoPassword")) project.property("repoPassword") as String
            else System.getenv("repoPassword")
        if (!version.toString().endsWith("SNAPSHOT")) {
            maven("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/") {
                name = "ossrh-staging-api"
                credentials {
                    username = id
                    password = pw
                }
            }
        } else {
            maven("https://central.sonatype.com/repository/maven-snapshots/") {
                name = "ossrh-staging-api"
                credentials {
                    username = id
                    password = pw
                }
            }
        }
    }

    publications.withType(MavenPublication::class) {
        groupId = project.group as String?
        version = project.version as String?

        artifact(dokkaHtmlJar)

        pom {
            name.set("kt_kisopenapi")
            description.set("한국투자증권의 오픈 API 서비스를 Kotlin/Java 환경에서 사용할 수 있는 라이브러리")
            url.set("https://github.com/devngho/kt_kisopenapi")


            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/devngho/kt_kisopenapi/blob/master/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("devngho")
                    name.set("devngho")
                    email.set("yjh135908@gmail.com")
                }
            }
            scm {
                connection.set("https://github.com/devngho/kt_kisopenapi.git")
                developerConnection.set("https://github.com/devngho/kt_kisopenapi.git")
                url.set("https://github.com/devngho/kt_kisopenapi")
            }
        }
    }
}

kotlin {

    // copied from ionspin/kotlin-multiplatform-bignum (at build.gradle.kts), Apache 2.0
    // removed watchosDeviceArm64 and modified js
    js {
        nodejs()
        browser()
    }
    linuxX64()
    linuxArm64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    mingwX64()
    // copy end

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    wasmJs {
        browser()
        nodejs()
        d8()
    }
//    wasmWasi() // kotlinx-datetime, ktor, kotest doesn't support wasm-wasi

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(project(":ksp-annotations"))

                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)

                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.bignum)
                implementation(libs.bignum.serialization.kotlinx)
                implementation(libs.ktor.client.cio)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)

            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotations.common)
            implementation(libs.kotlin.reflect)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.websockets)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.slf4j.simple)
            implementation(libs.mockk)
        }

        applyDefaultHierarchyTemplate()
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":ksp-processor"))
}

tasks {
    // copied from ionspin/kotlin-multiplatform-bignum (at build.gradle.kts), Apache 2.0
    // fixed for correct task dependencies in this project
    all {
        val targets = listOf(
            "AndroidNativeArm32",
            "AndroidNativeArm64",
            "AndroidNativeX64",
            "AndroidNativeX86",
            "Js",
            "Jvm",
            "KotlinMultiplatform",
            "LinuxArm64",
            "LinuxX64",
            "WasmJs",
            "MingwX64",
            "IosArm64",
            "IosSimulatorArm64",
            "IosX64",
            "MacosArm64",
            "MacosX64",
            "TvosArm64",
            "TvosSimulatorArm64",
            "TvosX64",
            "WatchosArm32",
            "WatchosArm64",
            "WatchosSimulatorArm64",
            "WatchosX64"
        )

        targets.dropLast(1).forEachIndexed { index, target ->
            if (this.name.startsWith("sign${target}Publication")) {
                this.mustRunAfter("sign${targets[index + 1]}Publication")
            }
        }

        if (this.name.startsWith("publish") || this.name.startsWith("linkDebugTest") || this.name.startsWith("compileTest")) {
            targets.forEach {
                this.mustRunAfter("sign${it}Publication")
            }
        }

        targets.forEach {
            if (it == "KotlinMultiplatform") return@forEach

            named("compileKotlin${it}") {
                dependsOn("kspCommonMainKotlinMetadata")
            }

            named(
                "${
                    it.let {
                        it[0].lowercase() + it.substring(1)
                    }
                }SourcesJar"
            ) {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }

        named("sourcesJar") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
    // copy end

    named<Test>("jvmTest") {
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
        }
        testLogging @ExperimentalStdlibApi {
            showExceptions = true
            showStandardStreams = true
            events = TestLogEvent.entries.toSet()
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}