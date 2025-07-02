import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kotlin.android)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.javaVersion.get())
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.huanli233.biliwebapi"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }
}

dependencies {
    implementation(libs.retrofit2.compat.retrofit)
    //noinspection GradleDependency
    implementation(libs.google.gson)
    implementation(libs.retrofit2.compat.converter.gson) {
        exclude(group = "com.google.code.gson")
    }
    implementation(libs.kotlinx.coroutines.core)
}