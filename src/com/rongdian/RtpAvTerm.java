package com.rongdian;

interface RtpAvTermListener
{
    void onRecvAudio(
        long   term,
        byte[] pcm16Data,
        int    dataSize
        );

    void onRecvVideo(long term);

    void onRequestKeyFrame(long term);
}

public class RtpAvTerm
{
    public static final short RAVT_CODEC_G711U       = 1;
    public static final short RAVT_CODEC_H264        = 101;

    public static final short RAVT_COLOR_YUV420P_UV  = 1;
    public static final short RAVT_COLOR_YUV420P_VU  = 2;
    public static final short RAVT_COLOR_YUV420SP_UV = 3;
    public static final short RAVT_COLOR_YUV420SP_VU = 4;

    public static native long ravtCreateTerm(RtpAvTermListener listener);

    public static native void ravtDeleteTerm(long term);

    public static native boolean ravtOpenAudioSession(
        long   term,
        byte[] localIp,
        int    localPort
        );

    public static native boolean ravtGetAudioSessionLocalAddr(
        long   term,
        byte[] localIp_64,
        int[]  localPort_1
        );

    public static native boolean ravtSetAudioSessionRemoteAddr(
        long   term,
        byte[] remoteIp,
        int    remotePort
        );

    public static native boolean ravtSetAudioSessionOutputPayloadType(
        long term,
        byte payloadType
        );

    public static native void ravtCloseAudioSession(long term);

    public static native boolean ravtOpenLocalAudio(
        long  term,
        short audioCodec,
        long  recordFrequency
        );

    public static native boolean ravtPutLocalAudio(
        long   term,
        byte[] pcm16Data
        );

    public static native void ravtCloseLocalAudio(long term);

    public static native boolean ravtOpenRemoteAudio(
        long  term,
        short audioCodec,
        long  playFrequency
        );

    public static native void ravtCloseRemoteAudio(long term);

    public static native boolean ravtOpenVideoSession(
        long   term,
        byte[] localIp,
        int    localPort
        );

    public static native boolean ravtGetVideoSessionLocalAddr(
        long   term,
        byte[] localIp_64,
        int[]  localPort_1
        );

    public static native boolean ravtSetVideoSessionRemoteAddr(
        long   term,
        byte[] remoteIp,
        int    remotePort
        );

    public static native boolean ravtSetVideoSessionOutputPayloadType(
        long term,
        byte payloadType
        );

    public static native void ravtCloseVideoSession(long term);

    public static native boolean ravtOpenLocalVideoPreview(
        long term,
        long videoWidth,
        long videoHeight
        );

    public static native void ravtCloseLocalVideoPreview(long term);

    public static native boolean ravtOpenLocalVideoOutput(
        long  term,
        short videoCodec,
        long  videoWidth,
        long  videoHeight,
        long  videoBitRate,
        long  videoFrameRate,
        long  videoKeyFrameInterval,
        long  videoPacketSize
        );

    public static native boolean ravtPutLocalVideoOutput(
        long   term,
        byte[] yuvData,
        long   yuvWidth,
        long   yuvHeight,
        short  yuvColorSpace,
        long   makeRotationClockwise
        );

    public static native boolean ravtMakeKeyFrameOutput(long term);

    public static native void ravtCloseLocalVideoOutput(long term);

    public static native boolean ravtOpenRemoteVideoPreview(
        long  term,
        short videoCodec
        );

    ////
    //// OpenGL [[[[---------------------------------------------------------
    ////

    public static native boolean ravtRemoteVideoInitRenderer(
        long term,
        long rendererWidth,
        long rendererHeight
        );

    public static native boolean ravtRemoteVideoRender(long term);

    public static native void ravtRemoteVideoFiniRenderer(long term);

    ////
    //// OpenGL ]]]]---------------------------------------------------------
    ////

    public static native void ravtCloseRemoteVideoPreview(long term);

    static
    {
        System.loadLibrary("avutil");
        System.loadLibrary("swscale");
        System.loadLibrary("avcore");
        System.loadLibrary("avcodec");
        System.loadLibrary("sipphone");
        System.loadLibrary("rtp_framework");
        System.loadLibrary("rtp_foundation");
        System.loadLibrary("mm_transport");
        System.loadLibrary("rtp_av_term_android");
    }
}
