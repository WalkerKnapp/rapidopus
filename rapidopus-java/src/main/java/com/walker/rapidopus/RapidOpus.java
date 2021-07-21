package com.walker.rapidopus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RapidOpus {
    public static final Cleaner cleaner = Cleaner.create();

    static {
        String rapidOpusLibraryName = System.mapLibraryName("rapidopus-natives");
        String libraryExtension = rapidOpusLibraryName.substring(rapidOpusLibraryName.indexOf('.'));

        String osDirectory = getOsDirectory();
        String archDirectory = getArchDirectory();

        // This is a nasty hack to get android to load *.so files from a jar.
        // Normally, these would be expected to be bundled in an *.arr, but it is difficult to construct
        // one without the com.android.library plugin, which is incompatible with the java-library plugin.
        // If anyone has a better solution for this, I would be very open to suggestions.
        if (osDirectory.equals("android")) {
            rapidOpusLibraryName = "librapidopus-natives.androidnative";
        }

        Path rapidOpusNativesPath = extractNative("rapidopus-natives", libraryExtension,
                "/natives/" + osDirectory + "/" + archDirectory + "/" + rapidOpusLibraryName);

        if (rapidOpusNativesPath == null) {
            throw new IllegalStateException("This build of RapidOpus is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/rapidopus.");
        }

        System.load(rapidOpusNativesPath.toAbsolutePath().toString());
    }

    private static String getOsDirectory() {
        final String osNameProperty = System.getProperty("os.name").toLowerCase();
        final String javaRuntimeProperty = System.getProperty("java.runtime.name");
        if (javaRuntimeProperty != null && javaRuntimeProperty.toLowerCase().contains("android")) {
            return "android";
        } else if (osNameProperty.contains("nix") || osNameProperty.contains("nux")) {
            return "linux";
        } else if (osNameProperty.contains("win")) {
            return "windows";
        } else if (osNameProperty.contains("mac")) {
            return "macos";
        } else {
            throw new IllegalStateException("Unsupported OS: " + osNameProperty + ". Please open an issue at https://github.com/WalkerKnapp/rapidopus/issues");
        }
    }

    private static String getArchDirectory() {
        final String osArchProperty = System.getProperty("os.arch").toLowerCase();
        if (osArchProperty.contains("aarch64") || (osArchProperty.contains("arm") && (osArchProperty.contains("64") || osArchProperty.contains("v8")))) {
            return "arm64-v8a";
        } else if (osArchProperty.contains("aarch32") || (osArchProperty.contains("arm") && (osArchProperty.contains("32") || osArchProperty.contains("v7")))) {
            return "armv7a";
        } else if (osArchProperty.contains("64")) {
            return "x86-64";
        } else if (osArchProperty.contains("86")) {
            return "x86";
        } else {
            throw new IllegalStateException("Unsupported Arch: " + osArchProperty + ". Please open an issue at https://github.com/WalkerKnapp/rapidopus/issues");
        }
    }

    private static Path extractNative(String prefix, String suffix, String pathInJar) {
        try(InputStream is = RapidOpus.class.getResourceAsStream(pathInJar)) {
            if(is == null) {
                return null;
            }

            // Get a temporary directory to place natives
            Path tempPath = Files.createTempFile(prefix, suffix);

            // Create a lock file for this dll
            Path tempLock = tempPath.resolveSibling(tempPath.getFileName().toString() + ".lock");
            Files.createFile(tempLock);
            tempLock.toFile().deleteOnExit();

            // Copy the natives to be loaded
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);

            // Clean up any natives from previous runs that do not have a corresponding lock file
            Files.list(tempPath.getParent())
                    .filter(path -> path.getFileName().toString().startsWith(prefix) && path.getFileName().toString().endsWith(suffix))
                    .filter(path -> !Files.exists(path.resolveSibling(path.getFileName().toString() + ".lock")))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // ignored, the file is in use without a lock
                        }
                    });

            return tempPath;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
    * Gets the libopus version string.
    * If the build is fixed-point, will have the substring "-fixed," otherwise the build is floating-point.
    *
    * @return The version string.
    */
    public static native String getVersion();
}
