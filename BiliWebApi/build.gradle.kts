plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
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
dependencies {
    implementation("com.huanli233.retrofit2-compat:retrofit:2.12.0-p2")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.huanli233.retrofit2-compat:converter-gson:2.12.0-p2") {
        exclude(group = "com.google.code.gson")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}