package com.xy.shareme_tomcat.data;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class AlbumImageProvider {
    private final int REQUEST_ALBUM = 1;
    private final int REQUEST_CROP = 2;

    private File imageFile = null;
    private Bitmap imageBitmap = null;
    private int aspectX, aspectY, outputX, outputY;

    private Activity activity;
    private TaskListener taskListener;

    public interface TaskListener { void onFinished(Bitmap bitmap); }

    public AlbumImageProvider (Activity activity, int aspectX, int aspectY, int outputX, int outputY, TaskListener taskListener) {
        this.activity = activity;
        this.taskListener = taskListener;
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.outputX = outputX;
        this.outputY = outputY;
    }

    public void select() {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //檢查是否開啟寫入權限
        if (permission != PackageManager.PERMISSION_GRANTED) { // 無權限，向使用者請求
            //執行完後執行onRequestPermissionsResult
            ActivityCompat.requestPermissions(
                    activity,
                    new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    0 //requestCode
            );
        }else {
            //已有權限，準備選圖
            Intent albumIntent = new Intent(Intent.ACTION_PICK);
            albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(albumIntent, REQUEST_ALBUM);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //挑選完圖片後執行此方法
        if (resultCode != RESULT_OK)
            return;

        final Uri imageUri = data.getData();
        switch (requestCode) {
            case REQUEST_ALBUM:
                if (!createImageFile())
                    return;
                if (imageUri != null)
                    cropImage(imageUri);
                break;

            case REQUEST_CROP:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            imageBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(imageUri));
                            imageFile.delete();
                            imageFile.deleteOnExit();
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        hdrCreateImage.sendMessage(hdrCreateImage.obtainMessage());
                    }
                }).start();
                break;
        }
    }

    private Handler hdrCreateImage = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            taskListener.onFinished(imageBitmap);
        }
    };

    private boolean createImageFile() {
        imageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            imageFile.createNewFile();
            return imageFile.exists();
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void cropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("return-data", false);
        activity.startActivityForResult(intent, REQUEST_CROP);
    }

    public Bitmap getImage() {
        return imageBitmap;
    }

}
