package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class RegisterReqBody implements Serializable {
	public int userId;
	public String userName;
	public int conference2Join;
	//public String clientIP;
	//public int clientTCPPort = -1;
	//public int clientUDPPort = -1;
	
	public RegisterReqBody(int userid, String username, int conference2Join) {
		this.userId = userid;
		this.userName = username;
		this.conference2Join = conference2Join;
		//this.clientIP = clientIP;
		//this.clientTCPPort = clienttcpport;
		//this.clientUDPPort = clientudpport;
	}
}
