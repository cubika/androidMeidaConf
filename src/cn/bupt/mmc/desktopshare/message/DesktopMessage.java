package cn.bupt.mmc.desktopshare.message;

public class DesktopMessage {
	private int type;
	private String command;
	
	public DesktopMessage(int confID ,int userID, String userName, int type)
	{
		this.type = type;
		command = userName +"&"+ confID+"&"+userID;
	}
	public String getCommand()
	{
		return command;
	}
	public int getType()
	{
		return type;
	}
	
}
