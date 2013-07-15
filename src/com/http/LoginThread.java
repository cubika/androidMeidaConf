package com.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LoginThread extends Thread {
	ProgressDialog pdDialog = null;
    String urlString = null;
    Map<String,String> params = null;
    Handler handler = null;

    public LoginThread(Handler handler) {
        this.handler = handler;
    }
    
    public void doStart(String urlString,Map<String,String> params,
            Context context) {//����һЩ��ʼ������Ȼ�����start()���߳�����
        this.urlString = urlString;
        System.out.println("HttpThread: "+urlString);
        this.params = params;
        pdDialog = new ProgressDialog(context);
        pdDialog.setTitle("Login...");
        pdDialog.setMessage("Connecting to Server...");
        pdDialog.setIndeterminate(true);
        pdDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                pdDialog.cancel();
            }
        });
        pdDialog.show();
        this.start();
    }

    @Override
    public void run() {
        Message msg = new Message();
        Bundle data = new Bundle();
        try {
      //      boolean result = webServiceLogin();//ִ������������󣬺�ʱ����������
        	
        	
        	//String result = HttpUtils.sendPostMessage(urlString, params, "utf-8");
        	//����֮ǰ�Ĳ�֧�ִ�cookie������޷�����session�����Ը�������ĵ���
        	List<NameValuePair> myList = new ArrayList<NameValuePair>();
        	Iterator iterator=params.entrySet().iterator();
        	while (iterator.hasNext()) { 
        	    Entry entry = (Entry) iterator.next(); 
        	    String key = (String) entry.getKey(); 
        	    String val = (String) entry.getValue(); 
        	    myList.add(new BasicNameValuePair(key, val)); 
        	} 
        	String result = HttpUtils.execRequest(urlString, myList);
        	System.out.println("result is:"+result);
        	try {
				JSONObject jsonObject = new JSONObject(result);
				String logined = jsonObject.getString("logined");
				String name = jsonObject.getString("name");
				String pass = jsonObject.getString("pass");
				String userId = jsonObject.getString("userId");
			//	System.out.println("logined:"+logined+"  name:"+name+"  pass:"+pass+" userId:"+userId);
				
				data.putString("userId", userId);
				if(logined.equals("success")){
					msg.what=2; //��¼ʧ��,���Ժ�ˢ��ҳ�����µ�½
					data.putBoolean("login", false);
					data.putString("info", "Error");
					msg.setData(data);
				}else{
					if(name.equals("true")){
						msg.what=2; 
						data.putBoolean("login", false);
						data.putString("info", "nameError");
						msg.setData(data);
					}
					else if(pass.equals("true")){
						msg.what=2; 
						data.putBoolean("login", false);
						data.putString("info", "passError");
						msg.setData(data);
					}else{
						msg.what=1; 
						data.putBoolean("login", true);
						data.putString("info", "normal");
						msg.setData(data);
					}
				} 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
        } catch (IOException e) {
            msg.what=3;
            data.putString("info", "��������쳣��");
            msg.setData(data);
            e.printStackTrace();
        }  finally {
            pdDialog.dismiss();
            handler.sendMessage(msg);
        }
    }

    /*private boolean webServiceLogin() throws ParseException,ClientProtocolException, IOException {
	   	String res = httpUtils.sendPostMessage(urlString, params, "utf-8");
    	System.out.println("httpUtilsResponse:  "+res);
		if (res.contains("true")) {
		    return true;
		}
		return false;
	}*/
}
