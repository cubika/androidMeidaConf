package com.rongdian;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import com.http.HttpUtils;
import com.util.Constants;
import com.util.MyToast;

public class CreateConfActivity extends Activity {

	public static class FormFragment extends Fragment implements OnClickListener {
		private Spinner confResoSP;
		private ArrayAdapter<CharSequence> spinnerAdapter;
		private Button confirmBtn;
		private EditText confNameET, subjectET;
		private RadioGroup rg;
		private static int resoPos;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.conf_form, container, false);
			confirmBtn = (Button) v.findViewById(R.id.creatOKbtn);
			confirmBtn.setOnClickListener(this);

			confNameET = (EditText) v.findViewById(R.id.createConfNameET);
			subjectET = (EditText) v.findViewById(R.id.createConfSubET);

			rg = (RadioGroup) v.findViewById(R.id.createTypeRG);

			final int[] resolPosArray = { 2, 3, 4, 5, 1 };

			// 下拉列表框
			confResoSP = (Spinner) v.findViewById(R.id.ConfResoSP);
			spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
					R.array.resolList, android.R.layout.simple_spinner_item);
			spinnerAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			confResoSP.setAdapter(spinnerAdapter);
			confResoSP.setVisibility(View.VISIBLE);
			confResoSP.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					resoPos = resolPosArray[arg2];
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}

			});
			return v;
		}

		@Override
	      public void onActivityCreated(Bundle savedInstanceState) {
	          super.onActivityCreated(savedInstanceState);
	          FragmentManager fm=getFragmentManager();
	          FragmentTransaction ft=fm.beginTransaction();
	          MemberFragment memberFragment=(MemberFragment) fm.findFragmentByTag("member");
	          if(memberFragment==null)
	        	  memberFragment=new MemberFragment();
	          ft.replace(R.id.member, memberFragment, "member");
	        	  
		}
		  
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.creatOKbtn: // 提交表单
				String chairmanId = PadActivity.userId;
				String confName = confNameET.getText().toString().trim();
				String subject = subjectET.getText().toString().trim();
				String startTime = (Calendar.MONTH + 1) + "/"
						+ Calendar.DAY_OF_MONTH + "/" + Calendar.YEAR + " "
						+ Calendar.HOUR_OF_DAY + ":" + Calendar.MINUTE;
				int radioButtonID = rg.getCheckedRadioButtonId();
				View radioButton = rg.findViewById(radioButtonID);
				String radio = "" + (rg.indexOfChild(radioButton) + 1);
				String resolutionCombo = "" + resoPos;
				String members = MemberFragment.GetMemberIDs();

				Map<String, String> params = new HashMap<String, String>();
				params.put("chairmanId", chairmanId);
				params.put("confName", confName);
				params.put("subject", subject);
				params.put("startTime", startTime);
				params.put("radio", radio);
				params.put("resolutionCombo", resolutionCombo);
				params.put("members", members);

				if (confName.length() == 0) {
					MyToast.openToast(getActivity(), "会议名称不能为空！");
					break;
				}
				if (subject.length() == 0) {
					MyToast.openToast(getActivity(), "会议主题不能为空！");
					break;
				}
				if (members.length() == 0) {
					MyToast.openToast(getActivity(), "您还没有选择联系人！");
					break;
				}
				
				new CreateConfTask(params,getActivity()).execute(Constants.prefix+"conference/confBook.do?method=bookConf");
				break;
			}
		}
		
		public void done(){
			getActivity().finish();
		}
		
		class CreateConfTask extends AsyncTask<String,Integer,String>{

			private Map<String, String> paramMap;
			private ProgressDialog mpDialog;
			private Context context;
			
			public CreateConfTask(Map<String, String> paramMap,Context context){
				this.paramMap=paramMap;
				this.context=context;
				mpDialog=new ProgressDialog(context);
				
			}
			
			@Override
			protected String doInBackground(String... params) {
				Log.v("CreateConfTask","doInBackground");
				String url=params[0];
				Log.v("CreateConfTask","url is "+url);
				String result=null;
				try {
					result=HttpUtils.sendPostMessage(url, paramMap, "utf-8");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			}
			
			@Override
			protected void onPostExecute(String result){
				Log.v("CreateConfTask","onPostExecute");
				Log.v("CreateConfTask","result is:"+result);
				if(mpDialog.isShowing())
					mpDialog.dismiss();
				if(result!=null && result.trim().length()!=0){
					try {
						JSONObject obj = new JSONObject(result);
						Toast.makeText(context, obj.getString("msg"),Toast.LENGTH_LONG).show();
						done();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			protected void onPreExecute(){
				mpDialog.setTitle("提示");
				mpDialog.setMessage("正在创建会议，请稍后 。。。");
				mpDialog.show();
				
				for(String key:paramMap.keySet()){
					System.out.println("param include "+key+":"+paramMap.get(key));
				}
			}

		}
	}

	public static class MemberFragment extends Fragment{
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v=inflater.inflate(R.layout.create_conf_members, container, false);
			 TabHost mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);  
			 mTabHost.setup(); 
			 mTabHost.addTab(mTabHost.newTabSpec("myContact").setIndicator("我的好友").setContent(R.id.tabMyContact));  
			 mTabHost.addTab(mTabHost.newTabSpec("myGroup").setIndicator("我的群").setContent(R.id.tabMyGroup));  
			 
			 FragmentManager fm=getFragmentManager();
			 FragmentTransaction ft = fm.beginTransaction();
			 MyContactFragment contactFragment=(MyContactFragment) fm.findFragmentByTag("myContact");
			 MyGroupFragment groupFragment=(MyGroupFragment) fm.findFragmentByTag("myGroup");
			 if(contactFragment!=null)
				 ft.detach(contactFragment);
			 if(groupFragment!=null)
				 ft.detach(groupFragment);
			 
			 if(contactFragment==null)
				 ft.add(R.id.tabMyContact, new MyContactFragment(),"myContact");
			 else 
				 ft.attach(contactFragment);
			 
			 if(groupFragment==null)
				 ft.replace(R.id.tabMyGroup, new MyGroupFragment(),"myGroup");
			 else 
				 ft.attach(groupFragment);
			 ft.commit();
			 
//			 TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
			 //
//			 			@Override
//			 			public void onTabChanged(String tabId) {
//			 				FragmentManager fm=getFragmentManager();
//			 				FragmentTransaction ft = fm.beginTransaction();
//			 				BasicInfoFragment infoFragment=(BasicInfoFragment) fm.findFragmentByTag("info");
//			 				ResetPasswordFragment resetFragment=(ResetPasswordFragment) fm.findFragmentByTag("reset");
//			 				
//			 				if(infoFragment!=null)
//			 					ft.detach(infoFragment);
//			 				if(resetFragment!=null)
//			 					ft.detach(resetFragment);
//			 				
//			 				if(tabId.equalsIgnoreCase("basicInfo")){
//			 					if(infoFragment==null)
//			 						ft.add(R.id.tab1, new BasicInfoFragment());
//			 					else
//			 						ft.attach(infoFragment);
//			 				}else if(tabId.equalsIgnoreCase("resetPass")){
//			 					if(resetFragment==null)
//			 						ft.add(R.id.tab2, new ResetPasswordFragment());
//			 					else
//			 						ft.attach(resetFragment);
//			 						
//			 				}
//			 				
//			 			}
//			 			 
//			 		 };
//			 		 mTabHost.setOnTabChangedListener(tabChangeListener);
			return v;
		}
	
		public static String GetMemberIDs(){
			String temp = "";
			
			System.out.println("Contact checkedObject size:"+MyContactFragment.mCheckedObj.size());
			System.out.println("Group checkedObject size"+MyGroupFragment.mCheckedObj.size());
			for(int i=0;i<MyContactFragment.mCheckedObj.size();i++){
				HashMap<Integer, Boolean> checkHM=MyContactFragment.mCheckedObj.get(i);
				HashMap<Integer, String> childHM=MyContactFragment.mContactChildData.get(i);
				for(Integer key:checkHM.keySet()){
					String userName=childHM.get(key);
					String idContact=MyContactFragment.userRecord.get(userName).toString();
					temp+=idContact+"|";
				}
			}
			
			for(int i=0;i<MyGroupFragment.mCheckedObj.size();i++){
				HashMap<Integer, Boolean> checkHM=MyGroupFragment.mCheckedObj.get(i);
				HashMap<Integer, String> childHM=MyGroupFragment.mGroupChildData.get(i);
				for(Integer key:checkHM.keySet()){
					String userName=childHM.get(key);
					String idContact=MyGroupFragment.userRecord.get(userName).toString();
					temp+=idContact+"|";
				}
			}
			
			return temp;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_conf);
	}

}