package com.xy.shareme_tomcat.network_helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.ImageObj;

import java.net.URL;
import java.util.List;

public class GetBitmap extends AsyncTask<Void, Void, Void> {
    private Context context;
    private List<ImageObj> imageObjs;
    private ImageObj imgObj;
    private Resources res;

    private int type = 0, preLoadAmount = 0;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmap(Context context, Resources res, List<ImageObj> imageObjs, TaskListener taskListener){
        this.context = context;
        this.type = 0;
        this.imageObjs = imageObjs;
        this.taskListener = taskListener;
        this.res = res;
    }

    public GetBitmap(Context context, Resources res,ImageObj imgObj, TaskListener taskListener){
        this.context = context;
        this.type = 1;
        this.imgObj = imgObj;
        this.taskListener = taskListener;
        this.res = res;
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
        /*
                    type 0: 下載陣列裡的所有圖片，也可同時指定只要下載前幾個
                    type 1: 下載一張圖片
                */
        String imageLink = res.getString(R.string.link_image);
        if (type == 0) {
            int count;
            if (preLoadAmount == 0)
                count = imageObjs.size();
            else
                count = Math.min(preLoadAmount, imageObjs.size());

            for(int i=0; i<count; i++){
                ImageObj imageObj = imageObjs.get(i);
                imageObj.img = getImage(imageLink + imageObj.getImgURL());
            }
        }else if (type == 1) {
            imgObj.img = getImage(imageLink + imgObj.getImgURL());
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
