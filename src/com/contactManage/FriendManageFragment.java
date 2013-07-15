package com.contactManage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.http.HttpUtils;
import com.rongdian.PadActivity;
import com.rongdian.R;
import com.util.Constants;
import com.util.MyToast;

public class FriendManageFragment extends Fragment {

	private ExpandableListView friendELV;
	private FriendAdapter friendAdapter;
	private static List<String> mContactGroupData = new ArrayList<String>();
	private static List<Integer> mContactGroupIDS=new ArrayList<Integer>();
	private static List<HashMap<Integer, String>> mContactChildData = new ArrayList<HashMap<Integer, String>>();
	private static List<HashMap<Integer, Integer>> mContactChildIDS = new ArrayList<HashMap<Integer, Integer>>();
	private static int count = 1,groupCount=0,childCount=0;
	private int groupPosition,childPosition;
	private Button addGroupBTN;
	private ContactListener mListener;//����֪ͨactivity�¼��Ľӿڣ���activity�����ұ�fragment�ı仯

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v("FriendManageFragment","onCreateView");
		View V = inflater.inflate(R.layout.friend_manage, container, false);
		friendELV = (ExpandableListView) V.findViewById(R.id.friendELV);
		addGroupBTN=(Button)V.findViewById(R.id.addFriendGroup);

		addGroupBTN.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
				final EditText newGroup=new EditText(getActivity());
				 DialogInterface.OnClickListener addGroupListener=new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which==DialogInterface.BUTTON_POSITIVE){
								dialog.dismiss();
								if(newGroup.getText().toString()==null || newGroup.getText().toString().trim().length()<=0){
									MyToast.openToast(getActivity(), "�¼ӵķ��鲻��Ϊ��");
									return;
								}
								new GroupTask().execute(Constants.prefix+"cataManage.do?method=newCata&userID="
										+ PadActivity.userId+"&cataName="+newGroup.getText().toString().trim());
							}else if(which==DialogInterface.BUTTON_NEGATIVE){
								dialog.dismiss();
							}
						}
		            	  
		              };
				builder.setTitle("��������").setView(newGroup)
						.setPositiveButton("ȷ��", addGroupListener)
						.setNegativeButton("ȡ��", addGroupListener)
						.show();
			}
			
		});
		
		// ��������
