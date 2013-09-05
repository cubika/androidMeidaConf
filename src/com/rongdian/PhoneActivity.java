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
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.AccountManageActivity;
import com.activity.ConfManageActivity;
import com.activity.ContactManageActivity;
import com.activity.CreateConfActivity;
import com.activity.GenericTask;
import com.http.DisconnectUserThread;
import com.http.EndConfThread;
import com.http.GetUserInfoTask;
import com.http.HttpUtils;
import com.http.LogoutThread;
import com.http.RegisterThread;
import com.util.Constants;

/*
 * 1.�Ĳ˵�
 * 2.��ע�����ɹ�������
 * 3.����Ϣ����ʾ����json��Ϣת��Ϊ���ģ��Լ���Щ��ʱ����Ҫ�ӽ�����ʾ�ȵ�
 */
public class PhoneActivity extends RtpAvTermAndroidActivity {

	// sip���
	private SipTermListenerImpl listener; // ����Sip�¼����ڸü������з���msg����UI�̴߳���
	long sipTerm; // sip�նˣ���������ͬ����Activity

	// UI���
	AlertDialog menuDialog; // menu�˵�dialog
	GridView menuGrid; // ���ò˵����ݵ�gridView
	View menuView;
	public static String HostName;
	public static ListView confLV, participantLV;// ������Ϣ�Ͳλ���Ա��Listview
	// ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String,
	// String>>();
	public Constants constants;
	public static List<String> participantNames = new ArrayList<String>();
	public static List<String> participantIDs = new ArrayList<String>();

	public static ListView mDrawerList;// ��ർ��
	private Bundle saveMediaBundle; //���������Ա����ٴ�JoinProcessOK

