package cn.edu.bupt.mmc.docshare.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.rongdian.PadActivity;
import com.rongdian.R;
import com.util.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import cn.edu.bupt.mmc.client.BasicInfomation;
import cn.edu.bupt.mmc.client.TCPClient;
import cn.edu.bupt.mmc.docshare.apps.EditorState;

public class DocShareActivity extends Activity implements OnClickListener {
    private Button applyButton;
    private Button dropButton;
    private Button uploadButton;
    private Button nextPageButton;
    private Button prePageButton;
    private Button firstPageButton;
    private Button endPageButton;
    private Button gotoOkButton;

    public ProgressDialog progressDialog; // show the progress of file uploading and processing
    private EditText pageNumberTextField;
    private DocFragment docFragment;
    
    private DataShareProcessor processor;
    private Handler handler;
    private static final String TAG="DocShareActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_docshare);
		EditorState state = new EditorState();
        state.setConferenceId(BasicInfomation.conferenceid);
        state.ensureStringsAvailable();
        initComponent();
        processor = new DataShareProcessor(state, docFragment);
        processor.setApplyButton(applyButton);
        processor.setDropButton(dropButton);
        processor.setNextPageButton(nextPageButton);
        processor.setPrePageButton(prePageButton);
        processor.setUploadButton(uploadButton);
        processor.setFirstPageButton(firstPageButton);
        processor.setEndPageButton(endPageButton);
        processor.setPageNumberTextField(pageNumberTextField);
        processor.setButtonsEnable(state);
        processor.setProgressBar(progressDialog);
        
        //this.setBasicInformation("10.109.252.6", 2, 111, "leixiaojiang");
        this.setBasicInformation(Constants.registarIp, PadActivity.confId, Integer.parseInt(PadActivity.userId), PadActivity.UserName);
        TCPClient tcpClient = null;
        try {
			tcpClient = new TCPClient(BasicInfomation.userid,
			        BasicInfomation.username, BasicInfomation.conferenceid,
			        BasicInfomation.serverIP, BasicInfomation.serverTCPPort,state,handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
        docFragment.setTcpClient(tcpClient);

	}

	public void setBasicInformation(String serverIP, int conferenceID, int userID, String userName) {
    	BasicInfomation.serverIP = serverIP;
        BasicInfomation.conferenceid = conferenceID;
        BasicInfomation.username = userName;
        BasicInfomation.userid = userID;
    }
	
	private void initComponent() {
		applyButton = (Button)findViewById(R.id.applyBtn);
        applyButton.setOnClickListener(this);
        dropButton = (Button)findViewById(R.id.dropBtn);
        dropButton.setOnClickListener(this);
        uploadButton = (Button)findViewById(R.id.uploadBtn);
        uploadButton.setOnClickListener(this);
        nextPageButton = (Button)findViewById(R.id.nextBtn);
        nextPageButton.setOnClickListener(this);
        prePageButton = (Button)findViewById(R.id.preBtn);
        prePageButton.setOnClickListener(this);
        firstPageButton = (Button)findViewById(R.id.firstPage);
        firstPageButton.setOnClickListener(this);
        endPageButton = (Button)findViewById(R.id.endPage);
        endPageButton.setOnClickListener(this);
        pageNumberTextField =(EditText)findViewById(R.id.pageNumberET);
        gotoOkButton = (Button)findViewById(R.id.gotoOkBtn);
        gotoOkButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        docFragment = (DocFragment) getFragmentManager().findFragmentById(R.id.fragment1);
        ImageView view = (ImageView)findViewById(R.id.mainImage);
        view.setOnTouchListener(docFragment);
        
        Log.d(TAG, "UI thread id : "+Thread.currentThread().getId());
        handler = new Handler(){
	  		  @Override
	  		  public void handleMessage(Message msg) {
	  			 switch(msg.what){
	  			 	case 1://controllerChange
	  			 		EditorState state = docFragment.getTcpClient().state;
	  			 		processor.getPanel().setEditor(state);
	  			        processor.updateStatus();
	  			        processor.setButtonsEnable(state);
	  			        break;
	  			 	case 2://pushPage
	  			 		Bundle data = msg.getData();
	  			 		int len = data.getInt("len");
	  			 		byte[] buf = data.getByteArray("buf");
	  			 		ByteArrayInputStream imageis = new ByteArrayInputStream(buf,0,len);
	  			 		Log.d(TAG, "Handler Thread id : "+Thread.currentThread().getId());
	  					processor.pictureBufferedImageShow(imageis);
	  					processor.setButtonsEnable(docFragment.getTcpClient().state);
	  			 }
	  		  }
        };
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.applyBtn:
				System.out.println("apply ...");
				docFragment.getTcpClient().controlReq();
		        break;
			case R.id.dropBtn:
				System.out.println("drop ...");
				docFragment.getTcpClient().controlDrop();
		        break;
			case R.id.uploadBtn:
				 System.out.println("upload file ...");
				 docFragment.fileOpen();
				 break;
			case R.id.nextBtn:
				System.out.println("next page ...");
				docFragment.nextPage();
				break;
			case R.id.preBtn:
				 System.out.println("pre page ...");
				 docFragment.prePage();
				 break;
			case R.id.firstPage:
				System.out.println("first page ..");
				docFragment.firstPage();
				break;
			case R.id.endPage:
				System.out.println("end page ..");
				docFragment.endPage();
			case R.id.gotoOkBtn:
				try{
					int pageNumber = Integer.parseInt(pageNumberTextField.getText().toString());
					docFragment.findPage(pageNumber);
		        } catch (NumberFormatException e) {
		            e.printStackTrace();
		        }
		}
		
	}
	
	 @Override  
	 protected void onDestroy() {  
	    super.onDestroy();
	    Log.d(TAG, "on destory...");
	    docFragment.getTcpClient().quitConference();
	 }
	 

}
