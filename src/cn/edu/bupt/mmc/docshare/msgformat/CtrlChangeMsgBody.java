package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class CtrlChangeMsgBody implements Serializable{
	//public int oldCtrlerId=-1;
	//public String oldCtrlerName=null;
	public int newCtrlerId=-1;
	public String newCtrlerName=null;
	
	public CtrlChangeMsgBody(int newCtrlerId,String newCtrlerName){
		//this.oldCtrlerId = oldCtrlerId;
		//this.oldCtrlerName = oldCtrlerName;
		this.newCtrlerId = newCtrlerId;
		this.newCtrlerName = newCtrlerName;
	}
}
