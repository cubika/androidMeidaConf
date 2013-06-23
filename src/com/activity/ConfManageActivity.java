package com.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.activity.TableAdapter.TableCell;
import com.activity.TableAdapter.TableRow;
import com.http.GetCurtParListThread;
import com.http.HttpUtils;
import com.rongdian.PhoneActivity;
import com.rongdian.R;
import com.util.Constants;

public class ConfManageActivity extends Activity implements OnClickListener{

	public static String userId; //当前账号
	public static String confId;
	private ListView lv;
	private static final String[] columns={"用户ID","用户名称","用户状态","发言情况","强制离开","呼起用户","设置三号分屏","设置四号分屏","设置五号分屏","设置六号分屏"};
	private static final String[] strategies={"最小发言频度","最大请求频度","加权公平排队"};
	private ArrayList<TableRow> table;
	private static final int cellWidth=150; //表格中单元格的宽度
	private RadioGroup speakRG;
	private Button startSpeakBTN,startRecordBTN;
	private Button endSpeakBTN,endRecordBTN;
	private static Map<String,String> onlineUserMap=new HashMap<String,String>();
	
	private int policy=-1;	//发言的策略
	private String speakerUserId="-1"; //选择演讲时的选择id
	private String[] onlineUsers;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.conf_manage);
		userId=this.getIntent().getExtras().getString("userId");
		confId=this.getIntent().getExtras().getString("confId");
		lv = (ListView) this.findViewById(R.id.ListView01);  
		table = new ArrayList<TableRow>();  
        TableCell[] titles = new TableCell[columns.length];// 每行10个单元  
        // 定义标题  
        for (int i = 0; i < columns.length; i++) {  
            titles[i] = new TableCell(columns[i], cellWidth,LayoutParams.MATCH_PARENT,TableCell.STRING);  
        }  
        table.add(new TableRow(titles));  
        
        GetCurtParListThread curParThread =new GetCurtParListThread(handler);
        Map<String,String> params=new HashMap<String,String>();
        params.put("method", "getCurrentParticipateList");
        params.put("confId", confId);
        curParThread.doStart(params);
        
        speakRG=(RadioGroup)findViewById(R.id.speakChooseRG);
        startSpeakBTN=(Button)findViewById(R.id.startSpeakBTN);
        endSpeakBTN=(Button)findViewById(R.id.endSpeakBTN);
        
        startSpeakBTN.setOnClickListener(this);
        endSpeakBTN.setOnClickListener(this);
        
        startRecordBTN=(Button)findViewById(R.id.startRecordBTN);
        endRecordBTN=(Button)findViewById(R.id.endRecordBTN);
        startRecordBTN.setOnClickListener(this);
        endRecordBTN.setOnClickListener(this);
        
    }  
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			Bundle data=msg.getData();
			String result=(String) data.get("result");
			/*{rows:[{userId:'60107',userName:'liubochao',
			 * headUrl:'<img src=../image/head/head1.jpg height=64 width=64>',
			 * state:'<img class=opButton src=../image/conference/stateLeave.jpg />',
			 * speak:'<img class=opButton src=../image/conference/speakOffline.jpg />',
			 * callup:'<a href=javascript:callUser(5043,60107)><img class=opButton src=../image/conference/callOn.jpg /></a>',
			 * leave:'<img class=opButton src=../image/conference/leaveOff.jpg  />',
			 * silence:'null'},
			 * {userId:'60112',userName:'licheng',
			 * headUrl:'<img src=../image/head/head1.jpg height=64 width=64>',
			 * state:'<img class=opButton src=../image/conference/statePC.jpg />',
			 * speak:'<img class=opButton src=../image/conference/speakNoApply.jpg />',
			 * callup:'<img class=opButton src=../image/conference/callOff.jpg />',
			 * leave:'<a href=javascript:disconnectUser(5043,60112)><img class=opButton src=../image/conference/leaveOn.jpg  /></a>',
			 * silence:'null'}]}
			 */
			System.out.println("participant List"+result);
			JSONObject userListObj=null;
			JSONArray userListArray=null;
			try {
				userListObj=new JSONObject(result);
				userListArray=userListObj.getJSONArray("rows");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			for (int i = 0; userListArray != null && i < userListArray.length(); i++) {
				if (userListArray.opt(i) == null)
					continue;
				JSONObject userInfo=userListArray.optJSONObject(i);
				// 每行的数据  
		        TableCell[] cells = new TableCell[columns.length];// 每行10个单元  
		        String[] userData=new String[columns.length];
		        try {
					userData[0]=userInfo.getString("userId");
					userData[1]=userInfo.getString("userName");
					String state=userInfo.getString("state");
					userData[2]=(state.indexOf("Leave")==-1) ? "OnLine" : "OffLine";
					if(userData[2].equals("OnLine")){
						onlineUserMap.put(userData[1], userData[0]); // (username,userId)
					}
					String speak=userInfo.getString("speak");
					userData[3]=speak.substring(speak.indexOf("speak"), speak.indexOf(".jpg"));
					String leave=userInfo.getString("leave");
					userData[4]=leave.substring(leave.lastIndexOf("leave"), leave.indexOf(".jpg"));
					String callup=userInfo.getString("callup");
					userData[5]=callup.substring(callup.lastIndexOf("call"), callup.indexOf(".jpg"));
					
					
					for(int j=0;j<=5;j++)
						cells[j]=new TableCell(userData[j],cellWidth,LayoutParams.MATCH_PARENT, TableCell.STRING); //文字
				
					if(userData[3].equals("speakApply"))
						cells[3]=new TableCell(R.drawable.speak_apply,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					else if(userData[3].equals("speakNoApply"))
						cells[3]=new TableCell(R.drawable.speak_no_apply,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					else if(userData[3].equals("speakOffline"))
						cells[3]=new TableCell(R.drawable.speak_offline,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					else if(userData[3].equals("speakOn"))
						cells[3]=new TableCell(R.drawable.speak_on,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					
					if(userData[4].equals("leaveOff"))
						cells[4]=new TableCell(R.drawable.leave_off,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					else if(userData[4].equals("leaveOn"))
						cells[4]=new TableCell(R.drawable.leave_on,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					
					if(userData[5].equals("callOff"))
						cells[5]=new TableCell(R.drawable.call_off,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					else if(userData[5].equals("callOn"))
						cells[5]=new TableCell(R.drawable.call_on,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					
					cells[6]=new TableCell(R.drawable.three,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);  //图片
					cells[7]=new TableCell(R.drawable.four,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);  
					cells[8]=new TableCell(R.drawable.five,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);  
					cells[9]=new TableCell(R.drawable.six,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);
					
					// 把表格的行添加到表格 
					table.add(new TableRow(cells));
				} catch (JSONException e) {
					e.printStackTrace();
				}catch(StringIndexOutOfBoundsException e1){
					e1.printStackTrace();
				}
			}
			onlineUsers=onlineUserMap.keySet().toArray(new String[onlineUserMap.size()]);
			TableAdapter tableAdapter = new TableAdapter(ConfManageActivity.this, table);  
	        lv.setAdapter(tableAdapter);
		}
	};
		
	class SpeakTask extends AsyncTask<String,Integer,String>{

		@Override
		protected String doInBackground(String... params) {
			Log.v("SpeakTask","doInBackground");
			Log.v("SpeakTask","policy："+policy+" speakerUserId:"+speakerUserId);
			if(policy==-1)
				return null;
			String url=params[0]+"&policy="+policy;
			if(policy==4){
				if(speakerUserId.equals("-1"))
					return null;
				url+="&userId="+speakerUserId;
			}
			String result=null;
			try {
				result=HttpUtils.sendPostMessage(url, null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result){
			Log.v("SpeakTask","onPostExecute");
			Log.v("SpeakTask","SpeakControl result:"+result);
			if(result!=null && result.trim().length()!=0){
				//Toast.makeText(ConfManageActivity.this, result, Toast.LENGTH_LONG).show();
				try {
					JSONObject resultObj=new JSONObject(result);
					AlertDialog.Builder builder=new AlertDialog.Builder(ConfManageActivity.this);
					builder.setTitle("新消息").setMessage(resultObj.getString("content"))
							.setPositiveButton("确认", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,int which) {
									dialog.dismiss();
								}
						
					}).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		@Override
		protected void onPreExecute() {
			Log.v("SpeakTask","onPreExecute");
		}
		
	}
	
	

	@Override
	public void onClick(View v) {
		policy=-1;
		speakerUserId="-1";
		
		switch(v.getId()){
		case R.id.startSpeakBTN:
	        int choosenId=speakRG.getCheckedRadioButtonId();
			switch(choosenId){
			case R.id.radioYN: //演讲
				policy=4;
				AlertDialog.Builder builder = new AlertDialog.Builder(ConfManageActivity.this);
				builder.setTitle("请选择谁来演讲");
				SpeakerOnClick speakerOnClick=new SpeakerOnClick();
				builder.setSingleChoiceItems(onlineUsers, -1, speakerOnClick);
			    builder.setPositiveButton("确定", speakerOnClick);
			    builder.setNegativeButton("取消", speakerOnClick);
			    builder.show();
				break;
			case R.id.radioTW: //提问
				AlertDialog.Builder builder2 = new AlertDialog.Builder(ConfManageActivity.this);
				builder2.setTitle("请选择提问策略");
				AskOnClick askOnClick=new AskOnClick();
				builder2.setSingleChoiceItems(strategies, -1, askOnClick);
			    builder2.setPositiveButton("确定", askOnClick);
			    builder2.setNegativeButton("取消", askOnClick);
			    builder2.show();
				break;
			case R.id.radioTL: //讨论
				policy=5;
				new SpeakTask().execute("http://"+ Constants.registarIp+":8888/MediaConf/speakControl.do?confId="+confId);
				break;
			case R.id.radioZY: //自由
				policy=6;
				new SpeakTask().execute("http://"+ Constants.registarIp+":8888/MediaConf/speakControl.do?confId="+confId);
				break;
			}
			break;
		case R.id.endSpeakBTN:
			GenericTask stopTask=new GenericTask(ConfManageActivity.this);
			stopTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/stopSpeaking.do?confId="+confId);
			break;
		case R.id.startRecordBTN:
			GenericTask startRecordTask=new GenericTask(ConfManageActivity.this);
			startRecordTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/startRecord.do?confId="+confId+"&userId="+userId);
			break;
		case R.id.endRecordBTN:
			GenericTask stopRecordTask=new GenericTask(ConfManageActivity.this);
			stopRecordTask.execute("http://"+ Constants.registarIp+":8888/MediaConf/endRecord.do?confId="+confId);
			break;
		}
		
	}
	
	class SpeakerOnClick implements DialogInterface.OnClickListener{

		private int index=0;
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//点击的是确认按钮
			if (which == DialogInterface.BUTTON_POSITIVE){
				String userName=onlineUsers[index];
				speakerUserId=onlineUserMap.get(userName);
				System.out.println("userName:"+userName+" speakerUserId:"+speakerUserId);
				dialog.dismiss();
				new SpeakTask().execute("http://"+ Constants.registarIp+":8888/MediaConf/speakControl.do?confId="+confId);
			}else if(which == DialogInterface.BUTTON_NEGATIVE){
				dialog.dismiss();
			}else if (which >= 0){ //选择的是单选按钮
				index =which;
			}
		}
	}
	
	class AskOnClick implements DialogInterface.OnClickListener{

		private int index=0;
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//点击的是确认按钮
			if (which == DialogInterface.BUTTON_POSITIVE){
				policy=index+1;
				dialog.dismiss();
				new SpeakTask().execute("http://"+ Constants.registarIp+":8888/MediaConf/speakControl.do?confId="+confId);
			}else if(which == DialogInterface.BUTTON_NEGATIVE){
				dialog.dismiss();
			}else if (which >= 0){ //选择的是单选按钮
				index =which;
			}
		}
	}
	
}
