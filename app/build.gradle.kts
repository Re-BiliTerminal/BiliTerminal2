import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.FileInputStream
import java.util.Properties
import java.util.stream.StreamSupport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.materialthemebuilder)
    alias(libs.plugins.autoresconfig)
//    alias(libs.plugins.kapt)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.org.eclipse.jgit)
    }
}

fun getGitCommitCount(): Int {
    return kotlin.runCatching {
        val gitDir = project.rootDir.resolve(".git")
        val repository = FileRepositoryBuilder.create(gitDir)
        repository.use { repo ->
            val head = repo.resolve("HEAD")
            StreamSupport.stream(Git(repo).log().add(head).call().spliterator(), false).count().toInt()
        }
    }.getOrNull() ?: -1
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
    namespace = "com.huanli233.bilizepam"
    compileSdk = libs.versions.compileSdk.get().toInt()

    lint {
        baseline = file("lint-baseline.xml")
        disable.add("MissingTranslation")
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    defaultConfig {
        applicationId = "com.huanli233.bilizepam.compose"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = getGitCommitCount()
        versionName = "${readVersion()}+${getGitHash()}"

        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }

    packaging {
        resources.excludes += "META-INF/androidx.cardview_cardview.version"
    }

    buildFeatures {
        viewBinding = true
//        dataBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-XXLanguage:+WhenGuards")
    }

    applicationVariants.all variant@{
        outputs.all {
            val versionName = this@variant.versionName
            val abi = filters.find { it.filterType == "ABI" }?.identifier ?: "universal"
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "BiliZepam-${this@variant.name}-${versionName}-${abi}.apk"
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

autoResConfig {
    generateClass = true
    generatedClassFullName = "com.huanli233.bilizepam.Locales"
    generateRes = true
    generatedResPrefix = null
    generatedArrayFirstItem = "SYSTEM"
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Red" to "F44336",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "DeepPurple" to "673AB7",
            "Indigo" to "3F51B5",
            "Blue" to "2196F3",
            "LightBlue" to "03A9F4",
            "Cyan" to "00BCD4",
            "Teal" to "009688",
            "Green" to "4FAF50",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Yellow" to "FFEB3B",
            "Amber" to "FFC107",
            "Orange" to "FF9800",
            "DeepOrange" to "FF5722",
            "Brown" to "795548",
            "BlueGrey" to "607D8F",
            "Sakura" to "FF9CA8"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                primaryColor = "#$color"
            }
        }
    }
    generateTextColors = true
}

configurations.all {
    resolutionStrategy {
        force(libs.androidx.core)
        force(libs.androidx.appcompat)
        force(libs.androidx.transition)
        exclude(group = "com.google.android.material", module = "material")
    }
}

dependencies {

    // https://github.com/SkywalkerDarren/Skeleton, a fork of https://github.com/ethanhua/Skeleton
    implementation(project(":Skeleton"))
    implementation(libs.shimmer)

    implementation(project(":ijkplayer-java"))
    implementation(project(":DanmakuFlameMaster"))
    implementation(project(":brotlij"))
    implementation(project(":BiliWebApi")) {
        exclude("com.google.code.gson", "gson")
    }

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.multidex)

    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.transition)
    implementation(libs.zxing.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.materialpreferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.asynclayoutinflater)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.interpolator)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.preference.ktx)
//    implementation(libs.androidx.wear)

    implementation(libs.hikage.core)
    implementation(libs.hikage.recyclerview)
    implementation(libs.hikage.extension.betterandroid)
    ksp(libs.hikage.compiler)
    implementation(libs.hikage.widget.androidx)
    implementation(libs.hikage.widget.material)

    implementation(libs.flexbox)

    implementation(libs.protobuf.javalite)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.multitype)
    implementation(libs.photoview)

    implementation(libs.expandablelayout)

    implementation(libs.splitties.fun1.pack.android.base)
    implementation(libs.splitties.fun1.pack.android.material.components)

    implementation(libs.okhttp3.compat.okhttp)
    implementation(libs.retrofit2.compat.retrofit)
    implementation(libs.retrofit2.compat.converter.gson) {
        exclude("com.google.code.gson", "gson")
    }
    implementation(libs.google.gson)
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.eventbus)
    implementation(libs.geetest.sensebot) {
        exclude(group = "com.squareup.okhttp3")
    }

    implementation(libs.brotli.dec)
    implementation(libs.brotli4j)

    implementation(libs.xlog)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.material3)
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.compose.animation:animation-graphics:1.9.0")
    implementation("androidx.compose.material:material-ripple:1.9.0")
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")
    implementation("androidx.graphics:graphics-path:1.0.1")
//    implementation(libs.androidx.wear.material3)
//    implementation(libs.androidx.wear.navigation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp) {
        exclude(group = "com.squareup.okhttp3")
    }
    implementation(libs.material.kolor)
    implementation(libs.dotsindicator)
    implementation(libs.compose.shimmer)

    // Media3 for video playback
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-common:1.5.0")

    // Image zoom and processing
    implementation("net.engawapg.lib:zoomable:1.6.2")

    // WorkManager for download tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(kotlin("reflect"))
}