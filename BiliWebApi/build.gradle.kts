plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kotlin.android)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

android {
    compileSdk = 35
    namespace = "com.huanli233.biliwebapi"

    defaultConfig {
        minSdk = 14
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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