package com.activity;

import com.rongdian.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

public class ContactManageActivity extends FragmentActivity {
	private FragmentTabHost mTabHost;
	public static String userId;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_manage);
		
		userId=this.getIntent().getExtras().getString("userID"); 
		
		mTabHost = (FragmentTabHost)findViewById(R.id.contactManageTH);
        mTabHost.setup(ContactManageActivity.this, getSupportFragmentManager(), R.id.contactManageRTC);
        mTabHost.addTab(mTabHost.newTabSpec("changeFriend").setIndicator("好友管理",
                getResources().getDrawable(R.drawable.friend)),
                FriendManageFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("changeGroup").setIndicator("群组管理",
                getResources().getDrawable(R.drawable.group)),
                GroupManageFragment.class, null);
	}
	
}