	// ý�����
	long mediaTerm; // ý���նˣ���������ͬһ������
	SurfaceView mSurfaceView = null; // ��ʾ������Ƶ�������
	GL2JNIView mGL2JINView = null; // ��ʾԶ����Ƶ�������
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// �û��ͻ����ʶ
	Integer confId = -1;
	String userId = "-1";
	boolean speakable;
	String confName = null, confSubject = null, confDuration = null,
			chairmanId = null, chairmanName = null;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			/****************** SipTermListener��sip�¼��������� ***********************/
			case 0x1233: // ע��ɹ�
				System.out.println("Registerok sipע��ɹ�  ");
				processRegisterOK();
				break;
			case 0x1234: // ע��ʧ��
				processRegisterFailed();
				break;
			case 0x1235: // �������壬������������
				processLocalRing();
				break;
			case 0x1236: // �ɹ��������
				mGL2JINView.setBackgroundResource(0);
				mSurfaceView.setBackgroundResource(0);
				saveMediaBundle=message.getData();
				processJoinConfOK(saveMediaBundle);
				break;
			case 0x1237: // �˳�����
				mGL2JINView.setBackgroundResource(R.drawable.video);
				mSurfaceView.setBackgroundResource(R.drawable.video);
				if (message.getData().getString("show") != null)
					Toast.makeText(PhoneActivity.this,
							message.getData().getString("show"),
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(PhoneActivity.this, "�����˳�����",
							Toast.LENGTH_LONG).show();
				processQuitConf();
				break;
			/*****************************************/
			case 0x1238: // ������Ϣ�и���
				if (message.getData().getString("show") != null)
					Toast.makeText(PhoneActivity.this,
							message.getData().getString("show"),
							Toast.LENGTH_LONG).show();

				if (confLV != null && confLV.getAdapter() != null)
					// ֪ͨadapter������Ϣ����Դ����
					((BaseAdapter) confLV.getAdapter()).notifyDataSetChanged();

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
					JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
					String userName = null, userId = null;
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
					((BaseAdapter) participantLV.getAdapter())
							.notifyDataSetChanged();
				break;
			case 0x1240:
				Toast.makeText(PhoneActivity.this,
						message.getData().getString("show"), Toast.LENGTH_LONG)
						.show();
				if (message.getData().getString("show").contains("��ҿ������뷢��")) {
					speakable = true;
				} else if (message.getData().getString("show")
						.contains("����ʱ���ѵ���������������")) {
					speakable = false;
				}
				System.out.println("case 0x1240 "
						+ message.getData().getString("show"));
				break;
			case 0x2100:
				Toast.makeText(PhoneActivity.this, "��Ϣ����ʧ��", Toast.LENGTH_LONG)
						.show();
				break;
			case 0x2101:
				// Toast.makeText(MediaConfActivity.this, "��Ϣ���ͳɹ�",
				// Toast.LENGTH_LONG).show();

				break;
			case 0x2102:
				Toast.makeText(PhoneActivity.this, "�˳���¼�ɹ�", Toast.LENGTH_LONG).show();
				break;
			case 0x2103:
				Toast.makeText(PhoneActivity.this, "�˳���¼ʧ��", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	Timer timer = new Timer();
	// ��ѯ��ȡ�������
	TimerTask timertask = new TimerTask() {
		@Override
		public void run() {
			String url = "http://" + Constants.registarIp
					+ ":8888/MediaConf/clientRefresh.do?";
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

				for (int u = 0; u < messages.length; u++) {
					int updateIndex = messages[u].indexOf("update");
					int showIndex = messages[u].indexOf("show");
					int changeChairmanIndex = messages[u]
							.indexOf("changeChairman:");
					int confInfoIndex = messages[u].indexOf("confInfo");
					int callIndex = messages[u].indexOf("call");
					int changeIndex = messages[u].indexOf("change:");
					int afterChangeIndex = messages[u].indexOf("afterChange");
					int chairmainupdateIndex = messages[u]
							.indexOf("chairmainUpdate");
					int chairmanShowIndex = messages[u].indexOf("chairmanShow");

					if (chairmainupdateIndex >= 0) {
						String[] twoSub = messages[u].split(":");
						if (confId == -1) {
							confId = Integer.valueOf(twoSub[1]);
						}
					}

					// ˢ�������Ա
					if (updateIndex >= 0) {
						String[] twoSubString = messages[u].split(":");
						// String func=twoSubString[2];
						if (confId == -1) {
							confId = Integer.valueOf(twoSubString[1]);
						}
						Map<String, String> params = new HashMap<String, String>();
						params.put("confId", confId.toString());
						String particpantUrl = "http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/conference/conferenceinfo.do?method=getCurrentParticipateListFormain";
						String participants;
						try {
							participants = HttpUtils.sendPostMessage(
									particpantUrl, params, "utf-8");
							data.putString("paticipants", participants);
							Log.v("refreshData", "participants:" + participants);
						} catch (IOException e) {
							Log.e("refreshData", "get participants error");
							e.printStackTrace();
						}
						msg.what = 0x1239;
					}
					// ������ʾ��
					if (showIndex >= 0) {
						msg.what = 0x1238;
						data.putString("show", messages[u].split(":")[1]);
					}
					if (changeChairmanIndex >= 0) {
						msg.what = 0x1238;
						data.putString("show", messages[u].split(":")[2]);
					}

					// ���»�����Ϣ
					if (confInfoIndex >= 0) {
						confName = messages[u].split(":")[1];
						confSubject = messages[u].split(":")[2];
						confDuration = messages[u].split(":")[3];
						chairmanId = messages[u].split(":")[4];
						chairmanName = messages[u].split(":")[5];
						Log.i("refreshData", "confMessageUpdated");
						msg.what = 0x1238;
					}

					if (callIndex >= 0) {
						String[] subString = messages[u].split(":");
						String[] userInfo = subString[1].split(";");
						msg.what = 0x1240;
						data.putString("show", userInfo[1]);
					}

					if (changeIndex >= 0) {
						String[] subString = messages[u].split(":");
						String[] changeInfo = subString[1].split(";");
					}
					// ����������ϯ
					if (afterChangeIndex >= 0) {
						String[] subString = messages[u].split(":");
						chairmanId = subString[2];
						chairmanName = subString[1];
						msg.what = 0x1238;
						data.putString("show", subString[3]);
					}

					if (chairmanShowIndex >= 0) {
						confId = -1;
						msg.what = 0x1237;
						data.putString("show", "�����ѽ���");
					}
				}
				msg.setData(data);
				// ����Ϣ���͵���Ϣ����
				handler.sendMessage(msg);
			}
		}
	};

	// �������
	boolean debugLxj = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGL2JINView = getRomotePreview();
		mGL2JINView.setBackgroundResource(R.drawable.video);// ����֮ǰ�б���ͼ
		mSurfaceView.setBackgroundResource(R.drawable.video);

		// ������Ƶ��ʾ����Ŀ��͸�
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		int diswidth = (int) width / 3;
		int disheight = (int) height / 3;
		mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(diswidth,
				disheight));

