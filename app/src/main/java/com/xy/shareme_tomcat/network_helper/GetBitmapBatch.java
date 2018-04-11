package com.xy.shareme_tomcat.network_helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xy.shareme_tomcat.data.ImageObj;

import java.net.URL;

public class GetBitmapBatch extends AsyncTask<Void, Void, Void> {
    private ImageObj imageObj;
    private String linkPrefix;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmapBatch(ImageObj imageObj, String linkPrefix, TaskListener taskListener){
        this.imageObj = imageObj;
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
        try {
            if (!imageObj.getImgURL().equals("")) imageObj.img = getImage(linkPrefix + imageObj.getImgURL());
            if (!imageObj.getImgURL2().equals("")) imageObj.img2 = getImage(linkPrefix + imageObj.getImgURL2());
            if (!imageObj.getImgURL3().equals("")) imageObj.img3 = getImage(linkPrefix + imageObj.getImgURL3());
            if (!imageObj.getImgURL4().equals("")) imageObj.img4 = getImage(linkPrefix + imageObj.getImgURL4());
            if (!imageObj.getImgURL5().equals("")) imageObj.img5 = getImage(linkPrefix + imageObj.getImgURL5());
        }catch (NullPointerException e) {

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
}
