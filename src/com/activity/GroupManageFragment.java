package com.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.http.HttpUtils;
import com.rongdian.R;
import com.util.Constants;
import com.util.MyToast;

public class GroupManageFragment extends Fragment {
	
	private ExpandableListView groupELV;
	private GroupAdapter groupAdapter;
	private static List<String> mGroupGroupData = new ArrayList<String>();
	private static List<Integer> mGroupGroupIDS=new ArrayList<Integer>();
	private static List<HashMap<Integer, String>> mGroupChildData = new ArrayList<HashMap<Integer, String>>();
	private static List<HashMap<Integer, Integer>> mGroupChildIDS = new ArrayList<HashMap<Integer, Integer>>();
	private static int count = 1,groupCount=0,childCount=0;
	private int groupPosition,childPosition;
	private Button addGroupBTN;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View V = inflater.inflate(R.layout.group_manage, container, false);
		groupELV = (ExpandableListView) V.findViewById(R.id.groupELV);
		addGroupBTN=(Button)V.findViewById(R.id.addGroupGroup);
		addGroupBTN.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),AddGroupActivity.class);
				startActivity(intent);
			}
			
		});
		
		//������
		groupAdapter = new GroupAdapter(inflater, getActivity(),
				mGroupGroupData, mGroupChildData);
		groupELV.setAdapter(groupAdapter);
		//�󶨳����¼�
		groupELV.setOnItemLongClickListener(new OnItemLongClickListener() {
		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		          int itemType = ExpandableListView.getPackedPositionType(id);

		          if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) { //���������ӽڵ�
		              childPosition = ExpandableListView.getPackedPositionChild(id);
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition+" childPosition:"+childPosition);
		              
		              new CheckHostTask().execute("http://" + Constants.registarIp   //����Ƿ���Ⱥ��
								+ ":8888/MediaConf/group.do?method=checkDeleteHost"
								+"&userID="+ContactManageActivity.userId
								+"&groupID="+mGroupGroupIDS.get(groupPosition)
								+"&friendID="+mGroupChildIDS.get(groupPosition).get(childPosition));
		              
		             
		              return true; 

		          } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) { //��������group�ڵ�
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition);
		              
		              ListView listview=new ListView(getActivity());
		              String[] options = new String[] { "�鿴Ⱥ��Ϣ","������ϵ��","�뿪Ⱥ��"};  
		              List<String> optionList=new ArrayList<String>(Arrays.asList(options));
		              ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		            		  							android.R.layout.simple_expandable_list_item_1,optionList);
		              listview.setAdapter(adapter);
		              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		              builder.setView(listview);
		              final AlertDialog dialog=builder.setInverseBackgroundForced(true).show();
		              
		              listview.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							dialog.dismiss();
							if(arg2==0){ //�鿴Ⱥ��Ϣ
								new GetInfoTask().execute("http://" + Constants.registarIp 
										+ ":8888/MediaConf/group.do?method=searchGroup&groupID="
										+mGroupGroupIDS.get(groupPosition));
							}else if(arg2==1){ //������ϵ��
								new CheckHostTask2().execute("http://" + Constants.registarIp   //����Ƿ���Ⱥ��
										+ ":8888/MediaConf/group.do?method=checkHost"
										+"&userID="+ContactManageActivity.userId
										+"&groupID="+mGroupGroupIDS.get(groupPosition));
							}else if(arg2==2){ //�뿪Ⱥ��
					              DialogInterface.OnClickListener leaveListener=new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(which==DialogInterface.BUTTON_POSITIVE){	//ɾ����ϵ��
												dialog.dismiss();
												new LeaveGroupTask().execute("http://" + Constants.registarIp
														+ ":8888/MediaConf/group.do?method=leaveGroup"
														+"&groupID="+mGroupGroupIDS.get(groupPosition)
														+"&userID="+ContactManageActivity.userId);
												
											}else if(which==DialogInterface.BUTTON_NEGATIVE){
												dialog.dismiss();
											}
										}
						            	  
						              };
						              //�뿪Ⱥ��
						              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
						              builder.setTitle("��ʾ��Ϣ");
						              builder.setMessage("��ȷ��Ҫ�뿪Ⱥ����")
						              .setPositiveButton("ȷ��", leaveListener)
						              .setNegativeButton("ȡ��", leaveListener)
						              .show();
							}
						}
					});
		              
		              return true;

		          } else {
		              // null item; we don't consume the click
		              return false;
		          }
		      }
		  });
		return V;
	}

	class GetgroupTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("GetgroupTask", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("GetgroupTask", "result is:" + result);
			Log.v("GetgroupTask", "count is:" + count);
			if (count == 1) {
				count++;
				JSONArray array;
				try {
					array = new JSONArray(result);
					int length = array.length();
					for (int i = 0; i < length; i++) {
						if (array.getJSONObject(i) == null)
							continue;
						groupCount++;
						JSONObject item = array.getJSONObject(i);
						String group = (String) item.get("text");
						Integer id = (Integer) item.get("id");
						mGroupGroupData.add(group);
						mGroupGroupIDS.add(id);
						//groupAdapter.notifyDataSetChanged();
				        try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						new GetgroupTask().execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/addMember.do?userID="
								+ ContactManageActivity.userId+"&node="+id.toString());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else {
				count++;
				childCount++;
				HashMap<Integer, String> temp = new HashMap<Integer, String>();
				HashMap<Integer, Integer> temp2 = new HashMap<Integer, Integer>();
				try {
					JSONArray array = new JSONArray(result);
					int length = array.length();
					for (int i = 0; i < length; i++) {
						if (array.getJSONObject(i) == null)
							continue;
						JSONObject item = array.getJSONObject(i);
						String childName = (String) item.get("text");
						Integer id = (Integer) item.get("id");
						temp.put(i, childName);
						temp2.put(i, id);
					}
				} catch (JSONException e) {
				} finally {
					mGroupChildData.add(temp);
					mGroupChildIDS.add(temp2);
					System.out.println("groupCount:"+groupCount+" childCount:"+childCount);
					if(groupCount==childCount)
						groupAdapter.notifyDataSetChanged();
				}
			}
		}

	}
	
	class CheckHostTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("CheckHostTask", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("CheckHostTask", "result is:" + result);
			if(result==null)
				return;
			if(result.equals("success")){
	              DialogInterface.OnClickListener childListener=new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which==DialogInterface.BUTTON_POSITIVE){	//ɾ����ϵ��
							dialog.dismiss();
							new GenericTask(getActivity()).execute("http://" + Constants.registarIp
									+ ":8888/MediaConf/group.do?method=delGroupStuff"
									+"&groupID="+mGroupGroupIDS.get(groupPosition)
									+"&friendID="+mGroupChildIDS.get(groupPosition).get(childPosition));
							
						}else if(which==DialogInterface.BUTTON_NEGATIVE){
							dialog.dismiss();
						}
					}
	            	  
	              };
	              //ɾ����ϵ��
	              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
	              builder.setTitle("��ʾ��Ϣ");
	              builder.setMessage("��ȷ��Ҫɾ����λȺ���Ա��")
	              .setPositiveButton("ȷ��", childListener)
	              .setNegativeButton("ȡ��", childListener)
	              .show();
			}else if(result.equals("delSelfError")){
				Toast.makeText(getActivity(), "�û�����ɾ���Լ�����ʹ���˳�Ⱥ��", Toast.LENGTH_SHORT).show();
			}else if(result.equals("nonHostError")){
				Toast.makeText(getActivity(), "��Ⱥ���û�������ʹ��ɾ��������", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "ɾ����ϵ�˲���ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	class CheckHostTask2 extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("CheckHostTask2", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("CheckHostTask2", "result is:" + result);
			if(result==null)
				return;
			if(result.equals("success")){
				//����������ϵ��
				Intent intent = new Intent(getActivity(),AddContactActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("groupID", mGroupGroupIDS.get(groupPosition).toString());
				bundle.putString("groupName", mGroupGroupData.get(groupPosition));
				intent.putExtras(bundle);
				startActivity(intent);
			}else if(result.equals("nonHostError")){
				Toast.makeText(getActivity(), "��Ⱥ���û�������ʹ����ӳ�Ա������", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "������ϵ�˲���ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	class GetInfoTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("GetInfoTask", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("GetInfoTask", "result is:" + result);
			if(result==null)
				return;
			result=result.replaceAll("\\[\\[", "");
			result=result.replaceAll("\\]\\]", "");
			String[] infos=result.split(",");
			
			String[] titleArray={"Ⱥ��ID","Ⱥ������","����Ա����"};
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("�鿴Ⱥ����Ϣ");
			ListView lv=new ListView(getActivity());
			ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < titleArray.length; i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", titleArray[i]);
				map.put("content", infos[i]);
				data.add(map);
			}
			SimpleAdapter simperAdapter = new SimpleAdapter(getActivity(), data,
					android.R.layout.simple_expandable_list_item_2, new String[] {
							"title", "content" }, new int[] { android.R.id.text1,
							android.R.id.text2 });
			lv.setAdapter(simperAdapter);
			builder.setView(lv);
			builder.setInverseBackgroundForced(true).show();
		}

	}
	
	class LeaveGroupTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("LeaveGroupTask", "url is:" + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("LeaveGroupTask", "result is:" + result);
			if(result==null)
				return;
			if(result.equals("leave")){
				Toast.makeText(getActivity(), "�뿪Ⱥ��ɹ���", Toast.LENGTH_SHORT).show();
				reload();
			}else if(result.equals("dismiss")){
				Toast.makeText(getActivity(), "��ɢȺ�飡", Toast.LENGTH_SHORT).show();
				reload();
			}else{
				Toast.makeText(getActivity(), "�뿪Ⱥ�����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	public void reload(){
		count=1;
		groupCount=0;
		childCount=0;
		mGroupChildData.clear();
		mGroupGroupData.clear();
		mGroupChildIDS.clear();
		mGroupGroupIDS.clear();
		new GetgroupTask().execute("http://" + Constants.registarIp
				+ ":8888/MediaConf/addMember.do?userID="
				+ ContactManageActivity.userId+"&node=b");
	}
	
	@Override
	public void onStart(){
		super.onStart();
		reload();
	}

}
