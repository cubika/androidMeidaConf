package com.util;

import com.rongdian.SipTerm;

/**
 * 常用的配置参数*/
public class Constants {
	//注册服务器
	public static String registarIp = "172.17.201.1";
	public static int registarPort = 5060;
	public static String registarAccount = "as"; //注册服务器的用户名
	public static int registerInterval = 120;           //注册周期，单位秒
	
	public static String prefix="http://"+registarIp+":8888/MediaConf/";
	
	//本地参数
	public static int listenningPort = 6060;     //sip监听端口
	public static int sdpLocalAudioPort = 5010;  //音频通信端口
	public static int sdpLocalVideoPort = 5030;  //视频通信端口
	public static long sdpLocalVideoBitRate = 90000; //视频比特率
	public static short[] sdpLocalAudioRtpmaps = {SipTerm.RTPMAP_PCMU_8000};//{(short)0};
	public static byte[] sdpLocalAudioPayloads = {SipTerm.PAYLOAD_PCMU_8000};//{(byte)0};
    public static short[] sdpLocalVideoRtpmaps = {SipTerm.RTPMAP_H264_90000};//{(short)104};
    public static byte[]  sdpLocalVideoPayloads ={(byte)104};//{SipTerm.PAYLOAD_H264_90000};//{(byte)104};
  //  public static byte[] sdpLocalVideoProfileLevelIds_n_64 = {(byte)0x42e015};
    public static byte[] sdpLocalVideoProfileLevelIds_n_64 = "42e015\0".getBytes();

	//用户信息
	private byte[] myAccount;
	private byte[] myPasswd;
	private byte[] myIp;
	
	public Constants() {
		this.myIp = getLocalIp();
	}
	
	/**
	 * web登录成功后获取下列参数
	 *@param myAcount 登录用户名
	 *@param myPasswd 登录密码
	 */
	public Constants(byte[] myAccount, byte[] myPasswd) {
		this.myIp = getLocalIp();
		this.myAccount = myAccount;
		this.myPasswd = myPasswd;
	}
	
	/**
	 * 获取本机的IP地址，默认为0.0.0.0.待补充
	 * @return IP地址字节数组
	 */
	byte[] getLocalIp() {
		String myIp = "0.0.0.0";
		return myIp.getBytes(); 
	}

	public byte[] getMyAccount() {
		return myAccount;
	}

	public void setMyAccount(byte[] myAccount) {
		this.myAccount = myAccount;
	}

	public byte[] getMyPasswd() {
		return myPasswd;
	}

	public void setMyPasswd(byte[] myPasswd) {
		this.myPasswd = myPasswd;
	}

	public byte[] getMyIp() {
		return myIp;
	}

	public void setMyIp(byte[] myIp) {
		this.myIp = myIp;
	}
}
