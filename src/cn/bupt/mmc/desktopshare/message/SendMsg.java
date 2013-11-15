package cn.bupt.mmc.desktopshare.message;

import java.io.IOException;
import java.io.DataOutputStream;

public class SendMsg {
	private DesktopMessage msg;
	private DataOutputStream bos;
	public SendMsg(DesktopMessage msg, DataOutputStream bos)
	{
		this.msg = msg;
		this.bos = bos;
	}
	public SendMsg(DataOutputStream bos)
	{
		this.bos = bos;
	}
	public void send()
	{
		try {
			synchronized(bos)
			{
				System.out.println("type is "+msg.getType());
				bos.writeInt(msg.getType());
				bos.writeInt(msg.getCommand().getBytes().length);
				bos.write(msg.getCommand().getBytes());
				System.out.println("send a message");
				bos.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
