package com.rongdian;

interface SipTermListener
{
    void onCallLocalRinging(
        long term,
        long callId
        );

    void onCallRemoteRinging(
        long term,
        long callId
        );

    void onCallConnected(
        long term,
        long callId
        );

    void onCallDisconnected(
        long term,
        long callId
        );

    void onCallError(
        long term,
        long callId,
        int  errorCode
        );

    void onCallRequestKeyFrame(
        long term,
        long callId
        );

    void onRegisterOk(long term);

    void onRegisterError(
        long term,
        int  errorCode
        );
};

public class SipTerm
{
    public static final short RTPMAP_PCMU_8000   = 1;
    public static final short RTPMAP_H264_90000  = 101;

    public static final byte  PAYLOAD_PCMU_8000  = 0;
    public static final byte  PAYLOAD_H264_90000 = 109;

    public static native long createTerm(
        SipTermListener listener,
        byte[]          myAccount,
        byte[]          myIp,
        int             myPort,
        byte[]          regAccount,
        byte[]          regPassword,
        byte[]          registarIp,
        int             registarPort
        );

    public static native void deleteTerm(long term);

    public static native long makeOutgoingCall(
        long    term,
        byte[]  sipRemoteAccount,
        byte[]  sipRemoteIp,
        int     sipRemotePort,
        byte[]  sdpLocalIp,
        int     sdpLocalAudioPort,
        short[] sdpLocalAudioRtpmaps,
        byte[]  sdpLocalAudioPayloads,
        boolean sdpLocalAudioRecv,
        boolean sdpLocalAudioSend,
        int     sdpLocalVideoPort,
        long    sdpLocalVideoBitRate,
        short[] sdpLocalVideoRtpmaps,
        byte[]  sdpLocalVideoPayloads,
        byte[]  sdpLocalVideoProfileLevelIds_n_64,
        boolean sdpLocalVideoRecv,
        boolean sdpLocalVideoSend
        );

    public static native boolean acceptIncomingCall(
        long    term,
        long    callId,
        byte[]  sdpLocalIp,
        int     sdpLocalAudioPort,
        short[] sdpLocalAudioRtpmaps,
        byte[]  sdpLocalAudioPayloads,
        boolean sdpLocalAudioRecv,
        boolean sdpLocalAudioSend,
        int     sdpLocalVideoPort,
        long    sdpLocalVideoBitRate,
        short[] sdpLocalVideoRtpmaps,
        byte[]  sdpLocalVideoPayloads,
        byte[]  sdpLocalVideoProfileLevelIds_n_64,
        boolean sdpLocalVideoRecv,
        boolean sdpLocalVideoSend
        );

    public static native boolean getCallInfo(
        long      term,
        long      callId,
        byte[]    sipLocalIp_64,
        int[]     sipLocalPort_1,
        byte[]    sipRemoteAccount_256,
        byte[]    sipRemoteIp_64,
        int[]     sipRemotePort_1,
        byte[]    sipRemoteContactAccount_256,
        byte[]    sipRemoteContactIp_64,
        int[]     sipRemoteContactPort_1,
        byte[]    sdpLocalIp_64,
        int[]     sdpLocalAudioPort_1,
        short[]   sdpLocalAudioRtpmaps_16,
        byte[]    sdpLocalAudioPayloads_16,
        int[]     sdpLocalAudioCount_1,
        boolean[] sdpLocalAudioRecv_1,
        boolean[] sdpLocalAudioSend_1,
        int[]     sdpLocalVideoPort_1,
        long[]    sdpLocalVideoBitRate_1,
        short[]   sdpLocalVideoRtpmaps_16,
        byte[]    sdpLocalVideoPayloads_16,
        byte[]    sdpLocalVideoProfileLevelIds_16_64,
        int[]     sdpLocalVideoCount_1,
        boolean[] sdpLocalVideoRecv_1,
        boolean[] sdpLocalVideoSend_1,
        byte[]    sdpRemoteAudioIp_64,
        int[]     sdpRemoteAudioPort_1,
        short[]   sdpRemoteAudioRtpmaps_16,
        byte[]    sdpRemoteAudioPayloads_16,
        int[]     sdpRemoteAudioCount_1,
        boolean[] sdpRemoteAudioRecv_1,
        boolean[] sdpRemoteAudioSend_1,
        byte[]    sdpRemoteVideoIp_64,
        int[]     sdpRemoteVideoPort_1,
        long[]    sdpRemoteVideoBitRate_1,
        short[]   sdpRemoteVideoRtpmaps_16,
        byte[]    sdpRemoteVideoPayloads_16,
        byte[]    sdpRemoteVideoProfileLevelIds_16_64,
        int[]     sdpRemoteVideoCount_1,
        boolean[] sdpRemoteVideoRecv_1,
        boolean[] sdpRemoteVideoSend_1
        );

    public static native void destroyCall(
        long term,
        long callId
        );

    public static native boolean requestKeyFrame(
        long term,
        long callId
        );

    public static native boolean doKeepalive(
        long term,
        long callId
        );

    public static native boolean doRegister(
        long term,
        long durationInSeconds
        );

    static
    {
        System.loadLibrary("sipmod_jni");
    }
}
