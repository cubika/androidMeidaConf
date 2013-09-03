package com.rongdian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.BaseAdapter;

import com.http.GenericTask;
import com.http.GetUserInfoTask;
import com.http.HttpUtils;
import com.http.LogoutThread;
import com.http.RegisterThread;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.util.Constants;

public class PadActivity extends RtpAvTermAndroidActivity {
	
	//sip相关
	private SipTermListenerImpl listener;  //监听Sip事件，在该监听器中发送msg给本UI线程处理
	long sipTerm;                          //sip终端，生命周期同整个Activity
	
	//UI组件
	public static String UserName;
	private static ListView list;// 会中会议成员列表
	private Menu actionBarMenu;
	private Bundle saveMediaBundle; //保存起来以便于再次JoinProcessOK
	private Constants constants;
	
	private static ListView confLV, participantLV;// 会议信息和参会人员的Listview
	private static List<String> participantNames = new ArrayList<String>(); //参会人员名字
	private static List<String> participantIDs = new ArrayList<String>();  //参会人员ID
	//媒体相关
	long mediaTerm;                    //媒体终端，生命周期同一个会议
	SurfaceView mSurfaceView = null;  //显示本地视频界面组件
	GL2JNIView mGL2JINView = null;    //显示远端视频界面组件
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// 用户和会议标识
	public static Integer confId = -1;
	public static String userId = "-1";
	public static String chairmanId = null;
	boolean speakable;	
	String confName = null,confSubject = null,confDuration = null,chairmanName = null;	


