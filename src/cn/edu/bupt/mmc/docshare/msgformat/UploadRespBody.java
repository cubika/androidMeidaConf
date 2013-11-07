package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

import cn.edu.bupt.mmc.docshare.msgformat.RegisterRespBody.RESPONSE_TYPE;

public class UploadRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,REJECT};
	public RESPONSE_TYPE responseType;
	public String filename = null;
	public UploadRespBody(String filename){
		this.filename = filename;
	}
}
