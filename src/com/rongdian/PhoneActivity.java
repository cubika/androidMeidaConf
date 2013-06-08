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
import android.app.AlertDialog.Builder;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.activity.ConfManageActivity;
import com.activity.CreateConfActivity;
import com.http.DisconnectUserThread;
import com.http.EndConfThread;
import com.http.HttpUtils;
import com.http.LogoutThread;
import com.http.RegisterThread;
import com.util.Constants;

/*
 * 1.解决注销的问题 #
 * 2.菜单按键响应
 * 3.销毁资源
 * 4.必须进来之后再建会
 * 5.listview可不可以复用？
 * 6.alertDialog太宽了
 */
public class PhoneActivity extends RtpAvTermAndroidActivity {

	// sip相关
	private SipTermListenerImpl listener; // 监听Sip事件，在该监听器中发送msg给本UI线程处理
	long sipTerm; // sip终端，生命周期同整个Activity

	// UI组件
	AlertDialog menuDialog; // menu菜单dialog
	GridView menuGrid; // 放置菜单内容的gridView
	View menuView;
	public static String UserName;
	public static ListView confLV, participantLV;// 会议信息和参会人员的Listview
	// ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String,
	// String>>();
	public Constants constants;
	public static List<String> participantNames = new ArrayList<String>();
	public static List<String> participantIDs = new ArrayList<String>();

	public static ListView mDrawerList;// 左侧导航
	private Bundle saveMediaBundle; //保存起来以便于再次JoinProcessOK

	// 媒体相关
	long mediaTerm; // 媒体终端，生命周期同一个会议
	SurfaceView mSurfaceView = null; // 显示本地视频界面组件
	GL2JNIView mGL2JINView = null; // 显示远端视频界面组件
	long localVideoWidth = 352;
	long localVideoHeight = 288;
	long localVideoFrameRate = 15;

