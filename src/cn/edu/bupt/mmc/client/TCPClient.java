package cn.edu.bupt.mmc.client;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.edu.bupt.mmc.docshare.apps.EditorState;
import cn.edu.bupt.mmc.docshare.msgformat.ChangeLastTimePageReqBody;
import cn.edu.bupt.mmc.docshare.msgformat.ChangeLastTimePageRespBody;
import cn.edu.bupt.mmc.docshare.msgformat.ChangePageReqBody;
import cn.edu.bupt.mmc.docshare.msgformat.ChangePageRespBody;
import cn.edu.bupt.mmc.docshare.msgformat.CliToSerMsg;
import cn.edu.bupt.mmc.docshare.msgformat.CtrlChangeMsgBody;
import cn.edu.bupt.mmc.docshare.msgformat.CtrlDropRespBody;
import cn.edu.bupt.mmc.docshare.msgformat.CtrlReqBody;
import cn.edu.bupt.mmc.docshare.msgformat.CtrlRespBody;
import cn.edu.bupt.mmc.docshare.msgformat.FileListUpdateMsgBody;
import cn.edu.bupt.mmc.docshare.msgformat.PushPageMsgBody;
import cn.edu.bupt.mmc.docshare.msgformat.RegisterReqBody;
import cn.edu.bupt.mmc.docshare.msgformat.RegisterRespBody;
import cn.edu.bupt.mmc.docshare.msgformat.SerToCliMsg;
import cn.edu.bupt.mmc.docshare.msgformat.UploadReqBody;
import cn.edu.bupt.mmc.docshare.msgformat.UserListUpdateMsgBody;

public class TCPClient extends Thread{
	private String serverIP;
	private int serverPort;
	private int conferenceid;
	private int userId;
	private String userName;

//	private ObjectOutputStream outputstream= null;
//	private ObjectInputStream inputstream= null;
	private Socket socket = null;
	private boolean running=true;
	public EditorState state;
	private ProgressDialog progressDialog = null;
	private Handler handler;
	private static final String TAG="TCPClient";
	
	private OutputStream outStream;
	private InputStream inStream;
	private BufferedReader reader;
	private PrintWriter writer;
	private ObjectMapper mapper = new ObjectMapper();
	
	public TCPClient(int userId, String userName, int conferenceid,
            String serverIP, int serverPort, EditorState state, Handler handler) throws IOException {
        this.userId = userId;
        this.userName = userName;
        this.conferenceid = conferenceid;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.running = true;
//        System.out.println("Try to connect server "+ serverIP + ":" + serverPort);
//        socket = new Socket(this.serverIP, this.serverPort);
//
//        outputstream = new ObjectOutputStream(this.socket.getOutputStream());
//        inputstream = new ObjectInputStream(this.socket.getInputStream());

        this.state = state;
        this.handler = handler;
        
        this.setName("Client Thread");
        start();
	}
	
	public void run(){
		
		 System.out.println("Try to connect server "+ serverIP + ":" + serverPort);
		 try{
	        socket = new Socket(this.serverIP, this.serverPort);

//	        outputstream = new ObjectOutputStream(this.socket.getOutputStream());
//	        inputstream = new ObjectInputStream(this.socket.getInputStream());
	        outStream = socket.getOutputStream();
	        inStream = socket.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(inStream,"UTF-8"));
	        writer = new PrintWriter(new OutputStreamWriter(outStream,"UTF-8"));
		 }catch(IOException e){
		 }
		 register2Server();
	        
