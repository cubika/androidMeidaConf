package com.rongdian;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

public class AccountManageFragment extends DialogFragment {

	private static View v;
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
		 //���������д���ᵼ��BasicInfoFragment�������Σ��Ӷ��׳��쳣
		 //��֮�������Ƚϵ��ۣ����ұ���Ҫ��try/catch�������������ܽ������
		 if (v != null) {
				ViewGroup parent = (ViewGroup) v.getParent();
				if (parent != null)
					parent.removeView(v);
			}
		 getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		 //��ֹ�����Զ��ĳ������ˣ�����ֻ���Լ���������ʱ��pop up
		 getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		 try{
			 v = inflater.inflate(R.layout.account_manage, container, false);
			 TabHost mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);  
			 mTabHost.setup(); 
			 mTabHost.addTab(mTabHost.newTabSpec("basicInfo").setIndicator("������Ϣ",
		                getResources().getDrawable(R.drawable.information)).setContent(R.id.tab1));  
			 mTabHost.addTab(mTabHost.newTabSpec("resetPass").setIndicator("�޸�����",
		                getResources().getDrawable(R.drawable.modify_pass)).setContent(R.id.tab2));  
		 }catch (InflateException e) {
		        /* map is already there, just return view as it is */
		    }
		 
//		 TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
//
//			@Override
//			public void onTabChanged(String tabId) {
//				FragmentManager fm=getFragmentManager();
//				FragmentTransaction ft = fm.beginTransaction();
//				BasicInfoFragment infoFragment=(BasicInfoFragment) fm.findFragmentByTag("info");
//				ResetPasswordFragment resetFragment=(ResetPasswordFragment) fm.findFragmentByTag("reset");
//				
//				if(infoFragment!=null)
//					ft.detach(infoFragment);
//				if(resetFragment!=null)
//					ft.detach(resetFragment);
//				
//				if(tabId.equalsIgnoreCase("basicInfo")){
//					if(infoFragment==null)
//						ft.add(R.id.tab1, new BasicInfoFragment());
//					else
//						ft.attach(infoFragment);
//				}else if(tabId.equalsIgnoreCase("resetPass")){
//					if(resetFragment==null)
//						ft.add(R.id.tab2, new ResetPasswordFragment());
//					else
//						ft.attach(resetFragment);
//						
//				}
//				
//			}
//			 
//		 };
//		 mTabHost.setOnTabChangedListener(tabChangeListener);
		 return v;
	      
	 }
}
