pluginManagement {
    val kotlinVersion: String by settings
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id(quarkusPluginId) version quarkusPluginVersion
    }
}

rootProject.name = "kotlin-suspend-bridge-annotation-bug"
