package com.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rongdian.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;



@SuppressWarnings("deprecation")
public class ChooseMemberActivity extends TabActivity {

	private String userId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_member);
        TabHost tabHost = getTabHost();  
        TabHost.TabSpec spec;  
        Intent intent;  // Reusable Intent for each tab  
        
        // Create an Intent to launch an Activity for the tab (to be reused)  
        intent = new Intent().setClass(this, MyContactActivity.class); 
        userId=this.getIntent().getExtras().getString("userID");
		Bundle bundle = new Bundle();
		bundle.putString("userID", userId);
		intent.putExtras(bundle);
		
        // Initialize a TabSpec for each tab and add it to the TabHost  
        spec = tabHost.newTabSpec("myContact").setIndicator("我的联系人")
                      .setContent(intent);  
        tabHost.addTab(spec);  
  
        // Do the same for the other tabs  
        intent = new Intent().setClass(this, MyGroupActivity.class); 
		intent.putExtras(bundle);
        spec = tabHost.newTabSpec("myGroup").setIndicator("我的群组")
                      .setContent(intent);  
        tabHost.addTab(spec);  
  
  
        tabHost.setCurrentTab(0);  
	}
	
	public static String GetMemberIDs(){
		String temp = "";
		
		System.out.println("Contact checkedObject size:"+MyContactActivity.mCheckedObj.size());
		System.out.println("Group checkedObject size"+MyGroupActivity.mCheckedObj.size());
		for(int i=0;i<MyContactActivity.mCheckedObj.size();i++){
			HashMap<Integer, Boolean> checkHM=MyContactActivity.mCheckedObj.get(i);
			HashMap<Integer, String> childHM=MyContactActivity.mContactChildData.get(i);
			for(Integer key:checkHM.keySet()){
				String userName=childHM.get(key);
				String idContact=MyContactActivity.userRecord.get(userName).toString();
				temp+=idContact+"|";
			}
		}
		
		for(int i=0;i<MyGroupActivity.mCheckedObj.size();i++){
			HashMap<Integer, Boolean> checkHM=MyGroupActivity.mCheckedObj.get(i);
			HashMap<Integer, String> childHM=MyGroupActivity.mGroupChildData.get(i);
			for(Integer key:checkHM.keySet()){
				String userName=childHM.get(key);
				String idContact=MyGroupActivity.userRecord.get(userName).toString();
				temp+=idContact+"|";
			}
		}
		
		return temp;
	}


}