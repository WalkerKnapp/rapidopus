import java.util.Properties

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.4.30"
    }

    repositories {
        gradlePluginPortal()
        google()
        maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    }
}