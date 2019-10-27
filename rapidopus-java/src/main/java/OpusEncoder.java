import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class OpusEncoder implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            encoderDestroy(structPointer);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    private final long structPointer;

    /**
     * Allocates and initializes an encoder state.
     * Note: regardless of the sample rate and number of channels, the encoder can switch to a lower
     * audio bandwidth or number of channels if the bitrate selected is too low. This means it's safe to
     * always use 48kHz stereo input and let the encoder optimize the encoding.
     *
     * @param sampleRate Sample rate in Hz. This must be 8000, 12000, 16000, 24000, or 48000
     * @param channels Number of channels. Must be 1 or 2
     * @param applicationMode Application mode.
     */
    public OpusEncoder(int sampleRate, int channels, OpusApplicationMode applicationMode) {
        this.structPointer = encoderCreate(sampleRate, channels, applicationMode.id);

        this.state = new State(structPointer);
        this.cleanable = RapidOpus.cleaner.register(this, state);
    }

    /**
     * Encodes an opus frame.
     * The frame size must be a supported Opus frame size. For instance at 48kHz:
     * the permitted values are 120, 240, 480, 960, 1920, and 2880.
     * Passing a duration less than 10ms will prevent the encoder from using the LPC or hybrid modes.
     *
     * @param inputData The 16s PCM data (interleaved if 2 channels). Must be size frameSize * channels
     * @param frameSize Number of samples per channel in the input signal.
     * @param outputData A direct ByteBuffer to store the output. Will only write up to buffer capacity. A size of 4000 is recommended.
     * @return The length of the encoded packet
     */
    public int encode(short[] inputData, int frameSize, ByteBuffer outputData) {
        int res = encode(structPointer, inputData, frameSize, outputData);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Encodes an opus frame.
     * The frame size must be a supported Opus frame size. For instance at 48kHz:
     * the permitted values are 120, 240, 480, 960, 1920, and 2880.
     * Passing a duration less than 10ms will prevent the encoder from using the LPC or hybrid modes.
     *
     * @param inputData A direct ByteBuffer of 16s PCM data (interleaved if 2 channels). Must be size frameSize * channels * Short.BYTES
     * @param frameSize Number of samples per channel in the input signal.
     * @param outputData A direct ByteBuffer to store the output. Will only write up to buffer capacity. A size of 4000 is recommended.
     * @return The length of the encoded packet
     */
    public int encode(ByteBuffer inputData, int frameSize, ByteBuffer outputData) {
        int res = encode(structPointer, inputData, frameSize, outputData);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Encodes an Opus frame from floating point input.
     *
     * The PCM data  has a normal range of +/-1.0.
     * Samples beyond +/-1.0 are supported but will be clipped using the integer API by decoders and should only be used
     * if the receiving end is known to be supporting extended dynamic range.
     *
     * The frame size must be a supported Opus frame size. For instance at 48kHz:
     * the permitted values are 120, 240, 480, 960, 1920, and 2880.
     * Passing a duration less than 10ms will prevent the encoder from using the LPC or hybrid modes.
     *
     * @param inputData The floating-point PCM data (interleaved if 2 channels). Must be size frameSize * channels
     * @param frameSize The number of samples per channel in the input signal.
     * @param outputData A direct ByteBuffer to store the output. Will only write up to buffer capacity. A size of 4000 is recommended.
     * @return The length of the encoded packet.
     */
    public int encodeFloat(float[] inputData, int frameSize, ByteBuffer outputData) {
        int res = encodeFloat(structPointer, inputData, frameSize, outputData);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Encodes an Opus frame from floating point input.
     *
     * The PCM data  has a normal range of +/-1.0.
     * Samples beyond +/-1.0 are supported but will be clipped using the integer API by decoders and should only be used
     * if the receiving end is known to be supporting extended dynamic range.
     *
     * The frame size must be a supported Opus frame size. For instance at 48kHz:
     * the permitted values are 120, 240, 480, 960, 1920, and 2880.
     * Passing a duration less than 10ms will prevent the encoder from using the LPC or hybrid modes.
     *
     * @param inputData A direct ByteBuffer of the floating-point PCM data (interleaved if 2 channels). Must be size frameSize * channels * Float.BYTES
     * @param frameSize The number of samples per channel in the input signal.
     * @param outputData A direct ByteBuffer to store the output. Will only write up to buffer capacity. A size of 4000 is recommended.
     * @return The length of the encoded packet.
     */
    public int encodeFloat(ByteBuffer inputData, int frameSize, ByteBuffer outputData) {
        int res = encodeFloat(structPointer, inputData, frameSize, outputData);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    // Native methods

    private static native long encoderCreate(int sampleRate, int channels, int applicationMode);
    // TODO: opus_encoder_ctl
    private static native void encoderDestroy(long structPointer);

    private static native int encode(long structPointer, short[] inputData, int frameSize, ByteBuffer outputData);
    private static native int encode(long structPointer, ByteBuffer inputData, int frameSize, ByteBuffer outputData);
    private static native int encodeFloat(long structPointer, float[] inputData, int frameSize, ByteBuffer outputData);
    private static native int encodeFloat(long structPointer, ByteBuffer inputData, int frameSize, ByteBuffer outputData);
}
