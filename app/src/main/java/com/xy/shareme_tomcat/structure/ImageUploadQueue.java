package com.xy.shareme_tomcat.structure;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.ImageChild;
import com.xy.shareme_tomcat.network_helper.ImageUploadTask;

public class ImageUploadQueue extends Queue {
    private Resources res;
    private Context context;
    private String linkUpload;

    private int itemIndex = 0, entityAmount = 0, itemCount = 0;
    private ImageUploadTask imageTask;

    private Dialog dlgUpload;
    private TextView txtUploadHint;

    private String[] fileNames;

    // 宣告一個接收回傳結果的程式必須實作的介面
    public interface TaskListener { void onFinished(String[] fileNames); }
    private TaskListener taskListener;

    public ImageUploadQueue(Resources res, Context context, String linkPrefix) {
        this.res = res;
        this.context = context;
        this.linkUpload = linkPrefix;
    }

    @Override
    protected void onEnqueue(Object obj, boolean isFromFront) {

    }

    @Override
    protected void onDequeue(Object obj) {

    }

    public int getEntityAmount () {
        entityAmount = 0;
        for (int i = 0; i<size(); i++) {
            if (((ImageChild) get(i)).isEntity())
                entityAmount++;
        }
        return entityAmount;
    }

    public void startUpload(String[] fileNames, Dialog dlgUpload, TextView txtUploadHint, TaskListener taskListener) {
        this.fileNames = fileNames;
        this.dlgUpload = dlgUpload;
        this.txtUploadHint = txtUploadHint;
        this.taskListener = taskListener;
        this.dlgUpload.show();

        itemIndex = 0;
        itemCount = 0;

        createUploadTask();
    }

    private void initTrdWaitPhoto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
            }
        }).start();
    }

    private Handler hdrWaitPhoto = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (imageTask == null)
                return;

            String fileName = imageTask.getPhotoName();
            if (fileName == null) {
                initTrdWaitPhoto(); //還沒收到檔名，繼續監聽
            }else {
                //initTrdWaitPhoto();
                //寫入檔名
                fileNames[itemIndex] = fileName;

                //準備上傳下一張
                itemIndex++;
                if (itemIndex >= size() || ((ImageChild) get(itemIndex)).getBitmap() == null) { //圖片都上傳完，程式即將結束
                    onUploadFinished();
                    return;
                }
                createUploadTask();
            }
        }
    };

    private void createUploadTask() {
        while (itemIndex < size()) {
            if (((ImageChild) get(itemIndex)).isEntity()) { //只有剛剛從手機選取的實體圖片才會被上傳
                //開始上傳
                itemCount++;
                if (txtUploadHint != null)
                    txtUploadHint.setText(res.getString(R.string.hint_upload_photo, String.valueOf(itemCount), String.valueOf(entityAmount)));
                new Thread(new Runnable() {
                    public void run() {
                        imageTask = new ImageUploadTask(context, linkUpload);
                        imageTask.uploadFile(((ImageChild) get(itemIndex)).getBitmap());
                    }
                }).start();
                initTrdWaitPhoto(); //監聽正在上傳的圖片檔名
                //上傳完一張後，itemIndex才會遞增
                break;
            }else
                itemIndex++;
        }
        //未知情況
    }

    private void onUploadFinished() {
        //程式結束
        this.dlgUpload.dismiss();
        taskListener.onFinished(fileNames); //回傳給原Activity
    }

    public void cancelUpload() {
        imageTask = null;
        if (dlgUpload != null)
            dlgUpload.dismiss();
    }


    @Override
    public void destroy() {
        for (int i = 0; i < size(); i++)
            ((ImageChild) get(i)).setBitmap(null);

        clear();
    }
}
