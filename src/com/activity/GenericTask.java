package com.activity;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.http.HttpUtils;

public class GenericTask extends AsyncTask<String,Integer,String>{

	private Context context;
	
	public GenericTask(Context context){
		this.context=context;
	}
	
	@Override
	protected String doInBackground(String... params) {
		Log.v("GenericTask","doInBackground");
		String url=params[0];
		Log.v("GenericTask","url is "+url);
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
		Log.v("GenericTask","onPostExecute");
		Log.v("GenericTask","result is:"+result);
		if(result!=null && result.trim().length()!=0){
			//Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			try {
				JSONObject resultObj=new JSONObject(result);
				String answer=resultObj.getString("result");
				String message=answer.equals("success")?"操作成功":"操作失败";
				AlertDialog.Builder builder=new AlertDialog.Builder(context);
				builder.setTitle("新消息").setMessage(message)
						.setPositiveButton("确认", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,int which) {
								dialog.dismiss();
							}
					
				}).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Override
	protected void onPreExecute() {
		Log.v("GenericTask","onPreExecute");
	}
}