package cn.edu.bupt.mmc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.bupt.mmc.desktopshare.message.DesktopMessage;
import cn.bupt.mmc.desktopshare.message.MsgType;
import cn.bupt.mmc.desktopshare.message.SendMsg;

public class DesktopClient extends Thread{

	private int confID;
	private String serverIP;
	private int serverPort;
	private String userName;
	private int userID;
	private Socket socket;
	private DataOutputStream bos;
	private DataInputStream bis;
	private InputStream input;
	private OutputStream output;
	private boolean isStop = false;
	private Handler handler;
	
	public DesktopClient(String serverIP, int serverPort, int confID, int userID, String userName, Handler handler){
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.confID = confID;
		this.userID = userID;
		this.userName = userName;
		this.handler = handler;
		start();
	}
	
	public void run(){
		
		int type,len;
		try{
			System.out.println("before socket");
			socket = new Socket(serverIP,serverPort);
			System.out.println("client socket........"+socket);
			output = socket.getOutputStream();
			input = socket.getInputStream();
			System.out.println("creat input and output");
			bos = new DataOutputStream(output);
			bis = new DataInputStream(input);
			//Send Connection Message
			DesktopMessage conMsg= new DesktopMessage(confID,userID,userName,MsgType.CON_MSG);
			SendMsg sendMsg = new SendMsg(conMsg,bos);
			sendMsg.send();
		}catch (Exception e) {
			e.printStackTrace();
		} 
		
		
		while(!isStop)
		{
			try {
				type = bis.readInt();
				System.out.println("type is"+type);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("no read data");
				break;
			}
			switch(type)
			{
				case MsgType.APPLY_MSG:
					try {
						len = bis.readInt();
						byte[] temp = new byte[len];
						bis.readFully(temp, 0, len);
						String inform = new String(temp);
						String text = "当前共享的是用户为"+inform.split("&")[0]+"的桌面";
						System.out.println(text);
					}catch(IOException e){
						e.printStackTrace();
					}
					break;
				case MsgType.START_MSG:
					try {
						len = bis.readInt();
						byte[] temp = new byte[len];
						bis.readFully(temp, 0, len);
					}catch(IOException e){
						e.printStackTrace();
					}
					break;
				case MsgType.STOP_MSG:
					try {
						len = bis.readInt();
						byte[] temp = new byte[len];
						bis.readFully(temp, 0, len);
						String text = "当前没有用户在共享桌面";
						System.out.println(text);
					}catch(IOException e){
						e.printStackTrace();
					}
					break;
						
				case MsgType.KICK_MSG:
				case MsgType.END_CONF_MSG:
					try {
						DesktopMessage endMsg = new DesktopMessage(confID,userID,userName,MsgType.END_CONF_MSG);
						SendMsg sendMsg2 = new SendMsg(endMsg,bos);
						sendMsg2.send();
						isStop = true;
						socket.close();
						}catch (Exception ex){
							ex.printStackTrace();
						}
					break;
					// case END_CONF_MSG
				case MsgType.IMAGE_MSG:
					try
					{
					 len = bis.readInt();
					 System.out.println("receive a image len= "+len);
					 byte[] temp = new byte[len];
					 bis.readFully(temp, 0, len);
					 Message message = handler.obtainMessage();
					 message.what = 1;
					 Bundle bundle = new Bundle();
					 bundle.putByteArray("buf", temp);
					 bundle.putInt("len", len);
					 message.setData(bundle);
					 handler.sendMessage(message);
					 temp = null;
					}catch(IOException e)
					{
						e.printStackTrace();
					}
				    break;
			
				default: break;
				    
			}// end Switch
		}// end while
		
	}
}
