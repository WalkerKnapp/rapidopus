package me.walkerknapp.rapidopus;

/**
 * A specification for the bandwidth of an individual audio packet received by a decoder.
 */
public enum OpusBandwidth {
    /**
     *  {@literal <}4 kHz bandpass
     */
    NARROWBAND(1101),
    /**
     *  {@literal <}6 kHz bandpass
     */
    MEDIUMBAND(1102),
    /**
     *  {@literal <}8 kHz bandpass
     */
    WIDEBAND(1103),
    /**
     *  {@literal <}12 kHz bandpass
     */
    SUPERWIDEBAND(1104),
    /**
     *  {@literal <}20 kHz bandpass
     */
    FULLBAND(1105);

    int id;

    OpusBandwidth(int id) {
        this.id = id;
    }

    /**
     * Determines the bandwidth specification from the internal integer representation.
     * @param id The internal integer representation.
     * @return The bandwidth specification.
     */
    public static OpusBandwidth valueOf(int id) {
        switch (id) {
            case 1101:
                return NARROWBAND;
            case 1102:
                return MEDIUMBAND;
            case 1103:
                return WIDEBAND;
            case 1104:
                return SUPERWIDEBAND;
            case 1105:
                return FULLBAND;
            default:
                throw new IllegalArgumentException("Unknown bandwidth: " + id);
        }
    }
}
