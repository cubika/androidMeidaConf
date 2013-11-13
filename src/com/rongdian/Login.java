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
       // setTitle("��ý�����ϵͳ");
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
	
		//�����Ի��򣬲�ʹ���Զ��������
		//final Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom1));
		builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
	//	LoadUserDate();
		
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				int id = v.getId();
				//�������
				if (id == R.id.user_joinConf_btn) {				
					builder.setTitle("�������");
					
					//�ҵ�xml�����ļ���ʵ����
					TableLayout joinConfForm = (TableLayout) getLayoutInflater()
							.inflate(R.layout.user_joinconf_form, null);// װ��/res/layout/user_joinconf_form.xmlҳ�沼��
					builder.setView(joinConfForm);// ���öԻ�����ʾ��view����
					// Ϊ�Ի�������һ����ȷ������ť
					builder.setPositiveButton("�������",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							});
					builder.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					// ��������ʾ�Ի���
					dialog=builder.create();
					dialog.show();
				}
				//�û���¼
				else if (id == R.id.user_login_btn)
				{
					// builder.setIcon(R.drawable.login_pg);//���öԻ����ͼ��
					builder.setTitle("�û���¼");				
					

					//final TableLayout loginForm = (TableLayout) getLayoutInflater().inflate(R.layout.user_login_form, null);// װ��/res/layout/user_login_form.xmlҳ�沼��
					View loginScrollView=getLayoutInflater().inflate(R.layout.user_login_form, null);
					final TableLayout loginForm=(TableLayout) loginScrollView.findViewById(R.id.user_login_table);
					builder.setView(loginScrollView);// ���öԻ�����ʾ��view����
					// Ϊ�Ի�������һ����ȷ������ť
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

					builder.setPositiveButton("��¼",
							new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								//	showDialog("��ѡ����ȷ��");  

									// TODO Auto-generated method stub
								
								//	SaveUserDate();
									username = username_input.getText().toString();
									password = userpassword_input.getText().toString();		
									
									if(remoteIp.getText()!=null)
										Constants.registarIp = remoteIp.getText().toString();

							//		System.out.println("��¼positiveButton��Ӧ");
									success = validate(username, password);
									

									if (success == true) {
								//		System.out.println("��¼success=true,���벻Ϊ�գ������������û������������֤");
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
											//ʹ��sharedPreference�洢����
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

										//��ȡ�˲����������˲���֮�������µ��̷߳���ע������
										// �ڹ����߳���ִ�к�ʱ���񣬷�ֹUI�߳�����
										HttpThread httpThread = new HttpThread(loginHandler);
										// 10.0.2.2Ϊ���Զ���ģ�������Ե�IP��ַ��
										httpThread.doStart("http://"+Constants.registarIp+":8888/MediaConf/userLogin.do?method=login",params, Login.this);
									//	httpThread.doStart("http://10.108.167.10:8888/MediaConf/userLogin.do?method=login",params,Login.this);
										dialog.dismiss();
									}
									
								}
								
							});
					builder.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					// ��������ʾ�Ի���
					dialog=builder.create();
					dialog.show();
				} 
				//�����˺�
				else// create
				{
					builder.setTitle("�û�ע��");

					TableLayout createForm = (TableLayout) getLayoutInflater()
							.inflate(R.layout.user_create_form, null);// װ��/res/layout/user_login_form.xmlҳ�沼��
					builder.setView(createForm);// ���öԻ�����ʾ��view����
					// Ϊ�Ի�������һ����ȷ������ť
					builder.setPositiveButton("�ύ",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							});
					builder.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					// ��������ʾ�Ի���
					dialog=builder.create();
					dialog.show();
				}
			}

		
		};

		user_joinConf_btn.setOnClickListener(listener);
		user_login_btn.setOnClickListener(listener);
		user_register_btn.setOnClickListener(listener);

    }
    
    //Handler����������̵߳�Message���󣬰���Ϣ�������̶߳����У�������߳̽��и���UI
    private Handler loginHandler=new Handler(){
        public void handleMessage(Message message) {
            switch (message.what) {
            case 1://����ִ������
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
//            		//Toast.makeText(Login.this, "��½ʧ��,��ˢ�º����µ�½!", Toast.LENGTH_LONG).show();
//            		Toast.makeText(Login.this, "���Ѿ���¼���ˣ�����Ϊ��ע�����������µ�¼��", Toast.LENGTH_LONG).show();
//            	}
//            	else 
            	if(failure.equals("nameError")){
            		Toast.makeText(Login.this, "�û�������!", Toast.LENGTH_LONG).show();
            	}
            	else if(failure.equals("passError")){
            		Toast.makeText(Login.this, "�������!", Toast.LENGTH_LONG).show();
            	}
            	break;
            default://�����쳣...���������ӳ���
                Toast.makeText(Login.this, "�����쳣�����Ժ��ٳ���! ", Toast.LENGTH_LONG).show();
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
			MyToast.openToast(Login.this, "���������û���");
			return false;
		} else if (pass.length() == 0) {
			MyToast.openToast(Login.this, "������������");
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
