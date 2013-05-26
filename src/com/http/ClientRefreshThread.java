package com.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;


import android.content.Context;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ClientRefreshThread extends Thread {
	String urlString = null;
    Map<String,String> params = null;
    Handler handler = null;
    HttpUtils httpUtils;

    public ClientRefreshThread(Handler handler) {
        this.handler = handler;
    }
    public void doStart(String urlString,Map<String,String> params,
            Context context) {//进行一些初始化工作然后调用start()让线程运行
        this.urlString = urlString;
        this.params = params;
        System.out.println("ClientRefresh");
        this.start();
    }

    @Override
    public void run() {
        Message msg = new Message();
        Bundle data = new Bundle();
        try {
        	System.out.println("okkk");
        	String result = httpUtils.sendPostMessage(urlString, params, "utf-8");
        	System.out.println("refresh:"+result);
           
        } catch (ParseException e) {
            msg.what=3;
            data.putString("info", "netWorkError");
            msg.setData(data);
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            msg.what=3;
            data.putString("info", "netWorkError");
            msg.setData(data);
            msg.setData(data);
            e.printStackTrace();
        } catch (IOException e) {
            msg.what=3;
            data.putString("info", "netWorkError");
            msg.setData(data);
            e.printStackTrace();
        } finally {           
            handler.sendMessage(msg);
        }
    }
}
