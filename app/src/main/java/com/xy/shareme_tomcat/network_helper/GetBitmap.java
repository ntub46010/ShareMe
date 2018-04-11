package com.xy.shareme_tomcat.network_helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xy.shareme_tomcat.data.ImageObj;

import java.net.URL;
import java.util.ArrayList;

public class GetBitmap extends AsyncTask<Void, Void, Void> {
    private ArrayList<ImageObj> imageObjs;
    private String linkPrefix;
    private int preLoadAmount = 0;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmap(ArrayList<ImageObj> imageObjs, String linkPrefix, TaskListener taskListener) {
        this.imageObjs = imageObjs;
        this.linkPrefix = linkPrefix;
        this.taskListener = taskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        taskListener.onFinished();
    }

    //  由圖片地址下載圖片
    @Override
    protected Void doInBackground(Void... params) {
        int count;
        if (preLoadAmount == 0)
            count = imageObjs.size();
        else
            count = Math.min(preLoadAmount, imageObjs.size());

        for(int i=0; i<count; i++){
            ImageObj imageObj = imageObjs.get(i);
            imageObj.img = getImage(linkPrefix + imageObj.getImgURL());
        }
        return null;
    }

    private Bitmap getImage(String bitmapUrl) {
        URL url;
        Bitmap image = null;
        try {
            url = new URL(bitmapUrl);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void setPreLoadAmount(int amount) {
        this.preLoadAmount = amount;
    }
}
