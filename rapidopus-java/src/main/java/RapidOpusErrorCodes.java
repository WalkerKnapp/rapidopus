public class RapidOpusErrorCodes {
    /**
     * No Error
     */
    public static final int OPUS_OK = 0;
    /**
     * One or more invalid/out of range arguments.
     */
    public static final int OPUS_BAD_ARG = -1;
    /**
     * Not enough bytes allocated in the buffer.
     */
    public static final int OPUS_BUFFER_TOO_SMALL = -2;
    /**
     * An internal error was detected.
     */
    public static final int OPUS_INTERNAL_ERROR = -3;
    /**
     * The compressed data passed is corrupted
     */
    public static final int OPUS_INVALID_PACKET = -4;
    /**
     * Invalid/unsupported request number
     */
    public static final int OPUS_UNIMPLEMENTED = -5;
    /**
     * An encoder or decoder structure is invalid or already freed
     */
    public static final int OPUS_INVALID_STATE = -6;
    /**
     * Memory allocation has failed
     */
    public static final int OPUS_ALLOC_FAIL = -7;

    public static String translateError(int error) {
        switch (error) {
            case OPUS_OK:
                return "No Error";
            case OPUS_BAD_ARG:
                return "One or more invalid/out of range arguments";
            case OPUS_BUFFER_TOO_SMALL:
                return "Not enough bytes allocated in the buffer";
            case OPUS_INTERNAL_ERROR:
                return "Internal error.";
            case OPUS_INVALID_PACKET:
                return "The compressed data passed is corrupted";
            case OPUS_UNIMPLEMENTED:
                return "The requested method is currently unsupported.";
            case OPUS_INVALID_STATE:
                return "An encoder or decoder structure is invalid or already freed";
            case OPUS_ALLOC_FAIL:
                return "Memory allocation has failed";
            default:
                return "Unknown error: " + error;
        }
    }
}
