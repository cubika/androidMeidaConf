package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

import cn.edu.bupt.mmc.docshare.msgformat.ChangePageRespBody.RESPONSE_TYPE;

public class ChangeLastTimePageRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,NO_CONTROLER_REJECT,NO_FILE_REJECT};
	public RESPONSE_TYPE responseType; 
}
