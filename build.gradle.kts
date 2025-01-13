@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.kotest.multiplatform") version "5.9.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "0.2.5"

repositories {
    mavenCentral()
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

// copied from ionspin/kotlin-multiplatform-bignum (at build.gradle.kts), Apache 2.0
enum class HostOs {
    LINUX, WINDOWS, MAC
}


fun getHostOsName(): HostOs {
    val target = System.getProperty("os.name")
    if (target == "Linux") return HostOs.LINUX
    if (target.startsWith("Windows")) return HostOs.WINDOWS
    if (target.startsWith("Mac")) return HostOs.MAC
    throw GradleException("Unknown OS: $target")
}

val hostOs = getHostOsName()
// copy end

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

    jvm()

    wasmJs {
        browser()
        nodejs()
        d8()
    }
//    wasmWasi() // kotlinx-datetime, ktor, kotest doesn't support wasm-wasi
    
    sourceSets {
        val ktorVersion = "3.0.3"
        val coroutineVersion = "1.10.1"
        val kotestVersion = "5.9.1"
        val bigNumVersion = "0.3.10"
        val mockkVersion = "1.13.16"
        val slf4jVersion = "2.0.16"

        commonMain.dependencies {
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-websockets:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            api("com.ionspin.kotlin:bignum:$bigNumVersion")
            implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:$bigNumVersion")
        }
        commonTest.dependencies {
            implementation("io.kotest:kotest-framework-engine:$kotestVersion")
            implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(kotlin("reflect"))
        }
        jvmMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation("io.ktor:ktor-client-java:$ktorVersion")
        }
        jvmTest.dependencies {
            implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            implementation("org.slf4j:slf4j-simple:$slf4jVersion")
            implementation("io.mockk:mockk:$mockkVersion")
        }
        nativeMain.dependencies {
            implementation("io.ktor:ktor-client-cio:$ktorVersion")
        }

        applyDefaultHierarchyTemplate()
    }
}

tasks {
    // copied from ionspin/kotlin-multiplatform-bignum (at build.gradle.kts), Apache 2.0
    // fixed windows part for correct task dependencies
    all {
        if (hostOs == HostOs.LINUX) {
            // Linux task dependecies

            // @formatter:off
            if (this.name.equals("signAndroidNativeArm32Publication")) { this.mustRunAfter("signAndroidNativeArm64Publication") }
            if (this.name.equals("signAndroidNativeArm64Publication")) { this.mustRunAfter("signAndroidNativeX64Publication") }
            if (this.name.equals("signAndroidNativeX64Publication")) { this.mustRunAfter("signAndroidNativeX86Publication") }
            if (this.name.equals("signAndroidNativeX86Publication")) { this.mustRunAfter("signJsPublication") }
            if (this.name.equals("signJsPublication")) { this.mustRunAfter("signJvmPublication") }
            if (this.name.equals("signJvmPublication")) { this.mustRunAfter("signKotlinMultiplatformPublication") }
            if (this.name.equals("signKotlinMultiplatformPublication")) { this.mustRunAfter("signLinuxArm64Publication") }
            if (this.name.equals("signLinuxArm64Publication")) { this.mustRunAfter("signLinuxX64Publication") }
            if (this.name.equals("signLinuxX64Publication")) { this.mustRunAfter("signWasmJsPublication") }
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
            }
        }

        if (hostOs == HostOs.MAC) {
            // Macos task dependencies
            // @formatter:off
            if (this.name.equals("signIosArm64Publication")) { this.mustRunAfter("signIosSimulatorArm64Publication") }
            if (this.name.equals("signIosSimulatorArm64Publication")) { this.mustRunAfter("signIosX64Publication") }
            if (this.name.equals("signIosX64Publication")) { this.mustRunAfter("signMacosArm64Publication") }
            if (this.name.equals("signMacosArm64Publication")) { this.mustRunAfter("signMacosX64Publication") }
            if (this.name.equals("signMacosX64Publication")) { this.mustRunAfter("signTvosArm64Publication") }
            if (this.name.equals("signTvosArm64Publication")) { this.mustRunAfter("signTvosSimulatorArm64Publication") }
            if (this.name.equals("signTvosSimulatorArm64Publication")) { this.mustRunAfter("signTvosX64Publication") }
            if (this.name.equals("signTvosX64Publication")) { this.mustRunAfter("signWatchosArm32Publication") }
            if (this.name.equals("signWatchosArm32Publication")) { this.mustRunAfter("signWatchosArm64Publication") }
            if (this.name.equals("signWatchosArm64Publication")) { this.mustRunAfter("signWatchosDeviceArm64Publication") }
            if (this.name.equals("signWatchosDeviceArm64Publication")) { this.mustRunAfter("signWatchosSimulatorArm64Publication") }
            if (this.name.equals("signWatchosSimulatorArm64Publication")) { this.mustRunAfter("signWatchosX64Publication") }
            // @formatter:on

            if (this.name.startsWith("publish")) {
                this.mustRunAfter("signIosArm64Publication")
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
                this.mustRunAfter("signWatchosDeviceArm64Publication")
                this.mustRunAfter("signWatchosSimulatorArm64Publication")
                this.mustRunAfter("signWatchosX64Publication")
            }

            if (this.name.startsWith("compileTest")) {
                this.mustRunAfter("signIosArm64Publication")
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
                this.mustRunAfter("signWatchosDeviceArm64Publication")
                this.mustRunAfter("signWatchosSimulatorArm64Publication")
            }
            if (this.name.startsWith("linkDebugTest")) {
                this.mustRunAfter("signIosArm64Publication")
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
                this.mustRunAfter("signWatchosDeviceArm64Publication")
                this.mustRunAfter("signWatchosSimulatorArm64Publication")
            }
        }

        if (hostOs == HostOs.WINDOWS) {
            // Windows task dependecies

            // @formatter:off
            if (this.name.equals("signAndroidNativeArm32Publication")) { this.mustRunAfter("signAndroidNativeArm64Publication") }
            if (this.name.equals("signAndroidNativeArm64Publication")) { this.mustRunAfter("signAndroidNativeX64Publication") }
            if (this.name.equals("signAndroidNativeX64Publication")) { this.mustRunAfter("signAndroidNativeX86Publication") }
            if (this.name.equals("signAndroidNativeX86Publication")) { this.mustRunAfter("signJsPublication") }
            if (this.name.equals("signJsPublication")) { this.mustRunAfter("signJvmPublication") }
            if (this.name.equals("signJvmPublication")) { this.mustRunAfter("signKotlinMultiplatformPublication") }
            if (this.name.equals("signKotlinMultiplatformPublication")) { this.mustRunAfter("signLinuxArm64Publication") }
            if (this.name.equals("signLinuxArm64Publication")) { this.mustRunAfter("signLinuxX64Publication") }
            if (this.name.equals("signLinuxX64Publication")) { this.mustRunAfter("signWasmJsPublication") }
            if (this.name.equals("signWasmJsPublication")) { this.mustRunAfter("signMingwX64Publication") }
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
            }
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