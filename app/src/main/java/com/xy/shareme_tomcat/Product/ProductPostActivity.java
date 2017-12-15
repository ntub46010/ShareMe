package com.xy.shareme_tomcat.Product;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ImageQueueAdapter;
import com.xy.shareme_tomcat.data.ImageObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.adapter.ImageQueueAdapter.REQUEST_ALBUM;
import static com.xy.shareme_tomcat.adapter.ImageQueueAdapter.REQUEST_CROP;

public class ProductPostActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ImageView btnPost;

    private RecyclerView recyclerView;
    private ArrayList<ImageObject> images = new ArrayList<>();
    private ImageQueueAdapter queueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_post);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        //txtBarTitle.setText("刊登商品");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnPost = (ImageView) toolbar.findViewById(R.id.btnPost);

        Bitmap bitmap = null;
        images.add(new ImageObject(bitmap)); //此為空白圖

        recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        queueAdapter = new ImageQueueAdapter(context, images, this);
        recyclerView.setAdapter(queueAdapter);
        images = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //詢問是否開啟權限
        switch(requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    queueAdapter.pickImageDialog();
                }else {
                    //使用者拒絕權限，停用檔案存取功能，並顯示訊息
                    Toast.makeText(context, "權限不足，無法選擇圖片", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //挑選完圖片後執行此方法，將圖片放上cardView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        Uri selectedImageUri = data.getData();
        switch (requestCode) {
            case REQUEST_ALBUM:
                if (!queueAdapter.createImageFile())
                    return;
                if (selectedImageUri != null)
                    queueAdapter.cropImage(selectedImageUri);

                break;
            case REQUEST_CROP:
                final int position = queueAdapter.getPressedPosition();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageObject image = queueAdapter.getItem(position);
                        boolean isEmptyCard = image.getBitmap() == null;
                        boolean bitmapIsNull = true;
                        do {
                            bitmapIsNull = queueAdapter.mImageFileToBitmap(image);
                        } while (bitmapIsNull);

                        image.setEntity(true);
                        queueAdapter.setItem(position, image);

                        if (isEmptyCard && queueAdapter.getItemCount() < 5) {
                            Bitmap bitmap = null;
                            queueAdapter.addItem(new ImageObject(bitmap, false)); //再新增一張空白圖
                        }
                    }
                }).start();
                recyclerView.scrollToPosition(position);

                break;
        }
    }
}
