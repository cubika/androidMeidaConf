package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class RegisterRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,REJECT};
	public RESPONSE_TYPE responseType;
}
