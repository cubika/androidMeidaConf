package com.rongdian;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.http.GetUserInfoTask;
import com.http.HttpUtils;
import com.util.Constants;
import com.util.MyToast;

public class ResetPasswordFragment extends Fragment implements OnClickListener{

	private Button modifyBTN,resetBTN;
	private EditText oldPass,newPass,confirmPass;
	
	 @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {

		 View V = inflater.inflate(R.layout.reset_pass, container, false);
		 modifyBTN=(Button) V.findViewById(R.id.modifyBTN2);
		 resetBTN=(Button)V.findViewById(R.id.resetBTN2);
		 modifyBTN.setOnClickListener(this);
		 resetBTN.setOnClickListener(this);
		 oldPass=(EditText) V.findViewById(R.id.reset_old);
		 newPass=(EditText) V.findViewById(R.id.reset_new);
		 confirmPass=(EditText) V.findViewById(R.id.reset_confirm_new);
         return V;
	 }
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.modifyBTN2:
			boolean ret=validate();
			if(!ret)
				break;
			ResetPassTask resetTask=new ResetPassTask(getActivity());
			resetTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/updatePassword.do?userId="+GetUserInfoTask.userId
								+"&oldPasswd="+oldPass.getText().toString()+"&newPasswd="+newPass.getText().toString()
								+"&confirmPasswd="+confirmPass.getText().toString());
			break;
		case R.id.resetBTN2:
			oldPass.setText("");
			newPass.setText("");
			confirmPass.setText("");
			break;
			
		}
	}
	
	public boolean validate(){
		if(oldPass.getText().toString()==null || oldPass.getText().toString().trim().length()<=0){
			MyToast.openToast(getActivity(), "旧密码不能为空");
			return false;
		}
		if(newPass.getText().toString()==null || newPass.getText().toString().trim().length()<=0){
			MyToast.openToast(getActivity(), "新密码不能为空");
			return false;
		}
		if(confirmPass.getText().toString()==null || confirmPass.getText().toString().trim().length()<=0){
			MyToast.openToast(getActivity(), "确认密码不能为空");
			return false;
		}
		return true;
	}
	
	 class ResetPassTask extends AsyncTask<String,Integer,String>{

			private Context context;
			
			public ResetPassTask(Context context){
				this.context=context;
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
				Log.v("ResetPassTask","result is:"+result);
				if(result!=null && result.trim().length()!=0){
					try {
						JSONObject resultObj=new JSONObject(result);
						String success=resultObj.getString("success");
						String message=null;
						if(success.equals("true"))
							message="修改成功";
						else if(success.equals("false")){
							String error=resultObj.getString("error");
							if(error!=null)
								message=error;
							else
								message="修改失败";
						}	
						
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
		}
}