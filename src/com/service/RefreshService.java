package com.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.http.HttpUtils;
import com.util.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class RefreshService extends Service  {
	String confId,userId;
	HttpUtils httpUtils;
	
	private String result;
//	private int count;

	Timer timer = new Timer( );

	TimerTask task = new TimerTask( ) {
		public void run ( ) {
			Map<String,String> params = new HashMap<String,String>();  
	     	params.put("confId", confId.toString());  
	     	params.put("userId", userId);
	     	
	    	try {
	    	//	System.out.println("RefreshService timer "+result +"  "+Constants.registarIp);
				result = httpUtils.sendPostMessage("http://"+Constants.registarIp+":8888/MediaConf/clientRefresh.do?", params, "utf-8");
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  		
		}
	};
	
	private ServiceBinder serviceBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
    	
    	public String getResult(){ 		
    	//	System.out.println("refreshservice---"+result);
    		return result;
    	}
    }
    @Override
    public IBinder onBind(Intent intent) {
    	Bundle data = intent.getExtras();
    	confId = data.getString("confId");
    	userId = data.getString("userId");
    //	System.out.println("Service is binded "+data.getString("confId")+"  userId "+data.getString("userId"));  	
    	
        return serviceBinder;
    }
	@Override
    public void onCreate() {
        super.onCreate();    
        timer.schedule(task,10000,2000);
    }
		
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        Log.e("RefreshService","start onUnbind~~~");  
        timer.cancel();
        return super.onUnbind(intent);
    }  
	
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.v( " RefreshService " , " on destroy " );
    }
	

}
