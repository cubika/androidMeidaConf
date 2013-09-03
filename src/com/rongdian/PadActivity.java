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
	
	//sip���
	private SipTermListenerImpl listener;  //����Sip�¼����ڸü������з���msg����UI�̴߳���
	long sipTerm;                          //sip�նˣ���������ͬ����Activity
	
	//UI���
	public static String UserName;
	private static ListView list;// ���л����Ա�б�
	private Menu actionBarMenu;
	private Bundle saveMediaBundle; //���������Ա����ٴ�JoinProcessOK
	private Constants constants;
	
	private static ListView confLV, participantLV;// ������Ϣ�Ͳλ���Ա��Listview
	private static List<String> participantNames = new ArrayList<String>(); //�λ���Ա����
	private static List<String> participantIDs = new ArrayList<String>();  //�λ���ԱID
	//ý�����
	long mediaTerm;                    //ý���նˣ���������ͬһ������
	SurfaceView mSurfaceView = null;  //��ʾ������Ƶ�������
	GL2JNIView mGL2JINView = null;    //��ʾԶ����Ƶ�������
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// �û��ͻ����ʶ
	public static Integer confId = -1;
	public static String userId = "-1";
	public static String chairmanId = null;
	boolean speakable;	
	String confName = null,confSubject = null,confDuration = null,chairmanName = null;	


    Timer timer = new Timer();
    //��ȡ������ˢ������
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
	                
	                //ˢ�������Ա
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
	                
	                //������ʾ��
	                if(showIndex>=0){   	                
	                	msg.what = 0x1238;
	                	data.putString("show", messages[u].split(":")[1]);	                
	                }  
	                if(changeChairmanIndex>=0){              
	                   	msg.what = 0x1238;
	                   	data.putString("show", messages[u].split(":")[2]);         	
	                }
	               
	                //���»�����Ϣ
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
	                
	                // ����������ϯ
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
	                	data.putString("show", "�����ѽ���");
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
			 /******************SipTermListener��sip�¼�������***********************/
			 	case 0x1233: //ע��ɹ�
			 		System.out.println("Registerok sipע��ɹ�  ");
					MenuItem menuItem=actionBarMenu.findItem(R.id.helloUser);
					if(menuItem!=null)
						menuItem.setTitle("��ӭ����"+UserName+"��");
					processRegisterOK();
					
					break;
				case 0x1234: //ע��ʧ��
					processRegisterFailed();
					break;
				case 0x1235: //�������壬������������
					processLocalRing();
					break;
				case 0x1236: //�ɹ��������
					mGL2JINView.setBackgroundResource(0);
					mSurfaceView.setBackgroundResource(0);
					saveMediaBundle=message.getData();
					processJoinConfOK(saveMediaBundle);					
			       	break;
				case 0x1237: //�˳�����
					mGL2JINView.setBackgroundResource(R.drawable.video);
					mSurfaceView.setBackgroundResource(R.drawable.video);
					if(message.getData().getString("show")!=null)
						Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(PadActivity.this, "�����˳�����", Toast.LENGTH_LONG).show();
					processQuitConf();	
					break;
			 /*****************************************/		
				case 0x1238: // ������Ϣ�и���
					if(message.getData().getString("show")!=null)
						Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_LONG).show();					

					if (confLV != null && confLV.getAdapter() != null)
						// ֪ͨadapter������Ϣ����Դ����
						((BaseAdapter) confLV.getAdapter()).notifyDataSetChanged();
					
					break;
				case 0x1239: //�����Ա�и���
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
					// ֪ͨadapter�λ���Ա����Դ����
					if (participantLV != null && participantLV.getAdapter() != null)
						((BaseAdapter) participantLV.getAdapter()).notifyDataSetChanged();
					break;
				case 0x1240:
					Toast.makeText(PadActivity.this, message.getData().getString("show"), Toast.LENGTH_SHORT).show();
					if(message.getData().getString("show").contains("��ҿ������뷢��")){
						speakable = true;						
					}
					else if (message.getData().getString("show").contains("����ʱ���ѵ���������������")){
						speakable = false;						
					}
					System.out.println("case 0x1240 "+message.getData().getString("show"));
					break;
				case 0x2100:					
					Toast.makeText(PadActivity.this, "��Ϣ����ʧ��", Toast.LENGTH_LONG).show();
					break;
				case 0x2101:	            	
	            //	Toast.makeText(TestActivity.this, "��Ϣ���ͳɹ�", Toast.LENGTH_LONG).show();
					
					
					break;
				case 0x2102:
					Toast.makeText(PadActivity.this, "�˳���¼�ɹ�", Toast.LENGTH_LONG).show();
					break;
				case 0x2103:
					Toast.makeText(PadActivity.this, "�˳���¼ʧ��", Toast.LENGTH_LONG).show();
					break;
			}	
		}
	 };
	
	//�������
	boolean debugLxj = true;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		mGL2JINView = getRomotePreview();
		mGL2JINView.setBackgroundResource(R.drawable.video);// ����֮ǰ�б���ͼ
		mSurfaceView.setBackgroundResource(R.drawable.video);
		
		// ������Ƶ��ʾ����Ŀ�͸�
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		int diswidth = (int) width / 3;
		//int disheight = (int) height / 3;
		int disheight = diswidth*3/4;
		mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(diswidth,disheight));

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
															
		// �����̣߳�ÿ2���ȡһ�λ�����Ϣ���½��
		timer.schedule(task, 1000, 2000);
		// �÷����Ĳ����������û����������⣬��������ò��������ļ����õ���ʽ��
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), Constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				Constants.registarIp.getBytes(), Constants.registarPort);

		new RegisterThread(sipTerm).start();
		
		//��ȡ�û�����Ϣ
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
        //����slding menu�ļ�������ģʽ
        //TOUCHMODE_FULLSCREEN ȫ��ģʽ����contentҳ���У����������Դ�sliding menu
        //TOUCHMODE_MARGIN ��Եģʽ����contentҳ���У�������slding ,����Ҫ����Ļ��Ե�����ſ��Դ�slding menu
        //TOUCHMODE_NONE ��Ȼ�ǲ���ͨ�����ƴ���
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        //ʹ�����Ϸ�icon�ɵ㣬������onOptionsItemSelected����ſ��Լ�����R.id.home
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
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK){	//��Ȼ����д�����ǲ��񲻵�home��
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
					.setTitle("�˳�ϵͳ")
					.setMessage("���ڻ����У���ȷ���Ƿ�Ҫ�˳�ϵͳ��")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// �����ȷ�ϡ���Ĳ������˳�ϵ�y����ز���
									quitConf();
									timer.cancel();
									if (sipTerm > 0)
										SipTerm.deleteTerm(sipTerm);
									logout();
									finish();
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// ��������ء���Ĳ���,���ﲻ����û���κβ���
								}
							}).show();
		} else {
			new AlertDialog.Builder(new ContextThemeWrapper(this,
					R.style.AlertDialogCustom))
					.setTitle("ȷ���˳���")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// �����ȷ�ϡ���Ĳ������˳�ϵ�y����ز���
									timer.cancel();
									if (sipTerm > 0)
										SipTerm.deleteTerm(sipTerm);
									logout();
									finish();
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// ��������ء���Ĳ���,���ﲻ����û���κβ���
								}
							}).show();
		}
	}
	/**
	 * ʵ�ָ���ĳ��󷽷�����ȡ����Ԥ�����湹����
	 * @return ������ƵԤ������
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
	 * ʵ�ָ���ĳ��󷽷�������������Activity�Ĳ��֡��ڱ������������setContentView(getLayoutId());
	 * @return ���沼�����ID
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
			
		// ������Ϣ,��Ա��Ϣ���
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
		if(debugLxj) System.out.println("�˳��������");
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
            //toggle���ǳ����Զ��ж��Ǵ򿪻��ǹر�
            toggle();
            return true;
        case R.id.action_show_conf:
			if (confId == -1) {
				Toast.makeText(PadActivity.this, "����û�м�����飬�޷��鿴������Ϣ",
						Toast.LENGTH_LONG).show();
				break;
			}
			AlertDialog.Builder builder1 = new AlertDialog.Builder(PadActivity.this);
			builder1.setInverseBackgroundForced(true);
			builder1.setTitle("������Ϣ�б�");
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
				Toast.makeText(PadActivity.this, "����û�м�����飬�޷��鿴�λ���Ա",
						Toast.LENGTH_LONG).show();
				break;
			}
			AlertDialog.Builder builder2 = new AlertDialog.Builder(PadActivity.this);
			builder2.setInverseBackgroundForced(true);
			builder2.setTitle("�λ���Ա�б�");
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
				Toast.makeText(PadActivity.this, "����û�м�����飬�޷��˳�����",
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