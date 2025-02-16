@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("org.jetbrains.dokka") version libs.versions.dokka
    id("io.kotest.multiplatform") version libs.versions.kotest
    id("io.github.gradle-nexus.publish-plugin") version libs.versions.gradle.publish
    id("com.google.devtools.ksp") version libs.versions.ksp
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "0.2.6"

repositories {
    mavenCentral()
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

kotlin {
    publishing {
        signing {
            sign(publishing.publications)
        }

        repositories {
            val id: String =
                if (project.hasProperty("repoUsername")) project.property("repoUsername") as String
                else System.getenv("repoUsername")
            val pw: String =
                if (project.hasProperty("repoPassword")) project.property("repoPassword") as String
                else System.getenv("repoPassword")
            if (!version.toString().endsWith("SNAPSHOT")) {
                val repositoryId =
                    System.getenv("SONATYPE_REPOSITORY_ID")

                maven("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${repositoryId}/") {
                    name = "Sonatype"
                    credentials {
                        username = id
                        password = pw
                    }
                }
            } else {
                maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                    name = "Sonatype"
                    credentials {
                        username = id
                        password = pw
                    }
                }
            }
        }

        publications.withType(MavenPublication::class) {
            groupId = project.group as String?
            artifactId = "kt_kisopenapi"
            version = project.version as String?

            artifact(tasks["javadocJar"])

            pom {
                name.set(artifactId)
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
    named("compileKotlinJvm") {
        dependsOn("kspCommonMainKotlinMetadata")
    }

    afterEvaluate {
        named("sourcesJar") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }

    // copied from ionspin/kotlin-multiplatform-bignum (at build.gradle.kts), Apache 2.0
    // fixed for correct task dependencies in this project
    all {
        // @formatter:off
        when (this.name) {
            "signAndroidNativeArm32Publication" -> { this.mustRunAfter("signAndroidNativeArm64Publication") }
            "signAndroidNativeArm64Publication" -> { this.mustRunAfter("signAndroidNativeX64Publication") }
            "signAndroidNativeX64Publication" -> { this.mustRunAfter("signAndroidNativeX86Publication") }
            "signAndroidNativeX86Publication" -> { this.mustRunAfter("signJsPublication") }
            "signJsPublication" -> { this.mustRunAfter("signJvmPublication") }
            "signJvmPublication" -> { this.mustRunAfter("signKotlinMultiplatformPublication") }
            "signKotlinMultiplatformPublication" -> { this.mustRunAfter("signLinuxArm64Publication") }
            "signLinuxArm64Publication" -> { this.mustRunAfter("signLinuxX64Publication") }
            "signLinuxX64Publication" -> { this.mustRunAfter("signWasmJsPublication") }
            "signWasmJsPublication" -> { this.mustRunAfter("signMingwX64Publication") }
            "signMingwX64Publication" -> { this.mustRunAfter("signIosArm64Publication") }
            "signIosArm64Publication" -> { this.mustRunAfter("signIosSimulatorArm64Publication") }
            "signIosSimulatorArm64Publication" -> { this.mustRunAfter("signIosX64Publication") }
            "signIosX64Publication" -> { this.mustRunAfter("signMacosArm64Publication") }
            "signMacosArm64Publication" -> { this.mustRunAfter("signMacosX64Publication") }
            "signMacosX64Publication" -> { this.mustRunAfter("signTvosArm64Publication") }
            "signTvosArm64Publication" -> { this.mustRunAfter("signTvosSimulatorArm64Publication") }
            "signTvosSimulatorArm64Publication" -> { this.mustRunAfter("signTvosX64Publication") }
            "signTvosX64Publication" -> { this.mustRunAfter("signWatchosArm32Publication") }
            "signWatchosArm32Publication" -> { this.mustRunAfter("signWatchosArm64Publication") }
            "signWatchosArm64Publication" -> { this.mustRunAfter("signWatchosSimulatorArm64Publication") }
            "signWatchosSimulatorArm64Publication" -> { this.mustRunAfter("signWatchosX64Publication") }
        }
        // @formatter:on

        if (this.name.startsWith("publish")) {
            this.mustRunAfter("signAndroidNativeArm32Publication")
            this.mustRunAfter("signAndroidNativeArm64Publication")
            this.mustRunAfter("signAndroidNativeX64Publication")
            this.mustRunAfter("signAndroidNativeX86Publication")
            this.mustRunAfter("signJsPublication")
            this.mustRunAfter("signJvmPublication")
            this.mustRunAfter("signKotlinMultiplatformPublication")
            this.mustRunAfter("signLinuxArm64Publication")
            this.mustRunAfter("signLinuxX64Publication")
            this.mustRunAfter("signWasmJsPublication")
            this.mustRunAfter("signMingwX64Publication")
            this.mustRunAfter("signIosArm64Publication")
            this.mustRunAfter("signIosSimulatorArm64Publication")
            this.mustRunAfter("signIosX64Publication")
            this.mustRunAfter("signMacosArm64Publication")
            this.mustRunAfter("signMacosX64Publication")
            this.mustRunAfter("signTvosArm64Publication")
            this.mustRunAfter("signTvosSimulatorArm64Publication")
            this.mustRunAfter("signTvosX64Publication")
            this.mustRunAfter("signWatchosArm32Publication")
            this.mustRunAfter("signWatchosArm64Publication")
            this.mustRunAfter("signWatchosSimulatorArm64Publication")
            this.mustRunAfter("signWatchosX64Publication")
        }

        if (this.name.startsWith("compileTest")) {
            this.mustRunAfter("signAndroidNativeArm32Publication")
            this.mustRunAfter("signAndroidNativeArm64Publication")
            this.mustRunAfter("signAndroidNativeX64Publication")
            this.mustRunAfter("signAndroidNativeX86Publication")
            this.mustRunAfter("signJsPublication")
            this.mustRunAfter("signJvmPublication")
            this.mustRunAfter("signKotlinMultiplatformPublication")
            this.mustRunAfter("signLinuxArm64Publication")
            this.mustRunAfter("signLinuxX64Publication")
            this.mustRunAfter("signWasmJsPublication")
            this.mustRunAfter("signMingwX64Publication")
            this.mustRunAfter("signIosArm64Publication")
            this.mustRunAfter("signIosSimulatorArm64Publication")
            this.mustRunAfter("signIosX64Publication")
            this.mustRunAfter("signMacosArm64Publication")
            this.mustRunAfter("signMacosX64Publication")
            this.mustRunAfter("signTvosArm64Publication")
            this.mustRunAfter("signTvosSimulatorArm64Publication")
            this.mustRunAfter("signTvosX64Publication")
            this.mustRunAfter("signWatchosArm32Publication")
            this.mustRunAfter("signWatchosArm64Publication")
            this.mustRunAfter("signWatchosSimulatorArm64Publication")
        }
        if (this.name.startsWith("linkDebugTest")) {
            this.mustRunAfter("signAndroidNativeArm32Publication")
            this.mustRunAfter("signAndroidNativeArm64Publication")
            this.mustRunAfter("signAndroidNativeX64Publication")
            this.mustRunAfter("signAndroidNativeX86Publication")
            this.mustRunAfter("signJsPublication")
            this.mustRunAfter("signJvmPublication")
            this.mustRunAfter("signKotlinMultiplatformPublication")
            this.mustRunAfter("signLinuxArm64Publication")
            this.mustRunAfter("signLinuxX64Publication")
            this.mustRunAfter("signWasmJsPublication")
            this.mustRunAfter("signMingwX64Publication")
            this.mustRunAfter("signIosArm64Publication")
            this.mustRunAfter("signIosSimulatorArm64Publication")
            this.mustRunAfter("signIosX64Publication")
            this.mustRunAfter("signMacosArm64Publication")
            this.mustRunAfter("signMacosX64Publication")
            this.mustRunAfter("signTvosArm64Publication")
            this.mustRunAfter("signTvosSimulatorArm64Publication")
            this.mustRunAfter("signTvosX64Publication")
            this.mustRunAfter("signWatchosArm32Publication")
            this.mustRunAfter("signWatchosArm64Publication")
            this.mustRunAfter("signWatchosSimulatorArm64Publication")
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
            events = TestLogEvent.values().toSet()
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}