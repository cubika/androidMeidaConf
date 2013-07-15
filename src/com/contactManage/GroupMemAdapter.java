package com.contactManage;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.rongdian.R;

//这个adapter是为了添加群组时放ListView用的
public class GroupMemAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, String>> data;
	private LayoutInflater mInflater;  

	public GroupMemAdapter(LayoutInflater inflater,
			ArrayList<HashMap<String, String>> data) {
		this.data = data;
		mInflater = inflater; 
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.group_item, null);
			holder.userNameTV = (TextView) convertView.findViewById(R.id.unTV);
			holder.userIDTV = (TextView) convertView.findViewById(R.id.uiTV);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//获取ViewHolder中所填入的数据
		holder.userNameTV.setText((String)(data.get(position).get("userName")));
		holder.userIDTV.setText((String)(data.get(position).get("userID")));
		holder.checkbox.setChecked(data.get(position).get("flag").equals("true"));
		
		return convertView;

	}
	
	class ViewHolder {
		public CheckBox checkbox;
		public TextView userNameTV;
		public TextView userIDTV;
	}

}
