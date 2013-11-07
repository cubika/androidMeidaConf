package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

import cn.edu.bupt.mmc.docshare.msgformat.CtrlRespBody.RESPONSE_TYPE;

public class CtrlDropRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,NO_CONTROLER_REJECT};
	public RESPONSE_TYPE responseType;
}
