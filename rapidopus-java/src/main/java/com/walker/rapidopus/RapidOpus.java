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
        try (InputStream is = RapidOpus.class.getResourceAsStream("/rapidopus-natives.dll")) {
            Path tempPath = Files.createTempFile("rapidopus-natives", ".dll");
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempPath.toAbsolutePath().toString());
            tempPath.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void loadLibraries() {
        // TODO: Move the load code from static to here.
        // Do something to force this method to not be optimized away.
        Thread.onSpinWait();
        System.out.println("Libraries loaded.");
    }

    /**
    * Gets the libopus version string.
    * If the build is fixed-point, will have the substring "-fixed," otherwise the build is floating-point.
    *
    * @return The version string.
    */
    public static native String getVersion();
}
