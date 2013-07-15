package com.contactManage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;

import com.http.HttpUtils;
import com.rongdian.PadActivity;
import com.rongdian.R;
import com.util.Constants;

public class AddFriendFragment extends Fragment{

	private String groupID, userID;
	private AutoCompleteTextView friendACTV;
	private CustomArrayAdpater usersAdapter;
	private Map<String, String> userMap = new HashMap<String, String>();
	private ContactListener mListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.add_friend, container, false);
		userID=PadActivity.userId;
		groupID=getArguments().getString("groupId");
		friendACTV = (AutoCompleteTextView) v.findViewById(R.id.friendACTV);

		usersAdapter = new CustomArrayAdpater(getActivity(),
				android.R.layout.simple_dropdown_item_1line,new String[]{},friendACTV);
		friendACTV.setAdapter(usersAdapter);
		friendACTV.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				Log.v("AddFriendActivity", "after text changed!");
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				Log.v("AddFriendActivity", "before text changed!");
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.v("AddFriendActivity", "on text changed!");
				Log.v("AddFriendActivity", "s:" + s + " length:" + s.length());
				if (s.length() > 1) {
					new GetUsersTask().execute(Constants.prefix+"cataManage.do?"
							+ "method=indexSearchUser&userID=" + userID
							+ "&start=0&limit=10&query=" + s.toString());

				}
			}

		});
		

		friendACTV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				final String newName = friendACTV.getText().toString();
				final String newID = userMap.get(newName);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							dialog.dismiss();
							new AddgroupTask(getActivity())
									.execute(Constants.prefix+"cataManage.do?method=addFriend"
											+ "&userID=" + userID + "&newID="
											+ newID + "&newName=" + newName
											+ "&cataID=" + groupID);
						} else if (which == DialogInterface.BUTTON_NEGATIVE) {
							dialog.dismiss();
						}
					}
				};
				builder.setTitle("提示").setMessage("你确定要将" + newName + "加为好友吗？")
						.setPositiveButton("确定", listener)
						.setNegativeButton("取消", listener).show();
			}

		});
		return v;
	}

	class GetUsersTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("GetUsersTask", "url is:" + url);
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
			Log.v("GetUsersTask", "result is:" + result);
			if (result != null && result.trim().toString().length() > 0) {
				usersAdapter.clear();
				try {
					JSONObject object = new JSONObject(result);
					JSONArray rows = object.getJSONArray("rows");
					int length = rows.length();
					for (int i = 0; i < length; i++) {
						if (rows.getJSONObject(i) == null)
							continue;
						JSONObject item = rows.getJSONObject(i);
						Integer userID = (Integer) item.get("userID");
						String userName = (String) item.get("userName");
						userMap.put(userName, userID.toString());
						usersAdapter.add(userName);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				usersAdapter.notifyDataSetChanged();
			}
		}
	}

	class AddgroupTask extends AsyncTask<String, Integer, String> {

		private Context context;

		public AddgroupTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Log.v("AddgroupTask", "url is " + url);
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
			Log.v("AddgroupTask", "result is:" + result);
			mListener.notify(4, null,null);
			String message = ((result != null) && result.equals("success")) ? "操作成功" : "操作失败";
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("新消息").setMessage(message)
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,	int which) {
									dialog.dismiss();
								}

							}).show();
		}

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
