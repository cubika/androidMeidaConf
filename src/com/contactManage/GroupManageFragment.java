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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.http.GenericTask;
import com.http.HttpUtils;
import com.rongdian.PadActivity;
import com.rongdian.R;
import com.util.Constants;

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
	private ContactListener mListener;//用来通知activity事件的接口，让activity控制右边fragment的变化
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("GroupManageFragment","onCreateView");
		View V = inflater.inflate(R.layout.group_manage, container, false);
		groupELV = (ExpandableListView) V.findViewById(R.id.groupELV);
		addGroupBTN=(Button)V.findViewById(R.id.addGroupGroup);
		addGroupBTN.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mListener.notify(2, null,null);
			}
			
		});
		
		//绑定数据
		groupAdapter = new GroupAdapter(inflater, getActivity(),
				mGroupGroupData, mGroupChildData);
		groupELV.setAdapter(groupAdapter);
		//绑定长按事件
		groupELV.setOnItemLongClickListener(new OnItemLongClickListener() {
		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		          int itemType = ExpandableListView.getPackedPositionType(id);

		          if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) { //长按的是子节点
		              childPosition = ExpandableListView.getPackedPositionChild(id);
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition+" childPosition:"+childPosition);
		              
		              new CheckHostTask().execute(Constants.prefix+"group.do?method=checkDeleteHost"
								+"&userID="+PadActivity.userId
								+"&groupID="+mGroupGroupIDS.get(groupPosition)
								+"&friendID="+mGroupChildIDS.get(groupPosition).get(childPosition)); //检查是否是群主
		              
		             
		              return true; 

		          } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) { //长按的是group节点
		              groupPosition = ExpandableListView.getPackedPositionGroup(id);
		              
		              System.out.println("groupPosition:"+groupPosition);
		              
		              ListView listview=new ListView(getActivity());
		              String[] options = new String[] { "查看群信息","增加联系人","离开群组"};  
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
							if(arg2==0){ //查看群信息
								new GetInfoTask().execute("http://" + Constants.registarIp 
										+ ":8888/MediaConf/group.do?method=searchGroup&groupID="
										+mGroupGroupIDS.get(groupPosition));
							}else if(arg2==1){ //增加联系人
								new CheckHostTask2().execute(Constants.prefix+"group.do?method=checkHost"
										+"&userID="+PadActivity.userId
										+"&groupID="+mGroupGroupIDS.get(groupPosition));//检查是否是群主
							}else if(arg2==2){ //离开群组
					              DialogInterface.OnClickListener leaveListener=new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(which==DialogInterface.BUTTON_POSITIVE){	//删除联系人
												dialog.dismiss();
												new LeaveGroupTask().execute(Constants.prefix+"group.do?method=leaveGroup"
														+"&groupID="+mGroupGroupIDS.get(groupPosition)
														+"&userID="+PadActivity.userId);
												
											}else if(which==DialogInterface.BUTTON_NEGATIVE){
												dialog.dismiss();
											}
										}
						            	  
						              };
						              //离开群组
						              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
						              builder.setTitle("提示信息");
						              builder.setMessage("你确定要离开群组吗？")
						              .setPositiveButton("确定", leaveListener)
						              .setNegativeButton("取消", leaveListener)
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
						new GetgroupTask().execute(Constants.prefix+"addMember.do?userID="
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
						if(which==DialogInterface.BUTTON_POSITIVE){	//删除联系人
							dialog.dismiss();
							new GenericTask(getActivity()).execute(Constants.prefix+"group.do?method=delGroupStuff"
									+"&groupID="+mGroupGroupIDS.get(groupPosition)
									+"&friendID="+mGroupChildIDS.get(groupPosition).get(childPosition));
							
						}else if(which==DialogInterface.BUTTON_NEGATIVE){
							dialog.dismiss();
						}
					}
	            	  
	              };
	              //删除联系人
	              AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
	              builder.setTitle("提示信息");
	              builder.setMessage("你确定要删除这位群组成员吗？")
	              .setPositiveButton("确定", childListener)
	              .setNegativeButton("取消", childListener)
	              .show();
			}else if(result.equals("delSelfError")){
				Toast.makeText(getActivity(), "用户不能删除自己，请使用退出群！", Toast.LENGTH_SHORT).show();
			}else if(result.equals("nonHostError")){
				Toast.makeText(getActivity(), "非群主用户，不能使用删除操作！", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "删除联系人操作失败！", Toast.LENGTH_SHORT).show();
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
				//可以增加联系人
				mListener.notify(3, mGroupGroupIDS.get(groupPosition).toString(), mGroupGroupData.get(groupPosition));
			}else if(result.equals("nonHostError")){
				Toast.makeText(getActivity(), "非群主用户，不能使用添加成员操作！", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "增加联系人操作失败！", Toast.LENGTH_SHORT).show();
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
			
			String[] titleArray={"群组ID","群组名称","管理员名称"};
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("查看群组信息");
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
				Toast.makeText(getActivity(), "离开群组成功！", Toast.LENGTH_LONG).show();
				reload();
			}else if(result.equals("dismiss")){
				Toast.makeText(getActivity(), "解散群组！", Toast.LENGTH_LONG).show();
				reload();
			}else{
				Toast.makeText(getActivity(), "离开群组操作失败！", Toast.LENGTH_LONG).show();
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
		//第一次进入的时候，数据源是空的，ListView绑定到了它上面；
		//第二次进入的时候，ListView相当与重新建了一个，但是数据源已经不是空的了，然后你又重新加载，将数据源清空，但是没有通知ListView，因此会报错		
		groupAdapter.notifyDataSetChanged(); 
		new GetgroupTask().execute(Constants.prefix+"addMember.do?userID="
				+ PadActivity.userId+"&node=b");
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Log.i("GroupManageFragment","onStart");
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
