package me.walkerknapp

import org.gradle.api.Action
import org.gradle.internal.os.OperatingSystem
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.toolchain.*
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// Kotlin has issues if we try to get the working directory from inside a RuleSource, so put it in a static field.
class BuildContext {
    companion object {
        @JvmStatic
        lateinit var workingDirectory: File
    }
}

// For some reason, the compiler plugins (giving NativeToolChainRegistry the compiler factories) don't run
// until after the build script is evaluated, so we have to set up our toolchains as a part of a @Mutate rule.

open class ToolchainConfiguration : RuleSource() {

    @Mutate
    fun NativeToolChainRegistry.configureToolchains() {

        val addFpicArg: Action<MutableList<String>> = Action{
            it.add("-fPIC")
        }

        val compileWithFpic: Action<GccPlatformToolChain> = Action{ platform ->
            platform.cppCompiler.withArguments(addFpicArg)
        }

        // On linux, compile using gcc, mingw, and osxcross
        if (OperatingSystem.current().isLinux) {
            register("gcc", Gcc::class.java) { gcc ->
                gcc.target("linux_x86-64", compileWithFpic)
                gcc.target("linux_x86", compileWithFpic)

                gcc.target("windows_x86-64") {
                    it.getcCompiler().executable = "x86_64-w64-mingw32-gcc"
                    it.cppCompiler.executable = "x86_64-w64-mingw32-g++"
                    it.linker.executable = "x86_64-w64-mingw32-g++"
                    it.staticLibArchiver.executable = "x86_64-w64-mingw32-ar"

                    it.cppCompiler.withArguments(addFpicArg)
                }

                gcc.target("windows_x86") {
                    it.getcCompiler().executable = "i686-w64-mingw32-gcc"
                    it.cppCompiler.executable = "i686-w64-mingw32-g++"
                    it.linker.executable = "i686-w64-mingw32-g++"
                    it.staticLibArchiver.executable = "i686-w64-mingw32-ar"

                    it.cppCompiler.withArguments(addFpicArg)
                }
            }

            val osxcrossBin = locateOsxCross()
            if (osxcrossBin != null) {
                register("osxcross", Clang::class.java) { clang ->
                    clang.path(osxcrossBin, osxcrossBin.resolve("../binutils/bin"))

                    // Hack into the toolchain internals a bit to set a cached location for our SDK libraries, since
                    // gradle tries to run xcrun without using the path we set.
                    val sdkDirs = File(osxcrossBin.parent.resolve("SDK").toString()).listFiles()

                    if (sdkDirs != null && sdkDirs.isNotEmpty()) {
                        val sLD = AbstractGccCompatibleToolChain::class.java.getDeclaredField("standardLibraryDiscovery")
                        sLD.isAccessible = true
                        val pathLocator = org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery::class.java.getDeclaredField("macOSSdkPathLocator");
                        pathLocator.isAccessible = true
                        val cachedLocation = org.gradle.nativeplatform.toolchain.internal.xcode.AbstractLocator::class.java.getDeclaredField("cachedLocation");
                        cachedLocation.isAccessible = true
                        cachedLocation.set(pathLocator.get(sLD.get(clang)), sdkDirs[0])
                    }

                    clang.target("macos_x86-64") {
                        it as DefaultGccPlatformToolChain
                        it.getcCompiler().executable = "o64-clang"
                        it.cppCompiler.executable = "o64-clang++"
                        it.linker.executable = "o64-clang++"
                        it.assembler.executable = "o64-clang"
                        it.symbolExtractor.executable = "x86_64-apple-darwin19-objcopy"
                        it.stripper.executable = "x86_64-apple-darwin19-strip"

                        it.cppCompiler.withArguments(addFpicArg)
                    }
                }
            }
        }

        // On windows, compile using visualcpp or gcc. Unfortunately, cross compile from windows isn't really possible
        if (OperatingSystem.current().isWindows) {
            register("visualCpp", VisualCpp::class.java)

            register("gcc", Gcc::class.java) { gcc ->
                gcc.target("windows_x86-64")
                gcc.target("windows_x86")
            }
        }

        val androidNdk = locateAndroidNdk()
        if (androidNdk != null) {
            val extraIncludes = androidNdk.parent.parent.parent.parent.parent.resolve("sysroot").resolve("usr").resolve("include")

            register("androidNdk", Clang::class.java) { clang ->

                clang.path(androidNdk)

                clang.target("android_armv7a") { gcc ->
                    gcc as DefaultGccPlatformToolChain

                    val configureArguments: Action<MutableList<String>> = org.gradle.api.Action { 
                        it.add("-target")
                        it.add("armv7a-linux-androideabi21")
                        it.add(0, "-isystem")
                        it.add(1, extraIncludes.toAbsolutePath().toString())
                        it.add(2, "-isystem")
                        it.add(3, extraIncludes.resolve("arm-linux-androideabi").toAbsolutePath().toString())
                        it.add(4, "-isystem")
                        it.add(5, androidNdk.parent.resolve("sysroot").resolve("usr").resolve("include").resolve("c++").resolve("v1").toAbsolutePath().toString())

                        it.add("-fdeclspec")
                        it.add("-fPIC")
                    }

                    gcc.getcCompiler().executable = "clang"
                    gcc.getcCompiler().withArguments(configureArguments)
                    gcc.cppCompiler.executable = "clang++"
                    gcc.cppCompiler.withArguments(configureArguments)
                    gcc.linker.executable = "clang++"
                    gcc.linker.withArguments (configureArguments)

                    gcc.symbolExtractor.executable = "arm-linux-androideabi-objcopy"
                    gcc.staticLibArchiver.executable = "arm-linux-androideabi-ar"
                    gcc.stripper.executable = "arm-linux-androideabi-strip"
                }

                clang.target("android_arm64-v8a") { gcc ->
                    gcc as DefaultGccPlatformToolChain

                    val configureArguments: Action<MutableList<String>> = org.gradle.api.Action {
                        it.add("-target")
                        it.add("aarch64-linux-android21")
                        it.add(0, "-isystem")
                        it.add(1, extraIncludes.toAbsolutePath().toString())
                        it.add(2, "-isystem")
                        it.add(3, extraIncludes.resolve("aarch64-linux-android").toAbsolutePath().toString())
                        it.add(4, "-isystem")
                        it.add(5, androidNdk.parent.resolve("sysroot").resolve("usr").resolve("include").resolve("c++").resolve("v1").toAbsolutePath().toString())

                        it.add("-fdeclspec")
                        it.add("-fms-extensions")
                        it.add("-fPIC")
                    }

                    gcc.getcCompiler().executable = "clang"
                    gcc.getcCompiler().withArguments(configureArguments)
                    gcc.cppCompiler.executable = "clang++"
                    gcc.cppCompiler.withArguments(configureArguments)
                    gcc.linker.executable = "clang++"
                    gcc.linker.withArguments (configureArguments)

                    gcc.symbolExtractor.executable = "aarch64-linux-android-objcopy"
                    gcc.staticLibArchiver.executable = "aarch64-linux-android-ar"
                    gcc.stripper.executable = "aarch64-linux-android-strip"
                }

                clang.target("android_x86") { gcc ->
                    gcc as DefaultGccPlatformToolChain

                    val configureArguments: Action<MutableList<String>> = org.gradle.api.Action {
                        it.add("-target")
                        it.add("i686-linux-android21")
                        it.add(0, "-isystem")
                        it.add(1, extraIncludes.toAbsolutePath().toString())
                        it.add(2, "-isystem")
                        it.add(3, extraIncludes.resolve("i686-linux-android").toAbsolutePath().toString())
                        it.add(4, "-isystem")
                        it.add(5, androidNdk.parent.resolve("sysroot").resolve("usr").resolve("include").resolve("c++").resolve("v1").toAbsolutePath().toString())

                        it.add("-fdeclspec")
                        it.add("-fPIC")
                    }


                    gcc.getcCompiler().executable = "clang"
                    gcc.getcCompiler().withArguments(configureArguments)
                    gcc.cppCompiler.executable = "clang++"
                    gcc.cppCompiler.withArguments(configureArguments)
                    gcc.linker.executable = "clang++"
                    gcc.linker.withArguments (configureArguments)

                    gcc.symbolExtractor.executable = "i686-linux-android-objcopy"
                    gcc.staticLibArchiver.executable = "i686-linux-android-ar"
                    gcc.stripper.executable = "i686-linux-android-strip"
                }

                clang.target("android_x86-64") { gcc ->
                    gcc as DefaultGccPlatformToolChain

                    val configureArguments: Action<MutableList<String>> = org.gradle.api.Action {
                        it.add("-target")
                        it.add("x86_64-linux-android21")
                        it.add(0, "-isystem")
                        it.add(1, extraIncludes.toAbsolutePath().toString())
                        it.add(2, "-isystem")
                        it.add(3, extraIncludes.resolve("x86_64-linux-android").toAbsolutePath().toString())
                        it.add(4, "-isystem")
                        it.add(5, androidNdk.parent.resolve("sysroot").resolve("usr").resolve("include").resolve("c++").resolve("v1").toAbsolutePath().toString())

                        it.add("-fdeclspec")
                        it.add("-fPIC")
                    }


                    gcc.getcCompiler().executable = "clang"
                    gcc.getcCompiler().withArguments(configureArguments)
                    gcc.cppCompiler.executable = "clang++"
                    gcc.cppCompiler.withArguments(configureArguments)
                    gcc.linker.executable = "clang++"
                    gcc.linker.withArguments (configureArguments)

                    gcc.symbolExtractor.executable = "x86_64-linux-android-objcopy"
                    gcc.staticLibArchiver.executable = "x86_64-linux-android-ar"
                    gcc.stripper.executable = "x86_64-linux-android-strip"
                }
            }
        }
    }

