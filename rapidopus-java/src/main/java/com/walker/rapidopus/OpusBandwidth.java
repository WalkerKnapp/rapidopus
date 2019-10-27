package com.walker.rapidopus;

public enum OpusBandwidth {
    /**
     *  < 4 kHz bandpass
     */
    NARROWBAND(1101),
    /**
     *  < 6 kHz bandpass
     */
    MEDIUMBAND(1102),
    /**
     *  < 8 kHz bandpass
     */
    WIDEBAND(1103),
    /**
     *  < 12 kHz bandpass
     */
    SUPERWIDEBAND(1104),
    /**
     *  < 20 kHz bandpass
     */
    FULLBAND(1105);

    int id;

    OpusBandwidth(int id) {
        this.id = id;
    }

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
