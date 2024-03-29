package me.walkerknapp.rapidopus;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

/**
 * An object that decodes an audio stream encoded in opus, returning decoded packets of audio data.
 */
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

    /**
     * Creates and initializes an opus decoder.
     *
     * Internally Opus stores data at 48000 Hz, so that should be the default value for Fs.
     * However, the decoder can efficiently decode to buffers at 8, 12, 16, and 24 kHz so
     * if for some reason the caller cannot use data at the full sample rate, or knows the
     * compressed data doesn't use the full frequency range, it can request decoding at a reduced rate.
     *
     * Likewise, the decoder is capable of filling in either mono or interleaved stereo pcm buffers,
     * at the caller's request.
     *
     * @param sampleRate Sample rate to decode at, in Hz
     * @param channels Number of channels (Must be 1 or 2) to decode.
     */
    public OpusDecoder(int sampleRate, int channels) {
        // This line needs to be here, since RapidOpus must be forced to load before native methods can be called.
        Cleaner cleaner = RapidOpus.cleaner;

        this.structPointer = decoderCreate(sampleRate, channels);

        this.state = new State(structPointer);
        this.cleanable = cleaner.register(this, state);
    }

    /**
     * Creates and initializes an opus decoder.
     *
     * The decoder is capable of filling in either mono or interleaved
     * stereo pcm buffers at the caller's request.
     *
     * @param channels Number of channels (Must be 1 or 2) to decode.
     */
    public OpusDecoder(int channels) {
        this(48000, channels);
    }

    // Generic CTLs

    /**
     * Resets the decoder to a freshly initialized state.
     */
    public void resetState() {
        decoderResetState(structPointer);
    }

    /**
     * Gets the decoder's last bandpass.
     *
     * @return The decoder's last bandpass.
     */
    public OpusBandwidth getBandwidth() {
        return OpusBandwidth.valueOf(decoderGetBandwidth(structPointer));
    }

    /**
     * Gets the sampling rate the decoder was initialized with.
     *
     * @return The sample rate
     */
    public int getSampleRate() {
        return decoderGetSampleRate(structPointer);
    }

    // Decoder CTLs

    /**
     * Returns the amount the decoder is scaling the signal by in Q8 dB units.
     *
     * @return The current gain
     */
    public int getGain() {
        return decoderGetGain(structPointer);
    }

    /**
     * Sets the decoder gain adjustment.
     * Scales the decoded output by a factor in Q8 dB units.
     * This has a maximum range of -32768 to 32767 inclusive
     * The default gain is 0 for no adjustment.
     *
     * @param gain The gain to set.
     */
    public void setGain(int gain) {
        decoderSetGain(structPointer, gain);
    }

    /**
     * Gets the pitch of the last decoded frame, if available.
     * If the last frame was not voiced, or if the pitch was not coded in the frame, returns 0.
     *
     * @return Pitch period at 48kHz.
     */
    public int getLastFramePitch() {
        return decoderGetLastFramePitch(structPointer);
    }

    /**
     * Gets the duration (in samples) of the last packet decoded or concealed.
     *
     * @return Number of samples at current sampling rate.
     */
    public int getLastPacketDuration() {
        return decoderGetLastPacketDuration(structPointer);
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
    public void close() {
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
    private static native void decoderResetState(long pDecoder);
    private static native int decoderGetBandwidth(long pDecoder);
    private static native int decoderGetSampleRate(long pDecoder);
    private static native int decoderGetGain(long pDecoder);
    private static native void decoderSetGain(long pDecoder, int gain);
    private static native int decoderGetLastFramePitch(long pDecoder);
    private static native int decoderGetLastPacketDuration(long pDecoder);
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
