package cn.edu.bupt.mmc.docshare.gui;

import java.io.File;

import com.rongdian.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.bupt.mmc.client.TCPClient;
import cn.edu.bupt.mmc.docshare.apps.EditorState;

public class DocFragment extends Fragment implements OnTouchListener{

	private EditorState editor;
	private TextView statusBar;
	private ImageView imageView;
	private TCPClient tcpClient = null;
	private final int REQUEST_CODE_PICK_DIR = 1;
	private final int REQUEST_CODE_PICK_FILE = 2;
	private static final String TAG="DocFragment";
	
	// These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    
    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_doc, container, false);
		statusBar = (TextView) v.findViewById(R.id.statusText);
		imageView = (ImageView) v.findViewById(R.id.mainImage);
		imageView.setScaleType(ImageView.ScaleType.MATRIX);
		return v;
	}
	
	public void setEditorState(EditorState editor){
		this.editor = editor;
	}
	
	public void setStatusBar(String text)
	{
		statusBar.setText(text);
	}
	
	public void updateImage()
	{
		Bitmap image = editor.getImage();

		if (image != null)
		{
			imageView.setImageBitmap(image);
			System.out.println("update image");
		}
		updateStatusBar();
		//menuWrapper.updateEnabled(processor);
	}
	
	public void updateStatusBar()
	{
		Log.d(TAG, "Update Thread id : "+Thread.currentThread().getId());	
		String statusBarText;
		if (editor == null)
		{
			statusBarText = "";
			
		}
		else
		{
            if (editor.getControllerName() == null) {
                statusBarText = "当前会议没有人控制";
            } else {
                System.out.println("Change Controller .....");
                statusBarText = "当前文件控制者：" + editor.getControllerName()
                        + " 当前在" + editor.getNowPageNumber() + "页, 共"
                        + editor.getTotalPageNumber() + "页";
                
                System.out.println("statusBarText = " + statusBarText);
                statusBar.setText("Test long long long long long characters");
                // setStatusBar(statusBarText);
            }
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 60; i++) {
		    sb.append(" ");
		}
		setStatusBar(statusBarText + sb.toString());
	}
	
	public void zoomIn(){
		
	}
	
	public void zoomOut(){
		
	}
	
	@Override
    public boolean onTouch(View v, MotionEvent event) 
    {
		Log.d(TAG,"on Touch");
		//if(v.getId() != R.id.mainImage) return false;
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) 
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                                                savedMatrix.set(matrix);
                                                start.set(event.getX(), event.getY());
                                                Log.d(TAG, "mode=DRAG"); // write to LogCat
                                                mode = DRAG;
                                                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                                                mode = NONE;
                                                Log.d(TAG, "mode=NONE");
                                                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                                                oldDist = spacing(event);
                                                Log.d(TAG, "oldDist=" + oldDist);
                                                if (oldDist > 5f) {
                                                    savedMatrix.set(matrix);
                                                    midPoint(mid, event);
                                                    mode = ZOOM;
                                                    Log.d(TAG, "mode=ZOOM");
                                                }
                                                break;

            case MotionEvent.ACTION_MOVE:

                                                if (mode == DRAG) 
                                                { 
                                                    matrix.set(savedMatrix);
                                                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                                                } 
                                                else if (mode == ZOOM) 
                                                { 
                                                    // pinch zooming
                                                    float newDist = spacing(event);
                                                    Log.d(TAG, "newDist=" + newDist);
                                                    if (newDist > 5f) 
                                                    {
                                                        matrix.set(savedMatrix);
                                                        scale = newDist / oldDist; // setting the scaling of the
                                                                                    // matrix...if scale > 1 means
                                                                                    // zoom in...if scale < 1 means
                                                                                    // zoom out
                                                        matrix.postScale(scale, scale, mid.x, mid.y);
                                                    }
                                                }
                                                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

	
	public void firstPage() {
	    editor.setNowPageNumber(1);
        tcpClient.changePageReq(editor.getFileName(), 1, editor
                .getUploadByUserid());
	}
	
	public void endPage() {
	    int totalPageNumber = editor.getTotalPageNumber();
	    editor.setNowPageNumber(totalPageNumber);
        tcpClient.changePageReq(editor.getFileName(), totalPageNumber, editor
                .getUploadByUserid());
	}
	
	public void findPage(int number) {
	    int total = editor.getTotalPageNumber();
	    if (number >= 1 && number <= total) {
            editor.setNowPageNumber(number);
            tcpClient.changePageReq(editor.getFileName(), number, editor
                    .getUploadByUserid());
        }
    }
	public void nextPage() {
	    int nowPageNumber = editor.getNowPageNumber();
	    int totalPageNumber = editor.getTotalPageNumber();
	    if (nowPageNumber < totalPageNumber) {
            nowPageNumber++;
            editor.setNowPageNumber(nowPageNumber);
            tcpClient.changePageReq(editor.getFileName(), nowPageNumber, editor
                    .getUploadByUserid());
            System.out.println("file name : " + editor.getFileName());
            System.out.println("next page : " + nowPageNumber);
            System.out.println("user id  : " + editor.getUploadByUserid());
        }
	    
	}
	
	public void prePage() {
        int nowPageNumber = editor.getNowPageNumber();
        if (nowPageNumber > 1) {
            nowPageNumber--;
            editor.setNowPageNumber(nowPageNumber);
            tcpClient.changePageReq(editor.getFileName(), nowPageNumber, editor
                    .getUploadByUserid());
            System.out.println("file name : " + editor.getFileName());
            System.out.println("next page : " + nowPageNumber);
            System.out.println("user id  : " + editor
                    .getUploadByUserid());
        }
        
    }
	
    /**
     * @return the tcpClient
     */
    public TCPClient getTcpClient() {
        return this.tcpClient;
    }

    /**
     * @param tcpClient the tcpClient to set
     */
    public void setTcpClient(TCPClient tcpClient) {
    	System.out.println("Set TCP Client For docFragement: "+ tcpClient);
        this.tcpClient = tcpClient;
    }
    
    public void fileOpen() {
        Intent fileExploreIntent = new Intent(
				FileBrowserActivity.INTENT_ACTION_SELECT_FILE,null,this.getActivity(),FileBrowserActivity.class);
		startActivityForResult(fileExploreIntent,REQUEST_CODE_PICK_FILE);
    }

    /**
     * @return the editor
     */
    public EditorState getEditor() {
        return editor;
    }

    /**
     * @param editor the editor to set
     */
    public void setEditor(EditorState editor) {
        this.editor = editor;
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	EditorState state = this.editor;
		if (requestCode == REQUEST_CODE_PICK_FILE) {
			if(resultCode == Activity.RESULT_OK) {
        		String newFile = data.getStringExtra(FileBrowserActivity.returnFileParameter);
        		Toast.makeText(getActivity(),"Received FILE path from file browser:\n"+newFile, 
        				Toast.LENGTH_LONG).show(); 
        		
        		File file = new File(newFile);
    	        if (!file.exists()) {
    	            return;
    	        }
    	        state.setCurrentDirectory(file.getParent());
    	        System.out.println("file = " + file.getAbsolutePath());
    	        state.setFileName(file.getName());
    	        TCPClient tcpClient =this.getTcpClient();
    	        tcpClient.uploadFile2Server(file.getAbsolutePath(), ((DocShareActivity)getActivity()).progressDialog);
    	        System.out.println(state.getFileName());
	        	
        	} else {
        		Toast.makeText(getActivity(),"Received NO result from file browser",
        				Toast.LENGTH_LONG).show(); 
        	}
        }
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) 
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) 
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    
}
