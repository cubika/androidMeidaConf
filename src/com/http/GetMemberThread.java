package com.http;

import java.io.IOException;
import java.util.Map;

import com.util.Constants;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetMemberThread extends Thread{

	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;
	public static int contactCount=1;
	public static int groupCount=1;
	private String type;
	
	public GetMemberThread(Handler handler,String type){
		this.handler=handler;
		this.type=type;
	}
	
	public void doStart(Map<String, String> params){
		this.params=params;
		urlString="http://"+Constants.registarIp+":8888/MediaConf/addMember.do?";
		this.start();
	}
	
	@Override
	public void run(){
		Message msg = new Message();
        Bundle data = new Bundle();
        if(type.equals("contact")){
        	data.putInt("count", contactCount);
        	contactCount++;
        }else if(type.equals("group")){
        	data.putInt("count", groupCount);
        	groupCount++;
        }
        
		try {
			String result=HttpUtils.sendPostMessage(urlString, params, "utf-8");
			data.putString("result", result);
		} catch (IOException e) {
			data.putString("info", "获取联系人失败！");
			e.printStackTrace();
		}finally{
			msg.setData(data);			
        	if(handler!=null)
        		handler.sendMessage(msg);
		}
	}
}
