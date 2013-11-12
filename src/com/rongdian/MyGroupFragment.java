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

public class MyGroupFragment extends Fragment implements
		OnGroupCollapseListener, OnGroupExpandListener, OnChildClickListener {

	private static ConfMemberAdapter GroupAdapter;
	public static List<String> mGroupGroupData;
	public static List<HashMap<Integer, String>> mGroupChildData;
	public static List<HashMap<Integer, Boolean>> mCheckedObj;
	public static Map<String, Integer> userRecord;
	private static int groupCount,groupSize,childSize;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.expandable, container, false);
		ExpandableListView groupELV = (ExpandableListView) v
				.findViewById(R.id.edlv);
		groupELV.setBackgroundColor(Color.WHITE);
		groupELV.setPadding(10, 20, 10, 0);
		groupCount = 1;
		groupSize = 0;
		childSize = 0;
		mGroupGroupData = new ArrayList<String>();
		mGroupChildData = new ArrayList<HashMap<Integer, String>>();
		mCheckedObj = new ArrayList<HashMap<Integer, Boolean>>();
		userRecord = new HashMap<String, Integer>();
		InitData();
		groupELV.setOnGroupCollapseListener(this);
		groupELV.setOnGroupExpandListener(this);
		groupELV.setOnChildClickListener(this);
		GroupAdapter = new ConfMemberAdapter(inflater, getActivity(),
				mGroupGroupData, mGroupChildData);
		groupELV.setAdapter(GroupAdapter);
		return v;
	}

	private void InitData() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("userID", PadActivity.userId);
		params.put("node", "b");
		 new GetGroupTask(getActivity(), params).execute(Constants.prefix+"addMember.do?");

		for (int groupId = 0; groupId < mGroupGroupData.size(); ++groupId) {
			HashMap<Integer, Boolean> temp = new HashMap<Integer, Boolean>();
			mCheckedObj.add(temp);
		}

	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		Log.i("myContact", "onGroupCollapse");
		setChecked();
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		Log.i("myContact", "onGroupExpand");
		setChecked();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Log.i("mycontact", "onChildClick");
		GroupAdapter.toggleItemCheckBoxStatus(v, groupPosition, childPosition);
		if (mCheckedObj.get(groupPosition).containsKey(childPosition)) {
			mCheckedObj.get(groupPosition).remove(childPosition);
		} else {
			mCheckedObj.get(groupPosition).put(childPosition, true);
		}
		return true;

	}

	public void setChecked() {
		for (int i = 0; i < mGroupGroupData.size(); i++) {
			HashMap<Integer, Boolean> checkHM = mCheckedObj.get(i);
			for (Integer key : checkHM.keySet()) {
				GroupAdapter.setItemCheckBoxStatus(i, key, true);
			}
		}
	}

	class GetGroupTask extends AsyncTask<String, Integer, String> {

		private Context context;
		private Map<String, String> paramMap;

		public GetGroupTask(Context context, Map<String, String> paramMap) {
			this.context = context;
			this.paramMap = paramMap;
		}

		@Override
		protected String doInBackground(String... params) {
			Log.v("GetContactTask", "doInBackground");
			String url = params[0];
			Log.v("GetContactTask", "url is " + url);
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(url, paramMap, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("GetGroupTask", "onPostExecute");
			Log.v("GetGroupTask", "result is:" + result);
			if (result != null && result.trim().length() != 0) {
				if (groupCount == 1) { // 第一次获取，得到的数据是group数据，如“我的好友”
					try {
						groupCount++;
						JSONArray array = new JSONArray(result);
						int length = array.length();
						for (int i = 0; i < length; i++) {
							if (array.getJSONObject(i) == null)
								continue;
							groupSize++;
							JSONObject item = array.getJSONObject(i);
							String group = (String) item.get("text");
							Integer id = (Integer) item.get("id");
							mGroupGroupData.add(group);
							GroupAdapter.notifyDataSetChanged();
							Map<String, String> params = new HashMap<String, String>();
							params.put("userID", PadActivity.userId);
							params.put("node", id.toString());
							new GetGroupTask(context, params).execute(Constants.prefix+"addMember.do?");
						}
					} catch (JSONException e) {
					}

				} else { // 第二次获取，得到的数据时child数据，如联系人的名字和id
					HashMap<Integer, String> temp = new HashMap<Integer, String>();
					try {
						JSONArray array = new JSONArray(result);
						int length = array.length();
						for (int i = 0; i < length; i++) {
							if (array.getJSONObject(i) == null)
								continue;
							JSONObject item = array.getJSONObject(i);
							String childName = (String) item.get("text");
							Integer id = (Integer) item.get("id");
							userRecord.put(childName, id);
							temp.put(i, childName);
						}
					} catch (JSONException e) {
					} finally {
						childSize++;
						mGroupChildData.add(temp);
						GroupAdapter.notifyDataSetChanged();
						System.out.println("groupSize:" + groupSize
								+ "childSize:" + childSize);
						if (childSize == groupSize) { // 数据加载完毕，可以初始化mCheckedObj
							System.out.println("Data Loaded......");
							for (int groupId = 0; groupId < mGroupGroupData
									.size(); ++groupId) {
								HashMap<Integer, Boolean> empty = new HashMap<Integer, Boolean>();
								mCheckedObj.add(empty);
							}

							// 初始化是否选中，默认全部未选中
							List<HashMap<Integer, Boolean>> isSelected = new ArrayList<HashMap<Integer, Boolean>>();
							for (int groupId = 0; groupId < mGroupGroupData
									.size(); ++groupId) {
								HashMap<Integer, Boolean> mChildCheck = new HashMap<Integer, Boolean>();
								for (int childId = 0; childId < mGroupChildData
										.get(groupId).size(); ++childId) {
									mChildCheck.put(childId, false);
								}
								isSelected.add(mChildCheck);
							}
							GroupAdapter.setIsSelected(isSelected);

						}
					}
				}
			}
		}

	}
}