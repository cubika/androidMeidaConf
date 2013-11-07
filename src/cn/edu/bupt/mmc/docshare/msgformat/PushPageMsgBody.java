package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class PushPageMsgBody implements Serializable{
	public String filename=null;
	public int uploadByUserid=-1;
	public int pageNum = -1;
	public int totalPage = -1;
	public long imageLength = -1;
	public PushPageMsgBody(String filename,int uploadByUserid,int pageNum,int totalPage,long imageLength){
		this.filename = filename;
		this.uploadByUserid = uploadByUserid;
		this.pageNum = pageNum;
		this.totalPage = totalPage;
		this.imageLength = imageLength;
	}
}
