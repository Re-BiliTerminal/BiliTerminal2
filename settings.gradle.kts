pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repositories/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repositories/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenLocal()
    }
}

rootProject.name = "BiliTerminal2"
include(":app")
include(":ijkplayer-java")
include(":DanmakuFlameMaster")
include(":brotlij")
include(":BiliWebApi")
