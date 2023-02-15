import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "0.1.29"

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
            if (version.toString().endsWith("SNAPSHOT")) {
                maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                    name = "sonatypeSnapshotRepository"
                    credentials(PasswordCredentials::class)
                }
            } else {
                maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                    name = "sonatypeReleaseRepository"
                    credentials(PasswordCredentials::class)
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
                description.set("A Kotlin library for korea stock trading.")
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

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useTestNG()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    val platform = when {
        hostOs == "Mac Os X" -> "macosx64"
        hostOs == "Linux" -> "linuxx64"
        isMingwX64 -> "mingwx64"
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val ktorVersion = "2.1.3"
        val coroutineVersion = "1.6.4"

        fun KotlinDependencyHandler.standard() {
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-websockets:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            implementation("com.ionspin.kotlin:bignum:0.3.7")
            implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:0.3.2")
        }

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("com.soywiz.korlibs.krypto:krypto:4.0.0-alpha-1")
                standard()
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                standard()
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                standard()
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-$platform:$coroutineVersion")
                standard()
            }
        }
    }
}