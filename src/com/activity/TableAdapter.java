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
	 * TableRowView ʵ�ֱ���е���ʽ
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
								"��ѡ���˽��û�" + tableRow.getCellValue(0).value
										+ "ǿ���뿪", Toast.LENGTH_SHORT).show();
						break;
					case 5:
						Toast.makeText(getContext(),
								"��ѡ���˺����û�" + tableRow.getCellValue(0).value,
								Toast.LENGTH_SHORT).show();
						break;
					case 6:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"���û������ߣ��������÷�����", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"��ѡ����Ϊ�û�" + tableRow.getCellValue(0).value
										+ "�������ŷ���", Toast.LENGTH_SHORT).show();
						SetThreeScreenTask threeTask = new SetThreeScreenTask();
						threeTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setThirdScreen&confId="
								+ ConfManageActivity.confId + "&screen3UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 7:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"���û������ߣ��������÷�����", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"��ѡ����Ϊ�û�" + tableRow.getCellValue(0).value
										+ "�����ĺŷ���", Toast.LENGTH_SHORT).show();
						SetFourScreenTask fourTask = new SetFourScreenTask();
						fourTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setFourthScreen&confId="
								+ ConfManageActivity.confId + "&screen4UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 8:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"���û������ߣ��������÷�����", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"��ѡ����Ϊ�û�" + tableRow.getCellValue(0).value
										+ "������ŷ���", Toast.LENGTH_SHORT).show();
						SetFiveScreenTask fiveTask = new SetFiveScreenTask();
						fiveTask.execute("http://"
								+ Constants.registarIp
								+ ":8888/MediaConf/divideFour.do?method=setFifthScreen&confId="
								+ ConfManageActivity.confId + "&screen5UserId="
								+ tableRow.getCellValue(0).value);
						break;
					case 9:
						if(tableRow.getCellValue(2).value.toString().equals("OffLine")){
							Toast.makeText(getContext(),"���û������ߣ��������÷�����", Toast.LENGTH_SHORT).show();
							break;
						}
						Toast.makeText(
								getContext(),
								"��ѡ����Ϊ�û�" + tableRow.getCellValue(0).value
										+ "�������ŷ���", Toast.LENGTH_SHORT).show();
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
			for (int i = 0; i < tableRow.getSize(); i++) {// �����Ԫ��ӵ���
				TableCell tableCell = tableRow.getCellValue(i);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						tableCell.width, tableCell.height);// ���ո�Ԫָ���Ĵ�С���ÿռ�
				layoutParams.setMargins(0, 0, 1, 1);// Ԥ����϶����߿�
				if (tableCell.type == TableCell.STRING) {// �����Ԫ���ı�����
					TextView textCell = new TextView(context);
					textCell.setOnClickListener(clickListener);
					textCell.setLines(1);
					textCell.setTag(i);
					textCell.setGravity(Gravity.CENTER);
					textCell.setBackgroundColor(Color.WHITE);// ������ɫ
					textCell.setTextColor(Color.BLACK);// �����ɫ
					textCell.setText(String.valueOf(tableCell.value));
					addView(textCell, layoutParams);
				} else if (tableCell.type == TableCell.IMAGE) {// �����Ԫ��ͼ������
					ImageView imgCell = new ImageView(context);
					imgCell.setBackgroundColor(Color.WHITE);// ������ɫ
					imgCell.setOnClickListener(clickListener);
					imgCell.setImageResource((Integer) tableCell.value);
					imgCell.setTag(i);
					addView(imgCell, layoutParams);
				}
			}
			this.setBackgroundColor(Color.WHITE);// ������ɫ�����ÿ�϶��ʵ�ֱ߿�
		}
	}

	/**
	 * TableRow ʵ�ֱ�����
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
	 * TableCell ʵ�ֱ��ĸ�Ԫ
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
			// ������ʾ��
			pd=ProgressDialog.show(context, "��ʾ", "�������÷��������Ժ� ������");
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
			// ������ʾ�Ի���
//			AlertDialog.Builder builder=new AlertDialog.Builder(context);
//			builder.setTitle("��ʾ").setMessage("��ȷ��Ҫ����4�ŷ�����")
//					.setPositiveButton("ȷ��", null)
//					.setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							SetFourScreenTask.this.cancel(true);
//						}
//						
//					})
//					.show();
			pd=ProgressDialog.show(context, "��ʾ", "�������÷��������Ժ� ������");
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
			// ������ʾ��
			pd=ProgressDialog.show(context, "��ʾ", "�������÷��������Ժ� ������");
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
			// ������ʾ�Ի���
			pd=ProgressDialog.show(context, "��ʾ", "�������÷��������Ժ� ������");
		}
	}

}