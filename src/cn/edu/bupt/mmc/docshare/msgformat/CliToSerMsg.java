package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class CliToSerMsg implements Serializable{
	public enum MSG_TYPE {REGISTER_REQ,UPLOAD_REQ,CONTROL_REQ,CONTRL_DROP_REQ,CHANGE_PAGE_REQ,CHANGE_LASTTIME_PAGE_REQ,
		BYE};
	public MSG_TYPE msgType=null;
	public Object MsgBody=null;
	public CliToSerMsg(){
		this.MsgBody = null;
	}
}
