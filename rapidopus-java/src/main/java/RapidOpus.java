import java.lang.ref.Cleaner;

public class RapidOpus {
    public static final Cleaner cleaner = Cleaner.create();

    /**
     * Gets the libopus version string.
     * If the build is fixed-point, will have the substring "-fixed," otherwise the build is floating-point.
     *
     * @return The version string.
     */
    public static native String getVersion();
}
