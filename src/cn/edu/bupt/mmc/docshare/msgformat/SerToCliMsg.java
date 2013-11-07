package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class SerToCliMsg implements Serializable{
	public enum MSG_TYPE {REG_RESPONSE,CTRL_RESPONSE,UPLOAD_RESPONSE,CHANGE_PAGE_RESPONSE,CONTRL_DROP_RESPONSE,
		CTRL_CHANGE,FILE_LIST_UPDATE,USER_LIST_UPDATE,PUSH_PAGE,CHANGE_LASTTIME_PAGE_RESP,BYE};
	public MSG_TYPE msgType;
	public Object msgBody;
	public SerToCliMsg(){
		this.msgBody=null;
	}
}
