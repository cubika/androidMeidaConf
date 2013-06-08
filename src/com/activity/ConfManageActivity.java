package com.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.activity.TableAdapter.TableCell;
import com.activity.TableAdapter.TableRow;
import com.http.GetCurtParListThread;
import com.rongdian.R;

public class ConfManageActivity extends Activity{

	public static String userId;
	public static String confId;
	private ListView lv;
	private static String[] columns={"用户ID","用户名称","用户状态","发言情况","强制离开","呼起用户","设置三号分屏","设置四号分屏","设置五号分屏","设置六号分屏"};
	private ArrayList<TableRow> table;
	private int cellWidth=150;
	
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
					String speak=userInfo.getString("speak");
					userData[3]=speak.substring(speak.indexOf("speak"), speak.indexOf(".jpg"));
					String leave=userInfo.getString("leave");
					userData[4]=leave.substring(leave.lastIndexOf("leave"), leave.indexOf(".jpg"));
					String callup=userInfo.getString("callup");
					userData[5]=callup.substring(callup.lastIndexOf("call"), callup.indexOf(".jpg"));
					
					
					for(int j=0;j<=5;j++)
						cells[j]=new TableCell(userData[j],cellWidth,LayoutParams.MATCH_PARENT, TableCell.STRING); 
				
					cells[6]=new TableCell(R.drawable.three,cellWidth,LayoutParams.WRAP_CONTENT,TableCell.IMAGE);  
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
			
			TableAdapter tableAdapter = new TableAdapter(ConfManageActivity.this, table);  
	        lv.setAdapter(tableAdapter);
		}
	};
		
}
