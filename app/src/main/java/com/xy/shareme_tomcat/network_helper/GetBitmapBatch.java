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

public class GetBitmapBatch extends AsyncTask<Void, Void, Void> {
    private Context context;
    private ImageObj imageObj;
    private Resources res;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmapBatch(Context context, Resources res, ImageObj imageObj, TaskListener taskListener){
        this.context = context;
        this.imageObj = imageObj;
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
        String imageLink = res.getString(R.string.link_image);
        if (!imageObj.getImgURL().equals("")) imageObj.img = getImage(imageLink + imageObj.getImgURL());
        if (!imageObj.getImgURL2().equals("")) imageObj.img2 = getImage(imageLink + imageObj.getImgURL2());
        if (!imageObj.getImgURL3().equals("")) imageObj.img3 = getImage(imageLink + imageObj.getImgURL3());
        if (!imageObj.getImgURL4().equals("")) imageObj.img4 = getImage(imageLink + imageObj.getImgURL4());
        if (!imageObj.getImgURL5().equals("")) imageObj.img5 = getImage(imageLink + imageObj.getImgURL5());
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
