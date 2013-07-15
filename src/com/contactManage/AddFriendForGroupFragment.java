package com.contactManage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.contactManage.GroupMemAdapter.ViewHolder;
import com.http.HttpUtils;
import com.rongdian.R;
import com.util.Constants;

public class AddFriendForGroupFragment extends Fragment{

	private AutoCompleteTextView contactACTV;
	private CustomArrayAdpater autoAdapter;
	private Map<String, String> userMap = new HashMap<String, String>();
	private ListView memLV;
	private GroupMemAdapter groupMemAdapter;
	private ArrayList<HashMap<String,String>> data=new ArrayList<HashMap<String,String>>();
	private static String groupName,groupID;
	private ContactListener mListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.add_friend_for_group, container, false);
		groupName=getArguments().getString("groupName");
		groupID=getArguments().getString("groupId");
		//搜索联系人工作
				contactACTV = (AutoCompleteTextView) v.findViewById(R.id.contactACTV);
				autoAdapter = new CustomArrayAdpater(getActivity(),
						android.R.layout.simple_dropdown_item_1line,new String[]{},contactACTV);
				contactACTV.setAdapter(autoAdapter);
				contactACTV.setThreshold(2);
				
				contactACTV.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable arg0) {
						Log.v("AddContactActivity", "after text changed!");
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						Log.v("AddContactActivity", "before text changed!");
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before,
							int count) {
						Log.v("AddContactActivity", "on text changed!");
							new GetMemTask().execute("http://" + Constants.registarIp
									+ ":8888/MediaConf/group.do?method=searchUser"
									+ "&start=0&limit=10&query=" + s.toString());

					}

				});
				contactACTV.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,	//点击时加入到listview中
							int position, long id) {	
						getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
						HashMap<String,String> temp=new HashMap<String,String>();
						String userName=contactACTV.getText().toString();
						for(int i=0;i<data.size();i++){
							if(data.get(i).containsValue(userName)){
								Toast.makeText(getActivity(), "已经在列表中，不能再次添加", Toast.LENGTH_SHORT).show();
								return;
							}
						}
						temp.put("userName", userName);
						temp.put("userID", userMap.get(userName));
						temp.put("flag", "false");
						data.add(temp);
						groupMemAdapter.notifyDataSetChanged();
						contactACTV.setText("");
					}

				});
				//显示已添加的联系人列表
				memLV=(ListView)v.findViewById(R.id.contactsLV);
				groupMemAdapter=new GroupMemAdapter(inflater,data);
				memLV.setAdapter(groupMemAdapter);
				OnClickListener mylistener=new OnClickListener(){

					@Override
					public void onClick(View v) {
						switch(v.getId()){
						case R.id.selectAllBTN2:	//全选
							for (int i = 0; i < data.size(); i++) {
			                    data.get(i).put("flag", "true");
			                }
							groupMemAdapter.notifyDataSetChanged();
							break;
						case R.id.selectNoneBTN2:	//全不选
							for (int i = 0; i < data.size(); i++) {
			                    data.get(i).put("flag", "false");
			                }
							groupMemAdapter.notifyDataSetChanged();
							break;
						case R.id.deleteSBTN2:	//删除
							for (int i = 0; i < data.size(); i++) {
			                    HashMap<String, String> temp=data.get(i);
			                    if(temp.get("flag").equals("true")){
			                    	data.remove(i);
			                    }
			                }
							groupMemAdapter.notifyDataSetChanged();
							break;
						case R.id.addContactOKbtn:
							String newID="",newName="";
							for (int i = 0; i < data.size(); i++) {
			                    HashMap<String, String> temp=data.get(i);
			                    newID+=temp.get("userID")+"|";
			                    newName+=temp.get("userName")+"|";
			                }
							Map<String,String> paramMap=new HashMap<String, String>();
							paramMap.put("method", "addGroupMember");
							paramMap.put("groupName", groupName);
							paramMap.put("groupID", groupID);
							paramMap.put("newID", newID);
							paramMap.put("newName", newName);
							new NewContactTask(paramMap).execute("http://" + Constants.registarIp
									+ ":8888/MediaConf/group.do");
							break;
						}
						
					}
					
				};
				Button selectAllBTN=(Button)v.findViewById(R.id.selectAllBTN2);
				selectAllBTN.setOnClickListener(mylistener);
				Button selectNoneBTN=(Button)v.findViewById(R.id.selectNoneBTN2);
				selectNoneBTN.setOnClickListener(mylistener);
				Button deleteBTN=(Button)v.findViewById(R.id.deleteSBTN2);
				deleteBTN.setOnClickListener(mylistener);
				Button confirmBTN=(Button)v.findViewById(R.id.addContactOKbtn);
				confirmBTN.setOnClickListener(mylistener);
				//点击孩子事件
				memLV.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						Log.v("AddGroupActivity","listview child clicked in "+arg2);
						ViewHolder holder = (ViewHolder) arg1.getTag();
						holder.checkbox.toggle();
						if(holder.checkbox.isChecked())
							data.get(arg2).put("flag", "true");
						else
							data.get(arg2).put("flag", "false");
					}
				});
				return v;
			}
			
			class GetMemTask extends AsyncTask<String, Integer, String> {

				@Override
				protected String doInBackground(String... params) {
					String url = params[0];
					Log.v("GetMemTask", "url is:" + url);
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
					Log.v("GetMemTask", "result is:" + result);
					if (result != null && result.trim().toString().length() > 0) {
						autoAdapter.clear();
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
								autoAdapter.add(userName);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						autoAdapter.notifyDataSetChanged();
					}
				}
			}

			
			class NewContactTask extends AsyncTask<String, Integer, String> {

				private Map<String,String> paramMap;
				
				public NewContactTask(Map<String,String> params) {
					paramMap=params;
				}
				
				@Override
				protected String doInBackground(String... params) {
					String url = params[0];
					Log.v("NewContactTask", "url is:" + url);
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
					Log.v("NewContactTask", "result is:" + result);
					if (result != null && result.trim().toString().length() > 0) {
						if(result.equals("success")){
							Toast.makeText(getActivity(), "新增群组成员成功！！", Toast.LENGTH_LONG).show();
							mListener.notify(4, null,null);
						}
						else
							Toast.makeText(getActivity(), "新增群组成员操作错误！", Toast.LENGTH_LONG).show();
					}
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
