package com.rongdian;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.http.ApplySpeakingThread;
import com.http.ClientRefreshThread;
import com.http.DisconnectUserThread;
import com.http.HttpThread;
import com.http.HttpUtils;
import com.http.LogoutThread;
import com.service.RefreshService;
import com.util.Constants;

public class MediaConfActivity extends RtpAvTermAndroidActivity {
	
	//sip相关
	private SipTermListenerImpl listener;  //监听Sip事件，在该监听器中发送msg给本UI线程处理
	long sipTerm;                          //sip终端，生命周期同整个Activity
	
	//UI组件
	public static TextView loginedText = null;
	public static TextView confTitle = null;
	public static TextView chairman = null;
	public static TextView subject = null;
	public static TextView duration = null;
	public static String UserName;
	public static Button apply_speak, exit_conf;// 申请发言、申请离开按钮
	public static ListView list;// 会中会议成员列表
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
	SimpleAdapter mSchedule;
	public Constants constants;
	
	//媒体相关
	long mediaTerm;                    //媒体终端，生命周期同一个会议
	SurfaceView mSurfaceView = null;  //显示本地视频界面组件
	GL2JNIView mGL2JINView = null;    //显示远端视频界面组件
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// 用户和会议标识
	Integer confId = -1;
	String userId = "-1";
	boolean speakable;	
	String confName = null,confSubject = null,confDuration = null,chairmanId = null,chairmanName = null;	

	RefreshService.ServiceBinder binder;
	
