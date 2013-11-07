package cn.edu.bupt.mmc.docshare.msgformat;

import java.io.Serializable;

public class ChangePageRespBody implements Serializable{
	public enum RESPONSE_TYPE{OK,NO_CONTROLER_REJECT,NO_FILE_REJECT,EXCEED_MAXPAGE_REJECT};
	public RESPONSE_TYPE responseType; 
}
