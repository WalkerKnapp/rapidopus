plugins {
    kotlin("jvm")
    id("groovy")
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins.create("opus-build") {
        id = "opus-build"
        implementationClass = "me.walkerknapp.OpusBuildPlugin"
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation("me.walkerknapp.use-cmake-library:me.walkerknapp.use-cmake-library.gradle.plugin:0.0.4")
    implementation("org.apache.commons:commons-lang3:3+")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.dslplatform:dsl-json-java8:1+")
    //implementation(files("B:\\Workspace\\cfi-java\\build\\libs\\cfi-java-0.0.1.jar"))
    //implementation(files("B:\\Workspace\\gradle-use-cmake-library\\build\\libs\\gradle-use-cmake-library-0.0.4.jar"))
    //implementation("kr.jclab.gradle.cmakeplugin.cmake-library:kr.jclab.gradle.cmakeplugin.cmake-library.gradle.plugin:0.0.1-rc2")
}