	// 用户和会议标识
	Integer confId = -1;
	String userId = "-1";
	boolean speakable;
	String confName = null, confSubject = null, confDuration = null,
			chairmanId = null, chairmanName = null;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			/****************** SipTermListener的sip事件处理函数 ***********************/
			case 0x1233: // 注册成功
				System.out.println("Registerok sip注册成功  ");
				processRegisterOK();
				break;
			case 0x1234: // 注册失败
				processRegisterFailed();
				break;
			case 0x1235: // 本地振铃，被邀请加入会议
				processLocalRing();
				break;
			case 0x1236: // 成功加入会议
				mGL2JINView.setBackgroundResource(0);
				mSurfaceView.setBackgroundResource(0);
				saveMediaBundle=message.getData();
				processJoinConfOK(saveMediaBundle);
				break;
			case 0x1237: // 退出会议
				mGL2JINView.setBackgroundResource(R.drawable.video);
				mSurfaceView.setBackgroundResource(R.drawable.video);
				if (message.getData().getString("show") != null)
					Toast.makeText(PhoneActivity.this,
							message.getData().getString("show"),
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(PhoneActivity.this, "您已退出会议",
							Toast.LENGTH_LONG).show();
				processQuitConf();
				break;
			/*****************************************/
			case 0x1238: // 会议信息有更新
				if (message.getData().getString("show") != null)
					Toast.makeText(PhoneActivity.this,
							message.getData().getString("show"),
							Toast.LENGTH_LONG).show();

				if (confLV != null && confLV.getAdapter() != null)
					// 通知adapter会议信息数据源更新
					((BaseAdapter) confLV.getAdapter()).notifyDataSetChanged();

				participantNames.clear();
				participantIDs.clear();
				JSONObject jsonObject;
				JSONArray jsonArray = null;
				Log.v("handler","participants:"	+ message.getData().getString("paticipants"));
				try {
					jsonObject = new JSONObject(message.getData().getString("paticipants"));
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
				// 通知adapter参会人员数据源更新
				if (participantLV != null && participantLV.getAdapter() != null)
					((BaseAdapter) participantLV.getAdapter())
							.notifyDataSetChanged();
				break;
			case 0x1240:
				Toast.makeText(PhoneActivity.this,
						message.getData().getString("show"), Toast.LENGTH_LONG)
						.show();
				if (message.getData().getString("show").contains("大家可以申请发言")) {
					speakable = true;
				} else if (message.getData().getString("show")
						.contains("申请时间已到，不能再申请了")) {
					speakable = false;
				}
				System.out.println("case 0x1240 "
						+ message.getData().getString("show"));
				break;
			case 0x2100:
				Toast.makeText(PhoneActivity.this, "消息发送失败", Toast.LENGTH_LONG)
						.show();
				break;
			case 0x2101:
				// Toast.makeText(MediaConfActivity.this, "消息发送成功",
				// Toast.LENGTH_LONG).show();

				break;
			case 0x2102:
				Toast.makeText(PhoneActivity.this, "退出登录成功", Toast.LENGTH_LONG).show();
				break;
			case 0x2103:
				Toast.makeText(PhoneActivity.this, "退出登录失败", Toast.LENGTH_LONG).show();
				break;
			case 0x2201:
				Toast.makeText(PhoneActivity.this, "结束会议成功", Toast.LENGTH_LONG).show();
				break;
			case 0x2202:
				Toast.makeText(PhoneActivity.this, "结束会议失败", Toast.LENGTH_LONG).show();
				break;
			case 0x2203:
				Toast.makeText(PhoneActivity.this, "申请发言成功", Toast.LENGTH_LONG).show();
				break;
			case 0x2204:
				Toast.makeText(PhoneActivity.this, "申请发言失败", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	Timer timer = new Timer();
	// 轮询获取会议参数
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

					// 刷新与会人员
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
						msg.what = 0x1238;
					}
					// 弹出提示框
					if (showIndex >= 0) {
						msg.what = 0x1238;
						data.putString("show", messages[u].split(":")[1]);
					}
					if (changeChairmanIndex >= 0) {
						msg.what = 0x1238;
						data.putString("show", messages[u].split(":")[2]);
					}

					// 更新会议信息
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
					// 更换会议主席
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
						data.putString("show", "会议已结束");
					}
				}
				msg.setData(data);
				// 将消息发送到消息队列
				handler.sendMessage(msg);
			}
		}
	};

	// 调试相关
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
		int disheight = (int) height / 3;
		mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(diswidth,
				disheight));

		// 菜单窗体的视图
		menuView = View.inflate(this, R.layout.gridview_menu, null);
		// 创建AlertDialog
		menuDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,
				android.R.style.Theme_Translucent_NoTitleBar))
				.setInverseBackgroundForced(true).create();
		menuDialog.setView(menuView);
		// 如果按了菜单键，就取消菜单
		menuDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)// 监听按键
					dialog.dismiss();
				return false;
			}
		});

		Resources res = getResources();
		String[] menuNames = res.getStringArray(R.array.items);
		int[] menuImages = { R.drawable.ic_launcher, R.drawable.ic_launcher,
				R.drawable.ic_launcher, R.drawable.ic_launcher,
				R.drawable.ic_launcher, R.drawable.ic_launcher,
				R.drawable.ic_launcher, R.drawable.ic_launcher,
				R.drawable.ic_launcher };

		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		// 为菜单内容绑定数据
		menuGrid.setAdapter(getMenuAdapter(menuNames, menuImages));
		/** 监听menu选项 **/
		menuGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				menuDialog.dismiss();
				switch (arg2) {
				case 0: // 创建会议
					Intent intent = new Intent(PhoneActivity.this,
							CreateConfActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("userID", userId);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				case 1: //退出会议
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "您还没有加入会议，无法退出会议",
								Toast.LENGTH_LONG).show();
						break;
					}
					EndConfThread endThread=new EndConfThread(handler);
					Map<String,String> endParams=new HashMap<String,String>();
					endParams.put("confId", confId.toString());
					endThread.doStart(endParams);
					break;
				case 2: //会议管理
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "您还没有加入会议，无法进行会议管理",
								Toast.LENGTH_LONG).show();
						break;
					}
					if(chairmanId.trim().equals(userId)){ //该用户为主席
						Intent cmIntent=new Intent(PhoneActivity.this,ConfManageActivity.class);
						Bundle bundle2 = new Bundle();
						bundle2.putString("userId", userId);
						bundle2.putString("confId", confId.toString());
						cmIntent.putExtras(bundle2);
						System.out.println("开始进入会议管理界面：");
						startActivity(cmIntent);
					}else{
						System.out.println("会议主席："+chairmanId+" userId:"+userId);
					}
