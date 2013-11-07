package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class CtrlReqBody implements Serializable{
	public int userId;
	public String userName;
	public int conferenceid;
	
	public CtrlReqBody(int userid,String username,int confid){
		this.userId = userid;
		this.userName = username;
		this.conferenceid = confid;
	}
}
