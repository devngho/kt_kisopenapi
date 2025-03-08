@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = rootProject.group
version = rootProject.version

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
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
        gradle.projectsEvaluated {
            artifactId = "kt_kisopenapi-$artifactId"
        }
        version = project.version as String?

        artifact(tasks["javadocJar"])

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

    sourceSets {
        applyDefaultHierarchyTemplate()
    }
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
    }
}