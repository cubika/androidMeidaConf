package com.navDrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rongdian.R;

public class MyAdapter extends ArrayAdapter<ListItem>{
	public MyAdapter(Context context) {
		super(context, 0);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			//自定义listview的layout
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
		}
		ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
		icon.setImageDrawable(getItem(position).icon);
		TextView title = (TextView) convertView.findViewById(R.id.row_title);
		title.setText(getItem(position).title);

		return convertView;
	}
}