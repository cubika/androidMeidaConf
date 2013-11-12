package com.rongdian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfMemberAdapter extends BaseExpandableListAdapter {  
    private List<String>      mGroupData;
    private List<HashMap<Integer,String>> mChildData;
    private Context               mContext;  
    LayoutInflater                  mInflater;  
      
    // 用来控制CheckBox的选中状况    
    private  List<HashMap<Integer,Boolean>> isSelected;    //notifyDataSetChanged不能更改isSelected的大小
      
    public ConfMemberAdapter(LayoutInflater inflater,Context mcontext,List<String> mGroup,List<HashMap<Integer,String>> mChild)   
    {  
        mInflater = inflater;  
        mContext = mcontext;  
        //isSelected = new ArrayList<HashMap<Integer,Boolean>>();  
        mGroupData = mGroup;  
        mChildData = mChild;  
    }    
      
//    public void initSelectedMap(){  
//        //默认全部未勾选状态。  
//        for(int groupId = 0; groupId < mGroupData.size();++groupId){  
//            HashMap<Integer,Boolean> mChildCheck = new HashMap<Integer,Boolean>();  
//            for(int childId = 0; childId< mChildData.get(groupId).size();++childId){  
//                mChildCheck.put(childId, false);  
//            }     
//            isSelected.add(mChildCheck);  
//        }  
//    }  
  
    public void setListViewData(List<String> mGroup,List<HashMap<Integer,String>> mChild){  
        mGroupData = mGroup;  
        mChildData = mChild;  
    }  
      
    public List<HashMap<Integer, Boolean>> getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(List<HashMap<Integer, Boolean>> isSelected) {
		this.isSelected = isSelected;
	}

	public void toggleItemCheckBoxStatus(View mView,int groupPosition,int childPostion){  
  
        ViewTag holder = (ViewTag) mView.getTag();    
        // 改变CheckBox的状态    
        holder.mCheckBox.toggle();    
        // 将CheckBox的选中状况记录下来    
        isSelected.get(groupPosition).put(childPostion, holder.mCheckBox.isChecked());   
  
    }  
    
    public void setItemCheckBoxStatus(int groupPosition,int childPostion,boolean mFlag){  
        isSelected.get(groupPosition).put(childPostion, mFlag);
    }  
      
    public void setAllCheckBoxStatus(Boolean mFlag){  
        for(int groupId = 0; groupId < mGroupData.size();++groupId){  
            for(int childId = 0; childId< mChildData.get(groupId).size();++childId){  
                isSelected.get(groupId).put(childId, mFlag);  
            }         
        }  
  
    }  
  
    public class ViewTag{  
        public TextView mTextView;  
        public CheckBox mCheckBox;  
        public ImageView mIcon;  
    }  
  
    @Override  
    public Object getChild(int groupPosition, int childPosition) {  
        // TODO Auto-generated method stub  
        return mChildData.get(groupPosition).get(childPosition);  
    }  
  
    @Override  
    public long getChildId(int groupPosition, int childPosition) {  
        // TODO Auto-generated method stub  
        return childPosition;  
    }  
  
    @Override  
    public View getChildView(int groupPosition, int childPosition,  
            boolean isLastChild, View convertView, ViewGroup parent) {  
        // TODO Auto-generated method stub  
        ViewTag mVobj = new ViewTag();  
        View v;  
          
        if(convertView != null){  
            v = convertView;  
        }  
        else{  
            v = mInflater.inflate(R.layout.conf_member_child_item, null);      
        }  
        mVobj.mTextView = (TextView) v.findViewById(R.id.childTV);  
        mVobj.mCheckBox = (CheckBox) v.findViewById(R.id.childCB);  
        mVobj.mCheckBox.setChecked(isSelected.get(groupPosition).get(childPosition));  
        mVobj.mTextView.setText(mChildData.get(groupPosition).get(childPosition));  
        v.setTag(mVobj);  
        return v;  
    }  
  
    @Override  
    public int getChildrenCount(int groupPosition) {  
        // TODO Auto-generated method stub  
        return mChildData.get(groupPosition).size();  
    }  
  
    @Override  
    public Object getGroup(int groupPosition) {  
        // TODO Auto-generated method stub  
        return mGroupData.get(groupPosition);  
    }  
  
    @Override  
    public int getGroupCount() {  
        // TODO Auto-generated method stub  
        return mGroupData.size();  
    }  
  
    @Override  
    public long getGroupId(int groupPosition) {  
        // TODO Auto-generated method stub  
        return groupPosition;  
    }  
  
    @Override  
    public View getGroupView(int groupPosition, boolean isExpanded,  
            View convertView, ViewGroup parent) {  
        // TODO Auto-generated method stub  
        ViewTag mVobj = new ViewTag();  
        View v;  
          
        if(convertView != null){  
            v = convertView;  
        }  
        else{  
            v = mInflater.inflate(R.layout.conf_member_group_item, null);    
        }  
        mVobj.mTextView = (TextView) v.findViewById(R.id.groupTV);  
        mVobj.mTextView.setText(mGroupData.get(groupPosition));  
 //       mVobj.mIcon = (ImageView) v.findViewById(R.id.icon);  
          
        v.setTag(mVobj);  
          
//         if (isExpanded) {   
//             mVobj.mIcon.setImageResource(R.drawable.up);   
//         } else {   
//             mVobj.mIcon.setImageResource(R.drawable.down);   
//         }   
        return v;  
    	
    }  
  
    @Override  
    public boolean hasStableIds() {  
        // TODO Auto-generated method stub  
        return false;  
    }  
  
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}  
