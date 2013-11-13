package com.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.http.GetMemberThread;

import android.app.ExpandableListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class MyContactActivity extends ExpandableListActivity implements OnChildClickListener{

	private static ConfMemberAdapter ContactAdapter;
	public static List<String> mContactGroupData;
	public static List<HashMap<Integer, String>> mContactChildData;
	//记录child的checkbox是否被选中，如果选中则HaspMap中有
	public static List<HashMap<Integer, Boolean>> mCheckedObj;
	//记录用户名和id的对应关系
	public static Map<String,Integer> userRecord;
	private static String userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("myContact","OnCreate");
		userId=this.getIntent().getExtras().getString("userID"); 
		ExpandableListView contactELV = this.getExpandableListView();
		contactELV.setBackgroundColor(Color.WHITE);
		contactELV.setPadding(10, 20, 10, 0);
		mContactGroupData = new ArrayList<String>();
		mContactChildData = new ArrayList<HashMap<Integer, String>>();
		mCheckedObj = new ArrayList<HashMap<Integer, Boolean>>();
		userRecord=new HashMap<String,Integer>();
		InitData();//第一次进入时加载数据
		ContactAdapter = new ConfMemberAdapter(getLayoutInflater(), this,
				mContactGroupData, mContactChildData);
		setListAdapter(ContactAdapter);
		
	}

    @Override  
    public void onGroupCollapse (int groupPosition){  
    	Log.i("myContact","onGroupCollapse");
    	setChecked();
    }  
    
    @Override
    public void onGroupExpand (int groupPosition){
    	Log.i("myContact","onGroupExpand");
    	setChecked();
    }

    @Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id){
		Log.i("mycontact","onChildClick");
		ContactAdapter.toggleItemCheckBoxStatus(v, groupPosition,
				childPosition);
		if (mCheckedObj.get(groupPosition).containsKey(childPosition)) {
			mCheckedObj.get(groupPosition).remove(childPosition);
		} else {
			mCheckedObj.get(groupPosition).put(childPosition, true);
		}
				return true;
		
	}

	private void InitData() {
        Map<String, String> params = new HashMap<String,String>();
        params.put("userID", userId);
        params.put("node", "a");
        GetMemberThread contactThread=new GetMemberThread(handler,"contact");
        contactThread.doStart(params);
	}
	

	public void setChecked(){
		System.out.println("mCheckedObjSize:"+mCheckedObj.size()+" mContactGroupDataSize:"+mContactGroupData.size());
		for(int i=0;i<mContactGroupData.size();i++){
			HashMap<Integer, Boolean> checkHM=mCheckedObj.get(i);
			for(Integer key:checkHM.keySet()){
				ContactAdapter.setItemCheckBoxStatus(i, key, true);
			}
		}
	}
	
	private static Handler handler=new Handler(){
		
		int groupSize=0;
		int childSize=0;
		public void handleMessage(Message msg) {
			Bundle data=msg.getData();
			String result=(String) data.get("result");
			int count=data.getInt("count");			
			System.out.println("get Contact data:"+result+" count:"+count);

			if(count==1){ //第一次获取，得到的数据是group数据，如“我的好友”
				try {
					JSONArray array=new JSONArray(result);
					int length=array.length();
					for(int i=0;i<length;i++){
						if(array.getJSONObject(i)==null)
							continue;
						groupSize++;
						JSONObject item=array.getJSONObject(i);
						String group=(String) item.get("text");
						Integer id=(Integer) item.get("id");
						mContactGroupData.add(group);
						ContactAdapter.notifyDataSetChanged();
						Map<String, String> params = new HashMap<String,String>();
				        params.put("userID", userId);
				        params.put("node", id.toString());
				        GetMemberThread contactThread=new GetMemberThread(handler,"contact");
				        try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				        contactThread.doStart(params);
					}
				} catch (JSONException e) {
				}
		        
			}else{  //第二次获取，得到的数据时child数据，如联系人的名字和id
				HashMap<Integer, String> temp = new HashMap<Integer, String>();
				try {
					JSONArray array=new JSONArray(result);
					int length=array.length();	
					for(int i=0;i<length;i++){
						if(array.getJSONObject(i)==null)
							continue;
						JSONObject item=array.getJSONObject(i);
						String childName=(String) item.get("text");
						Integer id=(Integer) item.get("id");
						userRecord.put(childName, id);
						temp.put(i, childName);
					}
				} catch (JSONException e) {
				}finally{
					childSize++;
					mContactChildData.add(temp);
					ContactAdapter.notifyDataSetChanged();
					System.out.println("groupSize:"+groupSize+"childSize:"+childSize);
					if(childSize==groupSize){ //数据加载完毕，可以初始化mCheckedObj
						System.out.println("Data Loaded......");
				        for(int groupId = 0; groupId < mContactGroupData.size();++groupId){  
				            HashMap<Integer,Boolean> empty = new HashMap<Integer,Boolean>();
				            mCheckedObj.add(empty);  
				        }
				        
				        //初始化是否选中，默认全部未选中
				        List<HashMap<Integer,Boolean>> isSelected= new ArrayList<HashMap<Integer,Boolean>>();  
				        for(int groupId = 0; groupId < mContactGroupData.size();++groupId){  
				            HashMap<Integer,Boolean> mChildCheck = new HashMap<Integer,Boolean>();  
				            for(int childId = 0; childId< mContactChildData.get(groupId).size();++childId){  
				                mChildCheck.put(childId, false);  
				            }     
				            isSelected.add(mChildCheck);  
				        }
				        ContactAdapter.setIsSelected(isSelected);
				        
					}
				}
			}

		}
	};
}
