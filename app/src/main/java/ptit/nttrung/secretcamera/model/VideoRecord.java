package ptit.nttrung.secretcamera.model;

import android.graphics.Bitmap;

/**
 * Created by TrungNguyen on 5/9/2017.
 */

public class VideoRecord {
    private String filePath;
    private Bitmap thumbnails;
    private String timeVideo;
    private String fileName;
    private String date;
    private boolean isSelect;

    public VideoRecord() {
        isSelect = false;
    }

    public VideoRecord(String filePath, Bitmap thumbnails, String timeVideo, String fileName, String date) {
        this.filePath = filePath;
        this.thumbnails = thumbnails;
        this.timeVideo = timeVideo;
        this.fileName = fileName;
        this.date = date;
        isSelect = false;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Bitmap getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Bitmap thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getTimeVideo() {
        return timeVideo;
    }

    public void setTimeVideo(String timeVideo) {
        this.timeVideo = timeVideo;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