    private fun locateAndroidNdk(): Path? {
        // Check system property
        var androidNdk = if (System.getProperty("androidNdk") != null) Paths.get(System.getProperty("androidNdk")) else null

        // Check "ANDROID_NDK_ROOT" environment variable
        if (androidNdk == null && System.getenv("ANDROID_NDK_ROOT") != null) {
            androidNdk = Paths.get(System.getenv("ANDROID_NDK_ROOT"))
        }

        if (androidNdk == null && System.getenv("ANDROID_NDK_HOME") != null) {
            androidNdk = Paths.get(System.getenv("ANDROID_NDK_HOME"))
        }

        // Check the working directory
        if (androidNdk == null) {
            Files.list(BuildContext.workingDirectory.toPath()).forEach {
                if (it.fileName.startsWith("android-ndk")) {
                    androidNdk = it
                }
            }
        }

        if (androidNdk == null) {
            System.err.println("No Android NDK found, android builds will be unavailable. Please set the ANDROID_NDK_ROOT or ANDROID_NDK_HOME variables to the install location, run gradle with -DandroidNdk=<Install Path>, or symlink the install path to your \"devolay\" folder.")
        }

        val prebuilts = androidNdk?.resolve("toolchains")?.resolve("llvm")?.resolve("prebuilt")
        var bin: Path? = null

        if (prebuilts != null) {
            java.nio.file.Files.list(prebuilts).findFirst().ifPresent {
                bin = it.resolve("bin")
            }
        }

        return bin
    }

    private fun locateOsxCross(): Path? {
        // Check system property
        var osxcross = if (System.getProperty("osxcrossBin") != null) Paths.get(System.getProperty("osxcrossBin")) else null

        // Check the working directory
        if (osxcross == null) {
            Files.list(BuildContext.workingDirectory.toPath()).forEach {
                if (it.fileName.startsWith("osxcross")) {
                    osxcross = it.resolve("target").resolve("bin")
                }
            }
        }

        // Check the system path
        val searchResult = ToolSearchPath(OperatingSystem.current()).locate(ToolType.C_COMPILER, "xcrun")
        if (osxcross == null && searchResult.isAvailable) {
            osxcross = searchResult.tool.parentFile.toPath();
        }


        if (osxcross == null) {
            System.err.println("No Osxcross found, Macos builds will be unavailable. Please add the osxcross/target/bin path to your PATH variable, run gradle with -Dosxcross=<Bin Path>, or symlink the install path to your \"devolay\" folder.")
        }

        println("Located osxcross (sub) at $osxcross")

        return osxcross
    }
}
