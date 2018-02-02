package com.xy.shareme_tomcat.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.DataHelper;
import com.xy.shareme_tomcat.data.ImageChild;
import com.xy.shareme_tomcat.structure.ImageUploadQueue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class ImageUploadAdapter extends RecyclerView.Adapter<ImageUploadAdapter.DataViewHolder> {
    private Context context;
    private Activity activity;
    private ImageUploadQueue queue;
    private int pressedPosition = 0;

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_ALBUM = 2;
    public static final int REQUEST_CROP = 3;

    private File mImageFile = null;
    private Bitmap mImageFileBitmap = null;

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private CardView cardView;
        private int position;
        private FrameLayout layBookPic;
        private ImageView imgBookPic;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            layBookPic = (FrameLayout) itemView.findViewById(R.id.layBookImg);
            imgBookPic = (ImageView) itemView.findViewById(R.id.imgBook);

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getEntityAmount() > 5) {
                        Toast.makeText(context, "最多新增五張圖片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pressedPosition = position;
                    //將寫入使用者對寫入的權限指定至permission
                    int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    //檢查是否開啟寫入權限
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // 無權限，向使用者請求
                        //執行完後執行onRequestPermissionsResult
                        ActivityCompat.requestPermissions(
                                activity,
                                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                0 //requestCode
                        );
                    }else{
                        //已有權限，準備選圖
                        pickImageDialog();
                    }
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() { //長按跳出選項清單
                @Override
                public boolean onLongClick(View v) {
                    pressedPosition = position;
                    if (getItem(position).getBitmap() != null || getItem(position).getFileName() != "") {
                        prepareDialog();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    // 將連結的資料
    public ImageUploadAdapter(Resources res, Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        queue = new ImageUploadQueue(res, context);
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_book_img, viewGroup, false);
        DataViewHolder dataViewHolder = new DataViewHolder(view);
        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目 的對應
        dataViewHolder.position = i;

        if (((ImageChild) queue.get(i)).getBitmap() != null || ((ImageChild) queue.get(i)).getFileName() != "") {
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#FAFAFA"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.FIT_CENTER);//
            dataViewHolder.imgBookPic.setImageBitmap(((ImageChild) queue.get(i)).getBitmap());
        }else { //加號圖片
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#DDDDDD"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            dataViewHolder.imgBookPic.setImageResource(R.drawable.icon_add);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void pickImageDialog() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(albumIntent, REQUEST_ALBUM);
    }

    public void cropImage(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 4);
        intent.putExtra("outputX", 900);
        intent.putExtra("outputY", 1200);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("return-date", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
        activity.startActivityForResult(intent, REQUEST_CROP);
    }

    public boolean createImageFile() {
        mImageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            mImageFile.createNewFile();
            return mImageFile.exists();
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean mImageFileToBitmap(ImageChild image) {
        mImageFileBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
        if (mImageFileBitmap != null) {
            image.setBitmap(mImageFileBitmap);
            if (mImageFile.delete()) {
                Log.w("Delete File", "deleted");
            }
            else {
                mImageFile.deleteOnExit();
                Log.w("Delete File", "delete failed and call deleteOnExit()");
            }
            return false;
        }
        else
            return true;
    }

    public int getPressedPosition() {
        return pressedPosition;
    }

    private void prepareDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_gallery_options);
        dialog.setCancelable(true);

        String[] textGroup = {"左移", "右移", "移除"};
        int[] iconGroup = {
                R.drawable.icon_turn_left,
                R.drawable.icon_turn_right,
                R.drawable.icon_delete
        };

        ListView listView = (ListView) dialog.findViewById(R.id.lstGalleryOptions);
        listView.setAdapter(DataHelper.getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon_black,
                R.id.imgIcon,
                R.id.txtTitle,
                iconGroup,
                textGroup
        ));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = ((TextView)view.findViewById(R.id.txtTitle)).getText().toString();
                switch (itemName) {
                    case "左移":
                        moveItem(pressedPosition, -1);
                        break;
                    case "右移":
                        moveItem(pressedPosition, 1);
                        break;
                    case "移除":
                        removeItem(pressedPosition);
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public ImageChild getItem(int position) {
        return (ImageChild) queue.get(position);
    }

    public void setItem(int position, ImageChild item) {
        queue.dequeueFromRear();
        queue.enqueueFromRear(item);
        //notifyDataSetChanged(); //會出錯
        notifyItemChanged(position);
    }

    public void addItem(ImageChild image) {
        queue.enqueueFromRear(image);
        notifyDataSetChanged();
    }

    private void removeItem(int position) {
        int bmpAmount = 0;
        //第一條件，至少有兩張圖片(可能含一張空白)；第二條件，點選的不是空白圖
        if (getItemCount() > 1 && (getItem(position).getBitmap() != null)) {
            queue.remove(position);
            for (int i=0; i<queue.size(); i++) {
                if (getItem(i).getBitmap() != null)
                    bmpAmount++;
            }
            if (bmpAmount == 5) {
                //若移除前原先有5張真圖，要補一張空白圖
                addItem(new ImageChild(null, false));
            }
            notifyDataSetChanged();
        }else
            Toast.makeText(context, "移除失敗", Toast.LENGTH_SHORT).show();
    }

    private void moveItem(int position, int distance) {
        int destination;
        if (distance > 0)
            destination = (position + distance >= getItemCount()) ? getItemCount() : position + distance;
        else
            destination = (position + distance < 0) ? 0 : position + distance;

        if (getItem(destination).getBitmap() != null) {
            Collections.swap(queue, position, destination);
            notifyDataSetChanged();
        }
    }

    public int getEntityAmount() {
        return queue.getEntityAmount();
    }

    public void startUpload(String[] fileNames, Dialog dlgUpload, TextView txtUploadHint, ImageUploadQueue.TaskListener taskListener) {
        queue.startUpload(fileNames, dlgUpload, txtUploadHint, taskListener);
    }

    public void cancelUpload() {
        queue.cancelUpload();
    }

    public void destroyQueue(boolean isFully) {
        if (queue != null) {
            queue.destroy();
            if (isFully)
                queue = null;
        }
    }

    public String getImageStatus() {
        StringBuffer sb = new StringBuffer();
        sb.append("fileName:\n");
        for (int i = 0; i<queue.size(); i++)
            sb.append(String.valueOf(i)).append(": ").append(((ImageChild) queue.get(i)).getFileName()).append("、\n");
        sb.append("isEntity:\n");
        for (int i = 0; i<queue.size(); i++) {
            if (((ImageChild) queue.get(i)).isEntity())
                sb.append("O");
            else
                sb.append("X");
        }

        return sb.toString();
    }
}