package com.rongdian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.http.HttpUtils;
import com.util.Constants;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class MyContactFragment extends Fragment implements OnGroupCollapseListener,OnGroupExpandListener,OnChildClickListener{

	private static ConfMemberAdapter ContactAdapter;
	public static List<String> mContactGroupData;
	public static List<HashMap<Integer, String>> mContactChildData;
	//记录child的checkbox是否被选中，如果选中则HaspMap中有
	public static List<HashMap<Integer, Boolean>> mCheckedObj;
	//记录用户名和id的对应关系
	public static Map<String,Integer> userRecord;
	private static int contactCount,groupSize,childSize;
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			contactCount=1;
			groupSize=0;
			childSize=0;
			mContactGroupData = new ArrayList<String>();
			mContactChildData = new ArrayList<HashMap<Integer, String>>();
			mCheckedObj = new ArrayList<HashMap<Integer, Boolean>>();
			userRecord=new HashMap<String,Integer>();
			View v=inflater.inflate(R.layout.expandable, container, false);
			ExpandableListView contactELV = (ExpandableListView) v.findViewById(R.id.edlv);
			contactELV.setBackgroundColor(Color.WHITE);
			contactELV.setPadding(10, 20, 10, 0);
			contactELV.setAdapter(ContactAdapter);
			InitData();//加载数据
			contactELV.setOnGroupCollapseListener(this);
			contactELV.setOnGroupExpandListener(this);
			contactELV.setOnChildClickListener(this);
			ContactAdapter = new ConfMemberAdapter(inflater, getActivity(),
					mContactGroupData, mContactChildData);
			contactELV.setAdapter(ContactAdapter);
			 return v;
	 }
	 
	 private void InitData() {
	        Map<String, String> params = new HashMap<String,String>();
	        params.put("userID", PadActivity.userId);
	        params.put("node", "a");
	        new GetContactTask(getActivity(), params).execute(Constants.prefix+"addMember.do?");
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

		public void setChecked(){
			System.out.println("mCheckedObjSize:"+mCheckedObj.size()+" mContactGroupDataSize:"+mContactGroupData.size());
			for(int i=0;i<mContactGroupData.size();i++){
				HashMap<Integer, Boolean> checkHM=mCheckedObj.get(i);
				for(Integer key:checkHM.keySet()){
					ContactAdapter.setItemCheckBoxStatus(i, key, true);
				}
			}
		}
		
		class GetContactTask extends AsyncTask<String,Integer,String>{

			private Context context;
			private Map<String,String> paramMap;
			
			public GetContactTask(Context context,Map<String,String> paramMap){
				this.context=context;
				this.paramMap=paramMap;
			}
			
			@Override
			protected String doInBackground(String... params) {
				Log.v("GetContactTask","doInBackground");
				String url=params[0];
				Log.v("GetContactTask","url is "+url);
				String result=null;
				try {
					result=HttpUtils.sendPostMessage(url, paramMap, "utf-8");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			}
			
			@Override
			protected void onPostExecute(String result){
				Log.v("GetContactTask","onPostExecute");
				Log.v("GetContactTask","result is:"+result);
				if(result!=null && result.trim().length()!=0){
					if(contactCount==1){ //第一次获取，得到的数据是group数据，如“我的好友”
						try {
							contactCount++;
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
								Map<String, String> paramMap2 = new HashMap<String,String>();
								paramMap2.put("userID", PadActivity.userId);
								paramMap2.put("node", id.toString());
						        new GetContactTask(context, paramMap2).execute(Constants.prefix+"addMember.do?");
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
			}
		}
}
