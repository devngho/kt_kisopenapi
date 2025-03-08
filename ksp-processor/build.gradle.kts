plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ksp-annotations"))

    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp.symbol.processing.api)
}