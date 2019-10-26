import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

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
     * @param outData A direct ByteBuffer no less than the required size to fit all data.
     *                Size should be frameSize * channels * Short.BYTES
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
     * @param outData A direct ByteBuffer no less than the required size to fit all data.
     *                Size should be frameSize * channels * Short.BYTES
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

    /**
     * Decodes an opus packet into floating point samples.
     *
     * @param inputData Binary data of the input packet
     * @param outData A direct ByteBuffer no less than the required size to fit all data.
     *                Size should be frameSize * channels * Float.BYTES
     * @param frameSize Number of samples per channel of available space in outData.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz), this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==NULL) or FEC (decode_fec=1), then frame_size needs to be exactly the duration of audio that is missing, otherwise the decoder will not be in the optimal state to decode the next incoming packet.
     *                  or the PLC and FEC cases, frame_size must be a multiple of 2.5 ms
     * @param decodeFec Either 0 or 1 to request that any in-band forward error correction data be decoded
     *                  If no data is found, the frame is decoded as if it was lost.
     * @return The number of decoded samples.
     */
    public int decodeFloat(byte[] inputData, ByteBuffer outData, int frameSize, int decodeFec) {
        int res = decodeFloat(structPointer, inputData, outData, frameSize, decodeFec);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Decodes an opus packet into floating point samples.
     *
     * @param inputData A direct ByteBuffer of the opus packet data
     * @param outData A direct ByteBuffer no less than the required size to fit all data.
     *                Size should be frameSize * channels * Float.BYTES
     * @param frameSize Number of samples per channel of available space in outData.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz), this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==NULL) or FEC (decode_fec=1), then frame_size needs to be exactly the duration of audio that is missing, otherwise the decoder will not be in the optimal state to decode the next incoming packet.
     *                  or the PLC and FEC cases, frame_size must be a multiple of 2.5 ms
     * @param decodeFec Either 0 or 1 to request that any in-band forward error correction data be decoded
     *                  If no data is found, the frame is decoded as if it was lost.
     * @return The number of decoded samples.
     */
    public int decodeFloat(ByteBuffer inputData, ByteBuffer outData, int frameSize, int decodeFec) {
        int res = decodeFloat(structPointer, inputData, outData, frameSize, decodeFec);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Get the number of samples in a packet in the context of the decoder.
     *
     * @param packet Binary data of the input packet
     * @return The number of samples in the packet
     */
    public int getNbSamplesDecoder(byte[] packet) {
        int res = decoderGetNbSamples(structPointer, packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Get the number of samples in a packet in the context of the decoder.
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @return The number of samples in the packet
     */
    public int getNbSamplesDecoder(ByteBuffer packet) {
        int res = decoderGetNbSamples(structPointer, packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    /**
     * Gets the bandwidth of an Opus packet
     *
     * @param packet Binary data of the input packet
     * @return The bandwidth of the packet data.
     */
    public static OpusBandwidth getPacketBandwidth(byte[] packet) {
        int res = packetGetBandwidth(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return OpusBandwidth.valueOf(res);
    }

    /**
     * Gets the bandwidth of an Opus packet
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @return The bandwidth of the packet data.
     */
    public static OpusBandwidth getPacketBandwidth(ByteBuffer packet) {
        int res = packetGetBandwidth(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return OpusBandwidth.valueOf(res);
    }

    /**
     * Gets the number of channels from an Opus packet.
     *
     * @param packet Binary data of the input packet
     * @return The number of channels of the packet.
     */
    public static int getPacketChannelCount(byte[] packet) {
        int res = packetGetNbChannels(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of channels from an Opus packet.
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @return The number of channels of the packet.
     */
    public static int getPacketChannelCount(ByteBuffer packet) {
        int res = packetGetNbChannels(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }


    /**
     * Gets the number of frames in an opus packet.
     *
     * @param packet Binary data of the input packet
     * @return The number of frames of this packet
     */
    public static int getPacketFrameCount(byte[] packet) {
        int res = packetGetNbFrames(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of frames in an opus packet.
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @return The number of frames of this packet
     */
    public static int getPacketFrameCount(ByteBuffer packet) {
        int res = packetGetNbFrames(packet);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of samples of an opus packet.
     *
     * @param packet Binary data of the input packet
     * @param sampleRate The sample rate in Hz. Must be a multiple of 400 or inaccurate results will be returned.
     * @return The number of samples.
     */
    public static int getPacketSampleCount(byte[] packet, int sampleRate) {
        int res = packetGetNbSamples(packet, sampleRate);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of samples of an opus packet.
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @param sampleRate The sample rate in Hz. Must be a multiple of 400 or inaccurate results will be returned.
     * @return The number of samples.
     */
    public static int getPacketSampleCount(ByteBuffer packet, int sampleRate) {
        int res = packetGetNbSamples(packet, sampleRate);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of samples per frame from an opus packet.
     *
     * @param packet Binary data of the input packet
     * @param sampleRate The sample rate in Hz. Must be a multiple of 400 or inaccurate results will be returned.
     * @return The number of samples per frame.
     */
    public static int getPacketSamplesPerFrame(byte[] packet, int sampleRate) {
        int res = packetGetSamplesPerFrame(packet, sampleRate);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Gets the number of samples per frame from an opus packet.
     *
     * @param packet A direct ByteBuffer of the opus packet data
     * @param sampleRate The sample rate in Hz. Must be a multiple of 400 or inaccurate results will be returned.
     * @return The number of samples per frame.
     */
    public static int getPacketSamplesPerFrame(ByteBuffer packet, int sampleRate) {
        int res = packetGetSamplesPerFrame(packet, sampleRate);

        if(res < RapidOpusErrorCodes.OPUS_OK) {
            throw new IllegalStateException(RapidOpusErrorCodes.translateError(res));
        }

        return res;
    }

    /**
     * Applies soft-clipping to bring a float signal to the [-1, 1] range.
     * If the signal is already in that range, nothing happens, but if it is outside,
     * the signal is clipped as smoothly as possible to fit in the range and avoid excessive distortion.
     *
     * @param pcm A float[] of the input pcm. This will be clipped. Must be size frameSize * channels
     * @param frameSize Number of samples per channel to process
     * @param channels Number of channels to process
     * @param softClipMemory State memory for the clipping process. Length must be the channel count.
     */
    public static void pcmSoftClip(float[] pcm, int frameSize, int channels, float[] softClipMemory) {
        nPcmSoftClip(pcm, frameSize, channels, softClipMemory);
    }

    /**
     * Applies soft-clipping to bring a float signal to the [-1, 1] range.
     * If the signal is already in that range, nothing happens, but if it is outside,
     * the signal is clipped as smoothly as possible to fit in the range and avoid excessive distortion.
     *
     * @param pcm A ByteBuffer of the input pcm. This will be clipped. Must be size frameSize * channels * Float.BYTES
     * @param frameSize Number of samples per channel to process
     * @param channels Number of channels to process
     * @param softClipMemory State memory for the clipping process. Length must be channels.
     */
    public static void pcmSoftClip(ByteBuffer pcm, int frameSize, int channels, float[] softClipMemory) {
        nPcmSoftClip(pcm, frameSize, channels, softClipMemory);
    }

    /**
     * Applies soft-clipping to bring a float signal to the [-1, 1] range.
     * If the signal is already in that range, nothing happens, but if it is outside,
     * the signal is clipped as smoothly as possible to fit in the range and avoid excessive distortion.
     *
     * @param pcm A float[] of the input pcm. This will be clipped. Must be size frameSize * channels
     * @param frameSize Number of samples per channel to process
     * @param channels Number of channels to process
     * @param softClipMemory Direct ByteBuffer to store state. Length must be the channels * Float.BYTES.
     */
    public static void pcmSoftClip(float[] pcm, int frameSize, int channels, ByteBuffer softClipMemory) {
        nPcmSoftClip(pcm, frameSize, channels, softClipMemory);
    }

    /**
     * Applies soft-clipping to bring a float signal to the [-1, 1] range.
     * If the signal is already in that range, nothing happens, but if it is outside,
     * the signal is clipped as smoothly as possible to fit in the range and avoid excessive distortion.
     *
     * @param pcm A ByteBuffer of the input pcm. This will be clipped. Must be size frameSize * channels * Float.BYTES
     * @param frameSize Number of samples per channel to process
     * @param channels Number of channels to process
     * @param softClipMemory Direct ByteBuffer to store state. Length must be the channels * Float.BYTES.
     */
    public static void pcmSoftClip(ByteBuffer pcm, int frameSize, int channels, ByteBuffer softClipMemory) {
        nPcmSoftClip(pcm, frameSize, channels, softClipMemory);
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
    private static native int packetGetBandwidth(ByteBuffer packet);
    private static native int packetGetNbChannels(byte[] packet);
    private static native int packetGetNbChannels(ByteBuffer packet);
    private static native int packetGetNbFrames(byte[] packet);
    private static native int packetGetNbFrames(ByteBuffer packet);
    private static native int packetGetNbSamples(byte[] packet, int sampleRate);
    private static native int packetGetNbSamples(ByteBuffer packet, int sampleRate);
    private static native int packetGetSamplesPerFrame(byte[] packet, int sampleRate);
    private static native int packetGetSamplesPerFrame(ByteBuffer packet, int sampleRate);
    // TODO: Opus_packet_parse

    private static native void nPcmSoftClip(float[] pcm, int frameSize, int channels, float[] softclipMem);
    private static native void nPcmSoftClip(ByteBuffer pcm, int frameSize, int channels, float[] softclipMem);
    private static native void nPcmSoftClip(float[] pcm, int frameSize, int channels, ByteBuffer softclipMem);
    private static native void nPcmSoftClip(ByteBuffer pcm, int frameSize, int channels, ByteBuffer softclipMem);
}
