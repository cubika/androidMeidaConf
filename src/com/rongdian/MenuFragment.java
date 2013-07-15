package com.rongdian;

import com.contactManage.ContactManageActivity;
import com.http.GenericTask;
import com.util.Constants;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuFragment extends PreferenceFragment implements OnPreferenceClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //set the preference xml to the content view
        addPreferencesFromResource(R.xml.menu);
        //add listener
        findPreference("home").setOnPreferenceClickListener(this);
        findPreference("createConf").setOnPreferenceClickListener(this);
        findPreference("confManage").setOnPreferenceClickListener(this);
        findPreference("accountManage").setOnPreferenceClickListener(this);
        findPreference("contactManage").setOnPreferenceClickListener(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 20, 0, 0);
            
            lv.setBackgroundColor(Color.rgb(4, 26, 55)); 
        }
        return v;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        ((PadActivity)getActivity()).getSlidingMenu().toggle();
        if("home".equals(key)) {  //��ʾ��ҳ�Ļ��������е�fragment��ջ���õ�������
            FragmentManager fm = getActivity().getFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
                fm.popBackStack();
            }
        }else if("createConf".equals(key)) {
        	Intent intent=new Intent(getActivity(),CreateConfActivity.class);
        	startActivity(intent);
        }else if("confManage".equals(key)) {
        	//anyway , show the sliding menu
			if (PadActivity.confId == -1) {
				Toast.makeText(getActivity(), "����û�м�����飬�޷����л������",
						Toast.LENGTH_LONG).show();
				return false;
			}
			System.out.println("������ϯ��"+PadActivity.chairmanId+" userId:"+PadActivity.userId);
			if(PadActivity.chairmanId.trim().equals(PadActivity.userId)){ //���û�Ϊ��ϯ
	            FragmentTransaction ft = getFragmentManager().beginTransaction();
	            DialogFragment prev = (DialogFragment) getFragmentManager().findFragmentByTag("confManageDialog");
	            if (prev != null) {
	            	prev.dismiss();
	                ft.remove(prev);
	            }
	            ft.addToBackStack(null);
	            ConfManageFragment confFragment=new ConfManageFragment();
	            confFragment.show(ft, "confManageDialog");
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				AlertDialog dialog=
						builder.setTitle("���뷢��").setMessage("��������ϯ��ֻ�����뷢�ԡ�\n ��ȷ��Ҫ���뷢����")
						.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							GenericTask applyTask=new GenericTask(getActivity());
							applyTask.execute(Constants.prefix+"applySpeaking.do?confId="
									+PadActivity.confId+"&userId="+PadActivity.userId);
							dialog.dismiss();
							
						}
						
					}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
						
					}).show();
				TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
			}

			return true;
        }else if("accountManage".equals(key)) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment prev = (DialogFragment) getFragmentManager().findFragmentByTag("accountManageDialog");
            if (prev != null) {
            	prev.dismiss();
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            AccountManageFragment accountFragment=new AccountManageFragment();
            accountFragment.show(ft, "accountManageDialog");

        }else if("contactManage".equals(key)) {
        	Intent intent=new Intent(getActivity(),ContactManageActivity.class);
        	startActivity(intent);
        }
        return false;
    }
}