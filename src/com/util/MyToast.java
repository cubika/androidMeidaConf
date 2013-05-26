package com.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rongdian.R;

public class MyToast {
	
	public MyToast() {

	}
	
	public static void openToast(Activity activity, String message)
	{
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast,
				(ViewGroup) activity.findViewById(R.id.Toast_Layout));
		ImageView image = (ImageView) layout
				.findViewById(R.id.Toast_Image);
		image.setImageResource(R.drawable.alert_dialog_icon);
		TextView text = (TextView) layout
				.findViewById(R.id.Toast_Text);
		text.setText("\n"+message);
		Toast toast = new Toast(activity.getApplicationContext());
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
}
