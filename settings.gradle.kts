pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven (url = "https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven (url = "https://dl.bintray.com/kotlin/kotlin-eap" )
        maven (url = "https://api.xposed.info/" )
        maven (url = "https://jitpack.io" )
    }
}

rootProject.name = "Shamrock"
include(
    ":app",
    ":xposed",
    ":qqinterface"
)