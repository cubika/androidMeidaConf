package cn.bupt.mmc.desktopshare.gui;

import com.rongdian.PadActivity;
import com.rongdian.R;
import com.util.Constants;

import cn.edu.bupt.mmc.client.DesktopClient;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class DesktopShareActivity extends Activity{
	
	private ImageView desktopImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_desktopshare);
		desktopImage = (ImageView) findViewById(R.id.desktopImage);
		Handler handler = new Handler(){
			 @Override
	  		  public void handleMessage(Message msg) {
	  			 switch(msg.what){
	  			 	case 1://receive img
	  			 		byte[] buf=msg.getData().getByteArray("buf");
	  			 		int len=msg.getData().getInt("len");
	  			 		Bitmap bitmap = BitmapFactory.decodeByteArray(buf, 0, len);
	  			 		desktopImage.setImageBitmap(bitmap);
	  			        break;
	  			 }
			 }
		};

		//DesktopClient client = new DesktopClient("10.109.252.6", 50000,9373, 60089,"libin",handler);
		DesktopClient client = new DesktopClient(Constants.registarIp, 50000,
				PadActivity.confId, Integer.parseInt(PadActivity.userId),PadActivity.UserName,handler);
	}
}
