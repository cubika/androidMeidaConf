package cn.edu.bupt.mmc.docshare.datastructure;

import java.io.Serializable;

public class ShareFileToPush implements Serializable {
    private String fileName = null;
    private int maxPage = -1;
    private int uploadByUserId = -1;

    public ShareFileToPush(String fileName, int maxPage, int uploadByUserId) {
        this.fileName = fileName;
        this.maxPage = maxPage;
        this.uploadByUserId = uploadByUserId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getUploadByUserId() {
        return uploadByUserId;
    }

    public void setUploadByUserId(int uploadByUserId) {
        this.uploadByUserId = uploadByUserId;
    }

}
