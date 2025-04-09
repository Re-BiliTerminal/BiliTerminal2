pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        mavenLocal()
    }
}

rootProject.name = "BiliTerminal2"
include(":app")
include(":ijkplayer-java")
include(":DanmakuFlameMaster")
include(":brotlij")
include(":BiliWebApi")
