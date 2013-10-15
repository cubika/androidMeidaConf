package com.http;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.rongdian.SipTerm;
import com.util.Constants;
import android.os.AsyncTask;
import android.util.Log;

public class GetConfTypeTask extends AsyncTask<String,Integer,String>{
	
	private long term,callId;
	
	public GetConfTypeTask(long term,long callId){
		this.term = term;
		this.callId = callId;
	}
	@Override
	protected String doInBackground(String... params) {
		String url=params[0];
		String result=null;
		try {
			result=HttpUtils.sendPostMessage(url, null, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(String result){
		Log.v("GetConfTypeTask","onPostExecute");
		Log.v("GetConfTypeTask","result is:"+result);
		if(result!=null && result.trim().length()!=0){
				int resoIndex =Integer.parseInt(result);
				//QCIF,CIF,4CIF,720p,1080p
				int[] payloadArr = {125,104,123,122,121};
				byte[] payload = {(byte)payloadArr[resoIndex-1]};
				
				byte[] profileLevelId = new byte[64];
				byte[] tmp = "42e015\0".getBytes();
				for(int i = 0; i < tmp.length;  i++)
					profileLevelId[i] = tmp[i];
				
		 		if(SipTerm.acceptIncomingCall(term, callId, "0.0.0.0".getBytes(),
						Constants.sdpLocalAudioPort, Constants.sdpLocalAudioRtpmaps, Constants.sdpLocalAudioPayloads, true, true, 
						Constants.sdpLocalVideoPort, Constants.sdpLocalVideoBitRate, Constants.sdpLocalVideoRtpmaps,
						payload, profileLevelId, true, true))
					System.out.println("acceptIncomingCall called OK");
				else
					System.out.println("acceptIncomingCall called bad");

		}
	}

}