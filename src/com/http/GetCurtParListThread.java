package com.http;

import java.io.IOException;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.util.Constants;

public class GetCurtParListThread extends Thread {
	private Handler handler = null;
	private String urlString = null;
	private Map<String, String> params = null;

	public GetCurtParListThread(Handler handler) {
		this.handler = handler;
	}

	public void doStart(Map<String, String> params) {
		this.params = params;
		urlString = "http://"
				+ Constants.registarIp
				+ ":8888/MediaConf/conference/conferenceinfo.do";
		System.out.println("getCurPar_dostart£º" + params);
		this.start();

	}

	public void run() {
		Message msg = new Message();
		Bundle data = new Bundle();
		String result = null;
		try {
			result = HttpUtils.sendPostMessage(urlString, params, "utf-8");
			data.putString("result", result);
			System.out.println("getCurPar:" + result);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			msg.setData(data);
			if (handler != null)
				handler.sendMessage(msg);
		}

	}
}
