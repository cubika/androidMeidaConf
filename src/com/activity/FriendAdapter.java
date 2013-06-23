package com.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rongdian.R;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class FriendAdapter extends BaseExpandableListAdapter{
    private List<String>      mGroupData = new ArrayList<String>();  
    private List<HashMap<Integer,String>> mChildData= new ArrayList<HashMap<Integer,String>>();   
    private Context               mContext;  
    LayoutInflater                  mInflater;  
    
    public FriendAdapter(LayoutInflater inflater,Context mcontext,List<String> mGroup,List<HashMap<Integer,String>> mChild)   
    {  
        mInflater = inflater;  
        mContext = mcontext;  
        mGroupData = mGroup;  
        mChildData = mChild;  
    }  
    
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildData.get(groupPosition).get(childPosition);  
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		TextView row = (TextView)convertView;
	    if(row == null) {
	      row = new TextView(mContext);
	    }
	    row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
	    row.setPadding(80, 0, 0, 0);
	    row.setText(mChildData.get(groupPosition).get(childPosition));
	    return row;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildData.get(groupPosition).size();  
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);  
	}

	@Override
	public int getGroupCount() {
		return mGroupData.size();  
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition; 
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
//	    TextView row = (TextView)convertView;
//	    if(row == null) {
//	      row = new TextView(mContext);
//	    }
//	    row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
//	    row.setPadding(60, 0, 0, 0);
//	    row.setText(mGroupData.get(groupPosition));
//	    return row;
		
		View v;
		 if(convertView != null)
			 v = convertView;  
	     else
	    	 v = mInflater.inflate(R.layout.conf_member_group_item, null);    
		TextView tv=(TextView)v.findViewById(R.id.groupTV);
		tv.setText(mGroupData.get(groupPosition));
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
