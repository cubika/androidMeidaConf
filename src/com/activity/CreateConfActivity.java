package com.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.http.CreateConfThread;
import com.rongdian.Login;
import com.rongdian.PhoneActivity;
import com.rongdian.R;
import com.rongdian.R.array;
import com.rongdian.R.id;
import com.rongdian.R.layout;
import com.util.MyToast;

public class CreateConfActivity extends Activity implements OnClickListener{

	 private Spinner confResoSP;
	 private ArrayAdapter<CharSequence> spinnerAdapter;
	 private Button selectContactBtn,confirmBtn;
	 private EditText confNameET,subjectET;
	 private RadioGroup rg;
	 private String userId;
	 private static int resoPos;
	 
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.create_conf);
		userId=this.getIntent().getExtras().getString("userID");
		
		selectContactBtn=(Button)findViewById(R.id.selContactBtn);
		confirmBtn=(Button)findViewById(R.id.creatOKbtn);
		selectContactBtn.setOnClickListener(this);
		confirmBtn.setOnClickListener(this);
		
		confNameET=(EditText)findViewById(R.id.createConfNameET);
		subjectET=(EditText)findViewById(R.id.createConfSubET);
		
		rg=(RadioGroup) findViewById(R.id.createTypeRG);
		
		final int[] resolPosArray={2,3,4,5,1};
		
		//下拉列表框
		confResoSP=(Spinner)findViewById(R.id.ConfResoSP);
		spinnerAdapter=ArrayAdapter.createFromResource(this, R.array.resolList, android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		confResoSP.setAdapter(spinnerAdapter);
		confResoSP.setVisibility(View.VISIBLE);
		confResoSP.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				resoPos=resolPosArray[arg2];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.selContactBtn:	//从联系人中选择参会人员
			Intent intent=new Intent(CreateConfActivity.this,ChooseMemberActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("userID", userId);
			intent.putExtras(bundle);
			CreateConfActivity.this.startActivity(intent);
			break;
		case R.id.creatOKbtn:		//提交表单
			String chairmanId=userId;
			String confName=confNameET.getText().toString().trim();
			String subject=subjectET.getText().toString().trim();
			String startTime=(Calendar.MONTH+1)+"/"+Calendar.DAY_OF_MONTH+"/"+Calendar.YEAR+" "+Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE;
			int radioButtonID=rg.getCheckedRadioButtonId();
			View radioButton=rg.findViewById(radioButtonID);
			String radio=""+(rg.indexOfChild(radioButton)+1);
			String resolutionCombo=""+resoPos;
			String members=ChooseMemberActivity.GetMemberIDs();
			
			Map<String,String> params=new HashMap<String,String>();
			params.put("chairmanId", chairmanId);
			params.put("confName", confName);
			params.put("subject", subject);
			params.put("startTime", startTime);
			params.put("radio", radio);
			params.put("resolutionCombo", resolutionCombo);
			params.put("members", members);
			
			if(confName.length()==0){
				MyToast.openToast(CreateConfActivity.this,"会议名称不能为空！");
				break;
			}
			if(subject.length()==0){
				MyToast.openToast(CreateConfActivity.this,"会议主题不能为空！");
				break;
			}
			if(members.length()==0){
				MyToast.openToast(CreateConfActivity.this,"您还没有选择联系人！");
				break;
			}
			
			
			CreateConfThread thread=new CreateConfThread(handler);
			thread.doStart(params,CreateConfActivity.this);
			break;
		}
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			Bundle data=msg.getData();
			String result=(String) data.get("result");
			Toast.makeText(CreateConfActivity.this, result, Toast.LENGTH_LONG).show();
			finish();
		}
	};
	
		
}
