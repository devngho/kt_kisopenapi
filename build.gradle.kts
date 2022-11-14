import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    kotlin("multiplatform") version "1.7.20"
}

group = "com.github.devngho"
version = "1.0-SNAPSHOT"

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
    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
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

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
            }
        }
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-$platform:$coroutineVersion")
            }
        }
        val nativeTest by getting
    }
}
