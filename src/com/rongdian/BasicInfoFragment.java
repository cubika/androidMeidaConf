package com.rongdian;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.http.GenericTask;
import com.http.GetUserInfoTask;
import com.util.Constants;
import com.util.MyToast;

public class BasicInfoFragment extends Fragment implements OnClickListener {

	private EditText userNameET;
	private EditText userIdET;
	private EditText emailET;
	private EditText cellPhoneET;
	private Button modifyBTN, resetBTN;
	private RadioGroup genderRG;
	private RadioButton maleRB,femaleRB,privacyRB;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View V = inflater.inflate(R.layout.user_info, container, false);
		userNameET = (EditText) V.findViewById(R.id.info_un);
		userIdET = (EditText) V.findViewById(R.id.info_id);
		cellPhoneET = (EditText) V.findViewById(R.id.info_tel);
		emailET = (EditText) V.findViewById(R.id.info_email);

		modifyBTN = (Button) V.findViewById(R.id.modifyBTN1);
		resetBTN = (Button) V.findViewById(R.id.resetBTN1);
		
		userNameET.setText(GetUserInfoTask.userName);
		userIdET.setText(GetUserInfoTask.userId);
		cellPhoneET.setText(GetUserInfoTask.cellphoneNumber);
		emailET.setText(GetUserInfoTask.email);
		
		
		modifyBTN.setOnClickListener(this);
		resetBTN.setOnClickListener(this);
		
		genderRG=(RadioGroup) V.findViewById(R.id.genderRG);
		maleRB=(RadioButton) V.findViewById(R.id.genderMale);
		femaleRB=(RadioButton) V.findViewById(R.id.genderFemale);
		privacyRB=(RadioButton) V.findViewById(R.id.genderPrivacy);
		String oldGen=GetUserInfoTask.gender;
		if(oldGen==null || oldGen.trim().length()<=0 || oldGen.equals("privacy"))
			privacyRB.setChecked(true);
		else if(oldGen.equals("male"))
			maleRB.setChecked(true);
		else if(oldGen.equals("female"))
			femaleRB.setChecked(true);
		return V;
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.modifyBTN1:
			Log.v("BasicInfoFragment", "modify button clicked!");
			boolean ret=validate();
			if(!ret)
				break;
			GenericTask updateInfoTask=new GenericTask(getActivity());
			int radioButtonID=genderRG.getCheckedRadioButtonId();
			RadioButton rb=(RadioButton) genderRG.findViewById(radioButtonID);
			updateInfoTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/updateUserInfo.do?userId="+userIdET.getText().toString()
									+"&gender="+rb.getText()+"&email="+emailET.getText().toString()+"&cellphone="+cellPhoneET.getText().toString());
			break;
		case R.id.resetBTN1:
			Log.v("BasicInfoFragment", "reset button clicked!");
			userNameET.setText(GetUserInfoTask.userName);
			userIdET.setText(GetUserInfoTask.userId);
			cellPhoneET.setText(GetUserInfoTask.cellphoneNumber);
			emailET.setText(GetUserInfoTask.email);
			break;
		}
		
	}
	
	public boolean validate(){
		if(cellPhoneET.getText().toString()==null || cellPhoneET.getText().toString().trim().length()<=0){
			MyToast.openToast(getActivity(), "手机号码不能为空");
			return false;
		}
		if(emailET.getText().toString()==null || emailET.getText().toString().trim().length()<=0){
			MyToast.openToast(getActivity(), "邮件地址不能为空");
			return false;
		}
		int checkedID=genderRG.getCheckedRadioButtonId();
		if(checkedID==-1){
			MyToast.openToast(getActivity(), "性别不能为空");
			return false;
		}
		return true;
	}

	
}
