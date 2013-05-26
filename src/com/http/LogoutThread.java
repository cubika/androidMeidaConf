package com.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LogoutThread extends Thread{
	
	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;
	HttpUtils httpUtils;

	public LogoutThread(Handler handler) {
	    this.handler = handler;
	    }
	
	public void doStart(String urlString,Map<String,String> params,
	        Context context){
			this.urlString = urlString;
		    this.params = params;
		    System.out.println("logout_dostart");
		    this.start();
		    
	}
	
	public void run(){
		Message msg = new Message();
        Bundle data = new Bundle();
        System.out.println("logout_run");
        try{
        	System.out.println("logout_run_try");
        	String result = httpUtils.sendPostMessage(urlString, params, "utf-8");
        	System.out.println(result);
        	try {
        		System.out.println("logout_try");
				JSONObject jsonObject = new JSONObject(result);
				String flag = jsonObject.getString("result");					
				System.out.println("flag");
				
				if(flag.equals("success"))
				{	msg.what = 0x2102;	
					System.out.println("logout");
				}
				else if(flag.equals("failure"))
					msg.what = 0x2103;
				
        	} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }catch (ParseException e) {
            msg.what=0x2103;
            data.putString("info", "netWorkError");
            msg.setData(data);
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            msg.what=0x2103;
            data.putString("info", "netWorkError");
            msg.setData(data);
            e.printStackTrace();
        } catch (IOException e) {
            msg.what=0x2103;
            data.putString("info", "netWorkError");
            msg.setData(data);
            e.printStackTrace();
        } finally {
//            pdDialog.dismiss();
            handler.sendMessage(msg);
        }
	}
}
