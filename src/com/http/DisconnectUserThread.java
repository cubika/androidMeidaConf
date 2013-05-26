package com.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class DisconnectUserThread extends Thread{
	
	ProgressDialog pdDialog = null;
	String urlString = null;
	Map<String,String> params = null;
	Handler handler = null;
	HttpUtils httpUtils;
	public DisconnectUserThread(Handler handler) {
	    this.handler = handler;
	    }

	public void doStart(String urlString,Map<String,String> params,
	        Context context) {//进行一些初始化工作然后调用start()让线程运行
	    this.urlString = urlString;
	    this.params = params;
//	    pdDialog = new ProgressDialog(context);
	  //  pdDialog.setTitle("Quit...");
//	    pdDialog.setMessage("正在退出会议，请稍后...");
//	    pdDialog.setIndeterminate(true);
//	    pdDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
//	        public void onClick(DialogInterface dialog, int which) {
//	            pdDialog.cancel();
//	        }
//        });
//        pdDialog.show();
        this.start();
    }
	
	 @Override
	    public void run() {
	        Message msg = new Message();
	        Bundle data = new Bundle();	       
	        try {
	      //      boolean result = webServiceLogin();//执行网络服务请求，耗时操作。。。
	        	String result = httpUtils.sendPostMessage(urlString, params, "utf-8");	        	
	        	try {
	        		
					JSONObject jsonObject = new JSONObject(result);
					String flag = jsonObject.getString("result");					
					
					if(flag.equals("success"))
						msg.what = 0x2101;					
					else if(flag.equals("failure"))
						msg.what = 0x2100;
					
	        	} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	           
	        } catch (ParseException e) {
	            msg.what=0x2100;
	            data.putString("info", "netWorkError");
	            msg.setData(data);
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            msg.what=0x2100;
	            data.putString("info", "netWorkError");
	            msg.setData(data);
	            e.printStackTrace();
	        } catch (IOException e) {
	            msg.what=0x2100;
	            data.putString("info", "netWorkError");
	            msg.setData(data);
	            e.printStackTrace();
	        } finally {
//	            pdDialog.dismiss();
	            handler.sendMessage(msg);
	        }
	    }
}
