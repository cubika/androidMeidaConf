package com.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.rongdian.R;

/*
 * icon
 * margin
 * gender
 */
public class AccountManageActivity extends FragmentActivity  {

	private FragmentTabHost mTabHost;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_manage);
		
		
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(AccountManageActivity.this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("basicInfo").setIndicator("基本信息",
                getResources().getDrawable(R.drawable.information)),
                BasicInfoFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("resetPass").setIndicator("修改密码",
                getResources().getDrawable(R.drawable.modify_pass)),
                ResetPasswordFragment.class, null);
	}
	

}
