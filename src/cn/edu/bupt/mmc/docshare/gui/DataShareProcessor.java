package cn.edu.bupt.mmc.docshare.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.TextView;

import cn.edu.bupt.mmc.client.BasicInfomation;
import cn.edu.bupt.mmc.docshare.apps.EditorState;
import cn.edu.bupt.mmc.docshare.apps.Strings;

public class DataShareProcessor{

    private DocFragment panel;
    private EditorState editorState;
    private Button applyButton;
    private Button dropButton;
    private Button uploadButton;
    private Button nextPageButton;
    private Button prePageButton;

//    private Button zoomInButton;
//    private Button zoomOutButton;
    private Button firstPageButton;
    private Button endPageButton;
    private TextView pageNumberTextField;
    private ProgressDialog progressBar;

    public DataShareProcessor(EditorState editorState, DocFragment panel) {
        this.editorState = editorState;
        this.panel = panel;
    }

    /**
	 * @return the progressBar
	 */
	public ProgressDialog getProgressBar() {
		return progressBar;
	}

	/**
	 * @param progressBar the progressBar to set
	 */
	public void setProgressBar(ProgressDialog progressBar) {
		this.progressBar = progressBar;
	}

	public void editRedo() {
        this.panel.nextPage();
    }

    public void editUndo() {
        this.panel.prePage();
    }

    public void applyControl() {
        this.panel.getTcpClient().controlReq();
    }

    public void dropControl() {
        this.panel.getTcpClient().controlDrop();
    }

    public void pictureBufferedImageShow(ByteArrayInputStream in) {
        EditorState state = getEditorState();
        Strings strings = state.getStrings();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        try {
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] data = out.toByteArray();
//        java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(data);
//        MediaTracker mediaTracker = new MediaTracker(new Frame());
//        mediaTracker.addImage(awtImage, 0);
//        try {
//            mediaTracker.waitForID(0);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        PixelImage image = ImageCreator.convertImageToRGB24Image(awtImage);
        Bitmap image=BitmapFactory.decodeByteArray(data,0,data.length);
        if (image == null) {
            return;
        }
        state.setImage(image, false);
        panel.setEditor(state);
        panel.updateStatusBar();
        //state.setFileName(fullName);
        panel.updateImage();
    }

    public void updateStatus() {
        panel.updateStatusBar();
    }

    /**
     * Returns the EditorState object given to this object's constructor.
     * @return EditorState object used by this processor
     */
    public EditorState getEditorState() {
        return this.editorState;
    }

    /**
     * @param editorState the editorState to set
     */
    public void setEditorState(EditorState editorState) {
        this.editorState = editorState;
    }

    /**
     * @return the panel
     */
    public DocFragment getPanel() {
        return panel;
    }

    /**
     * @param panel the panel to set
     */
    public void setPanel(DocFragment panel) {
        this.panel = panel;
    }

    public void setButtonsEnable(EditorState editorState) {
        boolean hasImage = (editorState.getImage() != null);
        boolean canApply = (editorState.getControllerName() == null);
        boolean canControl = (editorState.getControllerName() != null)
                && (editorState.getControllerName()
                        .equals(BasicInfomation.username));
        this.applyButton.setEnabled(canApply);
        this.dropButton.setEnabled(canControl);
        this.uploadButton.setEnabled(canControl);
        this.nextPageButton.setEnabled(hasImage
                && canControl
                && editorState.getNowPageNumber() != editorState
                        .getTotalPageNumber());
        this.prePageButton.setEnabled(hasImage && canControl
                && editorState.getNowPageNumber() != 1);
//        this.zoomInButton.setEnabled(hasImage);
//        this.zoomOutButton.setEnabled(hasImage);
        this.firstPageButton.setEnabled(hasImage && canControl
                && editorState.getNowPageNumber() != 1);
        this.endPageButton.setEnabled(hasImage
                && canControl
                && editorState.getNowPageNumber() != editorState
                        .getTotalPageNumber());
        this.pageNumberTextField.setEnabled(hasImage && canControl);
    }

    /**
     * @return the applyButton
     */
    public Button getApplyButton() {
        return applyButton;
    }

    /**
     * @param applyButton the applyButton to set
     */
    public void setApplyButton(Button applyButton) {
        this.applyButton = applyButton;
    }

    /**
     * @return the dropButton
     */
    public Button getDropButton() {
        return dropButton;
    }

    /**
     * @param dropButton the dropButton to set
     */
    public void setDropButton(Button dropButton) {
        this.dropButton = dropButton;
    }

    /**
     * @return the uploadButton
     */
    public Button getUploadButton() {
        return uploadButton;
    }

    /**
     * @param uploadButton the uploadButton to set
     */
    public void setUploadButton(Button uploadButton) {
        this.uploadButton = uploadButton;
    }

    /**
     * @return the nextPageButton
     */
    public Button getNextPageButton() {
        return nextPageButton;
    }

    /**
     * @param nextPageButton the nextPageButton to set
     */
    public void setNextPageButton(Button nextPageButton) {
        this.nextPageButton = nextPageButton;
    }

    /**
     * @return the prePageButton
     */
    public Button getPrePageButton() {
        return prePageButton;
    }

    /**
     * @param prePageButton the prePageButton to set
     */
    public void setPrePageButton(Button prePageButton) {
        this.prePageButton = prePageButton;
    }

//    /**
//     * @return the zoomInButton
//     */
//    public Button getZoomInButton() {
//        return zoomInButton;
//    }
//
//    /**
//     * @param zoomInButton the zoomInButton to set
//     */
//    public void setZoomInButton(Button zoomInButton) {
//        this.zoomInButton = zoomInButton;
//    }
//
//    /**
//     * @return the zoomOutButton
//     */
//    public Button getZoomOutButton() {
//        return zoomOutButton;
//    }
//
//    /**
//     * @param zoomOutButton the zoomOutButton to set
//     */
//    public void setZoomOutButton(Button zoomOutButton) {
//        this.zoomOutButton = zoomOutButton;
//    }

    /**
     * @return the firstPageButton
     */
    public Button getFirstPageButton() {
        return firstPageButton;
    }

    /**
     * @param firstPageButton the firstPageButton to set
     */
    public void setFirstPageButton(Button firstPageButton) {
        this.firstPageButton = firstPageButton;
    }

    /**
     * @return the endPageButton
     */
    public Button getEndPageButton() {
        return endPageButton;
    }

    /**
     * @param endPageButton the endPageButton to set
     */
    public void setEndPageButton(Button endPageButton) {
        this.endPageButton = endPageButton;
    }

    /**
     * @return the pageNumberTextField
     */
    public TextView getPageNumberTextField() {
        return pageNumberTextField;
    }

    /**
     * @param pageNumberTextField the pageNumberTextField to set
     */
    public void setPageNumberTextField(TextView pageNumberTextField) {
        this.pageNumberTextField = pageNumberTextField;
    }
    
}
