import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class OpusDecoder implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            decoderDestroy(structPointer);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    private final long structPointer;

    public OpusDecoder(int sampleRate, int channels) {
        this.structPointer = decoderCreate(sampleRate, channels);

        this.state = new State(structPointer);
        this.cleanable = RapidOpus.cleaner.register(this, state);
    }

    /**
     * Decodes an opus packet into 16s samples.
     *
     * @param inputData Binary data of the input packet
     * @param outData A direct ByteBuffer no les than the required size to fit all data.
     * @param frameSize Number of samples per channel of available space in outData.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz), this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==NULL) or FEC (decode_fec=1), then frame_size needs to be exactly the duration of audio that is missing, otherwise the decoder will not be in the optimal state to decode the next incoming packet.
     *                  For the PLC and FEC cases, frame_size must be a multiple of 2.5 ms
     * @param decodeFec Either 0 or 1 to request that any in-band forward error correction data be decoded
     *                  If no data is found, the frame is decoded as if it was lost.
     * @return The number of decoded samples.
     */
    public int decode(byte[] inputData, ByteBuffer outData, int frameSize, int decodeFec) {
        int res = decode(structPointer, inputData, outData, frameSize, decodeFec);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Decodes an opus packet into 16s samples.
     *
     * @param inputData A direct ByteBuffer of the opus packet data.
     * @param outData A direct ByteBuffer no les than the required size to fit all data.
     * @param frameSize Number of samples per channel of available space in outData.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz), this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==NULL) or FEC (decode_fec=1), then frame_size needs to be exactly the duration of audio that is missing, otherwise the decoder will not be in the optimal state to decode the next incoming packet.
     *                  For the PLC and FEC cases, frame_size must be a multiple of 2.5 ms
     * @param decodeFec Either 0 or 1 to request that any in-band forward error correction data be decoded
     *                  If no data is found, the frame is decoded as if it was lost.
     * @return The number of decoded samples.
     */
    public int decode(ByteBuffer inputData, ByteBuffer outData, int frameSize, int decodeFec) {
        int res = decode(structPointer, inputData, outData, frameSize, decodeFec);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    // Native Methods

    private static native long decoderCreate(int sampleRate, int channels);
    // TODO: DecoderCtl
    private static native void decoderDestroy(long pDecoder);

    private static native int decode(long pDecoder, byte[] inputData, ByteBuffer outData, int frameSize, int decodeFec);
    private static native int decode(long pDecoder, ByteBuffer inputData, ByteBuffer outData, int frameSize, int decodeFec);
    private static native int decodeFloat(long pDecoder, byte[] inputData, ByteBuffer outData, int frameSize, int decodeFec);
    private static native int decodeFloat(long pDecoder, ByteBuffer inputData, ByteBuffer outData, int frameSize, int decodeFec);

    private static native int decoderGetNbSamples(long pDecoder, byte[] packet);
    private static native int decoderGetNbSamples(long pDecoder, ByteBuffer packet);

    private static native int packetGetBandwidth(byte[] packet);
    private static native int packetGetBandwidth(long pDecoder, ByteBuffer packet);
    private static native int packetGetNbChannels(byte[] packet);
    private static native int packetGetNbChannels(ByteBuffer packet);
    private static native int packetGetNbFrames(byte[] packet);
    private static native int packetGetNbFrames(ByteBuffer packet);
    private static native int packetGetNbSamples(byte[] packet, int sampleRate);
    private static native int packetGetNbSamples(ByteBuffer packet, int sampleRate);
    private static native int packetGetSamplesPerFrame(byte[] packet, int sampleRate);
    private static native int packetGetSamplesPerFrame(ByteBuffer packet, int sampleRate);
    // TODO: Opus_packet_parse

    private static native void pcmSoftClip(float[] pcm, int frameSize, int channels, float[] softclipMem);
    private static native void pcmSoftClip(ByteBuffer pcm, int frameSize, int channels, float[] softclipMem);
    private static native void pcmSoftClip(float[] pcm, int frameSize, int channels, ByteBuffer softclipMem);
    private static native void pcmSoftClip(ByteBuffer pcm, int frameSize, int channels, ByteBuffer softclipMem);
}
