package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class UploadReqBody implements Serializable{
	public String filename = null;
	public int userId = -1;
	public int conferenceId=-1;
	public long length; 
	public UploadReqBody(int userId,int conferenceId,String filename,long length){
		this.userId = userId;
		this.conferenceId = conferenceId;
		this.filename = filename;
		this.length = length;
	}
}