//		new GetFriendTask().execute("http://" + Constants.registarIp
//				+ ":8888/MediaConf/addMember.do?userID="
//				+ ContactManageActivity.userId+"&node=a");
		//������
		friendAdapter = new FriendAdapter(inflater, getActivity(),
				mContactGroupData, mContactChildData);
		friendELV.setAdapter(friendAdapter);
		//�󶨳����¼�
		friendELV.setOnItemLongClickListener(new OnItemLongClickListener() {
		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		          int itemType = ExpandableListView.getPackedPositionType(id);

		          if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) { //���������ӽڵ�
		              childPosition = ExpandableListView.getPackedPositionChild(id);
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition+" childPosition:"+childPosition);
		              
		              DialogInterface.OnClickListener childListener=new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which==DialogInterface.BUTTON_POSITIVE){	//ɾ������
								dialog.dismiss();
								new GroupTask().execute(Constants.prefix+"cataManage.do?method=deleteFriend&cataID="+mContactGroupIDS.get(groupPosition)
										+"&friendID="+mContactChildIDS.get(groupPosition).get(childPosition));
								
							}else if(which==DialogInterface.BUTTON_NEGATIVE){
								dialog.dismiss();
							}
						}
		            	  
		              };
		              //ɾ������
		              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		              builder.setTitle("��ʾ��Ϣ");
		              builder.setMessage("��ȷ��Ҫɾ����λ������")
		              .setPositiveButton("ȷ��", childListener)
		              .setNegativeButton("ȡ��", childListener)
		              .show();
		             
		              return true; 

		          } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) { //��������group�ڵ�
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition);
		              
		              //���������飬��Ӻ���
		              ListView listview=new ListView(getActivity());
		              String[] options = new String[] { "������","��Ӻ���","ɾ������"};  
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
							if(arg2==0){ //������
								AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
								final EditText renameGroup=new EditText(getActivity());
								 DialogInterface.OnClickListener renameGroupListener=new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(which==DialogInterface.BUTTON_POSITIVE){
												dialog.dismiss();
												if(renameGroup.getText().toString()==null || renameGroup.getText().toString().trim().length()<=0){
													MyToast.openToast(getActivity(), "�������ķ��鲻��Ϊ��");
													return;
												}
												//�ύ
												new GroupTask().execute(Constants.prefix+"cataManage.do?method=updateCataName&userID="
														+ PadActivity.userId+"&cataID="+mContactGroupIDS.get(groupPosition)
														+"&newCataName="+renameGroup.getText().toString());
											}else if(which==DialogInterface.BUTTON_NEGATIVE){
												dialog.dismiss();
											}
										}
						            	  
						              };
								builder.setTitle("���÷���������Ϊ").setView(renameGroup)
										.setPositiveButton("ȷ��", renameGroupListener)
										.setNegativeButton("ȡ��", renameGroupListener)
										.show();
							}else if(arg2==1){ //��Ӻ���
								mListener.notify(1, mContactGroupIDS.get(groupPosition).toString(),null); //֪ͨ�ұ߱�Ϊ���Ӻ��ѵ�fragment
							}else if(arg2==2){ //ɾ������
					              DialogInterface.OnClickListener deleteListener=new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(which==DialogInterface.BUTTON_POSITIVE){
												dialog.dismiss();
												new DeleteGroupTask().execute(Constants.prefix+"cataManage.do?method=deleteCata&userID="
														+ PadActivity.userId+"&cataID="+mContactGroupIDS.get(groupPosition));
												
											}else if(which==DialogInterface.BUTTON_NEGATIVE){
												dialog.dismiss();
											}
										}
						            	  
						              };
						              //ɾ������
						              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
						              builder.setTitle("��ʾ��Ϣ");
						              builder.setMessage("��ȷ��Ҫɾ���÷�����")
						              .setPositiveButton("ȷ��", deleteListener)
						              .setNegativeButton("ȡ��", deleteListener)
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
	class GetFriendTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("GetFriendTask", "url is:" + url);
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
			Log.v("GetFriendTask", "result is:" + result);
			Log.v("GetFriendTask", "count is:" + count);
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
						mContactGroupData.add(group);
						mContactGroupIDS.add(id);
						//friendAdapter.notifyDataSetChanged();
				        try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						new GetFriendTask().execute(Constants.prefix+"addMember.do?userID="
								+ PadActivity.userId+"&node="+id.toString());
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
					mContactChildData.add(temp);
					mContactChildIDS.add(temp2);
					System.out.println("groupCount:"+groupCount+" childCount:"+childCount);
					if(groupCount==childCount)
						friendAdapter.notifyDataSetChanged();
				}
			}
		}

	}
	
	
	class GroupTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("GroupTask", "url is:" + url);
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
			Log.v("AddGroupTask", "result is:" + result);
			if(result!=null && result.equals("success")){
				//���¼���һ��
				reload();
			}else{
				Toast.makeText(getActivity(), "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	
	class DeleteGroupTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("DeleteGroupTask", "url is:" + url);
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
			Log.v("DeleteGroupTask", "result is:" + result);
			if(result==null)
				return;
			if(result.equals("success")){
				//���¼���һ��
				reload();
			}else if(result.equals("defaultCataError")){
				Toast.makeText(getActivity(), "Ĭ�Ϸ��಻��ɾ����", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	public void reload(){
		count=1;
		groupCount=0;
		childCount=0;
		mContactChildData.clear();
		mContactGroupData.clear();
		mContactChildIDS.clear();
		mContactGroupIDS.clear();
		friendAdapter.notifyDataSetChanged();
		new GetFriendTask().execute("http://" + Constants.registarIp
				+ ":8888/MediaConf/addMember.do?userID="
				+ PadActivity.userId+"&node=a");
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Log.v("FriendManageFragment","onStart");
		reload();
	}
	
	 @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mListener = (ContactListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement listeners!");
	        }
	    }

}