		// �˵��������ͼ
		menuView = View.inflate(this, R.layout.gridview_menu, null);
		// ����AlertDialog
		menuDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,
				android.R.style.Theme_Translucent_NoTitleBar))
				.setInverseBackgroundForced(true).create();
		//���öԻ����λ��Ϊ������ 
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(menuDialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity=Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
		menuDialog.getWindow().setAttributes(lp);

		menuDialog.setView(menuView);
		// ������˲˵�������ȡ���˵�
		menuDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)// ��������
					dialog.dismiss();
				return false;
			}
		});

		Resources res = getResources();
		String[] menuNames = res.getStringArray(R.array.items);
		int[] menuImages = { R.drawable.create_conf, R.drawable.conf_manage, R.drawable.conf_info,
				R.drawable.participant, R.drawable.account,
				R.drawable.contact, R.drawable.doc,R.drawable.quit_conf,
				R.drawable.exit_app };

		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		// Ϊ�˵����ݰ�����
		menuGrid.setAdapter(getMenuAdapter(menuNames, menuImages));
		/** ����menuѡ�� **/
		menuGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				menuDialog.dismiss();
				switch (arg2) {
				case 0: // ��������
					Intent intent = new Intent(PhoneActivity.this,
							CreateConfActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("userID", userId);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				case 1: //�������
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "����û�м�����飬�޷����л������",
								Toast.LENGTH_LONG).show();
						break;
					}
					if(chairmanId.trim().equals(userId)){ //���û�Ϊ��ϯ
						Intent cmIntent=new Intent(PhoneActivity.this,ConfManageActivity.class);
						Bundle bundle2 = new Bundle();
						bundle2.putString("userId", userId);
						bundle2.putString("confId", confId.toString());
						cmIntent.putExtras(bundle2);
						System.out.println("��ʼ��������������");
						startActivity(cmIntent);
					}else{
						System.out.println("������ϯ��"+chairmanId+" userId:"+userId);
						AlertDialog.Builder builder = new AlertDialog.Builder(PhoneActivity.this);
						AlertDialog dialog=
								builder.setTitle("���뷢��").setMessage("��������ϯ��ֻ�����뷢�ԡ�\n ��ȷ��Ҫ���뷢����")
								.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									GenericTask applyTask=new GenericTask(PhoneActivity.this);
									applyTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/applySpeaking.do?confId="
											+confId+"&userId="+userId);
									dialog.dismiss();
									
								}
								
							}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
								
							}).show();
						TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
						messageView.setGravity(Gravity.CENTER);
					}
