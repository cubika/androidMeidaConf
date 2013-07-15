package com.util;

import com.rongdian.SipTerm;

/**
 * ���õ����ò���*/
public class Constants {
	//ע�������
	public static String registarIp = "172.17.201.1";
	public static int registarPort = 5060;
	public static String registarAccount = "as"; //ע����������û���
	public static int registerInterval = 120;           //ע�����ڣ���λ��
	
	public static String prefix="http://"+registarIp+":8888/MediaConf/";
	
	//���ز���
	public static int listenningPort = 6060;     //sip�����˿�
	public static int sdpLocalAudioPort = 5010;  //��Ƶͨ�Ŷ˿�
	public static int sdpLocalVideoPort = 5030;  //��Ƶͨ�Ŷ˿�
	public static long sdpLocalVideoBitRate = 90000; //��Ƶ������
	public static short[] sdpLocalAudioRtpmaps = {SipTerm.RTPMAP_PCMU_8000};//{(short)0};
	public static byte[] sdpLocalAudioPayloads = {SipTerm.PAYLOAD_PCMU_8000};//{(byte)0};
    public static short[] sdpLocalVideoRtpmaps = {SipTerm.RTPMAP_H264_90000};//{(short)104};
    public static byte[]  sdpLocalVideoPayloads ={(byte)104};//{SipTerm.PAYLOAD_H264_90000};//{(byte)104};
  //  public static byte[] sdpLocalVideoProfileLevelIds_n_64 = {(byte)0x42e015};
    public static byte[] sdpLocalVideoProfileLevelIds_n_64 = "42e015\0".getBytes();

	//�û���Ϣ
	private byte[] myAccount;
	private byte[] myPasswd;
	private byte[] myIp;
	
	public Constants() {
		this.myIp = getLocalIp();
	}
	
	/**
	 * web��¼�ɹ����ȡ���в���
	 *@param myAcount ��¼�û���
	 *@param myPasswd ��¼����
	 */
	public Constants(byte[] myAccount, byte[] myPasswd) {
		this.myIp = getLocalIp();
		this.myAccount = myAccount;
		this.myPasswd = myPasswd;
	}
	
	/**
	 * ��ȡ������IP��ַ��Ĭ��Ϊ0.0.0.0.������
	 * @return IP��ַ�ֽ�����
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
