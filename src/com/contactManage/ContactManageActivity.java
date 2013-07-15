package com.contactManage;

import com.rongdian.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

public class ContactManageActivity extends Activity implements ContactListener{
	
	final int ACTION_ADD_FRIEND=1;
	final int ACTION_ADD_GROUP=2;
	final int ACTION_ADD_FRIEND_FOR_GROUP=3;
	final int ACTION_RELOAD=4;
	private static String currentTab;
	
	public static class ContactFragment extends Fragment{
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.contact_fragment, container, false);
			 TabHost mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);  
			 mTabHost.setup(); 
			 mTabHost.addTab(mTabHost.newTabSpec("Friend").setIndicator("我的好友").setContent(R.id.tabFriend));  
			 mTabHost.addTab(mTabHost.newTabSpec("Group").setIndicator("我的群").setContent(R.id.tabGroup));  
			 
			 FragmentManager fm=getFragmentManager();
			 FragmentTransaction ft = fm.beginTransaction();
			 
			 FriendManageFragment friendFragment=(FriendManageFragment) fm.findFragmentByTag("friend");
			 GroupManageFragment groupFragment=(GroupManageFragment) fm.findFragmentByTag("group");
			 
			 if(friendFragment==null)
				 ft.replace(R.id.tabFriend, new FriendManageFragment(),"friend");
			 
			 if(groupFragment==null)
				 ft.replace(R.id.tabGroup, new GroupManageFragment(),"group");

			 ft.commit();
			 
			 TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
			 
			 			@Override
			 			public void onTabChanged(String tabId) {
			 				currentTab = tabId;
			 				FragmentManager fm=getFragmentManager();
			 				FragmentTransaction ft = fm.beginTransaction();
			 				AddFriendFragment fragment=(AddFriendFragment) fm.findFragmentByTag("addFriend");
			 				AddGroupFragment fragment1=(AddGroupFragment) fm.findFragmentByTag("addGroup");
			 				AddFriendForGroupFragment fragment2=(AddFriendForGroupFragment) fm.findFragmentByTag("addFriendForGroup");
			 				if(fragment!=null)
			 					ft.remove(fragment);
			 				if(fragment1!=null)
			 					ft.remove(fragment1);
			 				if(fragment2!=null)
			 					ft.remove(fragment2);
			 				ft.commit();
			 			}
			 			 
			 		 };
			 		 mTabHost.setOnTabChangedListener(tabChangeListener);
			return v;
	}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_manage);
	}


	@Override
	public void notify(int action, String groupId,String groupName) {
		FragmentManager fm=getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Log.i("ContactManage","action is "+action);
		switch(action){
		case ACTION_ADD_FRIEND:
			AddFriendFragment fragment=(AddFriendFragment) fm.findFragmentByTag("addFriend");
			if(fragment==null){
				AddFriendFragment addFriend = new AddFriendFragment();
				Bundle args = new Bundle();
		        args.putString("groupId", groupId);
		        addFriend.setArguments(args);
		        ft.replace(R.id.add, addFriend, "addFriend");
			}
			ft.commit();
			break;
		case ACTION_ADD_GROUP:
			AddGroupFragment fragment1=(AddGroupFragment) fm.findFragmentByTag("addGroup");
			if(fragment1==null){
				AddGroupFragment addGroup = new AddGroupFragment();
		        ft.replace(R.id.add, addGroup, "addGroup");
			}
			ft.commit();
			break;
		case ACTION_ADD_FRIEND_FOR_GROUP:
			AddFriendForGroupFragment fragment2=(AddFriendForGroupFragment) fm.findFragmentByTag("addFriendForGroup");
			if(fragment2==null){
				AddFriendForGroupFragment addFriendForGroup = new AddFriendForGroupFragment();
				Bundle args = new Bundle();
		        args.putString("groupId", groupId);
		        args.putString("groupName", groupName);
		        addFriendForGroup.setArguments(args);
		        ft.replace(R.id.add, addFriendForGroup, "addFriendForGroup");
			}
			ft.commit();
			break;
		case ACTION_RELOAD:
			FriendManageFragment friend=(FriendManageFragment) fm.findFragmentByTag("friend");
			GroupManageFragment group=(GroupManageFragment) fm.findFragmentByTag("group");
			if(friend!=null && currentTab.equalsIgnoreCase("Friend"))
				friend.reload();
			if(group!=null && currentTab.equalsIgnoreCase("Group"))
				group.reload();
			break;
		}
		
	}
}