//					AlertDialog.Builder screenBuilder=new AlertDialog.Builder(PhoneActivity.this);
//					screenBuilder.setTitle("���÷���");
//					String[] screens={"3�ŷ���","4�ŷ���","5�ŷ���","6�ŷ���"};
//					String[] screenUsers={
//					final int screenChoice=-1;
//					screenBuilder.setSingleChoiceItems(screens, 0, new DialogInterface.OnClickListener() {
//
//			            @Override
//			            public void onClick(DialogInterface dialog,
//			                int which) {
//			            	screenChoice=which;
//			            }
//			        });
//
//					SetScreenThread screenThread=new SetScreenThread(handler,screenChoice);
//					Map<String,String> screenParams=new HashMap<String,String>();
//					speakParams.put("confId", confId.toString());
//					speakParams.put("userId",userId);
//					speakThread.doStart(speakParams);
					break;
				case 2: // �鿴������Ϣ
					Log.v("menuClick", confId + ":" + confName);
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "����û�м�����飬�޷��鿴������Ϣ",
								Toast.LENGTH_LONG).show();
						break;
					}
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PhoneActivity.this);
					builder1.setInverseBackgroundForced(true);
					builder1.setTitle("������Ϣ�б�");
					View confMessageView = View.inflate(PhoneActivity.this,
							R.layout.conf_message_dialog, null);
					builder1.setView(confMessageView);
					confLV = (ListView) confMessageView
							.findViewById(R.id.confLV);
					String[] confMessageTitles = getResources().getStringArray(
							R.array.confMessages);
					String[] confMessageContents = { confName, confSubject,
							confDuration, chairmanName };
					confLV.setAdapter(getMessageAdapter(confMessageTitles,
							confMessageContents));
					builder1.show();
					break;
				case 3: // �鿴�����Ա��Ϣ
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "����û�м�����飬�޷��鿴�λ���Ա",
								Toast.LENGTH_LONG).show();
						break;
					}
					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							PhoneActivity.this);
					builder2.setInverseBackgroundForced(true);
					builder2.setTitle("�λ���Ա�б�");
					View participantMessageView = View.inflate(
							PhoneActivity.this,
							R.layout.participant_message_dialog, null);
					builder2.setView(participantMessageView);
					participantLV = (ListView) participantMessageView
							.findViewById(R.id.participantLV);
					participantLV.setAdapter(getMessageAdapter(
							participantNames.toArray(new String[] {}),
							participantIDs.toArray(new String[] {})));
					builder2.show();
					break;
				case 4: // �˻�����
					Intent accountIntent = new Intent(PhoneActivity.this,AccountManageActivity.class);
					Bundle accountBundle = new Bundle();
					accountBundle.putString("userID", userId);
					accountIntent.putExtras(accountBundle);
					startActivity(accountIntent);
					break;
				case 5: // ��ϵ�˹���
					Intent contactIntent = new Intent(PhoneActivity.this,ContactManageActivity.class);
					Bundle contactBundle = new Bundle();
					contactBundle.putString("userID", userId);
					contactIntent.putExtras(contactBundle);
					startActivity(contactIntent);
					break;
				case 6: // ���ݹ���
					Toast.makeText(PhoneActivity.this, "�����ڴ�������", Toast.LENGTH_SHORT).show();
					break;
				case 7: //�˳�����
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "����û�м�����飬�޷��˳�����",
								Toast.LENGTH_LONG).show();
						break;
					}
					EndConfThread endThread=new EndConfThread(handler);
					Map<String,String> endParams=new HashMap<String,String>();
					endParams.put("confId", confId.toString());
					endThread.doStart(endParams);
					break;
				case 8: //�˳�����
					quitApp();
					break;
				}

			}

		});

		listener = new SipTermListenerImpl(handler);// ����Sip�¼����ڸü������з���msg����UI�̴߳���

		// ��ȡ��¼������û�����������û�����Ϣ
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		String myAccount = data.getString("myAccount");
		HostName = myAccount;
		String passWord = data.getString("password");
		userId = data.getString("userId");
		System.out.println("get Account:" + HostName + "@" + userId);
		constants = new Constants(myAccount.getBytes(), passWord.getBytes());

		// �����̣߳�ÿ2���ȡһ�λ�����Ϣ���½��
		timer.schedule(timertask, 1000, 2000);
		// �÷����Ĳ����������û����������⣬��������ò��������ļ����õ���ʽ��
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), Constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				Constants.registarIp.getBytes(), Constants.registarPort);

		// ����ע���̣߳�����ע��
		new RegisterThread(sipTerm).start();
		
		//��ȡ�û�����Ϣ
		new GetUserInfoTask().execute("http://"+ Constants.registarIp+":8888/MediaConf/user/userInfo1.jsp");
		
		
	}

	public void quitConf() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("confId", confId.toString());
		params.put("userId", userId);
		DisconnectUserThread disconnectUserThread = new DisconnectUserThread(
				handler);
		// 10.0.2.2Ϊ���Զ���ģ�������Ե�IP��ַ��
		disconnectUserThread.doStart("http://" + Constants.registarIp
				+ ":8888/MediaConf/disconnectUser.do?method=leave", params,
				PhoneActivity.this);
	}

	public void logout() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("confId", confId.toString());
		params.put("userId", userId);
		System.out.println("logout_phoneActivity");
		LogoutThread logoutthread = new LogoutThread(handler);
		logoutthread.doStart();
	}

	// @Override
	// public void onAttachedToWindow()
	// {
	// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	// super.onAttachedToWindow();
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME
				|| keyCode == KeyEvent.KEYCODE_BACK) {
			quitApp();
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
	 * 
	 * @return ������ƵԤ������
	 */
	@Override
	public SurfaceView getLocalPreview() {
		mSurfaceView = (SurfaceView) findViewById(R.id.localVideoV);
		Log.i("PhoneActivity","Set Local Video Preview");
		return mSurfaceView;
	}

	@Override
	protected GL2JNIView getRomotePreview() {
		//Log.i("PhoneActivity","Set Remote Video Preview");
		return (GL2JNIView) findViewById(R.id.remoteVideoV);
	}

	/**
	 * ʵ�ָ���ĳ��󷽷�������������Activity�Ĳ��֡��ڱ������������setContentView(getLayoutId());
	 * 
	 * @return ���沼�����ID
	 */
	@Override
	public int getLayoutId() {
		// return R.layout.activity_media_conf;
		return R.layout.phone_layout;
	}

	@Override
	public void onRecvAudio(long term, byte[] pcmData, int size) {
		super.onRecvAudio(term, pcmData, size);
		// ravtPutLocalAudio(term, pcmData);
		// ravtOpenRemoteAudio(mediaTerm, (short)0);
		// if(debugLxj) System.out.println("leisure" + "recvAudio false");
	}

	@Override
	public void onRecvVideo(long term) {
		super.onRecvVideo(term);
	}

	/**
	 * ע��ɹ����¼�������������Ҫ���ڽ���UI������£������䣩 ���⻹��Ҫ���ö�ʱ������ʱ����ע�ᣬ��֤ע��������ϵ�ע��ɹ���Ϣ������
	 */
	private void processRegisterOK() {
		if (debugLxj)
			System.out.println("ע��ɹ�");
	}

	/**
	 * ע��ʧ�ܵ��¼������������쳣����
	 */
	private void processRegisterFailed() {
		// TODO Auto-generated method stub

	}

	/**
	 * ���յ�INVITE����������ʱ���¼�����
	 */
	private void processLocalRing() {

		if (debugLxj)
			System.out.println("����");

		ravtCreateTerm(); // ����ý�崦��Terminal

		// ������Ƶ�Ự
		boolean audioFlag = ravtOpenAudioSession("0.0.0.0".getBytes(),
				Constants.sdpLocalAudioPort);
		boolean videoFlag = ravtOpenVideoSession("0.0.0.0".getBytes(),
				Constants.sdpLocalVideoPort);

		if (debugLxj)
			System.out.println("������Ƶ�ĻỰ�������Ϊ��" + audioFlag + "," + videoFlag);

	}

	/**
	 * �������ɹ�����ȡý��Э�̽������ʼ�����������Ƶ������ʾ��������ƵԤ��
	 */
	private void processJoinConfOK(Bundle mediaInfo) {
		if (debugLxj)
			System.out.println("�ɹ�������飬��ʼ�������Ƶ");

		// ��Ƶ����
		System.out.println("remoteAudioPayload = "
				+ mediaInfo.getByte("remoteAudioPayload"));
		if (ravtSetAudioSessionOutputPayloadType(mediaInfo
				.getByte("remoteAudioPayload"))) {
			System.out.println("ravtSetAudioSessionOutputPayloadType = true");
		} else {
			System.out.println("ravtSetAudioSessionOutputPayloadType = false");
		}
		if (ravtSetAudioSessionRemoteAddr(
				mediaInfo.getByteArray("remoteAudioAddr"),
				mediaInfo.getInt("remoteAudioPort"))) {
			System.out.println("ravtSetAudioSessionRemoteAddr = true");
		} else {
			System.out.println("ravtSetAudioSessionRemoteAddr = false");
		}

		if (ravtOpenLocalAudio(mediaInfo.getShort("remoteAudioType"))) {
			System.out.println("ravtOpenLocalAudio = true");
		} else {
			System.out.println("ravtOpenLocalAudio = false");
		}
		if (ravtOpenRemoteAudio(mediaInfo.getShort("remoteAudioType"))) {
			System.out.println("ravtOpenRemoteAudio = true");
		} else {
			System.out.println("ravtOpenRemoteAudio = false");
		}

		// ��Ƶ����
		if (!ravtSetVideoSessionOutputPayloadType(mediaInfo
				.getByte("remoteVideoPayload"))) {// (byte)109)) {
			if (debugLxj)
				System.out.println("��Ƶ�������ʹ���");
			return;
		}
		if (debugLxj)
			System.out.println("��Ƶ������������");

		int videoPort = mediaInfo.getInt("remoteVideoPort");
		byte[] videoAddr = mediaInfo.getByteArray("remoteVideoAddr");
		if (debugLxj)
			System.out.println("videoPort = " + videoPort);
		if (!ravtSetVideoSessionRemoteAddr(
				mediaInfo.getByteArray("remoteVideoAddr"),
				mediaInfo.getInt("remoteVideoPort"))) {
			if (debugLxj)
				System.out.println("Զ�˵�ַ���ô���");
			return;
		}
		if (debugLxj)
			System.out.println("Զ�˵�ַ��������");

		if (!ravtOpenLocalVideoOutput(RtpAvTerm.RAVT_CODEC_H264,
				localVideoWidth, localVideoHeight,
				mediaInfo.getLong("videoByteRate"), localVideoFrameRate,
				localVideoFrameRate * 3, 1024)) {
			if (debugLxj)
				System.out.println("������Ƶ�������");
			return;
		}
		if (debugLxj)
			System.out.println("������Ƶ�������");

		// �򿪱�����ƵԤ����ע������Ԥ�����ܲɼ���Ƶ�����ϴ���ý���������
		// ���������Ƶ������ȥ��һ��preview,һ��output
		// ������Ŀ���ֻ����preview�������ƣ�
		boolean localResult=ravtOpenLocalVideoPreview(localVideoWidth, localVideoHeight);
		if(localResult)
			System.out.println("ravtOpenLocalVideoPreview success��");
		else
			System.out.println("ravtOpenLocalVideoPreview failed��");

		System.out.println("remoteVideoPayload = "
				+ mediaInfo.getByte("remoteVideoPayload"));
		
		boolean res = ravtOpenRemoteVideoPreview(mediaInfo
				.getShort("remoteVideoType"));
		if (res)
			System.out.println("ravtOpenRemoteVideoPreview success��");
		else
			System.out.println("ravtOpenRemoteVideoPreview failed��");

		// ��������I-Frame�Ĳ���������
		long term = mediaInfo.getLong("term");
		long callId = mediaInfo.getLong("callId");
		SipTerm.requestKeyFrame(term, callId);
	}

	/**
	 * ���������ֹͣ��������Ƶ�ɼ���������ͷ���Դ
	 */
	private void processQuitConf() {
		if (debugLxj)
			System.out.println("�˳�����");

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
		if (debugLxj)
			System.out.println("�˳��������");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("onDestory");
		timer.cancel();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}

	// �˵�������
	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.menu_item_image, R.id.menu_item_text });
		return simperAdapter;
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

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menuDialog == null) {
			menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		} else {
			menuDialog.show();
		}
		return false;// ����Ϊtrue ����ʾϵͳmenu
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
}