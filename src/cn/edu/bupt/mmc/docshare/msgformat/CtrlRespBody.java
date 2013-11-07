package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

import cn.edu.bupt.mmc.docshare.msgformat.RegisterRespBody.RESPONSE_TYPE;

public class CtrlRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,REJECT};
	public RESPONSE_TYPE responseType;
	public CtrlRespBody(){
		responseType = RESPONSE_TYPE.REJECT;
	}
}
