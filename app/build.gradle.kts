import java.io.ByteArrayOutputStream
import java.util.Properties
import java.io.FileInputStream
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:7.2.0.202503040940-r")
    }
}

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
} else {
    versionProps["VERSION_CODE"] = "1"
}

tasks.register("incrementVersionCode") {
    doLast {
        val versionCode = versionProps["VERSION_CODE"].toString().toInt()
        versionProps["VERSION_CODE"] = (versionCode + 1).toString()
        versionProps.store(versionPropsFile.writer(), null)
        println("Incremented VERSION_CODE to ${versionCode + 1}")
    }
}

tasks.configureEach {
    if (name == "assembleRelease") {
        dependsOn("incrementVersionCode")
    }
}

fun readVersionCode(): Int {
    val versionFile = file("version.properties")
    val props = Properties()
    props.load(FileInputStream(versionFile))
    return props["VERSION_CODE"].toString().toInt()
}

fun readVersion(): String {
    val versionFile = file("version.properties")
    val props = Properties()
    props.load(FileInputStream(versionFile))
    return props["VERSION"].toString()
}

fun getGitHash(): String {
    return kotlin.runCatching {
        val gitDir = project.rootDir.resolve(".git")
        val repository = FileRepositoryBuilder.create(gitDir)
        repository.use { repo ->
            val head = repo.resolve("HEAD")
            head?.abbreviate(8)?.name()
        }
    }.getOrNull() ?: "nogit"
}

android {
    namespace = "com.huanli233.biliterminal2"
    compileSdk = 35

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    defaultConfig {
        applicationId = "com.huanli233.biliterminal2"
        minSdk = 14
        targetSdk = 35
        versionCode = readVersionCode()
        versionName = "${readVersion()}+${getGitHash()}"

        multiDexEnabled = true

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += setOf("armeabi-v7a", "x86", "mips")
        }
    }

    signingConfigs {
        create("release") {
            val localProps = rootProject.file("local.properties")
            if (localProps.exists()) {
                val props = Properties()
                props.load(localProps.inputStream())

                if (props.containsKey("KEY_PATH")) {
                    storeFile = file(props.getProperty("KEY_PATH") ?: "key.jks")
                    storePassword = props.getProperty("KEY_PASSWORD")
                    keyAlias = props.getProperty("ALIAS_NAME")
                    keyPassword = props.getProperty("ALIAS_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        getByName("release") {
            buildConfigField("boolean", "BETA", "false")
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val localProps = rootProject.file("local.properties")
            if (localProps.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        getByName("debug") {
            buildConfigField("boolean", "BETA", "true")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources.excludes += "META-INF/androidx.cardview_cardview.version"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    applicationVariants.all variant@{
        outputs.all {
            val versionName = this@variant.versionName
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = "BiliTerminal2-${this@variant.name}-${versionName}.apk"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    val multidexVersion = "2.0.1"
    implementation("androidx.multidex:multidex:$multidexVersion")

    // noinspection GradleDependency
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    // noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.10.1")
    // noinspection GradleDependency
    implementation("androidx.activity:activity-ktx:1.8.2")
    // noinspection GradleDependency
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // noinspection GradleDependency
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // noinspection GradleDependency
    implementation("com.google.zxing:core:3.3.0")
    // noinspection GradleDependency
    implementation("androidx.appcompat:appcompat:1.5.1")
    //noinspection GradleDependency
    implementation("com.google.android.material:material:1.9.0")
    // noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.asynclayoutinflater:asynclayoutinflater:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    implementation("com.drakeet.multitype:multitype:4.3.0")
    //noinspection GradleDependency
    implementation("org.jsoup:jsoup:1.10.2")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("com.huanli233.okhttp3-compat:okhttp:5.0.0-p2")
    implementation("com.huanli233.retrofit2-compat:retrofit:2.12.0-p2")
    implementation("com.huanli233.retrofit2-compat:converter-gson:2.12.0-p2") {
        exclude("com.google.code.gson", "gson")
    }
    //noinspection GradleDependency
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.github.bumptech.glide:glide:4.13.2")

    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.geetest.sensebot:sensebot:4.4.2.1") {
        exclude(group = "com.squareup.okhttp3")
    }

    implementation(project(":ijkplayer-java"))
    implementation(project(":DanmakuFlameMaster"))
    implementation(project(":brotlij"))
    implementation(project(":BiliWebApi"))

    implementation("org.brotli:dec:0.1.2")
    implementation("com.aayushatharva.brotli4j:brotli4j:1.16.0")

    implementation("com.elvishew:xlog:1.11.0")

}
