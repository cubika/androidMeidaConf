package com.http;

import java.io.IOException;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.util.Constants;

public class SetScreenTherad extends Thread {
	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;
	private int choice;
	
	public SetScreenTherad(Handler handler,int screenChoice) {
	    this.handler = handler;
	    this.choice=screenChoice;
	    }
	
	public void doStart(Map<String, String> params){
		this.params=params;
			urlString = "http://"+Constants.registarIp+":8888/MediaConf/conference/confBook.do?method=bookConf";
			for(String key:params.keySet()){
				System.out.println("param include "+key+":"+params.get(key));
			}
		    this.start();
		    
	}
	
	public void run(){
		Message msg = new Message();
        Bundle data = new Bundle();
        String result = null;
        try {
			result=HttpUtils.sendPostMessage(urlString, params, "utf-8");
		} catch (IOException e) {
            data.putString("info", "创建会议时出现异常！");
			e.printStackTrace();
		}finally{
			System.out.println("CreateConf result:"+result);
			data.putString("result", result);
			msg.setData(data);
        	if(handler!=null)
        		handler.sendMessage(msg);
		}
        
	}
}
