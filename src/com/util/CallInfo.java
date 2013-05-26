package com.util;

import java.io.UnsupportedEncodingException;

//会话详细信息
public class CallInfo {
	public byte[] sipLocalIp_64;
	public int[] sipLocalPort_1;
	
	public byte[] sipRemoteAccount_256;
	public byte[] sipRemoteIp_64;
	public int[] sipRemotePort_1;
	
	public byte[] sipRemoteContactAccount_256;
	public byte[] sipRemoteContactIp_64;
	public int[] sipRemoteContactPort_1;
	
	public byte[] sdpLocalIp_64;
	public int[] sdpLocalAudioPort_1;
	public short[] sdpLocalAudioRtpmaps_16;
    public byte[] sdpLocalAudioPayloads_16;
    public int[] sdpLocalAudioCount_1;
	
	public int[] sdpLocalVideoPort_1;
	public long[] sdpLocalVideoBitRate_1;
	public short[] sdpLocalVideoRtpmaps_16;
    public byte[] sdpLocalVideoPayloads_16;
    public int[] sdpLocalVideoCount_1;
	
	
	public byte[] sdpRemoteAudioIp_64;
	public int[] sdpRemoteAudioPort_1;
	public short[] sdpRemoteAudioRtpmaps_16;
    public byte[] sdpRemoteAudioPayloads_16;
    public int[] sdpRemoteAudioCount_1;
     
	public byte[] sdpRemoteVideoIp_64;
	public int[] sdpRemoteVideoPort_1;
	public long[] sdpRemoteVideoBitRate_1;
	public short[] sdpRemoteVideoRtpmaps_16;
    public byte[] sdpRemoteVideoPayloads_16;
    public int[] sdpRemoteVideoCount_1;
    
    
    public boolean[] sdpLocalAudioRecv_1;
    public boolean[] sdpLocalAudioSend_1;
    public boolean[] sdpLocalVideoRecv_1;
    public boolean[] sdpLocalVideoSend_1;
    public boolean[] sdpRemoteAudioRecv_1;
    public boolean[] sdpRemoteAudioSend_1;
    public boolean[] sdpRemoteVideoRecv_1;
    public boolean[] sdpRemoteVideoSend_1;
    
    public byte[] sdpRemoteVideoProfileLevelIds_16_64;
    public byte[] sdpLocalVideoProfileLevelIds_16_64;
    
	public CallInfo(){
		sipLocalIp_64 = new byte[64];
		sipLocalPort_1 = new int[1];
		
		sipRemoteAccount_256 = new byte[256];
		sipRemoteIp_64 = new byte[64];
		sipRemotePort_1 = new int[1];
		
		sipRemoteContactAccount_256 = new byte[256];
		sipRemoteContactIp_64 = new byte[64];
		sipRemoteContactPort_1 = new int[1];
		
		sdpLocalIp_64 = new byte[64];
		sdpLocalAudioPort_1 = new int[1];
		sdpLocalAudioRtpmaps_16 = new short[16];
	    sdpLocalAudioPayloads_16 = new byte[16];
	    sdpLocalAudioCount_1 = new int[1];
		
		sdpLocalVideoPort_1 = new int[1];
		sdpLocalVideoBitRate_1 = new long[1];
		sdpLocalVideoRtpmaps_16 = new short[16];
	    sdpLocalVideoPayloads_16 = new byte[16];
	    sdpLocalVideoCount_1 = new int[1];
		
		
		sdpRemoteAudioIp_64 = new byte[64];
		sdpRemoteAudioPort_1 = new int[1];
		sdpRemoteAudioRtpmaps_16 = new short[16];
	    sdpRemoteAudioPayloads_16 = new byte[16];
	    sdpRemoteAudioCount_1 = new int[1];
		
		sdpRemoteVideoIp_64 = new byte[64];
		sdpRemoteVideoPort_1 = new int[1];
		sdpRemoteVideoBitRate_1 = new long[1];
		sdpRemoteVideoRtpmaps_16 = new short[16];
	    sdpRemoteVideoPayloads_16 = new byte[16];
	    sdpRemoteVideoCount_1 = new int[1];
	    
	    
	    
	    sdpLocalAudioRecv_1 = new boolean[1];
	    sdpLocalAudioSend_1 = new boolean[1];
	    sdpLocalVideoRecv_1 = new boolean[1];
	    sdpLocalVideoSend_1 = new boolean[1];
	    sdpRemoteAudioRecv_1 = new boolean[1];
	    sdpRemoteAudioSend_1 = new boolean[1];
	    sdpRemoteVideoRecv_1 = new boolean[1];
	    sdpRemoteVideoSend_1 = new boolean[1];
	    sdpRemoteVideoProfileLevelIds_16_64 = new byte[16*64];
	    sdpLocalVideoProfileLevelIds_16_64 = new byte[16*64];
	}
	
	


