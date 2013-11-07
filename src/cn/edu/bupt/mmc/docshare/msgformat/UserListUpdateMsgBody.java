package cn.edu.bupt.mmc.docshare.msgformat;
import cn.edu.bupt.mmc.docshare.datastructure.*;
import java.io.Serializable;
import java.util.ArrayList;

public class UserListUpdateMsgBody implements Serializable{
	public ArrayList<ShareUserToPush> userlist = null;
	public UserListUpdateMsgBody(){
		this.userlist = null;
	}
}
