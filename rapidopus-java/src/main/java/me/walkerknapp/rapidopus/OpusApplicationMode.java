package me.walkerknapp.rapidopus;

/**
 * An operating mode to start the Opus encoder with, to optimize for different voice signals and network scenarios.
 */
public enum OpusApplicationMode {
    /**
     * Gives best quality at a given bitrate for voice signals.
     * Will enhance input signal by high-pass filtering and emphasizing formants and harmonics
     * Optionally includes in-band forward error correction to protect against packet loss.
     * Use this mode for typical VoIP applications.
     * Even at high bitrates, input may sound different from output.
     */
    VOIP(2048),
    /**
     * Gives best quality at a given bitrate for most non-voice signals like music.
     * Use this mdoe for music and mixed (music/voice) content, broadcast, and applications requiring {@literal <}15ms of delay.
     */
    AUDIO(2049),
    /**
     * Low-delay mode that disables speech-optimized mode in exchange for slightly reduced delay.
     */
    RESTRICTED_LOWDELAY(2051);

    int id;

    OpusApplicationMode(int id) {
        this.id = id;
    }

    /**
     * Determines the application mode from the internal integer representation.
     * @param id The internal integer representation of this mode.
     * @return The application mode.
     */
    public static OpusApplicationMode valueOf(int id) {
        switch (id) {
            case 2048:
                return VOIP;
            case 2049:
                return AUDIO;
            case 2051:
                return RESTRICTED_LOWDELAY;
            default:
                throw new IllegalArgumentException("Unknown application mode: " + id);
        }
    }
}