    Timer timer = new Timer();
    //获取服务器刷新数据
	TimerTask task = new TimerTask( ) {
		public void run ( ) {		
			String url = Constants.prefix+"clientRefresh.do?";
			String result = null;
			Bundle data = new Bundle();
			Message msg = new Message();
			try {
				Map<String, String> params = new HashMap<String, String>();
				params.put("confId", confId.toString());
				params.put("userId", userId);
				Log.v("refreshData", "userId:" + userId + " confId:" + confId);
				result = HttpUtils.sendPostMessage(url, params, "utf-8");
			} catch (IOException e) {
				Log.e("refreshData", "get refresh data error");
				e.printStackTrace();
			}
			if (result != null && !result.equals("null")) {
				Log.v("timertask", "get refresh result:" + result);
				String[] messages = result.split("@"); 		 
	    		
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
	                   
	                   Map<String,String> params = new HashMap<String,String>();  
	       	     	   params.put("confId", confId.toString());  
	       	     	   String participants = "";
	                   try {
	                	   participants = HttpUtils.sendPostMessage(Constants.prefix+"conference/conferenceinfo.do?method=getCurrentParticipateListFormain", params, "utf-8");
	                	   data.putString("participants", participants);
							Log.v("refreshData", "participants:" + participants);
						} catch (IOException e) {
							Log.e("refreshData", "get participants error");
							e.printStackTrace();
						}
						msg.what = 0x1239;
	   				  
	                }
	                
	                //弹出提示框
	                if(showIndex>=0){   	                
	                	msg.what = 0x1238;
	                	data.putString("show", messages[u].split(":")[1]);	                
	                }  
	                if(changeChairmanIndex>=0){              
	                   	msg.what = 0x1238;
	                   	data.putString("show", messages[u].split(":")[2]);         	
	                }
	               
	                //更新会议信息
	                if(confInfoIndex>=0){
	                	confName=messages[u].split(":")[1];
	                	confSubject=messages[u].split(":")[2];                	
	                	confDuration=messages[u].split(":")[3];
	                	chairmanId=messages[u].split(":")[4];
	                	chairmanName = messages[u].split(":")[5];
	                	Log.v("refreshData", "confMessageUpdated");
	                			
	    				data.putString("confName",confName);
	    				data.putString("confSubject", confSubject);
	    				data.putString("chairmanName",chairmanName);
	    				data.putString("confDuration", confDuration);
	    				msg.what = 0x1238;    				
	                } 
	                
	                if(callIndex>=0){                	
	                    String[] subString=messages[u].split(":");
	                    String[] userInfo=subString[1].split(";");
	                    String js=userInfo[0];  
	                    
	                    msg.what = 0x1240;
	                   // speakable = true;
	                    data.putString("show", userInfo[1]);	                   
	                }
	                if(changeIndex>=0){                	
	                    String[] subString=messages[u].split(":");
	                    String[] changeInfo=subString[1].split(";");
	                    Log.v("refreshData", "change chairman url:" + changeInfo[1]);	                   
	                }
	                
	                // 更换会议主席
	                if(afterChangeIndex>=0){ 	                
	 	                String[] subString=messages[u].split(":");
	 	                chairmanId=subString[2];
	 	                chairmanName=subString[1];
	 	                msg.what = 0x1238;
	 	                data.putString("show", subString[3]);	 	               	                
	 	            } 
	                
	                if(chairmanShowIndex>=0) {
	                	confId=-1; 		                	
	                	msg.what = 0x1237;
	                	data.putString("show", "会议已结束");
	 	            }          
	    		}    			
				 
				msg.setData(data);
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
					MenuItem menuItem=actionBarMenu.findItem(R.id.helloUser);
					if(menuItem!=null)
						menuItem.setTitle("欢迎您，"+UserName+"！");
					processRegisterOK();
					
					break;
				case 0x1234: //注册失败
					processRegisterFailed();
					break;
				case 0x1235: //本地振铃，被邀请加入会议
					processLocalRing();
					break;
				case 0x1236: //成功加入会议
					mGL2JINView.setBackgroundResource(0);
					mSurfaceView.setBackgroundResource(0);
					saveMediaBundle=message.getData();
					processJoinConfOK(saveMediaBundle);					
			       	break;
				case 0x1237: //退出会议
					mGL2JINView.setBackgroundResource(R.drawable.video);
					mSurfaceView.setBackgroundResource(R.drawable.video);
					if(message.getData().getString("show")!=null)
						Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(PadActivity.this, "您已退出会议", Toast.LENGTH_LONG).show();
					processQuitConf();	
					break;
			 /*****************************************/		
				case 0x1238: // 会议信息有更新
					if(message.getData().getString("show")!=null)
						Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();					

					if (confLV != null && confLV.getAdapter() != null)
						// 通知adapter会议信息数据源更新
						((BaseAdapter) confLV.getAdapter()).notifyDataSetChanged();
					
					break;
				case 0x1239: //与会人员有更新
					participantNames.clear();
					participantIDs.clear();
					JSONObject jsonObject;
			        JSONArray jsonArray = null;
			        String parString=message.getData().getString("participants");
			        Log.v("handler","participants:"	+ parString);
			        if(parString==null)
			        	break;
					try {
						jsonObject = new JSONObject(parString);
						jsonArray = jsonObject.getJSONArray("rows");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.v("handler", "jsonArray " + jsonArray);
					for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
						if (jsonArray.opt(i) == null)
							continue;
			        	JSONObject jsonObject2 = (JSONObject)jsonArray.opt(i);
			        	String userName = null,userId = null;        	        	
			        	try {
			        		userName = jsonObject2.getString("userName");
							userId = jsonObject2.getString("userId");
							participantNames.add(userName);
							participantIDs.add(userId);
						} catch (JSONException e) {
							e.printStackTrace();
						}
			        }        
					// 通知adapter参会人员数据源更新
					if (participantLV != null && participantLV.getAdapter() != null)
						((BaseAdapter) participantLV.getAdapter()).notifyDataSetChanged();
					break;
				case 0x1240:
					Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_SHORT).show();
					if(message.getData().getString("show").contains("大家可以申请发言")){
						speakable = true;						
					}
					else if (message.getData().getString("show").contains("申请时间已到，不能再申请了")){
						speakable = false;						
					}
					System.out.println("case 0x1240 "+message.getData().getString("show"));
					break;
				case 0x2100:					
					Toast.makeText(PadActivity.this, "消息发送失败", Toast.LENGTH_LONG).show();
					break;
				case 0x2101:	            	
	            //	Toast.makeText(TestActivity.this, "消息发送成功", Toast.LENGTH_LONG).show();
					
					
					break;
				case 0x2102:
					Toast.makeText(PadActivity.this, "退出登录成功", Toast.LENGTH_LONG).show();
					break;
				case 0x2103:
					Toast.makeText(PadActivity.this, "退出登录失败", Toast.LENGTH_LONG).show();
					break;
			}	
		}
	 };
	
	//调试相关
	boolean debugLxj = true;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		mGL2JINView = getRomotePreview();
		mGL2JINView.setBackgroundResource(R.drawable.video);// 进会之前有背景图
		mSurfaceView.setBackgroundResource(R.drawable.video);
		
		// 设置视频显示组件的宽和高
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		int diswidth = (int) width / 3;
		//int disheight = (int) height / 3;
		int disheight = diswidth*3/4;
		mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(diswidth,disheight));

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
															
		// 启动线程，每2秒获取一次会议信息更新结果
		timer.schedule(task, 1000, 2000);
		// 该方法的参数，除了用户名和密码外，其它的最好采用配置文件配置的形式。
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), Constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				Constants.registarIp.getBytes(), Constants.registarPort);

		new RegisterThread(sipTerm).start();
		
		//获取用户的信息
		new GetUserInfoTask().execute(Constants.prefix+"user/userInfo1.jsp");
		
		setBehindContentView(R.layout.frame_menu);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MenuFragment menuFragment = new MenuFragment();
        fragmentTransaction.replace(R.id.menu, menuFragment);
        //fragmentTransaction.replace(R.id.content, new ContentFragment("Welcome"));
        fragmentTransaction.commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
        //设置slding menu的几种手势模式
        //TOUCHMODE_FULLSCREEN 全屏模式，在content页面中，滑动，可以打开sliding menu
        //TOUCHMODE_MARGIN 边缘模式，在content页面中，如果想打开slding ,你需要在屏幕边缘滑动才可以打开slding menu
        //TOUCHMODE_NONE 自然是不能通过手势打开啦
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        //使用左上方icon可点，这样在onOptionsItemSelected里面才可以监听到R.id.home
        getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	public void quitConf(){
     	if(chairmanId.trim().equals(userId)){
     		new SimpleTask().execute(Constants.prefix+"endConference.do?confId="+confId);
     		}else{
     			new GenericTask(PadActivity.this).execute(Constants.prefix+"disconnectUser.do?method=leave&confId="+confId+"&userId="+userId);
    	}
	}
	public void logout(){
//		Map<String,String> params = new HashMap<String,String>();  
//	     params.put("confId", confId.toString());  
//	     params.put("userId", userId);
	     LogoutThread logoutthread = new LogoutThread(handler);
	     logoutthread.doStart(Constants.prefix+"userLogin.do?method=logout",null, PadActivity.this);
	}
	
