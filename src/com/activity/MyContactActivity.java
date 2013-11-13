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
	//��¼child��checkbox�Ƿ�ѡ�У����ѡ����HaspMap����
	public static List<HashMap<Integer, Boolean>> mCheckedObj;
	//��¼�û�����id�Ķ�Ӧ��ϵ
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
		InitData();//��һ�ν���ʱ��������
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

			if(count==1){ //��һ�λ�ȡ���õ���������group���ݣ��硰�ҵĺ��ѡ�
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
		        
			}else{  //�ڶ��λ�ȡ���õ�������ʱchild���ݣ�����ϵ�˵����ֺ�id
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
					if(childSize==groupSize){ //���ݼ�����ϣ����Գ�ʼ��mCheckedObj
						System.out.println("Data Loaded......");
				        for(int groupId = 0; groupId < mContactGroupData.size();++groupId){  
				            HashMap<Integer,Boolean> empty = new HashMap<Integer,Boolean>();
				            mCheckedObj.add(empty);  
				        }
				        
				        //��ʼ���Ƿ�ѡ�У�Ĭ��ȫ��δѡ��
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
