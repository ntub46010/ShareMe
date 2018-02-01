package com.xy.shareme_tomcat.structure;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.ImageObject;
import com.xy.shareme_tomcat.network_helper.ImageUploadTask;

public class ImageUploadQueue extends Queue {
    private Resources res;
    private Context context;

    private int itemIndex = 0, entityAmount = 0, itemCount = 0;
    private ImageUploadTask imageTask;

    private Dialog dlgUpload;
    private TextView txtUploadHint;

    // 宣告一個接收回傳結果的程式必須實作的介面
    public interface TaskListener { void onFinished(String[] fileNames); }
    private TaskListener taskListener;

    public ImageUploadQueue(Resources res, Context context) {
        this.res = res;
        this.context = context;
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
            if (((ImageObject) get(i)).isEntity())
                entityAmount++;
        }
        return entityAmount;
    }

    public void startUpload(Dialog dlgUpload, TextView txtUploadHint, TaskListener taskListener) {
        this.dlgUpload = dlgUpload;
        this.txtUploadHint = txtUploadHint;
        this.taskListener = taskListener;
        this.dlgUpload.show();

        itemIndex = 0;
        itemCount = 0;

        createUploadTask(itemIndex);
    }

    private void initTrdWaitPhoto(boolean restart) {
        Thread trdWaitPhoto = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
            }
        });
        if (restart)
            trdWaitPhoto.start();
    }

    private Handler hdrWaitPhoto = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (imageTask == null)
                return;

            String fileName = imageTask.getPhotoName();
            if (fileName == null) {
                initTrdWaitPhoto(true); //還沒收到檔名，繼續監聽
            }else {
                initTrdWaitPhoto(false);
                //寫入檔名
                ((ImageObject) get(itemIndex)).setFileName(fileName);

                //準備上傳下一張
                itemIndex++;
                if (itemIndex >= size() || ((ImageObject) get(itemIndex)).getBitmap() == null) { //圖片都上傳完，程式即將結束
                    onUploadFinished();
                    return;
                }
                createUploadTask(itemIndex);
            }
        }
    };

    private void createUploadTask(final int i) {
        while (itemIndex < size()) {
            if (((ImageObject) get(itemIndex)).isEntity()) { //只有剛剛從手機選取的實體圖片才會被上傳
                //開始上傳
                itemCount++;
                txtUploadHint.setText(res.getString(R.string.hint_upload_photo, String.valueOf(itemCount), String.valueOf(entityAmount)));
                new Thread(new Runnable() {
                    public void run() {
                        imageTask = new ImageUploadTask(context, res.getString(R.string.link_upload_image));
                        imageTask.uploadFile(((ImageObject) get(i)).getBitmap());
                    }
                }).start();
                initTrdWaitPhoto(true); //監聽正在上傳的圖片檔名
                return;
            }else
                itemIndex++;
        }
        //未知情況
    }

    private void onUploadFinished() {
        //程式結束
        this.dlgUpload.dismiss();

        //統整伺服器上的檔名
        String[] fileNames = new String[5];
        for (int i = 0; i < 5; i++)
            fileNames[i] = "";
        for (int i = 0; i < size(); i++) {
            if (((ImageObject) get(i)).getBitmap() != null)
                fileNames[i] = ((ImageObject) get(i)).getFileName();
        }
        taskListener.onFinished(fileNames); //回傳給原Activity
    }

    public void cancelUpload() {
        imageTask = null;
    }


    @Override
    public void destroy() {
        for (int i = 0; i < size(); i++)
            ((ImageObject) get(i)).setBitmap(null);

        clear();
    }
}