		String json=null;
		SerToCliMsg msg = null;
		try{
			while(running && (json = reader.readLine())!=null){
				System.out.println("While read message: "+json);
				msg = mapper.readValue(json, SerToCliMsg.class);
				LinkedHashMap body = (LinkedHashMap) msg.msgBody;
				
				if(msg.msgType==msg.msgType.FILE_LIST_UPDATE){
					//updateFileListHandler(msg);
				}
				if(msg.msgType==msg.msgType.CTRL_RESPONSE){
					//System.out.println(msg.msgType);
					CtrlRespBody respBody = new CtrlRespBody();
					respBody.responseType = CtrlRespBody.RESPONSE_TYPE.valueOf(body.get("responseType").toString());
					msg.msgBody = respBody;
					controlRespHandler(msg);
				}
				if(msg.msgType==msg.msgType.CTRL_CHANGE){
					//System.out.println(msg.msgType);
					String ctrlName = body.get("newCtrlerName") != null ? body.get("newCtrlerName").toString() : "";
					CtrlChangeMsgBody respBody = new CtrlChangeMsgBody(Integer.parseInt(body.get("newCtrlerId").toString()),ctrlName);
					msg.msgBody = respBody;
					controllerChangeHandler(msg);
				}
				
				if(msg.msgType==msg.msgType.CHANGE_PAGE_RESPONSE){
					//System.out.println(msg.msgType);
					//changePageRespHandler(msg);
				}
				
				if(msg.msgType==msg.msgType.PUSH_PAGE){
					// System.out.println(msg.msgType);
//					if(this.progressBar != null){
//						this.progressBar.setVisible(false);
//						this.progressBar = null;
//					}
					if(this.progressDialog != null){
						progressDialog.dismiss();
						progressDialog = null;
					}
						
					String filename=body.get("filename").toString();
					int uploadByUserid=(Integer) body.get("uploadByUserid");
					int pageNum = (Integer) body.get("pageNum");
					int totalPage = (Integer) body.get("totalPage");
					long imageLength = (Integer) body.get("imageLength");
					msg.msgBody = new PushPageMsgBody(filename, uploadByUserid, pageNum, totalPage, imageLength);
					
					pushPageMsgHandler(msg,inStream);
				}
				
				if(msg.msgType == msg.msgType.CONTRL_DROP_RESPONSE){
					//controlDropRespHandler(msg);
				}
				if(msg.msgType == msg.msgType.CHANGE_LASTTIME_PAGE_RESP){
					//changeLastTimePageRespHandler(msg);
				}
				if(msg.msgType == msg.msgType.USER_LIST_UPDATE){
					//updateUserListHandler(msg);
				}
				if(msg.msgType==msg.msgType.BYE){
					System.out.println("You have quit conference.");
					//break;
					running = false;
					socket.close();
//					outputstream.close();
//					inputstream.close();
					this.stop();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("The Thread for client has end.");
	}


	public void controlDropRespHandler(SerToCliMsg msg){
		CtrlDropRespBody respbody = (CtrlDropRespBody)msg.msgBody;
		if(respbody.responseType==respbody.responseType.OK){
			System.out.println("The server has accepted your request to drop your control.");
		}else if(respbody.responseType==respbody.responseType.NO_CONTROLER_REJECT){
			System.out.println("The server turn down your request to drop control,because you aren't the controller now");
		}
		
	}
	
	public void pushPageMsgHandler(SerToCliMsg msg,InputStream is ){
		PushPageMsgBody msgbody = (PushPageMsgBody) msg.msgBody;
        System.out.println(msgbody.filename);
        System.out.println(msgbody.imageLength);
        System.out.println("pageNum: " + msgbody.pageNum);
        System.out.println("totalPage: " + msgbody.totalPage);
        System.out.println(msgbody.uploadByUserid);
        state.setFileName(msgbody.filename);
        state.setNowPageNumber(msgbody.pageNum);
        state.setTotalPageNumber(msgbody.totalPage);
        state.setUploadByUserid(msgbody.uploadByUserid);
        int len = (int) msgbody.imageLength;
        int bufferSize = 99999;
        byte[] buf = new byte[bufferSize];
		//File a=null;
		//FileInputStream fis=null;
		try {
			//int read=0;
			//is.readFully(buf, 0, len);
			System.out.println("push page start to read to buf..");
			System.out.println("avaiable bytes: "+is.available());
			
			int count = 0;
			do {
				int read = 0;

				if (is != null) {
					read = is.read(buf, count, len-count);
					
				}
				System.out.println("read in upload " + read);
				if (read == -1)
					break;
				

				count += read;
			} while (count != len);

			
			//int reslen = is.read(buf, 0, len);
			//System.out.println("read length :"+reslen);
			System.out.println("push page read to buf complete!");
			
			
			Log.d(TAG, "TCPClient Thread id : "+Thread.currentThread().getId());
			Message message=handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putInt("len", len);
			bundle.putByteArray("buf", buf);
			message.setData(bundle);
			message.what = 2; //pushPage
			handler.sendMessage(message);
			
//			ByteArrayInputStream imageis = new ByteArrayInputStream(buf,0,len);
//			processor.pictureBufferedImageShow(imageis);
//			processor.setButtonsEnable(state);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//System.out.println("File length:"+a.length());
	}
	
	public void changeLastTimePageRespHandler(SerToCliMsg msg){
		ChangeLastTimePageRespBody msgbody= (ChangeLastTimePageRespBody)msg.msgBody;
		if(msgbody.responseType==msgbody.responseType.OK){
			System.out.println("Your Request to change last time page is accepted.");
		}else if(msgbody.responseType == msgbody.responseType.NO_CONTROLER_REJECT){
			System.out.println("You are not controller,so can't change time page page.");
		}else if(msgbody.responseType == msgbody.responseType.NO_FILE_REJECT){
			System.out.println("The file you choose doesn't exist,so can't change time page page.");
		}
	}
	
	public void changePageRespHandler(SerToCliMsg msg){
		ChangePageRespBody msgbody = (ChangePageRespBody)msg.msgBody;
		if(msgbody.responseType==msgbody.responseType.OK){
			System.out.println("Your Request to change page is accepted.");
		}else if(msgbody.responseType == msgbody.responseType.NO_CONTROLER_REJECT){
			System.out.println("You are not controller,so can't change page.");
		}else if(msgbody.responseType == msgbody.responseType.NO_FILE_REJECT){
			System.out.println("The file you choose doesn't exist,so can't change page.");
		}else if(msgbody.responseType == msgbody.responseType.EXCEED_MAXPAGE_REJECT){
			System.out.println("The page num you assign exceed the max page of the file,so can't change page.");
		}
	}
	public void controllerChangeHandler(SerToCliMsg msg){
		System.out.println("Controller of this conference change.");
		CtrlChangeMsgBody msgbody = (CtrlChangeMsgBody)msg.msgBody;
		//int oldctrlerId = msgbody.oldCtrlerId;
		//String oldctrlerName = msgbody.oldCtrlerName;
		int newctrlerId = msgbody.newCtrlerId;
		if (newctrlerId == -1) {
		    //this.processor.showError("You can apply to control");
		    state.setControllerName(null);
//		    this.processor.getPanel().setEditor(state);
//		    this.processor.updateStatus();
//		    processor.setButtonsEnable(state);
		    Message message=handler.obtainMessage();
		    message.what = 1;//controllerChange
	        handler.sendMessage(message);
		    
		} else {
		    String newctrlerName = msgbody.newCtrlerName;
	        state.setControllerName(newctrlerName);
	        Message message=handler.obtainMessage();
	        message.what = 1;//controllerChange
	        handler.sendMessage(message);
//	        this.processor.getPanel().setEditor(state);
//	        this.processor.updateStatus();
//	        processor.setButtonsEnable(state);
	        System.out.println("The current controller of conference is user "+newctrlerName+"("+newctrlerId+").");
		}
	}
	
	public void controlRespHandler(SerToCliMsg msg){
		CtrlRespBody body = (CtrlRespBody)msg.msgBody;
		if(body.responseType==body.responseType.OK){
			System.out.println("You can control the page now.");
		}else{
			System.out.println("The server turned down your request to be the controller.");
		}
	}
	
	//By leixj，without processBar，just for baking up
	public int uploadFile2Server(String filePath, ProgressDialog pgd ) {
		
		
		// Socket client=null;
		InputStream fis=null;
		File file;
		System.out.println("file path = " + filePath);
		int confid = this.conferenceid;
		int userid = this.userId;
		file = new File(filePath);
		if (!file.isFile()) {
			System.out.println("It's not a file");
			return -1;
		}
		if (!file.exists()) {
			System.out.println("The file to upload doesn't exist");
			return -1;
		}
		
//		this.progressBar = progressBar;
//		this.progressBar.setVisible(true);
//		this.progressBar.setString("上传文件: " + file.getName() + "...");
		this.progressDialog = pgd;
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		try {
			String filename = null;
			filename = file.getName();
			CliToSerMsg msg = new CliToSerMsg();
			msg.msgType = msg.msgType.UPLOAD_REQ;
			UploadReqBody body = new UploadReqBody(this.userId,this.conferenceid,filename,(long) file.length());
			msg.msgBody = body;
//			outputstream.writeObject(msg);
//			outputstream.flush();
			String json_message = mapper.writeValueAsString(msg);
			System.out.println("Upload_File Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
			
			fis = new FileInputStream(filePath);
			int bufferSize = 1024;
			byte[] buf = new byte[bufferSize];
			System.out.println("file length = " + file.length());
			while (true) {
				int read = 0;
				if (fis != null) {
					read = fis.read(buf);
				}
				// System.out.println("read " + read);
				if (read == -1) {
					break;
				}
				outStream.write(buf, 0, read);
				System.out.println("Write " + read + " bytes");
			}
			System.out.println("Write end");
			outStream.flush();
			fis.close();
			//this.progressBar.setString("处理文件: " + filename + "...");
			progressDialog.setMessage("处理文件: " + filename + "...");
			progressDialog.show();
			return 1;
		} catch (IOException e) {
		    //processor.showError("上传文件失败");
		    e.printStackTrace();
		    //this.progressBar.setVisible(false);
		    progressDialog.dismiss();
			return -1;
		}

	}
	
	/*public int uploadFile2Server(String filePath) {
		// Socket client=null;
		InputStream fis=null;
		File file;
		System.out.println("file path = " + filePath);
		int confid = this.conferenceid;
		int userid = this.userId;
		file = new File(filePath);
		if (!file.isFile()) {
			System.out.println("It's not a file");
			JOptionPane.showMessageDialog(null, "文件不存�?!", "错误...", JOptionPane.ERROR_MESSAGE);
			return -1;
		}
		if (!file.exists()) {
			System.out.println("The file to upload doesn't exist");
			JOptionPane.showMessageDialog(null, "文件不存�?", "错误...", JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		try {
			String filename = null;
			filename = file.getName();
			CliToSerMsg msg = new CliToSerMsg();
			msg.msgType = msg.msgType.UPLOAD_REQ;
			UploadReqBody body = new UploadReqBody(this.userId,this.conferenceid,filename,(long) file.length());
			msg.MsgBody = body;
			outputstream.writeObject(msg);
			outputstream.flush();
			fis = new FileInputStream(filePath);
			
			Thread uploadFileThread = new Upload2ServerThread((FileInputStream) fis, outputstream);
			uploadFileThread.start();

			return 1;
			/*ProgressMonitorInputStream pmInputStream = new ProgressMonitorInputStream(null, "Uploading... Please wait! ",   
	                fis); 
			ProgressMonitor progressMonitor = pmInputStream.getProgressMonitor();   
			progressMonitor.setMillisToDecideToPopup(10);
			progressMonitor.setMillisToPopup(0);
			
			int bufferSize = 1024;
			byte[] buf = new byte[bufferSize];
			System.out.println("file length = " + file.length());
			int read = 0;
			while ((read = pmInputStream.read(buf)) != -1) {
				outputstream.write(buf, 0, read);
				System.out.println("Write " + read + " bytes");
				
				if(progressMonitor.isCanceled()){
	            	System.out.println("Read canceled");
	            	fis.close();
	            	return -1;
	        	}
			}
			System.out.println("Write end");
			outputstream.flush();
			fis.close();
			return 1;*/
	/*	} catch (IOException e) {
		    //processor.showError("上传文件失败");
			JOptionPane.showMessageDialog(null, "上传文件失败!", "错误...", JOptionPane.ERROR_MESSAGE);
		    e.printStackTrace();
			return -1;
		}

	}*/
	
	public void register2Server(){
		CliToSerMsg msg = new CliToSerMsg();
		msg.msgType = msg.msgType.REGISTER_REQ;
		RegisterReqBody body = new RegisterReqBody(this.userId, this.userName, this.conferenceid);
		msg.msgBody = body;
		try {
			//outputstream.writeObject(msg);
			String json_req = mapper.writeValueAsString(msg);
			System.out.println("Register write json : "+json_req);
			writer.println(json_req);
			writer.flush();
			
//			SerToCliMsg respmsg = (SerToCliMsg)inputstream.readObject();
//			RegisterRespBody respbody = (RegisterRespBody)respmsg.msgBody;
//			if(respbody.responseType==respbody.responseType.OK){
//				System.out.println("The user has joined the conference.");
//			}else{
//				System.out.println("The server turn down the user's request to join the conference.");
//			}
			String json_res = reader.readLine();
			System.out.println("Register read json : "+json_res);
			
		} catch (Exception e) {
		    //processor.showError("连接服务器失�?);
		    e.printStackTrace();
		}
	}
	
	public void changePageReq(String filename,int toPage,int uploadByUserId){
		System.out.println("Try to change page");
		CliToSerMsg msg = new CliToSerMsg();
		msg.msgType = msg.msgType.CHANGE_PAGE_REQ;
		ChangePageReqBody msgbody = new ChangePageReqBody(filename,toPage,uploadByUserId,BasicInfomation.conferenceid,
				BasicInfomation.userid);
		msg.msgBody = msgbody;
		try {
			//outputstream.writeObject(msg);
			String json_message = mapper.writeValueAsString(msg);
			System.out.println("ChangePage Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
		} catch (IOException e) {
		    // processor.showError("连接服务器失�?);
		    e.printStackTrace();
		}
	}
	
	public void changeLastTimePage(String filename,int uploadByUserId){
		CliToSerMsg msg = new CliToSerMsg();
		msg.msgType = msg.msgType.CHANGE_LASTTIME_PAGE_REQ;
		ChangeLastTimePageReqBody msgbody = new ChangeLastTimePageReqBody(filename,uploadByUserId,BasicInfomation.conferenceid,
				BasicInfomation.userid);
		msg.msgBody = msgbody;
		try {
			//outputstream.writeObject(msg);
			String json_message = mapper.writeValueAsString(msg);
			System.out.println("ChangeLastTime Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void controlReq(){
		System.out.println("Try to request control.");
		try {
			//outputstream.writeUTF("REQ_CONTROL");
			CliToSerMsg reqmsg = new CliToSerMsg();
			reqmsg.msgType = CliToSerMsg.MSG_TYPE.CONTROL_REQ;
			CtrlReqBody reqmsgbody = new CtrlReqBody(this.userId,this.userName,this.conferenceid);
			reqmsg.msgBody = reqmsgbody;
			//outputstream.writeObject(reqmsg);
			//outputstream.flush();
			String json_message = mapper.writeValueAsString(reqmsg);
			System.out.println("ContrlReq Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
		} catch (IOException e) {
		    // processor.showError("连接服务器失�?);
		    e.printStackTrace();
		}
	}
	public void controlDrop(){
		try{
			CliToSerMsg reqmsg = new CliToSerMsg();
			reqmsg.msgType = CliToSerMsg.MSG_TYPE.CONTRL_DROP_REQ;
			//outputstream.writeObject(reqmsg);
			//outputstream.flush();
			String json_message = mapper.writeValueAsString(reqmsg);
			System.out.println("ContrlDrop Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
		}catch(Exception e){
		    // processor.showError("连接服务器失�?);
		    e.printStackTrace();
		}
	}
	
	public void updateUserListHandler(SerToCliMsg msg){
		UserListUpdateMsgBody body = (UserListUpdateMsgBody)msg.msgBody;
		System.out.println("The lastest user list is:");
		for(int i=0;i<body.userlist.size();i++){
			System.out.println("Username:"+body.userlist.get(i).getUsername()+" id:"+body.userlist.get(i).getUserid());
		}
	}
	
	public void updateFileListHandler(SerToCliMsg msg){
		FileListUpdateMsgBody body = (FileListUpdateMsgBody)msg.msgBody;
		System.out.println("The lastest file list is:");
		for(int i=0;i<body.fileList.size();i++){
			System.out.println("Filename: "+body.fileList.get(i).getFileName()+"  total page: "+body.fileList.get(i).getMaxPage()+
					" upload by user "+body.fileList.get(i).getUploadByUserId());
		}

	}
	
	public void quitConference(){
		CliToSerMsg msg = new CliToSerMsg();
		msg.msgType = msg.msgType.BYE;
		try {
			//outputstream.writeObject(msg);
			//outputstream.flush();
			String json_message = mapper.writeValueAsString(msg);
			System.out.println("QuitConf Write json : "+json_message);
			writer.println(json_message);
			writer.flush();
			
			//outputstream.close();
			//inputstream.close();
			outStream.close();
			inStream.close();
		} catch (IOException e) {
		    // processor.showError("�?��会议失败");
		    e.printStackTrace();
		}
		
	}

    /**
     * @return the state
     */
    public EditorState getEditorState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setEditorState(EditorState state) {
        this.state = state;
    }


  
}
