package cn.edu.bupt.mmc.docshare.datastructure;

import java.io.Serializable;

public class ShareUserToPush implements Serializable{
	private int userid = -1;
	private String username = null;
	public ShareUserToPush(int userid,String username){
		this.userid = userid;
		this.username = username;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
