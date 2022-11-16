import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    `maven-publish`
}

group = "com.github.devngho"
version = "1.0-SNAPSHOT"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/devngho/kt_kisopenapi") // Github Package
            credentials {
                //Fetch these details from the properties file or from Environment variables
                username = System.getenv("GPR_USER")
                password = System.getenv("GPR_API_KEY")
            }
        }
    }
}

repositories {
    mavenCentral()
}

kotlin {
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
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            implementation("com.ionspin.kotlin:bignum:0.3.7")
            implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:0.3.2")
        }

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                standard()
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
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
        val nativeTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-$platform:$coroutineVersion")
                standard()
            }
        }
    }
}