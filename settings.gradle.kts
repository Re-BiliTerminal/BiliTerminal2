pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        mavenLocal()
    }
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "BiliTerminal2"
include(":app")
include(":ijkplayer-java")
include(":DanmakuFlameMaster")
include(":brotlij")
include(":BiliWebApi")
include(":NativeBitmap")
include(":Skeleton")