	//绑定service，查询会议信息	
	private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {      	
        	System.out.println("---Service connected---");
        	binder = (RefreshService.ServiceBinder)service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        	System.out.println("---Service disconnected---");        
        }
    };

    Timer timer = new Timer();
    //轮询获取会议参数
	TimerTask task = new TimerTask( ) {
		public void run ( ) {		
			String result = binder.getResult();			
			if(result!=null){
				String[] messages = result.split("@");    
	    		
	    		Bundle refreshdata = new Bundle();
	    		Message msg = new Message();     		 
	    		
				for(int u = 0; u < messages.length; u++){
	    			int updateIndex=messages[u].indexOf("update");
	                int showIndex=messages[u].indexOf("show");
	                int changeChairmanIndex=messages[u].indexOf("changeChairman:");
	                int confInfoIndex=messages[u].indexOf("confInfo");
	                int callIndex=messages[u].indexOf("call");
	                int changeIndex=messages[u].indexOf("change:");
	                int afterChangeIndex=messages[u].indexOf("afterChange");
	                int chairmainupdateIndex=messages[u].indexOf("chairmainUpdate");
	                int chairmanShowIndex=messages[u].indexOf("chairmanShow");
	                
	                if(chairmainupdateIndex>=0){
	                  String[] twoSub=messages[u].split(":");
	                  if(confId==-1){
	                      confId=Integer.valueOf(twoSub[1]);
	                  }                 
	                }  
	                
	                //刷新与会人员
	                if(updateIndex>=0){                  
	                   String[] twoSubString=messages[u].split(":");
	                   String func=twoSubString[2];
	                   if(confId==-1){
	                      confId=Integer.valueOf(twoSubString[1]);
	                   }
	                   
	                            
	                   HttpUtils httpUtils = null;
	                   Map<String,String> params = new HashMap<String,String>();  
	       	     	   params.put("confId", confId.toString());  
	       	     	   String paticipants = "";
	                   try {
	                	   paticipants = httpUtils.sendPostMessage("http://"+constants.registarIp+":8888/MediaConf/conference/conferenceinfo.do?method=getCurrentParticipateListFormain", params, "utf-8");
	                   } catch (IOException e) {
	                	  // TODO Auto-generated catch block
	                	  e.printStackTrace();
	                   }                  
	                   
	                   refreshdata.putString("paticipants",paticipants);   				     				
	                   msg.what = 0x1238;
	   				  
	                }
	                
	                //弹出提示框
	                if(showIndex>=0){   	                
	                	msg.what = 0x1238;
	                	refreshdata.putString("show", messages[u].split(":")[1]);	                
	                }  
	                if(changeChairmanIndex>=0){              
	                   	msg.what = 0x1238;
	                	refreshdata.putString("show", messages[u].split(":")[2]);	                	
	                }
	               
	                //更新会议信息
	                if(confInfoIndex>=0){
	                	confName=messages[u].split(":")[1];
	                	confSubject=messages[u].split(":")[2];                	
	                	confDuration=messages[u].split(":")[3];
	                	chairmanId=messages[u].split(":")[4];
	                	chairmanName = messages[u].split(":")[5];        	
	                			
	    				refreshdata.putString("confName",confName);
	    				refreshdata.putString("confSubject", confSubject);
	    				refreshdata.putString("chairmanName",chairmanName);
	    				refreshdata.putString("confDuration", confDuration);
	    				msg.what = 0x1238;    				
	                } 
	                
	                if(callIndex>=0){                	
	                    String[] subString=messages[u].split(":");
	                    String[] userInfo=subString[1].split(";");
	                    String js=userInfo[0];  
	                    
	                    msg.what = 0x1240;
	                   // speakable = true;
	                	refreshdata.putString("show", userInfo[1]);	                   
	                }
	                if(changeIndex>=0){                	
	                    String[] subString=messages[u].split(":");
	                    String[] changeInfo=subString[1].split(";");
	                    String url=changeInfo[1];
	                    System.out.println("changeChairman "+url);
//	 	                   if(confirm(changeInfo[0])==true){
//	 						   $.ajax({
//	 					   type : "GET",
//	 					   url : url,
//	 					   dataType : 'json',
//	 					   cache : false,
//	 					   success : function(json) {}
//	 			        	});
//	 					   } 		                   
	                }
	                
	                // 更换会议主席
	                if(afterChangeIndex>=0){ 	                
	 	                String[] subString=messages[u].split(":");
	 	                chairmanId=subString[2];
	 	                chairmanName=subString[1];
	 	                msg.what = 0x1238;
	 	                refreshdata.putString("show", subString[3]);	 	               	                
	 	            } 
	                
	                if(chairmanShowIndex>=0) {
	                	confId=-1; 		                	
	                	msg.what = 0x1237;
	                	refreshdata.putString("show", "会议已结束");
	 	            }          
	    		}    			
				 
				msg.setData(refreshdata);
				handler.sendMessage(msg);
			}           						
		}
	};
	private Handler handler=new Handler(){
		 public void handleMessage(Message message) {
			 switch (message.what) {
			 /******************SipTermListener的sip事件处理函数***********************/
			 	case 0x1233: //注册成功
			 		System.out.println("Registerok sip注册成功  ");
					loginedText.setText( "欢迎您，"+UserName+"！");
					processRegisterOK();
					
					break;
				case 0x1234: //注册失败
					loginedText.setText( "未登录");
					processRegisterFailed();
					break;
				case 0x1235: //本地振铃，被邀请加入会议
					processLocalRing();
					break;
				case 0x1236: //成功加入会议
					apply_speak.setEnabled(true);
					exit_conf.setEnabled(true);
					mGL2JINView.setBackgroundResource(0);
					mSurfaceView.setBackgroundResource(0);
					processJoinConfOK(message.getData());					
			       	break;
				case 0x1237: //退出会议
					apply_speak.setEnabled(false);
					exit_conf.setEnabled(false);
					mGL2JINView.setBackgroundResource(R.drawable.video);
					mSurfaceView.setBackgroundResource(R.drawable.video);
					if(message.getData().getString("show")!=null)
						Toast.makeText(MediaConfActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();
					else
						Toast.makeText(MediaConfActivity.this, "您已退出会议", Toast.LENGTH_LONG).show();
					processQuitConf();	
					break;
			 /*****************************************/		
				case 0x1238:
					if(message.getData().getString("show")!=null)
						Toast.makeText(MediaConfActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();					
					if(message.getData().getString("confName")!=null){
						confTitle.setText(message.getData().getString("confName"));
						subject.setText(message.getData().getString("confSubject"));
						chairman.setText(message.getData().getString("chairmanName"));
						duration.setText(message.getData().getString("confDuration"));
					}
					
					mylist.clear();
					JSONObject jsonObject;
			        JSONArray jsonArray = null;
					try {
						jsonObject = new JSONObject(message.getData().getString("paticipants"));
						jsonArray = jsonObject.getJSONArray("rows");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
			        for(int i=0;i<jsonArray.length();i++){ 
			        	JSONObject jsonObject2 = (JSONObject)jsonArray.opt(i);
			        	String userName = null,userId = null;        	        	
			        	try {
			        		userName = jsonObject2.getString("userName");
							userId = jsonObject2.getString("userId");				
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	HashMap<String, String> map = new HashMap<String, String>();  
			            map.put("ItemTitle",userName );  
			            map.put("ItemText", userId);  
			            mylist.add(map);  
			        }        
			      //添加并且显示  
			        list.setAdapter(mSchedule); 			        
					break;
				case 0x1240:
					Toast.makeText(MediaConfActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();
					if(message.getData().getString("show").contains("大家可以申请发言")){
						speakable = true;						
					}
					else if (message.getData().getString("show").contains("申请时间已到，不能再申请了")){
						speakable = false;						
					}
					System.out.println("case 0x1240 "+message.getData().getString("show"));
					break;
				case 0x2100:					
					Toast.makeText(MediaConfActivity.this, "消息发送失败", Toast.LENGTH_LONG).show();
					break;
				case 0x2101:	            	
	            //	Toast.makeText(MediaConfActivity.this, "消息发送成功", Toast.LENGTH_LONG).show();
					
					
					break;
				case 0x2102:
					Toast.makeText(MediaConfActivity.this, "退出登录成功", Toast.LENGTH_LONG).show();
					break;
				case 0x2103:
					Toast.makeText(MediaConfActivity.this, "退出登录失败", Toast.LENGTH_LONG).show();
					break;
			}	
		}
	 };
	
	//调试相关
	boolean debugLxj = true;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		loginedText = (TextView) findViewById(R.id.loginedText);// handler里面设置用户注册的状态信息
		confTitle = (TextView) findViewById(R.id.confTitle);// 会议信息显示在tablelayout中
		chairman = (TextView) findViewById(R.id.confChairman);
		subject = (TextView) findViewById(R.id.confSubject);
		duration = (TextView) findViewById(R.id.confDuration);
		list = (ListView) findViewById(R.id.mem_info_lv);// 会议成员列表
		apply_speak = (Button) findViewById(R.id.apply_speak);
		exit_conf = (Button) findViewById(R.id.exit_conf);
		
		apply_speak.setEnabled(false);// 进会之前不可点
		exit_conf.setEnabled(false);

		mGL2JINView = getRomotePreview();
		mGL2JINView.setBackgroundResource(R.drawable.video);// 进会之前有背景图
		mSurfaceView.setBackgroundResource(R.drawable.video);

		listener = new SipTermListenerImpl(handler);// 监听Sip事件，在该监听器中发送msg给本UI线程处理

		// 获取登录界面的用户名、密码等用户的信息
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		String myAccount = data.getString("myAccount");
		UserName = myAccount;
		String passWord = data.getString("password");
		userId = data.getString("userId");
		System.out.println(myAccount+"  "+userId+"  "+passWord);
		constants = new Constants(myAccount.getBytes(), passWord.getBytes());		

		// 两个button的监听事件
		OnClickListener listener_btn = new OnClickListener() {
	    	public void onClick(View v){
	    		int id = v.getId();
				if (id == R.id.apply_speak){
					if(speakable){
						Map<String,String> params1 = new HashMap<String,String>();  
				     	params1.put("confId", confId.toString());  
				     	params1.put("userId", userId);		     
				     	ApplySpeakingThread applySpeakingThread = new ApplySpeakingThread(handler);
				     	applySpeakingThread.doStart("http://"+constants.registarIp+":8888/MediaConf/applySpeaking.do?", params1, MediaConfActivity.this);
					}
					else
						Toast.makeText(MediaConfActivity.this, "当前不能申请发言", Toast.LENGTH_LONG).show();
					
				}else{
					quitConf();
//					 Map<String,String> params = new HashMap<String,String>();  
//				     params.put("confId", confId.toString());  
//				     params.put("userId", userId);			     
//				     DisconnectUserThread disconnectUserThread=new DisconnectUserThread(handler); 
//				     //10.0.2.2为电脑对于模拟器而言的IP地址。
//				     disconnectUserThread.doStart("http://10.109.252.6:8888/MediaConf/disconnectUser.do?method=leave", params, MediaConfActivity.this);
				}
	    	}
	    };
		apply_speak.setOnClickListener(listener_btn);
		exit_conf.setOnClickListener(listener_btn);

		// 生成适配器，数组===》ListItem
		mSchedule = new SimpleAdapter(this, mylist,// 数据来源
				R.layout.my_listitem,// ListItem的XML实现
				new String[] { "ItemTitle", "ItemText" }, // 动态数组与ListItem对应的子项
				new int[] { R.id.userName, R.id.userId }); // ListItem的XML文件里面的两个TextView ID
															

		// 绑定service，刷新会议信息
		Intent refreshIntent = new Intent("com.http.RefreshService");
		Bundle senddata = new Bundle();
		senddata.putString("confId", confId.toString());
		senddata.putString("userId", userId);
		refreshIntent.putExtras(senddata);
		getApplicationContext().bindService(refreshIntent, serviceConnection,
				BIND_AUTO_CREATE);
		// 启动线程，每2秒获取一次会议信息更新结果
		timer.schedule(task, 1000, 2000);
		// 该方法的参数，除了用户名和密码外，其它的最好采用配置文件配置的形式。
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				constants.registarIp.getBytes(), constants.registarPort);

		//启动注册线程，定期注册
		new RegisterThread(sipTerm).start();

	}

	public void quitConf(){
		Map<String,String> params = new HashMap<String,String>();  
	     params.put("confId", confId.toString());  
	     params.put("userId", userId);			     
	     DisconnectUserThread disconnectUserThread=new DisconnectUserThread(handler); 
	     //10.0.2.2为电脑对于模拟器而言的IP地址。
	     disconnectUserThread.doStart("http://"+constants.registarIp+":8888/MediaConf/disconnectUser.do?method=leave", params, MediaConfActivity.this);
	}
	public void logout(){
		Map<String,String> params = new HashMap<String,String>();  
	     params.put("confId", confId.toString());  
	     params.put("userId", userId);
	     System.out.println("logout_main1");
	     LogoutThread logoutthread = new LogoutThread(handler);
	     logoutthread.doStart("http://"+Constants.registarIp+":8888/MediaConf/userLogin.do?method=logout",params, MediaConfActivity.this);
	}
	
//	@Override
//	public void onAttachedToWindow()   
//	{     
//       this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);        
//       super.onAttachedToWindow();     
//	}   

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean flag = false;
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			Log.e("test", "Home key down");
			System.out.println("Home key down   okooo");
//			onDestroy();
			// catchHomeKey=true;
			flag = super.onKeyDown(keyCode, event);
		}
		if(keyCode == KeyEvent.KEYCODE_BACK){	
			if(confId!=-1){
				new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle("退出系统")
				.setMessage("正在会议中，请确认是否要退出系统？")
			    .setIcon(android.R.drawable.ic_dialog_info) 
			    .setPositiveButton("确定", new DialogInterface.OnClickListener() {			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // 点击“确认”后的操作，退出系統的相关操作	
			        	quitConf();
						timer.cancel();
						getApplicationContext().unbindService(serviceConnection);
						if(sipTerm > 0)
				    		SipTerm.deleteTerm(sipTerm);						
						finish();
						logout();
			        } 
			    }) 
			    .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // 点击“返回”后的操作,这里不设置没有任何操作 			      
			        } 
			    }).show(); 
			}
			else{				
				new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle("确定退出吗？") 
			    .setIcon(android.R.drawable.ic_dialog_info) 
			    .setPositiveButton("确定", new DialogInterface.OnClickListener() {			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // 点击“确认”后的操作，退出系統的相关操作			
						timer.cancel();
						getApplicationContext().unbindService(serviceConnection);
						if(sipTerm > 0)
				    		SipTerm.deleteTerm(sipTerm);						
						finish();
						logout();
			        } 
			    }) 
			    .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // 点击“返回”后的操作,这里不设置没有任何操作 			      
			        } 
			    }).show(); 
			}			
			 	
		}
		return flag;
	}
	
	/**
	 * 实现父类的抽象方法，获取本地预览界面构件。
	 * @return 本地视频预览构件
	 */
	@Override
	public SurfaceView getLocalPreview() {
		// TODO Auto-generated method stub
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		return mSurfaceView;
	}
	
	@Override
	protected GL2JNIView getRomotePreview() {
		return (GL2JNIView) findViewById(R.id.remoteVideoView);
	}
	
	/**
	 * 实现父类的抽象方法，用于设置主Activity的布局。在本类中无需调用setContentView(getLayoutId());
	 * @return 界面布局类的ID
	 */
	@Override
	public int getLayoutId() {
		return R.layout.activity_media_conf;
	}
	
		
	@Override
	public void onRecvAudio(long term, byte[] pcmData, int size) {
		super.onRecvAudio(term, pcmData,size);
	//	ravtPutLocalAudio(term, pcmData);
		//ravtOpenRemoteAudio(mediaTerm, (short)0);
		//if(debugLxj) System.out.println("leisure" + "recvAudio false");
	}

	@Override
	public void onRecvVideo(long term) {
		super.onRecvVideo(term);
	}
	
	
	/**
	 * 注册成功的事件处理函数，主要用于界面UI组件更新（待补充）
	 * 此外还需要设置定时器，定时重新注册，保证注册服务器上的注册成功信息不过期
	 */
	private void processRegisterOK() {
		if(debugLxj) System.out.println("注册成功");
	}
	
	/**
	 * 注册失败的事件处理函数，异常处理
	 */
	private void processRegisterFailed() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 接收到INVITE，本地振铃时的事件处理
	 */
	private void processLocalRing() {
		
		if(debugLxj) System.out.println("振铃");
		
		ravtCreateTerm(); //创建媒体处理Terminal
		
		//打开音视频会话
		boolean audioFlag = ravtOpenAudioSession("0.0.0.0".getBytes(), Constants.sdpLocalAudioPort);
		boolean videoFlag = ravtOpenVideoSession("0.0.0.0".getBytes(), Constants.sdpLocalVideoPort);
		
		if(debugLxj) System.out.println("打开音视频的会话结果依次为：" + audioFlag + "," + videoFlag);
		
	}
	
	/**
	 * 加入会议成功后，提取媒体协商结果，开始输出本地音视频，并显示本地音视频预览
	 */
	private void processJoinConfOK(Bundle mediaInfo) {
		if(debugLxj) System.out.println("成功加入会议，开始输出音视频");
		
		//音频处理
		System.out.println("remoteAudioPayload = " + mediaInfo.getByte("remoteAudioPayload"));
		if(ravtSetAudioSessionOutputPayloadType(mediaInfo.getByte("remoteAudioPayload"))) {
			System.out.println("ravtSetAudioSessionOutputPayloadType = true");
		}else{
			System.out.println("ravtSetAudioSessionOutputPayloadType = false");
		}
		if(ravtSetAudioSessionRemoteAddr(mediaInfo.getByteArray("remoteAudioAddr"), 
				mediaInfo.getInt("remoteAudioPort"))){
			System.out.println("ravtSetAudioSessionRemoteAddr = true");
		}else{
			System.out.println("ravtSetAudioSessionRemoteAddr = false");
		}
			
		if(ravtOpenLocalAudio(mediaInfo.getShort("remoteAudioType"))){
			System.out.println("ravtOpenLocalAudio = true");
		}else{
			System.out.println("ravtOpenLocalAudio = false");
		}
		if(ravtOpenRemoteAudio(mediaInfo.getShort("remoteAudioType"))){
			System.out.println("ravtOpenRemoteAudio = true");
		}else{
			System.out.println("ravtOpenRemoteAudio = false");
		}
		
		//视频处理
		if (!ravtSetVideoSessionOutputPayloadType(mediaInfo.getByte("remoteVideoPayload"))) {//(byte)109)) {
			if(debugLxj) System.out.println("视频负载类型错误");
			return;
		}
		if(debugLxj) System.out.println("视频负载类型正常");
		
		int videoPort = mediaInfo.getInt("remoteVideoPort");
		byte[] videoAddr = mediaInfo.getByteArray("remoteVideoAddr");
		if(debugLxj) System.out.println("videoPort = " + videoPort);
		if (!ravtSetVideoSessionRemoteAddr(mediaInfo.getByteArray("remoteVideoAddr"),
				mediaInfo.getInt("remoteVideoPort"))){
			if(debugLxj) System.out.println("远端地址设置错误");
			return;
		}
		if(debugLxj) System.out.println("远端地址设置正常");
		
		if (!ravtOpenLocalVideoOutput(RtpAvTerm.RAVT_CODEC_H264,
				localVideoWidth, localVideoHeight, mediaInfo.getLong("videoByteRate"), localVideoFrameRate,
				localVideoFrameRate * 3, 1024)) {
			if(debugLxj) System.out.println("本地视频输出错误");
			return;
		}
		if(debugLxj) System.out.println("本地视频输出正常");
		
		//打开本地视频预览，注意必须打开预览才能采集视频数据上传到媒体服务器。
		//摄象机的视频有两个去向，一个preview,一个output
		//摄象机的开关只能有preview函数控制，
		ravtOpenLocalVideoPreview(localVideoWidth, localVideoHeight);
		
		System.out.println("remoteVideoPayload = " + mediaInfo.getByte("remoteVideoPayload"));
		boolean res = ravtOpenRemoteVideoPreview(mediaInfo.getShort("remoteVideoType"));
		if(res)
			System.out.println("ravtOpenRemoteVideoPreview success！");
		else
			System.out.println("ravtOpenRemoteVideoPreview failed！");
		
		//增加请求I-Frame的操作，避免
		long term = mediaInfo.getLong("term");
		long callId = mediaInfo.getLong("callId");
		SipTerm.requestKeyFrame(term, callId);
	}
	
	
	/**结束会议后，停止本地音视频采集和输出，释放资源
	 */
	private void processQuitConf() {
		if(debugLxj) System.out.println("退出会议");
		
		confTitle.setText("");
		subject.setText("");
		chairman.setText("");
		duration.setText("");					
		mylist.clear();					
		list.setAdapter(mSchedule); 
		ravtCloseRemoteAudio();
		ravtCloseLocalAudio();
		ravtCloseAudioSession();
		
		ravtCloseRemoteVideoPreview();
		ravtCloseLocalVideoPreview();
		ravtCloseLocalVideoOutput();
		ravtCloseVideoSession();	
		
		ravtDeleteTerm();
		if(debugLxj) System.out.println("退出会议完成");
	}
	
}


class RegisterThread extends Thread  
{  
	long term;  
    public RegisterThread(long term) {  
        this.term = term;       
    }
    @Override  
    public void run() {    	    	
    	
    	while(true){
			try {
				boolean flag = SipTerm.doRegister(term, 120);
				if(flag){			
				}
				else{	
				//	System.out.println("Register called bad!");
				}
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
    }
}

