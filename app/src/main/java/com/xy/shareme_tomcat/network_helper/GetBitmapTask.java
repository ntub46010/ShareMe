package com.xy.shareme_tomcat.network_helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xy.shareme_tomcat.data.ImageObj;

import java.net.URL;

public class GetBitmapTask extends AsyncTask<ImageObj, Void, Void> { //用來下載一個ImageObj的一張圖片
    private String linkPrefix;
    private TaskListener taskListener;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    public GetBitmapTask(String linkPrefix, TaskListener taskListener) { //這個物件會被指定給父類別ImageObj，由它啟動startDownloadImage()
        this.linkPrefix = linkPrefix;
        this.taskListener = taskListener;
    }

    @Override
    protected Void doInBackground(ImageObj... params) {
        params[0].setImg(getImage(linkPrefix + params[0].getImgURL()));
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        taskListener.onFinished();
    }

    private Bitmap getImage(String imgURL) {
        URL url;
        Bitmap image = null;
        try {
            url = new URL(imgURL);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}