//					AlertDialog.Builder screenBuilder=new AlertDialog.Builder(PhoneActivity.this);
//					screenBuilder.setTitle("设置分屏");
//					String[] screens={"3号分屏","4号分屏","5号分屏","6号分屏"};
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
				case 3: //呼起用户
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "您还没有加入会议，无法呼起用户",
								Toast.LENGTH_LONG).show();
						break;
					}
					if(chairmanId!=userId){
						Toast.makeText(PhoneActivity.this, "您不是会议主席，不能呼起用户",
								Toast.LENGTH_LONG).show();
						break;
					}
					break;
				case 4: // 查看会议信息
					Log.v("menuClick", confId + ":" + confName);
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "您还没有加入会议，无法查看会议信息",
								Toast.LENGTH_LONG).show();
						break;
					}
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PhoneActivity.this);
					builder1.setInverseBackgroundForced(true);
					builder1.setTitle("会议信息列表");
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
					builder1.create().show();
					break;
				case 5: // 查看与会人员信息
					if (confId == -1) {
						Toast.makeText(PhoneActivity.this, "您还没有加入会议，无法查看参会人员",
								Toast.LENGTH_LONG).show();
						break;
					}
					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							PhoneActivity.this);
					builder2.setInverseBackgroundForced(true);
					builder2.setTitle("参会人员列表");
					View participantMessageView = View.inflate(
							PhoneActivity.this,
							R.layout.participant_message_dialog, null);
					builder2.setView(participantMessageView);
					participantLV = (ListView) participantMessageView
							.findViewById(R.id.participantLV);
					participantLV.setAdapter(getMessageAdapter(
							participantNames.toArray(new String[] {}),
							participantIDs.toArray(new String[] {})));
					builder2.create().show();
					break;
				case 6: // 账户管理
					break;
				case 7: // 联系人管理
					break;
				case 8: //退出程序
				}

			}

		});

		listener = new SipTermListenerImpl(handler);// 监听Sip事件，在该监听器中发送msg给本UI线程处理

		// 获取登录界面的用户名、密码等用户的信息
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		String myAccount = data.getString("myAccount");
		UserName = myAccount;
		String passWord = data.getString("password");
		userId = data.getString("userId");
		System.out.println("get Account:" + UserName + "@" + userId);
		constants = new Constants(myAccount.getBytes(), passWord.getBytes());

		// 启动线程，每2秒获取一次会议信息更新结果
		timer.schedule(timertask, 1000, 2000);
		// 该方法的参数，除了用户名和密码外，其它的最好采用配置文件配置的形式。
		sipTerm = SipTerm.createTerm(listener, constants.getMyAccount(),
				constants.getMyIp(), Constants.listenningPort,
				constants.getMyAccount(), constants.getMyPasswd(),
				Constants.registarIp.getBytes(), Constants.registarPort);

		// 启动注册线程，定期注册
		new RegisterThread(sipTerm).start();

	}

	public void quitConf() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("confId", confId.toString());
		params.put("userId", userId);
		DisconnectUserThread disconnectUserThread = new DisconnectUserThread(
				handler);
		// 10.0.2.2为电脑对于模拟器而言的IP地址。
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
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 实现父类的抽象方法，获取本地预览界面构件。
	 * 
	 * @return 本地视频预览构件
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
	 * 实现父类的抽象方法，用于设置主Activity的布局。在本类中无需调用setContentView(getLayoutId());
	 * 
	 * @return 界面布局类的ID
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
	 * 注册成功的事件处理函数，主要用于界面UI组件更新（待补充） 此外还需要设置定时器，定时重新注册，保证注册服务器上的注册成功信息不过期
	 */
	private void processRegisterOK() {
		if (debugLxj)
			System.out.println("注册成功");
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

		if (debugLxj)
			System.out.println("振铃");

		ravtCreateTerm(); // 创建媒体处理Terminal

		// 打开音视频会话
		boolean audioFlag = ravtOpenAudioSession("0.0.0.0".getBytes(),
				Constants.sdpLocalAudioPort);
		boolean videoFlag = ravtOpenVideoSession("0.0.0.0".getBytes(),
				Constants.sdpLocalVideoPort);

		if (debugLxj)
			System.out.println("打开音视频的会话结果依次为：" + audioFlag + "," + videoFlag);

	}

	/**
	 * 加入会议成功后，提取媒体协商结果，开始输出本地音视频，并显示本地音视频预览
	 */
	private void processJoinConfOK(Bundle mediaInfo) {
		if (debugLxj)
			System.out.println("成功加入会议，开始输出音视频");

		// 音频处理
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

		// 视频处理
		if (!ravtSetVideoSessionOutputPayloadType(mediaInfo
				.getByte("remoteVideoPayload"))) {// (byte)109)) {
			if (debugLxj)
				System.out.println("视频负载类型错误");
			return;
		}
		if (debugLxj)
			System.out.println("视频负载类型正常");

		int videoPort = mediaInfo.getInt("remoteVideoPort");
		byte[] videoAddr = mediaInfo.getByteArray("remoteVideoAddr");
		if (debugLxj)
			System.out.println("videoPort = " + videoPort);
		if (!ravtSetVideoSessionRemoteAddr(
				mediaInfo.getByteArray("remoteVideoAddr"),
				mediaInfo.getInt("remoteVideoPort"))) {
			if (debugLxj)
				System.out.println("远端地址设置错误");
			return;
		}
		if (debugLxj)
			System.out.println("远端地址设置正常");

		if (!ravtOpenLocalVideoOutput(RtpAvTerm.RAVT_CODEC_H264,
				localVideoWidth, localVideoHeight,
				mediaInfo.getLong("videoByteRate"), localVideoFrameRate,
				localVideoFrameRate * 3, 1024)) {
			if (debugLxj)
				System.out.println("本地视频输出错误");
			return;
		}
		if (debugLxj)
			System.out.println("本地视频输出正常");

		// 打开本地视频预览，注意必须打开预览才能采集视频数据上传到媒体服务器。
		// 摄象机的视频有两个去向，一个preview,一个output
		// 摄象机的开关只能有preview函数控制，
		boolean localResult=ravtOpenLocalVideoPreview(localVideoWidth, localVideoHeight);
		if(localResult)
			System.out.println("ravtOpenLocalVideoPreview success！");
		else
			System.out.println("ravtOpenLocalVideoPreview failed！");

		System.out.println("remoteVideoPayload = "
				+ mediaInfo.getByte("remoteVideoPayload"));
		
		boolean res = ravtOpenRemoteVideoPreview(mediaInfo
				.getShort("remoteVideoType"));
		if (res)
			System.out.println("ravtOpenRemoteVideoPreview success！");
		else
			System.out.println("ravtOpenRemoteVideoPreview failed！");

		// 增加请求I-Frame的操作，避免
		long term = mediaInfo.getLong("term");
		long callId = mediaInfo.getLong("callId");
		SipTerm.requestKeyFrame(term, callId);
	}

	/**
	 * 结束会议后，停止本地音视频采集和输出，释放资源
	 */
	private void processQuitConf() {
		if (debugLxj)
			System.out.println("退出会议");

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
		if (debugLxj)
			System.out.println("退出会议完成");
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
		menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);
	}

	// 菜单适配器
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
		return false;// 返回为true 则显示系统menu
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.v("PhoneActivity","onPause");
		ravtCloseRemoteAudio();
		ravtCloseLocalAudio();
//		ravtCloseAudioSession();

		ravtCloseRemoteVideoPreview();
		ravtCloseLocalVideoPreview();
		ravtCloseLocalVideoOutput();
//		ravtCloseVideoSession();
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
