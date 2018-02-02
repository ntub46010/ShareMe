package com.xy.shareme_tomcat.data;

import android.graphics.Bitmap;

public class ImageChild {
    private Bitmap bitmap;
    private String fileName = ""; //伺服器上的圖檔名稱
    private boolean isEntity = false; //是否為剛剛從手機選取的圖，而非透過下載或空白圖

    public ImageChild(Bitmap bitmap, boolean isEntity) {
        this.bitmap = bitmap;
        this.isEntity = isEntity;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setEntity(boolean isEntity) {
        this.isEntity = isEntity;
    }

    public boolean isEntity() {
        return isEntity;
    }
}
