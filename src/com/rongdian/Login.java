package com.rongdian;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.Toast;

import com.http.HttpThread;
import com.http.HttpUtils;
import com.http.LogoutThread;
import com.util.MyToast;
import com.util.Constants;

public class Login extends Activity {

	private String username;
	private String password;
	boolean success = false;
	private CheckBox auto = null;
	private EditText username_input;
	private EditText userpassword_input;
	private EditText remoteIp;
	private Builder builder;
	private AlertDialog dialog;
	SharedPreferences sp = null;
	
	ImageButton user_joinConf_btn, user_login_btn, user_register_btn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setTitle("多媒体会议系统");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
            // hide statusbar of Android        
            // could also be done later       
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        
        sp = this.getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        user_joinConf_btn = (ImageButton) findViewById(R.id.user_joinConf_btn);
		user_login_btn = (ImageButton) findViewById(R.id.user_login_btn);
		user_register_btn = (ImageButton) findViewById(R.id.user_register_btn);
	
		//创建对话框，并使用自定义的主题
		//final Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom1));
		builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
	//	LoadUserDate();
		
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				int id = v.getId();
				//加入会议
				if (id == R.id.user_joinConf_btn) {				
					builder.setTitle("加入会议");
					
					//找到xml布局文件并实例化
					TableLayout joinConfForm = (TableLayout) getLayoutInflater()
							.inflate(R.layout.user_joinconf_form, null);// 装载/res/layout/user_joinconf_form.xml页面布局
					builder.setView(joinConfForm);// 设置对话框显示的view对象
					// 为对话框设置一个“确定”按钮
					builder.setPositiveButton("加入会议",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							});
					builder.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					// 创建并显示对话框
					dialog=builder.create();
					dialog.show();
				}
				//用户登录
				else if (id == R.id.user_login_btn)
				{
					// builder.setIcon(R.drawable.login_pg);//设置对话框的图标
					builder.setTitle("用户登录");				
					

					//final TableLayout loginForm = (TableLayout) getLayoutInflater().inflate(R.layout.user_login_form, null);// 装载/res/layout/user_login_form.xml页面布局
					View loginScrollView=getLayoutInflater().inflate(R.layout.user_login_form, null);
					final TableLayout loginForm=(TableLayout) loginScrollView.findViewById(R.id.user_login_table);
					builder.setView(loginScrollView);// 设置对话框显示的view对象
					// 为对话框设置一个“确定”按钮
					username_input = (EditText) loginForm.findViewById(R.id.username_input);
					userpassword_input = (EditText) loginForm.findViewById(R.id.userpassword_input);
					remoteIp = (EditText) loginForm.findViewById(R.id.remoteIp);
					auto = (CheckBox) loginForm.findViewById(R.id.auto);					
					if (sp.getBoolean("saveuserinfo", false))
			  		{
						username_input.setText(sp.getString("uname", null));
						userpassword_input.setText(sp.getString("upswd", null)); 
						remoteIp.setText(sp.getString("remoteIp", null)); 
						auto.setChecked(true);			  	     
			  		}

					builder.setPositiveButton("登录",
							new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								//	showDialog("你选择了确定");  

									// TODO Auto-generated method stub
								
								//	SaveUserDate();
									username = username_input.getText().toString();
									password = userpassword_input.getText().toString();		
									
									if(remoteIp.getText()!=null)
										Constants.registarIp = remoteIp.getText().toString();

							//		System.out.println("登录positiveButton响应");
									success = validate(username, password);
									

									if (success == true) {
								//		System.out.println("登录success=true,输入不为空，接下来进行用户名和密码的验证");
										boolean autoLogin = auto.isChecked();
										System.out.println("autoLogin: "+autoLogin);
										if (autoLogin)
										{
												Editor editor = sp.edit();
												editor.putString("uname", username);
												editor.putString("upswd", password);
												editor.putString("remoteIp", Constants.registarIp);
												editor.putBoolean("saveuserinfo", true);
												editor.commit();
										}
										else
										{  
											//使用sharedPreference存储数据
											Editor editor = sp.edit();
											editor.putString("uname", null);
											editor.putString("upswd", null);
//											editor.putString("remoteIp", null);
											editor.putBoolean("saveuserinfo", false);
											editor.commit();
										}
										Map<String, String> params = new HashMap<String, String>();
										params.put("name", username);
										params.put("pass", password);
										params.put("isRemStatus", "false");

										//获取了参数，保存了参数之后，启用新的线程发送注册请求，
										// 在工作线程中执行耗时任务，防止UI线程阻塞
										HttpThread httpThread = new HttpThread(loginHandler);
										// 10.0.2.2为电脑对于模拟器而言的IP地址。
										httpThread.doStart("http://"+Constants.registarIp+":8888/MediaConf/userLogin.do?method=login",params, Login.this);
									//	httpThread.doStart("http://10.108.167.10:8888/MediaConf/userLogin.do?method=login",params,Login.this);
										dialog.dismiss();
									}
									
								}
								
							});
					builder.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					// 创建并显示对话框
					dialog=builder.create();
					dialog.show();
				} 
				//创建账号
				else// create
				{
					builder.setTitle("用户注册");

					TableLayout createForm = (TableLayout) getLayoutInflater()
							.inflate(R.layout.user_create_form, null);// 装载/res/layout/user_login_form.xml页面布局
					builder.setView(createForm);// 设置对话框显示的view对象
					// 为对话框设置一个“确定”按钮
					builder.setPositiveButton("提交",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							});
					builder.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					// 创建并显示对话框
					dialog=builder.create();
					dialog.show();
				}
			}

		
		};

		user_joinConf_btn.setOnClickListener(listener);
		user_login_btn.setOnClickListener(listener);
		user_register_btn.setOnClickListener(listener);

    }
    
    //Handler负责接收子线程的Message对象，把消息放入主线程队列中，配合主线程进行更新UI
    private Handler loginHandler=new Handler(){
        public void handleMessage(Message message) {
            switch (message.what) {
            case 1://程序执行正常
            	Intent intent = new Intent(Login.this,PhoneActivity.class);
            	Bundle data = new Bundle();
                data.putString("myAccount", username);
                data.putString("password", password);
                data.putString("userId", message.getData().getString("userId"));
                System.out.println("username:"+username+"password:"+password+"userId"+message.getData().getString("userId"));
                intent.putExtras(data);
				startActivity(intent);
                break;
            case 2:
            	String failure = message.getData().getString("info");
//            	if(failure.equals("alreadyLogined")){
//            		//Toast.makeText(Login.this, "登陆失败,请刷新后重新登陆!", Toast.LENGTH_LONG).show();
//            		Toast.makeText(Login.this, "您已经登录过了，现在为您注销，请您重新登录！", Toast.LENGTH_LONG).show();
//            	}
//            	else 
            	if(failure.equals("nameError")){
            		Toast.makeText(Login.this, "用户名错误!", Toast.LENGTH_LONG).show();
            	}
            	else if(failure.equals("passError")){
            		Toast.makeText(Login.this, "密码错误!", Toast.LENGTH_LONG).show();
            	}
            	break;
            default://程序异常...（网络连接出错）
                Toast.makeText(Login.this, "网络异常，请稍后再尝试! ", Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
    

	private boolean validate(String username, String pass) {
		if (username.length() != 0 && pass.length() != 0) {
			return true;
		} else if (username.length() == 0) {
			MyToast.openToast(Login.this, "请先输入用户名");
			return false;
		} else if (pass.length() == 0) {
			MyToast.openToast(Login.this, "请先输入密码");
			return false;
		}
		return false;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(dialog!=null)
			dialog.dismiss();
	}
	
}
