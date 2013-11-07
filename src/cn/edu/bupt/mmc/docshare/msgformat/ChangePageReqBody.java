package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class ChangePageReqBody implements Serializable{
	public String filename = null;
	public int toPage = -1;
	public int uploadByUserid = -1;
	public int requestByUserid=-1;
	public int conferenceid = -1;
	public ChangePageReqBody(String filename,int toPage,int uploadByUserid,int conferenceid,int requestByUserid){
		this.filename = filename;
		this.toPage = toPage;
		this.uploadByUserid = uploadByUserid;
		this.conferenceid = conferenceid;
		this.requestByUserid = requestByUserid;
	}
}