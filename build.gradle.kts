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

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")

    when {
        hostOs == "Mac OS X" -> macosX64()
        hostOs == "Linux" -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

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
            implementation("io.ktor:ktor-client-curl:$ktorVersion")
        }

        applyDefaultHierarchyTemplate()
    }
}

tasks {
    getByName("signKotlinMultiplatformPublication") {
        dependsOn("publishJvmPublicationToSonatypeRepository", "publishJvmPublicationToMavenLocal")
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTargets = mutableListOf<String>()

    if (hostOs == "Mac OS X") nativeTargets.add("MacosX64")
    if (hostOs == "Linux") nativeTargets.add("LinuxX64")
    if (isMingwX64) nativeTargets.add("MingwX64")

    nativeTargets.forEach { target ->
        getByName("sign${target}Publication") {
            dependsOn(
                "publishJvmPublicationToSonatypeRepository",
                "publishJvmPublicationToMavenLocal",
                "publishKotlinMultiplatformPublicationToMavenLocal",
                "publishKotlinMultiplatformPublicationToSonatypeRepository"
            )
        }
    }
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