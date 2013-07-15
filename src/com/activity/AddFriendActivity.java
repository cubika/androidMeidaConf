package com.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.http.HttpUtils;
import com.rongdian.R;
import com.util.Constants;

/**
 * 如何应对分页的问题 
 * 要输入四个字符，而不是两个？
 */
public class AddFriendActivity extends Activity {

	private String groupID, userID;
	private AutoCompleteTextView friendACTV;
	private ArrayAdapter<String> usersAdapter;
	private Map<String, String> userMap = new HashMap<String, String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_friend);
		userID = this.getIntent().getExtras().getString("userID");
		groupID = this.getIntent().getExtras().getString("groupID");
		friendACTV = (AutoCompleteTextView) findViewById(R.id.friendACTV);

		usersAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
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
					new GetUsersTask().execute("http://" + Constants.registarIp
							+ ":8888/MediaConf/cataManage.do?"
							+ "method=indexSearchUser&userID=" + userID
							+ "&start=0&limit=10&query=" + s.toString());

				}
			}

		});
		friendACTV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(friendACTV.getWindowToken(), 0);
				final String newName = friendACTV.getText().toString();
				final String newID = userMap.get(newName);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AddFriendActivity.this);
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							dialog.dismiss();
							new AddgroupTask(AddFriendActivity.this)
									.execute("http://"
											+ Constants.registarIp
											+ ":8888/MediaConf/cataManage.do?method=addFriend"
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
}
