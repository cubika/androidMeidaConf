package cn.edu.bupt.mmc.docshare.msgformat;
import cn.edu.bupt.mmc.docshare.datastructure.*;
import java.io.Serializable;
import java.util.*;
public class FileListUpdateMsgBody implements Serializable{
	public ArrayList<ShareFileToPush> fileList=null;
	public FileListUpdateMsgBody(){
		fileList = new ArrayList<ShareFileToPush>();
	}
}
