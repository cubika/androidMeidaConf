package com.activity;

import java.io.IOException;
import java.util.List;

import com.http.HttpUtils;
import com.util.Constants;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TableAdapter extends BaseAdapter {
	private Context context;
	private List<TableRow> table;

	public TableAdapter(Context context, List<TableRow> table) {
		this.context = context;
		this.table = table;
	}

	@Override
	public int getCount() {
		return table.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public TableRow getItem(int position) {
		return table.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TableRow tableRow = table.get(position);
		return new TableRowView(this.context, tableRow);
	}

	/**
	 * TableRowView 实现表格行的样式
	 */
	class TableRowView extends LinearLayout {
		public TableRowView(Context context, final TableRow tableRow) {
			super(context);

			this.setOrientation(LinearLayout.HORIZONTAL);
			OnClickListener clickListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					switch (tag) {
					case 4:
						Toast.makeText(
								getContext(),
								"你选择了将用户" + tableRow.getCellValue(0).value
										+ "强制离开", Toast.LENGTH_SHORT).show();
						break;
					case 5:
						Toast.makeText(getContext(),
								"你选择了呼起用户" + tableRow.getCellValue(0).value,
								Toast.LENGTH_SHORT).show();
						break;
					case 6:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"改用户不在线，不能设置分屏！", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"你选择了为用户" + tableRow.getCellValue(0).value
										+ "设置三号分屏", Toast.LENGTH_SHORT).show();
						SetThreeScreenTask threeTask = new SetThreeScreenTask();
						threeTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setThirdScreen&confId="
								+ ConfManageActivity.confId + "&screen3UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 7:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"改用户不在线，不能设置分屏！", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"你选择了为用户" + tableRow.getCellValue(0).value
										+ "设置四号分屏", Toast.LENGTH_SHORT).show();
						SetFourScreenTask fourTask = new SetFourScreenTask();
						fourTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setFourthScreen&confId="
								+ ConfManageActivity.confId + "&screen4UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 8:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"改用户不在线，不能设置分屏！", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"你选择了为用户" + tableRow.getCellValue(0).value
										+ "设置五号分屏", Toast.LENGTH_SHORT).show();
						SetFiveScreenTask fiveTask = new SetFiveScreenTask();
						fiveTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setFifthScreen&confId="
								+ ConfManageActivity.confId + "&screen5UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 9:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"改用户不在线，不能设置分屏！", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"你选择了为用户" + tableRow.getCellValue(0).value
										+ "设置六号分屏", Toast.LENGTH_SHORT).show();
						SetSixScreenTask sixTask = new SetSixScreenTask();
						sixTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setSixthScreen&confId="
								+ ConfManageActivity.confId + "&screen6UserId="
								+ tableRow.getCellValue(0).value);
						break;

					}

				}

			};
			for (int i = 0; i < tableRow.getSize(); i++) {// 逐个格单元添加到行
				TableCell tableCell = tableRow.getCellValue(i);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						tableCell.width, tableCell.height);// 按照格单元指定的大小设置空间
				layoutParams.setMargins(0, 0, 1, 1);// 预留空隙制造边框
				if (tableCell.type == TableCell.STRING) {// 如果格单元是文本内容
					TextView textCell = new TextView(context);
					textCell.setOnClickListener(clickListener);
					textCell.setLines(1);
					textCell.setTag(i);
					textCell.setGravity(Gravity.CENTER);
					textCell.setBackgroundColor(Color.WHITE);// 背景白色
					textCell.setTextColor(Color.BLACK);// 字体白色
					textCell.setText(String.valueOf(tableCell.value));
					addView(textCell, layoutParams);
				} else if (tableCell.type == TableCell.IMAGE) {// 如果格单元是图像内容
					ImageView imgCell = new ImageView(context);
					imgCell.setBackgroundColor(Color.WHITE);// 背景黑色
					imgCell.setOnClickListener(clickListener);
					imgCell.setImageResource((Integer) tableCell.value);
					imgCell.setTag(i);
					addView(imgCell, layoutParams);
				}
			}
			this.setBackgroundColor(Color.WHITE);// 背景白色，利用空隙来实现边框
		}
	}

	/**
	 * TableRow 实现表格的行
	 */
	static public class TableRow {
		private TableCell[] cell;

		public TableRow(TableCell[] cell) {
			this.cell = cell;
		}

		public int getSize() {
			return cell.length;
		}

		public TableCell getCellValue(int index) {
			if (index >= cell.length)
				return null;
			return cell[index];
		}
	}

	/**
	 * TableCell 实现表格的格单元
	 */
	static public class TableCell {
		static public final int STRING = 0;
		static public final int IMAGE = 1;
		public Object value;
		public int width;
		public int height;
		private int type;

		public TableCell(Object value, int width, int height, int type) {
			this.value = value;
			this.width = width;
			this.height = height;
			this.type = type;
		}
	}

	class SetThreeScreenTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog pd;
		@Override
		protected String doInBackground(String... params) {
			Log.v("SetThreeScreenTask","doInBackground");
			if(this.isCancelled())
				return null;
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(params[0], null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("SetThreeScreenTask","onPostExecute");
			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// 弹出提示框
			pd=ProgressDialog.show(context, "提示", "正在设置分屏，请稍后 。。。");
		}
	}
	
	class SetFourScreenTask extends AsyncTask<String, Integer, String> {
		
		private ProgressDialog pd;
		@Override
		protected String doInBackground(String... params) {
			Log.v("SetFourScreenTask","doInBackground");
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(params[0], null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("SetFourScreenTask","onPostExecute");
			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// 弹出提示对话框
//			AlertDialog.Builder builder=new AlertDialog.Builder(context);
//			builder.setTitle("提示").setMessage("您确认要设置4号分屏吗？")
//					.setPositiveButton("确定", null)
//					.setNegativeButton("取消", new DialogInterface.OnClickListener(){
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							SetFourScreenTask.this.cancel(true);
//						}
//						
//					})
//					.show();
			pd=ProgressDialog.show(context, "提示", "正在设置分屏，请稍后 。。。");
		}
	}
	
	class SetFiveScreenTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog pd;
		@Override
		protected String doInBackground(String... params) {
			Log.v("SetFiveScreenTask","doInBackground");
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(params[0], null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("SetFiveScreenTask","onPostExecute");
			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// 弹出提示框
			pd=ProgressDialog.show(context, "提示", "正在设置分屏，请稍后 。。。");
		}
	}
	
	class SetSixScreenTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog pd;
		@Override
		protected String doInBackground(String... params) {
			Log.v("SetSixScreenTask","doInBackground");
			String result = null;
			try {
				result = HttpUtils.sendPostMessage(params[0], null, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v("SetSixScreenTask","onPostExecute");
			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// 弹出提示对话框
			pd=ProgressDialog.show(context, "提示", "正在设置分屏，请稍后 。。。");
		}
	}

}