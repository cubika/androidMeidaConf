package com.rongdian;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.util.CallInfo;
import com.util.Constants;

public class SipTermListenerImpl implements SipTermListener {
    private CallInfo callInfo;
    private Constants constants;
    private Handler handler;
	
    public SipTermListenerImpl(Handler handler){
    	this.handler = handler;
    }
    
    //接收了invite消息
    @Override
	public void onCallLocalRinging(long term, long callId) {
		// TODO Auto-generated method stub
		System.out.println("onCallLocalRinging term:" + String.valueOf(term)
				+ " callId:" + String.valueOf(callId));
		
		Message msg = new Message();
		msg.what = 0x1235;
		handler.sendMessage(msg);
		
		byte[] profileLevelId = new byte[64];
		byte[] tmp = "428014\0".getBytes();
		for(int i = 0; i < tmp.length;  i++)
			profileLevelId[i] = tmp[i];
		//终端调用acceptIncompingCall方法加入会议
 		if(SipTerm.acceptIncomingCall(term, callId, "0.0.0.0".getBytes(),
				Constants.sdpLocalAudioPort, Constants.sdpLocalAudioRtpmaps, Constants.sdpLocalAudioPayloads, true, true, 
				Constants.sdpLocalVideoPort, Constants.sdpLocalVideoBitRate, Constants.sdpLocalVideoRtpmaps,
				Constants.sdpLocalVideoPayloads, profileLevelId, true, true))
			System.out.println("acceptIncomingCall called OK");
		else
			System.out.println("acceptIncomingCall called bad");
	}

	@Override
	public void onCallRemoteRinging(long term, long callId) {
		// TODO Auto-generated method stub
		System.out.println("onCallRemoteRinging term:" + String.valueOf(term)
				+ "callId:" + String.valueOf(callId));
		
		
	}

	@Override
	public void onCallConnected(long term, long callId) {
		// TODO Auto-generated method stub
		System.out.println("onCallConnected term:" + String.valueOf(term)
				+ "callId:" + String.valueOf(callId));
		
		callInfo = new CallInfo();
		SipTerm.getCallInfo(term, callId, callInfo.sipLocalIp_64, callInfo.sipLocalPort_1, 
				callInfo.sipRemoteAccount_256, callInfo.sipRemoteIp_64, callInfo.sipRemotePort_1,
				callInfo.sipRemoteContactAccount_256, callInfo.sipRemoteContactIp_64, 
				callInfo.sipRemoteContactPort_1, callInfo.sdpLocalIp_64, callInfo.sdpLocalAudioPort_1, 
				callInfo.sdpLocalAudioRtpmaps_16, callInfo.sdpLocalAudioPayloads_16, 
				callInfo.sdpLocalAudioCount_1, callInfo.sdpLocalAudioRecv_1, callInfo.sdpLocalAudioSend_1, 
				callInfo.sdpLocalVideoPort_1, callInfo.sdpLocalVideoBitRate_1, 
				callInfo.sdpLocalVideoRtpmaps_16, callInfo.sdpLocalVideoPayloads_16, callInfo.sdpLocalVideoProfileLevelIds_16_64, 
				callInfo.sdpLocalVideoCount_1, callInfo.sdpLocalVideoRecv_1, callInfo.sdpLocalVideoSend_1, callInfo.sdpRemoteAudioIp_64, 
				callInfo.sdpRemoteAudioPort_1, callInfo.sdpRemoteAudioRtpmaps_16, callInfo.sdpRemoteAudioPayloads_16, 
				callInfo.sdpRemoteAudioCount_1, callInfo.sdpRemoteAudioRecv_1, callInfo.sdpRemoteAudioSend_1,
				callInfo.sdpRemoteVideoIp_64, callInfo.sdpRemoteVideoPort_1, callInfo.sdpRemoteVideoBitRate_1, 
				callInfo.sdpRemoteVideoRtpmaps_16, callInfo.sdpRemoteVideoPayloads_16, callInfo.sdpRemoteVideoProfileLevelIds_16_64, 
				callInfo.sdpRemoteVideoCount_1, callInfo.sdpRemoteVideoRecv_1, callInfo.sdpRemoteVideoSend_1);
		callInfo.printCallInfo();
		
		//通知UI
		Bundle data = new Bundle();
		data.putByteArray("remoteVideoAddr", callInfo.sdpRemoteVideoIp_64);
		data.putInt("remoteVideoPort", callInfo.getSdpRemoteVideoPort_1());
		data.putShort("remoteVideoType", callInfo.getSdpRemoteVideoRtpmaps_16()[0]);
		data.putByte("remoteVideoPayload", callInfo.getSdpRemoteVideoPayloads_16()[0]);
		
		data.putByteArray("remoteAudioAddr", callInfo.sdpRemoteAudioIp_64);
		data.putInt("remoteAudioPort", callInfo.getSdpRemoteAudioPort_1());
		data.putShort("remoteAudioType", callInfo.getSdpRemoteAudioRtpmaps_16()[0]);
		data.putByte("remoteAudioPayload", callInfo.getSdpRemoteAudioPayloads_16()[0]);
		
		System.out.println("RemoteAudioRtpmaps_16 = " + callInfo.getSdpRemoteAudioRtpmaps_16()[0]);
		System.out.println("RemoteVideoRtpmaps_16 = " + callInfo.getSdpRemoteVideoRtpmaps_16()[0]);
		
		data.putLong("term", term);
		data.putLong("callId", callId);
		
		data.putLong("videoByteRate", callInfo.getSdpRemoteVideoBitRate_1());  
		
		Message msg = new Message();
		msg.what = 0x1236;
		msg.setData(data);
		handler.sendMessage(msg);
		//启动媒体处理
	}

	@Override
	public void onCallDisconnected(long term, long callId) {
		// TODO Auto-generated method stub
		System.out.println("onCallDisconnected term:" + String.valueOf(term)
				+ "callId:" + String.valueOf(callId));
		//SipTerm.destroyCall(term, callId);
		Message msg = new Message();
		msg.what = 0x1237;
		handler.sendMessage(msg);
			
	}

	@Override
	public void onCallError(long term, long callId, int errorCode) {
		// TODO Auto-generated method stub
		System.out.println("onCallError term:" + String.valueOf(term)
				+ "callId:" + String.valueOf(callId) + "error:"
				+ String.valueOf(errorCode));
	}

	@Override
	public void onCallRequestKeyFrame(long term, long callId) {
		// TODO Auto-generated method stub
		System.out.println("onCallRequestKeyFrame term:" + String.valueOf(term)
				+ "callId:" + String.valueOf(callId));
		RtpAvTerm.ravtMakeKeyFrameOutput(term);

	}

	@Override
	public void onRegisterOk(long term) {
		// TODO Auto-generated method stub
		System.out.println("onRegisterOk term:" + String.valueOf(term));
		
		//通知UI
		Message msg = new Message();
		msg.what = 0x1233;
		handler.sendMessage(msg);
		
	}

	@Override
	public void onRegisterError(long term, int errorCode) {
		// TODO Auto-generated method stub
		System.out.println("onRegisterError term:" + String.valueOf(term)
				+ "errorCode:" + String.valueOf(errorCode));
		
		//通知UI
		Message msg = new Message();
		msg.what = 0x1234;
		handler.sendMessage(msg);
	}

}
