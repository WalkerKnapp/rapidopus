package me.walkerknapp;

import me.walkerknapp.usecmakelibrary.CMakeExtension
import me.walkerknapp.usecmakelibrary.CMakeLibrary
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.nativeplatform.TargetMachineFactory

open class OpusBuildPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.rootProject.name = "opus"
        target.gradle.rootProject { project ->
            project.group = "org.opus-codec"

            project.plugins.apply(CMakeLibrary::class.java)

            BuildContext.workingDirectory = project.file(".")
            project.apply(mapOf("plugin" to ToolchainConfiguration::class.java))

            val machines: TargetMachineFactory = project.extensions.getByType(TargetMachineFactory::class.java)

            project.extensions.configure<CMakeExtension>("cmake") {
                it.targetMachines.set(listOf(
                    machines.windows.x86, machines.windows.x86_64,
                    machines.macOS.x86_64,
                    machines.linux.x86, machines.linux.x86_64,
                    machines.os("android").architecture("armv7a"),
                    machines.os("android").architecture("arm64-v8a"),
                    machines.os("android").x86,
                    machines.os("android").x86_64))
            }
        }
    }
}
