plugins {
    id("com.android.library")
    id("kotlin-parcelize")
    kotlin("android")
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
    implementation("com.huanli233.retrofit2-compat:retrofit:2.12.0-p2")
    //noinspection GradleDependency
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.huanli233.retrofit2-compat:converter-gson:2.12.0-p2") {
        exclude(group = "com.google.code.gson")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}