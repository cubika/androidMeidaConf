package com.http;

import java.io.IOException;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.util.Constants;

public class CreateConfThread extends Thread {
	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;
	private ProgressDialog mpDialog = null;
	
	public CreateConfThread(Handler handler) {
	    this.handler = handler;
	    }
	
	public void doStart(Map<String, String> params,Context context){
		this.params=params;
			urlString = "http://"+Constants.registarIp+":8888/MediaConf/conference/confBook.do?method=bookConf";
			for(String key:params.keySet()){
				System.out.println("param include "+key+":"+params.get(key));
			}
			mpDialog = ProgressDialog.show(context, "提示", "正在创建会议，请稍后 。。。");
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
			mpDialog.dismiss();
			System.out.println("CreateConf result:"+result);
			data.putString("result", result);
			msg.setData(data);
        	if(handler!=null)
        		handler.sendMessage(msg);
		}
        
	}
}