	public void printCallInfo(){
		try {
			System.out.println("sipLocalIp_64 = " + new String(this.getSipLocalIp_64(), "utf-8"));
			System.out.println("sipLocalPort_1 = " + this.getSipLocalPort_1());
				
			System.out.println("sipRemoteAccount_256 = " + new String(this.getSipRemoteAccount_256(), "utf-8"));
			System.out.println("sipRemoteIp_64 = " + new String(this.getSipRemoteIp_64(), "utf-8"));
			System.out.println("sipRemotePort_1 = " + this.getSipRemotePort_1());
			
			System.out.println("sipRemoteContactAccount_256 = " + new String(this.getSipRemoteContactAccount_256(), "utf-8"));
			System.out.println("sipRemoteContactIp_64 = " + new String(this.getSipRemoteContactIp_64(), "utf-8"));
			System.out.println("sipRemoteContactPort_1 = " + this.getSipRemoteContactPort_1());
			
			
			System.out.println("sdpLocalIp_64 = " + new String(this.getSdpLocalIp_64(), "utf-8"));
			System.out.println("sdpLocalAudioPort_1 = " + this.getSdpLocalAudioPort_1());
			System.out.println("sdpLocalAudioRtpmaps_16 = " + short2String(sdpLocalAudioRtpmaps_16));
			System.out.println("sdpLocalAudioPayloads_16 = " + new String(this.getSdpLocalAudioPayloads_16(), "utf-8"));
			System.out.println("sdpLocalAudioCount_1 = " + this.getSdpLocalAudioCount_1());
			
			System.out.println("sdpLocalVideoPort_1 = " + this.getSdpLocalVideoPort_1());
			System.out.println("sdpLocalVideoBitRate_1 = " + this.getSdpLocalVideoBitRate_1());
			System.out.println("sdpLocalVideoRtpmaps_16 = " + short2String(sdpLocalVideoRtpmaps_16));
			System.out.println("sdpLocalVideoPayloads_16 = " + new String(this.getSdpLocalVideoPayloads_16(), "utf-8"));
			System.out.println("sdpLocalVideoCount_1 = " + this.getSdpLocalVideoCount_1());
			
			
			System.out.println("sdpRemoteAudioIp_64 = " + new String(this.getSdpRemoteAudioIp_64(), "utf-8"));
			System.out.println("sdpRemoteAudioPort_1 = " + this.getSdpRemoteAudioPort_1());
			System.out.println("sdpRemoteAudioRtpmaps_16 = " + short2String(sdpRemoteAudioRtpmaps_16));
			System.out.println("sdpRemoteAudioPayloads_16 = " + new String(this.getSdpRemoteAudioPayloads_16(), "utf-8"));
			System.out.println("sdpRemoteAudioCount_1 = " + this.getSdpRemoteAudioCount_1());
			
			System.out.println("sdpRemoteVideoIp_64 = " + new String(this.getSdpRemoteVideoIp_64(), "utf-8"));
			System.out.println("sdpRemoteVideoPort_1 = " + this.getSdpRemoteVideoPort_1());
			System.out.println("sdpRemoteVideoBitRate_1 = " + this.getSdpRemoteVideoBitRate_1());
			System.out.println("sdpRemoteVideoRtpmaps_16 = " + short2String(sdpRemoteVideoRtpmaps_16));
			System.out.println("sdpRemoteVideoPayloads_16 = " + new String(this.getSdpRemoteVideoPayloads_16(), "utf-8"));
			System.out.println("sdpRemoteVideoCount_1 = " + this.getSdpRemoteVideoCount_1());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public byte[] getSipLocalIp_64() {
		return sipLocalIp_64;
	}

	public int getSipLocalPort_1() {
		return sipLocalPort_1[0];
	}

	public byte[] getSipRemoteAccount_256() {
		return sipRemoteAccount_256;
	}

	public byte[] getSipRemoteIp_64() {
		return sipRemoteIp_64;
	}

	public int getSipRemotePort_1() {
		return sipRemotePort_1[0];
	}

	public byte[] getSipRemoteContactAccount_256() {
		return sipRemoteContactAccount_256;
	}

	public byte[] getSipRemoteContactIp_64() {
		return sipRemoteContactIp_64;
	}

	public int getSipRemoteContactPort_1() {
		return sipRemoteContactPort_1[0];
	}

	public byte[] getSdpLocalIp_64() {
		return sdpLocalIp_64;
	}

	public int getSdpLocalAudioPort_1() {
		return sdpLocalAudioPort_1[0];
	}

	public int getSdpLocalVideoPort_1() {
		return sdpLocalVideoPort_1[0];
	}

	public long getSdpLocalVideoBitRate_1() {
		return sdpLocalVideoBitRate_1[0];
	}

	public byte[] getSdpRemoteAudioIp_64() {
		return sdpRemoteAudioIp_64;
	}

	public int getSdpRemoteAudioPort_1() {
		return sdpRemoteAudioPort_1[0];
	}

	public byte[] getSdpRemoteVideoIp_64() {
		return sdpRemoteVideoIp_64;
	}

	public int getSdpRemoteVideoPort_1() {
		return sdpRemoteVideoPort_1[0];
	}

	public long getSdpRemoteVideoBitRate_1() {
		return sdpRemoteVideoBitRate_1[0];
	}
	
	/******************下面是新增的媒体类型接口************************/
	public short getSdpLocalAudioRtpmaps_16() {
		return sdpLocalAudioRtpmaps_16[this.getSdpLocalAudioCount_1()];
	}

	public byte[] getSdpLocalAudioPayloads_16() {
		return sdpLocalAudioPayloads_16;
	}

	public int getSdpLocalAudioCount_1() {
		return sdpLocalAudioCount_1[0];
	}
	
	public short[] getSdpLocalVideoRtpmaps_16() {
		return sdpLocalVideoRtpmaps_16;
	}

	public byte[] getSdpLocalVideoPayloads_16() {
		return sdpLocalVideoPayloads_16;
	}

	public int getSdpLocalVideoCount_1() {
		return sdpLocalVideoCount_1[0];
	}

	public short[] getSdpRemoteAudioRtpmaps_16() {
		return sdpRemoteAudioRtpmaps_16;
	}

	public byte[] getSdpRemoteAudioPayloads_16() {
		return sdpRemoteAudioPayloads_16;
	}

	public int getSdpRemoteAudioCount_1() {
		return sdpRemoteAudioCount_1[0];
	}

	public short[] getSdpRemoteVideoRtpmaps_16() {
		return sdpRemoteVideoRtpmaps_16;
	}
	
	public byte[] getSdpRemoteVideoPayloads_16() {
		return sdpRemoteVideoPayloads_16;
	}

	public int getSdpRemoteVideoCount_1() {
		return sdpRemoteVideoCount_1[0];
	}   
	
	
	
	
	public boolean getSdpLocalAudioRecv_1() {
		return sdpLocalAudioRecv_1[0];
	}

	public boolean getSdpLocalAudioSend_1() {
		return sdpLocalAudioSend_1[0];
	}

	public boolean getSdpLocalVideoRecv_1() {
		return sdpLocalVideoRecv_1[0];
	}

	public boolean getSdpLocalVideoSend_1() {
		return sdpLocalVideoSend_1[0];
	}

	public boolean getSdpRemoteAudioRecv_1() {
		return sdpRemoteAudioRecv_1[0];
	}
	
	public boolean getSdpRemoteAudioSend_1() {
		return sdpRemoteAudioSend_1[0];
	}

	public boolean getSdpRemoteVideoRecv_1() {
		return sdpRemoteVideoRecv_1[0];
	}

	public boolean getSdpRemoteVideoSend_1() {
		return sdpRemoteVideoSend_1[0];
	}




	/**
	 * @param array 待转化为String的short数组
	 * @return 转换后的String
	 */
	private String short2String(short[] array) {
		StringBuffer buffer = new StringBuffer();
		for(short tmp : array) {
			buffer.append(Short.toString(tmp));
		}
		return buffer.toString();
	}

}
