package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class ChangeLastTimePageReqBody implements Serializable{
	public String filename = null;
	public int uploadByUserid = -1;
	public int requestByUserid=-1;
	public int conferenceid = -1;
	
	public ChangeLastTimePageReqBody(String filename,int uploadByUserid,int conferenceid,int requestByUserid){
		this.filename = filename;
		this.uploadByUserid = uploadByUserid;
		this.requestByUserid = requestByUserid;
		this.conferenceid = conferenceid;
	}
}
