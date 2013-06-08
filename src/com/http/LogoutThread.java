package com.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.util.Constants;

import android.content.Context;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LogoutThread extends Thread{
	
	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;
	
	public LogoutThread(){
		
	}
	
	public LogoutThread(Handler handler) {
	    this.handler = handler;
	    }
	
	public void doStart(){
			urlString = "http://"+Constants.registarIp+":8888/MediaConf/userLogin.do?method=logout";
		    System.out.println("logout_dostart");
		    this.start();
		    
	}
	
	public void run(){
		Message msg = new Message();
        Bundle data = new Bundle();
        
        try {
			HttpUtils.sendPostMessage(urlString, params, "utf-8");
		} catch (IOException e) {
			msg.what=0x2103;
            data.putString("info", "注销时出现异常！");
            msg.setData(data);
			e.printStackTrace();
		}finally{
        	if(handler!=null)
        		handler.sendMessage(msg);
		}
        
	}
}
