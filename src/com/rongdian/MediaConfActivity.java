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
	
	//sip���
	private SipTermListenerImpl listener;  //����Sip�¼����ڸü������з���msg����UI�̴߳���
	long sipTerm;                          //sip�նˣ���������ͬ����Activity
	
	//UI���
	public static TextView loginedText = null;
	public static TextView confTitle = null;
	public static TextView chairman = null;
	public static TextView subject = null;
	public static TextView duration = null;
	public static String UserName;
	public static Button apply_speak, exit_conf;// ���뷢�ԡ������뿪��ť
	public static ListView list;// ���л����Ա�б�
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
	SimpleAdapter mSchedule;
	public Constants constants;
	
	//ý�����
	long mediaTerm;                    //ý���նˣ���������ͬһ������
	SurfaceView mSurfaceView = null;  //��ʾ������Ƶ�������
	GL2JNIView mGL2JINView = null;    //��ʾԶ����Ƶ�������
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// �û��ͻ����ʶ
	Integer confId = -1;
	String userId = "-1";
	boolean speakable;	
	String confName = null,confSubject = null,confDuration = null,chairmanId = null,chairmanName = null;	

	RefreshService.ServiceBinder binder;
	
	//��service����ѯ������Ϣ	
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
    //��ѯ��ȡ�������
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
	                
	                //ˢ�������Ա
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
	                
	                //������ʾ��
	                if(showIndex>=0){   	                
	                	msg.what = 0x1238;
	                	refreshdata.putString("show", messages[u].split(":")[1]);	                
	                }  
	                if(changeChairmanIndex>=0){              
	                   	msg.what = 0x1238;
	                	refreshdata.putString("show", messages[u].split(":")[2]);	                	
	                }
	               
	                //���»�����Ϣ
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
	                
	                // ����������ϯ
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
	                	refreshdata.putString("show", "�����ѽ���");
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
			 /******************SipTermListener��sip�¼�������***********************/
			 	case 0x1233: //ע��ɹ�
			 		System.out.println("Registerok sipע��ɹ�  ");
					loginedText.setText( "��ӭ����"+UserName+"��");
					processRegisterOK();
					
					break;
				case 0x1234: //ע��ʧ��
					loginedText.setText( "δ��¼");
					processRegisterFailed();
					break;
				case 0x1235: //�������壬������������
					processLocalRing();
					break;
				case 0x1236: //�ɹ��������
					apply_speak.setEnabled(true);
					exit_conf.setEnabled(true);
					mGL2JINView.setBackgroundResource(0);
					mSurfaceView.setBackgroundResource(0);
					processJoinConfOK(message.getData());					
			       	break;
				case 0x1237: //�˳�����
					apply_speak.setEnabled(false);
					exit_conf.setEnabled(false);
					mGL2JINView.setBackgroundResource(R.drawable.video);
					mSurfaceView.setBackgroundResource(R.drawable.video);
					if(message.getData().getString("show")!=null)
						Toast.makeText(MediaConfActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();
					else
						Toast.makeText(MediaConfActivity.this, "�����˳�����", Toast.LENGTH_LONG).show();
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
			      //��Ӳ�����ʾ  
			        list.setAdapter(mSchedule); 			        
					break;
				case 0x1240:
					Toast.makeText(MediaConfActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();
					if(message.getData().getString("show").contains("��ҿ������뷢��")){
						speakable = true;						
					}
					else if (message.getData().getString("show").contains("����ʱ���ѵ���������������")){
						speakable = false;						
					}
					System.out.println("case 0x1240 "+message.getData().getString("show"));
					break;
				case 0x2100:					
					Toast.makeText(MediaConfActivity.this, "��Ϣ����ʧ��", Toast.LENGTH_LONG).show();
					break;
				case 0x2101:	            	
	            //	Toast.makeText(MediaConfActivity.this, "��Ϣ���ͳɹ�", Toast.LENGTH_LONG).show();
					
					
					break;
				case 0x2102:
					Toast.makeText(MediaConfActivity.this, "�˳���¼�ɹ�", Toast.LENGTH_LONG).show();
					break;
				case 0x2103:
					Toast.makeText(MediaConfActivity.this, "�˳���¼ʧ��", Toast.LENGTH_LONG).show();
					break;
			}	
		}
	 };
	
	//�������
	boolean debugLxj = true;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		loginedText = (TextView) findViewById(R.id.loginedText);// handler���������û�ע���״̬��Ϣ
		confTitle = (TextView) findViewById(R.id.confTitle);// ������Ϣ��ʾ��tablelayout��
		chairman = (TextView) findViewById(R.id.confChairman);
		subject = (TextView) findViewById(R.id.confSubject);
		duration = (TextView) findViewById(R.id.confDuration);
		list = (ListView) findViewById(R.id.mem_info_lv);// �����Ա�б�
		apply_speak = (Button) findViewById(R.id.apply_speak);
		exit_conf = (Button) findViewById(R.id.exit_conf);
		
		apply_speak.setEnabled(false);// ����֮ǰ���ɵ�
		exit_conf.setEnabled(false);

		mGL2JINView = getRomotePreview();
		mGL2JINView.setBackgroundResource(R.drawable.video);// ����֮ǰ�б���ͼ
		mSurfaceView.setBackgroundResource(R.drawable.video);

		listener = new SipTermListenerImpl(handler);// ����Sip�¼����ڸü������з���msg����UI�̴߳���

		// ��ȡ��¼������û�����������û�����Ϣ
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		String myAccount = data.getString("myAccount");
		UserName = myAccount;
		String passWord = data.getString("password");
		userId = data.getString("userId");
		System.out.println(myAccount+"  "+userId+"  "+passWord);
		constants = new Constants(myAccount.getBytes(), passWord.getBytes());		

		// ����button�ļ����¼�
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
						Toast.makeText(MediaConfActivity.this, "��ǰ�������뷢��", Toast.LENGTH_LONG).show();
					
				}else{
					quitConf();
//					 Map<String,String> params = new HashMap<String,String>();  
//				     params.put("confId", confId.toString());  
//				     params.put("userId", userId);			     
//				     DisconnectUserThread disconnectUserThread=new DisconnectUserThread(handler); 
//				     //10.0.2.2Ϊ���Զ���ģ�������Ե�IP��ַ��
//				     disconnectUserThread.doStart("http://10.109.252.6:8888/MediaConf/disconnectUser.do?method=leave", params, MediaConfActivity.this);
				}
	    	}
	    };
		apply_speak.setOnClickListener(listener_btn);
		exit_conf.setOnClickListener(listener_btn);

		// ����������������===��ListItem
		mSchedule = new SimpleAdapter(this, mylist,// ������Դ
				R.layout.my_listitem,// ListItem��XMLʵ��
				new String[] { "ItemTitle", "ItemText" }, // ��̬������ListItem��Ӧ������
				new int[] { R.id.userName, R.id.userId }); // ListItem��XML�ļ����������TextView ID
															

		// ��service��ˢ�»�����Ϣ
		Intent refreshIntent = new Intent("com.http.RefreshService");
		Bundle senddata = new Bundle();
		senddata.putString("confId", confId.toString());
		senddata.putString("userId", userId);
		refreshIntent.putExtras(senddata);
		getApplicationContext().bindService(refreshIntent, serviceConnection,
				BIND_AUTO_CREATE);
		// �����̣߳�ÿ2���ȡһ�λ�����Ϣ���½��
		timer.schedule(task, 1000, 2000);
		// �÷����Ĳ����������û����������⣬��������ò��������ļ����õ���ʽ��
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				constants.registarIp.getBytes(), constants.registarPort);

		//����ע���̣߳�����ע��
		new RegisterThread(sipTerm).start();

	}

	public void quitConf(){
		Map<String,String> params = new HashMap<String,String>();  
	     params.put("confId", confId.toString());  
	     params.put("userId", userId);			     
	     DisconnectUserThread disconnectUserThread=new DisconnectUserThread(handler); 
	     //10.0.2.2Ϊ���Զ���ģ�������Ե�IP��ַ��
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
				new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle("�˳�ϵͳ")
				.setMessage("���ڻ����У���ȷ���Ƿ�Ҫ�˳�ϵͳ��")
			    .setIcon(android.R.drawable.ic_dialog_info) 
			    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // �����ȷ�ϡ���Ĳ������˳�ϵ�y����ز���	
			        	quitConf();
						timer.cancel();
						getApplicationContext().unbindService(serviceConnection);
						if(sipTerm > 0)
				    		SipTerm.deleteTerm(sipTerm);						
						finish();
						logout();
			        } 
			    }) 
			    .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() { 
			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // ��������ء���Ĳ���,���ﲻ����û���κβ��� 			      
			        } 
			    }).show(); 
			}
			else{				
				new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle("ȷ���˳���") 
			    .setIcon(android.R.drawable.ic_dialog_info) 
			    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // �����ȷ�ϡ���Ĳ������˳�ϵ�y����ز���			
						timer.cancel();
						getApplicationContext().unbindService(serviceConnection);
						if(sipTerm > 0)
				    		SipTerm.deleteTerm(sipTerm);						
						finish();
						logout();
			        } 
			    }) 
			    .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() { 
			 
			        @Override 
			        public void onClick(DialogInterface dialog, int which) { 
			        // ��������ء���Ĳ���,���ﲻ����û���κβ��� 			      
			        } 
			    }).show(); 
			}			
			 	
		}
		return flag;
	}
	
	/**
	 * ʵ�ָ���ĳ��󷽷�����ȡ����Ԥ�����湹����
	 * @return ������ƵԤ������
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
	 * ʵ�ָ���ĳ��󷽷�������������Activity�Ĳ��֡��ڱ������������setContentView(getLayoutId());
	 * @return ���沼�����ID
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
	 * ע��ɹ����¼�����������Ҫ���ڽ���UI������£������䣩
	 * ���⻹��Ҫ���ö�ʱ������ʱ����ע�ᣬ��֤ע��������ϵ�ע��ɹ���Ϣ������
	 */
	private void processRegisterOK() {
		if(debugLxj) System.out.println("ע��ɹ�");
	}
	
	/**
	 * ע��ʧ�ܵ��¼����������쳣����
	 */
	private void processRegisterFailed() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * ���յ�INVITE����������ʱ���¼�����
	 */
	private void processLocalRing() {
		
		if(debugLxj) System.out.println("����");
		
		ravtCreateTerm(); //����ý�崦��Terminal
		
		//������Ƶ�Ự
		boolean audioFlag = ravtOpenAudioSession("0.0.0.0".getBytes(), Constants.sdpLocalAudioPort);
		boolean videoFlag = ravtOpenVideoSession("0.0.0.0".getBytes(), Constants.sdpLocalVideoPort);
		
		if(debugLxj) System.out.println("������Ƶ�ĻỰ�������Ϊ��" + audioFlag + "," + videoFlag);
		
	}
	
	/**
	 * �������ɹ�����ȡý��Э�̽������ʼ�����������Ƶ������ʾ��������ƵԤ��
	 */
	private void processJoinConfOK(Bundle mediaInfo) {
		if(debugLxj) System.out.println("�ɹ�������飬��ʼ�������Ƶ");
		
		//��Ƶ����
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
		
		//��Ƶ����
		if (!ravtSetVideoSessionOutputPayloadType(mediaInfo.getByte("remoteVideoPayload"))) {//(byte)109)) {
			if(debugLxj) System.out.println("��Ƶ�������ʹ���");
			return;
		}
		if(debugLxj) System.out.println("��Ƶ������������");
		
		int videoPort = mediaInfo.getInt("remoteVideoPort");
		byte[] videoAddr = mediaInfo.getByteArray("remoteVideoAddr");
		if(debugLxj) System.out.println("videoPort = " + videoPort);
		if (!ravtSetVideoSessionRemoteAddr(mediaInfo.getByteArray("remoteVideoAddr"),
				mediaInfo.getInt("remoteVideoPort"))){
			if(debugLxj) System.out.println("Զ�˵�ַ���ô���");
			return;
		}
		if(debugLxj) System.out.println("Զ�˵�ַ��������");
		
		if (!ravtOpenLocalVideoOutput(RtpAvTerm.RAVT_CODEC_H264,
				localVideoWidth, localVideoHeight, mediaInfo.getLong("videoByteRate"), localVideoFrameRate,
				localVideoFrameRate * 3, 1024)) {
			if(debugLxj) System.out.println("������Ƶ�������");
			return;
		}
		if(debugLxj) System.out.println("������Ƶ�������");
		
		//�򿪱�����ƵԤ����ע������Ԥ�����ܲɼ���Ƶ�����ϴ���ý���������
		//���������Ƶ������ȥ��һ��preview,һ��output
		//������Ŀ���ֻ����preview�������ƣ�
		ravtOpenLocalVideoPreview(localVideoWidth, localVideoHeight);
		
		System.out.println("remoteVideoPayload = " + mediaInfo.getByte("remoteVideoPayload"));
		boolean res = ravtOpenRemoteVideoPreview(mediaInfo.getShort("remoteVideoType"));
		if(res)
			System.out.println("ravtOpenRemoteVideoPreview success��");
		else
			System.out.println("ravtOpenRemoteVideoPreview failed��");
		
		//��������I-Frame�Ĳ���������
		long term = mediaInfo.getLong("term");
		long callId = mediaInfo.getLong("callId");
		SipTerm.requestKeyFrame(term, callId);
	}
	
	
	/**���������ֹͣ��������Ƶ�ɼ���������ͷ���Դ
	 */
	private void processQuitConf() {
		if(debugLxj) System.out.println("�˳�����");
		
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
		if(debugLxj) System.out.println("�˳��������");
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