//	@Override
//	public void onAttachedToWindow()   
//	{     
//       this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);        
//       super.onAttachedToWindow();     
//	}   

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK){	//虽然这样写，但是捕获不到home键
			quitApp();		
			
		}if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            getSlidingMenu().toggle();
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	public void quitApp(){
		if (confId != -1) {
			new AlertDialog.Builder(new ContextThemeWrapper(this,
					R.style.AlertDialogCustom))
					.setTitle("退出系统")
					.setMessage("正在会议中，请确认是否要退出系统？")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击“确认”后的操作，退出系統的相关操作
									quitConf();
									timer.cancel();
									if (sipTerm > 0)
										SipTerm.deleteTerm(sipTerm);
									logout();
									finish();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击“返回”后的操作,这里不设置没有任何操作
								}
							}).show();
		} else {
			new AlertDialog.Builder(new ContextThemeWrapper(this,
					R.style.AlertDialogCustom))
					.setTitle("确定退出吗？")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击“确认”后的操作，退出系統的相关操作
									timer.cancel();
									if (sipTerm > 0)
										SipTerm.deleteTerm(sipTerm);
									logout();
									finish();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击“返回”后的操作,这里不设置没有任何操作
								}
							}).show();
		}
	}
	/**
	 * 实现父类的抽象方法，获取本地预览界面构件。
	 * @return 本地视频预览构件
	 */
	@Override
	public SurfaceView getLocalPreview() {
		mSurfaceView = (SurfaceView) findViewById(R.id.localVideoV);
		return mSurfaceView;
	}
	
	@Override
	protected GL2JNIView getRomotePreview() {
		return (GL2JNIView) findViewById(R.id.remoteVideoV);
	}
	
	/**
	 * 实现父类的抽象方法，用于设置主Activity的布局。在本类中无需调用setContentView(getLayoutId());
	 * @return 界面布局类的ID
	 */
	@Override
	public int getLayoutId() {
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
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
			
		// 会议信息,成员信息清空
		participantNames.clear();
	    participantIDs.clear();
	    if (confLV != null && confLV.getAdapter() != null)
			((BaseAdapter) confLV.getAdapter()).notifyDataSetChanged();
		if (participantLV != null && participantLV.getAdapter() != null)
			((BaseAdapter) participantLV.getAdapter()).notifyDataSetChanged();
		
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
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        actionBarMenu=menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            //toggle就是程序自动判断是打开还是关闭
            toggle();
            return true;
        case R.id.action_show_conf:
			if (confId == -1) {
				Toast.makeText(PadActivity.this, "您还没有加入会议，无法查看会议信息",
						Toast.LENGTH_LONG).show();
				break;
			}
			AlertDialog.Builder builder1 = new AlertDialog.Builder(PadActivity.this);
			builder1.setInverseBackgroundForced(true);
			builder1.setTitle("会议信息列表");
			View confMessageView = View.inflate(PadActivity.this,
					R.layout.message_dialog, null);
			builder1.setView(confMessageView);
			confLV = (ListView) confMessageView.findViewById(R.id.LV);
			String[] confMessageTitles = getResources().getStringArray(
					R.array.confMessages);
			String[] confMessageContents = { confName, confSubject,
					confDuration, chairmanName };
			confLV.setAdapter(getMessageAdapter(confMessageTitles,
					confMessageContents));
			builder1.show();
        	return true;
        case R.id.action_show_par:
			if (confId == -1) {
				Toast.makeText(PadActivity.this, "您还没有加入会议，无法查看参会人员",
						Toast.LENGTH_LONG).show();
				break;
			}
			AlertDialog.Builder builder2 = new AlertDialog.Builder(PadActivity.this);
			builder2.setInverseBackgroundForced(true);
			builder2.setTitle("参会人员列表");
			View participantMessageView = View.inflate(
					PadActivity.this,
					R.layout.message_dialog, null);
			builder2.setView(participantMessageView);
			participantLV = (ListView) participantMessageView
					.findViewById(R.id.LV);
			participantLV.setAdapter(getMessageAdapter(
					participantNames.toArray(new String[] {}),
					participantIDs.toArray(new String[] {})));
			builder2.show();
        	return true;
        case R.id.action_exit_conf:
        	if (confId == -1) {
				Toast.makeText(PadActivity.this, "您还没有加入会议，无法退出会议",
						Toast.LENGTH_SHORT).show();
				break;
			}
        	quitConf();
        	return true;
        case R.id.action_exit_app:
        	quitApp();
        	return true;
        case R.id.action_hide_surface:
        	if(mSurfaceView.isShown())
        		mSurfaceView.setVisibility(View.INVISIBLE);
        	else
        		mSurfaceView.setVisibility(View.VISIBLE);
        	return true;
        	
        }
        return super.onOptionsItemSelected(item);
    }
    
    private SimpleAdapter getMessageAdapter(String[] titleArray,
			String[] contentArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		if (titleArray.length == contentArray.length) {
			for (int i = 0; i < titleArray.length; i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", titleArray[i]);
				map.put("content", contentArray[i]);
				data.add(map);
			}
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						"title", "content" }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		return simperAdapter;
	}
    
	public void closeAll(){
		ravtCloseRemoteAudio();
		ravtCloseLocalAudio();
//		ravtCloseAudioSession();

		ravtCloseRemoteVideoPreview();
		ravtCloseLocalVideoPreview();
		ravtCloseLocalVideoOutput();
//		ravtCloseVideoSession();
	}
	@Override
	public void onPause(){
		super.onPause();
		Log.v("PhoneActivity","onPause");
		closeAll();
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.v("PhoneActivity", "onResume");
		if(confId!=-1 && saveMediaBundle!=null){
				processJoinConfOK(saveMediaBundle);
		}
	}

	class SimpleTask extends AsyncTask<String, Integer, String> {
		
		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("SimpleTask", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("SimpleTask", "result is:" + result);
		}
	}
}